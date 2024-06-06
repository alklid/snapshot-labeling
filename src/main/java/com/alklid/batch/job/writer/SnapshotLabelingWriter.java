package com.alklid.batch.job.writer;

import com.alklid.batch.model.data.SnapshotFile;
import com.alklid.batch.prop.SnapshotProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.File;
import java.time.format.DateTimeFormatterBuilder;

@Slf4j
@RequiredArgsConstructor
public class SnapshotLabelingWriter implements ItemWriter<SnapshotFile> {

    private final SnapshotProps snapshotProps;

    @Override
    public void write(Chunk<? extends SnapshotFile> items) throws Exception {
        for (SnapshotFile item : items) {
            // 월별 폴더 확인 및 생성
            String labelTargetPath = item.getDateLabel().format(new DateTimeFormatterBuilder().appendPattern("/yyyy/yyyy-MM").toFormatter());
            File labelDir = new File(StringUtils.join(snapshotProps.getTarget(), labelTargetPath));
            if (!labelDir.exists()) {
                labelDir.mkdirs();
            }

            // 파일 이동
            File sourceFile = item.getPath().toFile();
            File targetFile = new File(labelDir, sourceFile.getName());
            FileUtils.moveFile(sourceFile, targetFile);

            log.info("[WRITER] Moved({} -> {}) : {}",
                item.getDateLabel().format(new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter()),
                labelTargetPath,
                item.getPath().toString());
        }
    }

}
