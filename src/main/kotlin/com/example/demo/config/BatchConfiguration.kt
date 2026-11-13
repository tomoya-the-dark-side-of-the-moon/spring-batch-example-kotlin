package com.example.demo.config

import com.example.demo.domain.FullNameMember
import com.example.demo.domain.Member
import com.example.demo.listener.MemberStepExecutionListener
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import javax.sql.DataSource


@Configuration
@EnableJdbcJobRepository
class BatchConfiguration {

    @Bean
    fun itemReader(): ItemReader<Member> {
        return FlatFileItemReaderBuilder<Member>().name("itemReader").resource(ClassPathResource("bon_jovi.csv"))
            .delimited().names("id", "firstName", "lastName").linesToSkip(1).targetType(Member::class.java).build()
    }

    @Bean
    fun itemProcessor(): ItemProcessor<Member, FullNameMember> {
        return ItemProcessor { item: Member ->
            FullNameMember(
                item.id, item.firstName, item.lastName, item.firstName + " " + item.lastName
            )
        }
    }

    @Bean
    fun itemWriter(dataSource: DataSource): ItemWriter<FullNameMember> {
        return JdbcBatchItemWriterBuilder<FullNameMember>().dataSource(dataSource).sql(
            "INSERT INTO member (id, first_name, last_name, full_name) VALUES (:id, :firstName, :lastName, :fullName)"
        ).beanMapped().build()
    }

    @Bean
    fun stepExecutionListener(dataSource: DataSource): StepExecutionListener {
        return MemberStepExecutionListener(dataSource)
    }

    @Bean
    fun step(
        jobRepository: JobRepository,
        itemReader: ItemReader<Member>,
        itemProcessor: ItemProcessor<Member, FullNameMember>,
        itemWriter: ItemWriter<FullNameMember>,
        stepExecutionListener: StepExecutionListener
    ): Step {
        return StepBuilder(jobRepository).chunk<Member, FullNameMember>(5).reader(itemReader).processor(itemProcessor)
            .writer(itemWriter).listener(stepExecutionListener).build()
    }

    @Bean
    fun job(jobRepository: JobRepository, step: Step): Job? {
        return JobBuilder(jobRepository).start(step).build()
    }
}