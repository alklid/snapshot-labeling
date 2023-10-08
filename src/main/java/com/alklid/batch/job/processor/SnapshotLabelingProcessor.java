package com.alklid.batch.job.processor;

import com.alklid.batch.prop.SnapshotProps;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.io.File;

@Slf4j
public class SnapshotLabelingProcessor implements ItemProcessor<File, File> {

    private SnapshotProps snapshotProps;


    @Override
    public File process(@NonNull File item) {
        log.info("[PROCESSOR] item : {}", item.getAbsolutePath());
        return item;
    }


    public void setSnapshotProps(final SnapshotProps snapshotProps) {
        this.snapshotProps = snapshotProps;
    }

}
