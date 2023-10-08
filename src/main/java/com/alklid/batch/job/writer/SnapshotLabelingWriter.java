package com.alklid.batch.job.writer;

import com.alklid.batch.prop.SnapshotProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.io.File;
import java.util.List;

@Slf4j
public class SnapshotLabelingWriter implements ItemWriter<File> {

    private SnapshotProps snapshotProps;


    @Override
    public void write(List<? extends File> items) {
        for (File item : items) {
            log.info("[WRITER] File : {}", item.getAbsolutePath());
        }
    }


    public void setSnapshotProps(final SnapshotProps snapshotProps) {
        this.snapshotProps = snapshotProps;
    }
    
}
