package com.alklid.batch.job.writer;

import com.alklid.batch.model.data.SnapshotFile;
import com.alklid.batch.prop.SnapshotProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;

import static com.alklid.batch.Constant.Char.DOT;

@Slf4j
@RequiredArgsConstructor
public class SnapshotLabelingWriter implements ItemWriter<SnapshotFile> {

    private final SnapshotProps snapshotProps;

    @Override
    public void write(Chunk<? extends SnapshotFile> items) throws Exception {
        for (SnapshotFile item : items) {
            // 월별 폴더 확인 및 생성
            String labelTargetPath = makeTargetDir(item);

            // 파일 이동
            move(item);

            log.info("[WRITER] Moved({}) : {}", labelTargetPath, item.getPath().toString());
        }
    }


    private String makeTargetDir(final SnapshotFile item) {
        String labelTargetPath = item.getDateLabel().format(new DateTimeFormatterBuilder().appendPattern("/yyyy/yyyy-MM").toFormatter());
        File labelDir = new File(StringUtils.join(snapshotProps.getTarget(), labelTargetPath));
        if (!labelDir.exists()) {
            labelDir.mkdirs();
        }

        return labelTargetPath;
    }


    private void move(SnapshotFile item) throws Exception {
        String labelTargetPath = item.getDateLabel().format(new DateTimeFormatterBuilder().appendPattern("/yyyy/yyyy-MM").toFormatter());
        File labelDir = new File(StringUtils.join(snapshotProps.getTarget(), labelTargetPath));

        File sourceFile = item.getPath().toFile();
        File targetFile = new File(labelDir, sourceFile.getName());

        // 이미 해당 경로에 동일한 이름의 파일이 있는 경우, MD5 비교
        if (targetFile.exists()) {
            String sourceFileMd5 = null;
            String targetFileMd5 = null;
            try (InputStream is = Files.newInputStream(Paths.get(sourceFile.getAbsolutePath()))) {
                sourceFileMd5 = DigestUtils.md5DigestAsHex(is);
            }

            try (InputStream is = Files.newInputStream(Paths.get(targetFile.getAbsolutePath()))) {
                targetFileMd5 = DigestUtils.md5DigestAsHex(is);
            }

            // MD5 같으면 동일 파일로 판단하고 SKIP
            if (StringUtils.equals(sourceFileMd5, targetFileMd5)) {
                return;
            }

            String name = StringUtils.join(FilenameUtils.getName(sourceFile.getAbsolutePath()), Instant.now().toEpochMilli());
            String extension = FilenameUtils.getExtension(sourceFile.getAbsolutePath());
            targetFile = new File(labelDir, StringUtils.join(name, DOT, extension));
        }

        FileUtils.moveFile(sourceFile, targetFile);
    }


}
