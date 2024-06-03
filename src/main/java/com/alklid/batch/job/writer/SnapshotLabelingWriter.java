package com.alklid.batch.job.writer;

import com.alklid.batch.model.data.SnapshotFile;
import com.alklid.batch.prop.SnapshotProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class SnapshotLabelingWriter implements ItemWriter<SnapshotFile> {

    private final SnapshotProps snapshotProps;

    @Override
    public void write(Chunk<? extends SnapshotFile> items) throws Exception {
        for (SnapshotFile item : items) {
            log.info("[WRITER] File : {}", item.getPath().toString());
        }
    }

}
