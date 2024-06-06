package com.alklid.batch.job.processor;

import com.alklid.batch.model.data.SnapshotFile;
import com.alklid.batch.prop.SnapshotProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.time.format.DateTimeFormatterBuilder;

@Slf4j
@RequiredArgsConstructor
public class SnapshotLabelingProcessor implements ItemProcessor<SnapshotFile, SnapshotFile> {

    private final SnapshotProps snapshotProps;

    @Override
    public SnapshotFile process(final SnapshotFile item) {
        // TODO 파일의 메타데이터 설정
        //  파일명 기반으로 생성일 설정 예) yyyyMMdd_*.jpg 이면 앞의 yyyyMMdd 패턴으로 생성일을 설정

        // 파일의 생성일 구하기
        item.dateLabeling();

        log.info("[PROCESSOR] Labeling({}) : {}",
            item.getDateLabel().format(new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter()),
            item.getPath().toString());
        return item;
    }

}
