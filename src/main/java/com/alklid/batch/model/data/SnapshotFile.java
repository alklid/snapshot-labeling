package com.alklid.batch.model.data;

import com.alklid.batch.model.DateExtractType;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.file.FileSystemDirectory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class SnapshotFile {

    private LocalDateTime dateLabel;
    private Path path;

    @Builder
    public SnapshotFile(LocalDateTime dateLabel, Path path) {
        this.dateLabel = dateLabel;
        this.path = path;
    }

    public static SnapshotFile NoLabelSnapshotFile(Path path) {
        return SnapshotFile.builder().path(path).build();
    }


    public void dateLabeling() {
        dateLabeling(DateExtractType.METADATA.getType());
    }

    public void dateLabeling(Integer dateExtractType) {
        // 기본값 설정, 1900-01-01
        this.dateLabel = LocalDateTime.of(1900, 1, 1, 0, 0);

        if (DateExtractType.of(dateExtractType) == DateExtractType.METADATA) {
            dateLabelingByMetadata();
            return;
        }

        if (DateExtractType.of(dateExtractType) == DateExtractType.FILE_NAME_PATTERN) {
            dateLabelingByFileNamePattern();
            return;
        }
    }


    private void dateLabelingByFileNamePattern() {
        try {
            String dateMetadata = path.toFile().getName().substring(0, DateExtractType.FILE_NAME_PATTERN_FORMAT.length());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateExtractType.FILE_NAME_PATTERN_FORMAT);
            this.dateLabel = LocalDate.parse(dateMetadata, formatter).atStartOfDay();
        }
        catch (Exception e) {
            log.error("# get metadata failed! [{}]", path.toString(), e);
        }
    }


    private void dateLabelingByMetadata() {
        try {
            final Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
            FileSystemDirectory fileMetadata = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);

            // 라이브러리에서 생성일을 제공하지 않음.
            // 대부분의 파일이 생성 이후 별도 보정을 하지 않아 생성일과 수정일이 다르지 않음.
            // 만약 보정된 파일이고 보정일이 생성일과 차이가 큰 경우에는 생성일 기준으로 비교하는 로직이 필요할 수 있음.
            Date dateMetadata = fileMetadata.getDate(FileSystemDirectory.TAG_FILE_MODIFIED_DATE);
            this.dateLabel = Instant.ofEpochMilli(dateMetadata.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        catch (IOException | ImageProcessingException e) {
            log.error("# get metadata failed! [{}]", path.toString(), e);
        }
    }

}
