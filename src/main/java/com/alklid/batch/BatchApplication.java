package com.alklid.batch;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.util.Collection;

import static com.alklid.batch.Constant.Batch.SNAPSHOT_LABELING_JOB;
import static com.alklid.batch.Constant.Char.COMMA;

@Slf4j
@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        // 배치 실행 작업 이름 확인
        String jobNames = args[0];
        if (StringUtils.isEmpty(jobNames)) {
            log.error("JobNames argument is empty.");
            return;
        }

        ConfigurableApplicationContext ctx = SpringApplication.run(BatchApplication.class, args);

        try {
            JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);
            String[] jobList = StringUtils.split(jobNames, COMMA);

            for (String jobName : jobList) {
                StopWatch watch = new StopWatch(SNAPSHOT_LABELING_JOB);
                watch.start();

                Job batchJob = ctx.getBean(jobName, Job.class);
                JobExecution jobExecution = jobLauncher.run(batchJob, new JobParametersBuilder().toJobParameters());

                log.info("Batch executed! (" + jobExecution.getJobInstance() + ")");

                Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
                if (!CollectionUtils.isEmpty(stepExecutions)) {
                    StepExecution stepExecution = stepExecutions.iterator().next();
                    log.info("-> readCount: {}, writeCount: {}, commitCount: {}",
                            stepExecution.getReadCount(),
                            stepExecution.getWriteCount(),
                            stepExecution.getCommitCount());
                }

                watch.stop();
                log.info("-> summary: {}", watch.shortSummary());
            }
        }
        catch (Exception ex) {
            log.error("Batch job executed(" + jobNames + ") is failed!", ex);
        }

        ctx.close();
    }


    @Component
    public static class ContextCloseHandler
            implements ApplicationListener<ContextClosedEvent> {

        TaskExecutor executor;

        @Autowired
        public ContextCloseHandler(TaskExecutor batchTaskExecutor) {
            this.executor = batchTaskExecutor;
        }

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            ((ThreadPoolTaskExecutor) executor).shutdown();
        }
    }

}
