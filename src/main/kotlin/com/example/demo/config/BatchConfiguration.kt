package com.example.demo.config

import com.example.demo.domain.Member
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.Chunk
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource


@Configuration
class BatchConfiguration {
    @Bean
    fun itemReader(): ItemReader<Member> {
        return FlatFileItemReaderBuilder<Member>().name("itemReader")
            .resource(ClassPathResource("bon_jovi.csv")).delimited().names("id", "firstName", "lastName")
            .linesToSkip(1).targetType(Member::class.java).build()
    }

    @Bean
    fun itemWriter(): ItemWriter<Member> {
        return ItemWriter { chunk: Chunk<out Member>? -> chunk!!.forEach { x: Any? -> println(x) } }
    }

    @Bean
    fun step(jobRepository: JobRepository, itemReader: ItemReader<Member>, itemWriter: ItemWriter<Member>): Step {
        return StepBuilder(jobRepository).chunk<Member, Member>(5).reader(itemReader).writer(itemWriter).build()
    }

    @Bean
    fun job(jobRepository: JobRepository, step: Step): Job? {
        return JobBuilder(jobRepository).start(step).build()
    }
}