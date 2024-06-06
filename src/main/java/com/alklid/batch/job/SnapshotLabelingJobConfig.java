package com.alklid.batch.job;

import com.alklid.batch.config.InMemoryDataSourceConfiguration;
import com.alklid.batch.job.processor.SnapshotLabelingProcessor;
import com.alklid.batch.job.writer.SnapshotLabelingWriter;
import com.alklid.batch.model.data.SnapshotFile;
import com.alklid.batch.prop.SnapshotProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.alklid.batch.Constant.Batch.SNAPSHOT_LABELING_JOB;
import static com.alklid.batch.Constant.Batch.SNAPSHOT_LABELING_STEP;

@Configuration
@EnableBatchProcessing
@Import(InMemoryDataSourceConfiguration.class)
@RequiredArgsConstructor
@Slf4j
public class SnapshotLabelingJobConfig {

    private static final int CHUNK_SIZE             = 100;

    private final SnapshotProps snapshotProps;


    @Bean(name = SNAPSHOT_LABELING_JOB)
    public Job snapshotLabelingJob(JobRepository jobRepository,
                                   @Qualifier(SNAPSHOT_LABELING_STEP) Step snapshotLabelingStep) {
        return new JobBuilder(SNAPSHOT_LABELING_JOB, jobRepository)
                .preventRestart()
                .start(snapshotLabelingStep)
                .build();
    }


    @Bean(name = SNAPSHOT_LABELING_STEP)
    public Step snapshotLabelingStep(JobRepository jobRepository,
                                     JdbcTransactionManager transactionManager,
                                     ItemReader<SnapshotFile> snapshotLabelingItemReader,
                                     ItemProcessor<SnapshotFile, SnapshotFile> snapshotLabelingItemProcessor,
                                     ItemWriter<SnapshotFile> snapshotLabelingItemWriter) {
        return new StepBuilder(SNAPSHOT_LABELING_STEP, jobRepository)
                .<SnapshotFile, SnapshotFile>chunk(CHUNK_SIZE, transactionManager)
                .reader(snapshotLabelingItemReader)
                .processor(snapshotLabelingItemProcessor)
                .writer(snapshotLabelingItemWriter)
                .build();
    }


    @Bean
    @StepScope
    public IteratorItemReader<SnapshotFile> snapshotLabelingItemReader() {
        log.info("[READER] Source : {}", snapshotProps.getSource());
        List<SnapshotFile> snapshotFiles = new ArrayList<>();

        Path sourceDir = Paths.get(snapshotProps.getSource());
        try {
            List<Path> files = Files.walk(sourceDir)
                .filter(Files::isRegularFile)
                .filter(path -> !StringUtils.equalsIgnoreCase(".ds_store", path.getFileName().toString()))
                .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(files)) {
                for (Path path : files) {
                    snapshotFiles.add(SnapshotFile.NoLabelSnapshotFile(path));
                }
            }
        }
        catch (IOException e) {
            log.error("[READER] failed", e);
        }

        return new IteratorItemReader<>(snapshotFiles);
    }


    @Bean
    @StepScope
    public SnapshotLabelingProcessor snapshotLabelingItemProcessor() {
        return new SnapshotLabelingProcessor(snapshotProps);
    }


    @Bean
    @StepScope
    public SnapshotLabelingWriter snapshotLabelingItemWriter() {
        return new SnapshotLabelingWriter(snapshotProps);
    }

}