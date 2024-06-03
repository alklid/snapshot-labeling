package com.alklid.batch.job.processor;

import com.alklid.batch.model.data.SnapshotFile;
import com.alklid.batch.prop.SnapshotProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@RequiredArgsConstructor
public class SnapshotLabelingProcessor implements ItemProcessor<SnapshotFile, SnapshotFile> {

    private final SnapshotProps snapshotProps;

    @Override
    public SnapshotFile process(final SnapshotFile item) {
        // 파일의 생성일 구하기
        item.labeling();

        log.info("[PROCESSOR] ({}) : {}", item.getDateLabel(), item.getPath().toString());
        return item;
    }

}
