package com.example.demo.config

import com.example.demo.datasource.MemberRoutingDataSource
import com.example.demo.domain.FullNameMember
import com.example.demo.domain.Member
import com.example.demo.domain.Reservation
import com.example.demo.listener.MemberItemSkipListener
import com.example.demo.listener.MemberImportStepExecutionListener
import com.example.demo.partitioner.MemberImportStepPartitioner
import com.example.demo.tasklet.FindReservationTasklet
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.listener.SkipListener
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.partition.Partitioner
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor
import org.springframework.batch.infrastructure.item.validator.BeanValidatingItemProcessor
import org.springframework.batch.infrastructure.item.validator.ValidationException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.core.io.ClassPathResource
import org.springframework.core.task.VirtualThreadTaskExecutor
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
@EnableJdbcJobRepository
@Import(DataSourceConfiguration::class)
@ImportRuntimeHints(BatchRuntimeHints::class)
class BatchConfiguration {

    @Bean
    fun reservationTasklet(@Qualifier("batchDataSource") dataSource: DataSource): Tasklet {
        return FindReservationTasklet(dataSource)
    }

    @Bean
    @StepScope
    fun memberImportStepPartitioner(
        @Value("#{jobExecutionContext['reservations']}") reservations: MutableList<Reservation>,
        memberRoutingDataSource: MemberRoutingDataSource,
    ): Partitioner = MemberImportStepPartitioner(reservations, memberRoutingDataSource)

    @Bean
    @StepScope
    fun itemReader(@Value("#{stepExecutionContext['fileName']}") fileName: String): FlatFileItemReader<Member> {
        return FlatFileItemReaderBuilder<Member>().name("itemReader").resource(ClassPathResource(fileName))
            .delimited().names("id", "firstName", "lastName").linesToSkip(1).targetType(Member::class.java).build()
    }

    @Bean
    @StepScope
    @Throws(Exception::class)
    fun beanValidatingItemProcessor(): ItemProcessor<Member, Member> {
        val beanValidatingItemProcessor = BeanValidatingItemProcessor<Member>()
        beanValidatingItemProcessor.setFilter(false)
        beanValidatingItemProcessor.afterPropertiesSet()

        return beanValidatingItemProcessor
    }

    @Bean
    @StepScope
    @Throws(java.lang.Exception::class)
    fun itemProcessor(): ItemProcessor<Member, FullNameMember> {
        val fullNameItemProcessor = ItemProcessor { item: Member ->
            FullNameMember(
                item.id?.toIntOrNull(), item.firstName, item.lastName, item.firstName + " " + item.lastName
            )
        }

        return CompositeItemProcessor<Member, FullNameMember>(
            listOf(beanValidatingItemProcessor(), fullNameItemProcessor)
        )
    }

    @Bean
    @StepScope
    fun itemWriter(
        @Qualifier("memberImportEntityManagerFactory") entityManagerFactoryBean: LocalEntityManagerFactoryBean
    ): ItemWriter<FullNameMember> {
        return JpaItemWriterBuilder<FullNameMember>().entityManagerFactory(entityManagerFactoryBean.getObject()!!)
            .usePersist(true).build()
    }

    @Bean
    @StepScope
    fun skipListener(): SkipListener<Member, FullNameMember> {
        return MemberItemSkipListener()
    }

    @Bean
    @StepScope
    fun stepExecutionListener(): StepExecutionListener {
        return MemberImportStepExecutionListener()
    }

    @Bean("findReservationStep")
    fun tasklet(jobRepository: JobRepository, tasklet: Tasklet): Step {
        return StepBuilder(jobRepository).tasklet(tasklet).build()
    }

    @Bean("masterStep")
    fun masterStep(
        jobRepository: JobRepository,
        partitioner: Partitioner,
        @Qualifier("memberImportStep") memberImportSteps: Step,
    ): Step {
        return StepBuilder(jobRepository).partitioner("memberImportStepPartitioner", partitioner)
            .step(memberImportSteps).gridSize(5).taskExecutor(VirtualThreadTaskExecutor()).build()
    }

    @Bean("memberImportStep")
    fun step(
        jobRepository: JobRepository,
        itemReader: FlatFileItemReader<Member>,
        itemProcessor: ItemProcessor<Member, FullNameMember>,
        itemWriter: ItemWriter<FullNameMember>,
        stepExecutionListener: StepExecutionListener,
        skipListener: SkipListener<Member, FullNameMember>,
        @Qualifier("memberImportTransactionManager") memberImportTransactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder(jobRepository).chunk<Member, FullNameMember>(5).reader(itemReader)
            .processor(itemProcessor).writer(itemWriter).skip(ValidationException::class.java).skipListener(skipListener)
            .faultTolerant().listener(stepExecutionListener).transactionManager(memberImportTransactionManager)
            .build()
    }

    @Bean
    fun job(
        jobRepository: JobRepository,
        @Qualifier("findReservationStep") findReservationStep: Step,
        @Qualifier("masterStep") masterStep: Step,
    ): Job {
        return JobBuilder(jobRepository).start(findReservationStep).next(masterStep).build()
    }
}