package com.alklid.batch.job;

import com.alklid.batch.job.processor.SnapshotLabelingProcessor;
import com.alklid.batch.job.writer.SnapshotLabelingWriter;
import com.alklid.batch.prop.SnapshotProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;

import static com.alklid.batch.Constant.Batch.SNAPSHOT_LABELING_JOB;
import static com.alklid.batch.Constant.Batch.SNAPSHOT_LABELING_STEP;

@Configuration
@EnableBatchProcessing
@Slf4j
public class SnapshotLabelingJobConfig {

    private static final int CHUNK_SIZE             = 100;

    private final SnapshotProps snapshotProps;

    public SnapshotLabelingJobConfig(SnapshotProps snapshotProps) {
        this.snapshotProps = snapshotProps;
    }


    @Bean(name = SNAPSHOT_LABELING_JOB)
    public Job snapshotLabelingJob(JobBuilderFactory jobBuilderFactory,
                                   @Qualifier(SNAPSHOT_LABELING_STEP) Step snapshotLabelingStep) {
        return jobBuilderFactory.get(SNAPSHOT_LABELING_JOB)
                .preventRestart()
                .start(snapshotLabelingStep)
                .build();
    }


    @Bean(name = SNAPSHOT_LABELING_STEP)
    public Step snapshotLabelingStep(StepBuilderFactory stepBuilderFactory,
                                     ItemReader<File> snapshotLabelingItemReader,
                                     ItemProcessor<File, File> snapshotLabelingItemProcessor,
                                     ItemWriter<File> snapshotLabelingItemWriter) {
        return stepBuilderFactory.get(SNAPSHOT_LABELING_STEP)
                .<File, File>chunk(CHUNK_SIZE)
                .reader(snapshotLabelingItemReader)
                .processor(snapshotLabelingItemProcessor)
                .writer(snapshotLabelingItemWriter)
                .build();
    }


    @Bean
    @StepScope
    public IteratorItemReader<File> snapshotLabelingItemReader() {

        log.info("[READER] Source : {}", snapshotProps.getSource());
        return new IteratorItemReader<>(new ArrayList<>());

    }


    @Bean
    @StepScope
    public SnapshotLabelingProcessor snapshotLabelingItemProcessor(SnapshotProps snapshotProps) {
        SnapshotLabelingProcessor itemProcessor = new SnapshotLabelingProcessor();
        itemProcessor.setSnapshotProps(snapshotProps);
        return itemProcessor;
    }


    @Bean
    @StepScope
    public SnapshotLabelingWriter snapshotLabelingItemWriter() {
        return new SnapshotLabelingWriter();
    }

}