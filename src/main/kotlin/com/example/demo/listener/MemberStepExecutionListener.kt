package com.example.demo.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

class MemberStepExecutionListener(dataSource: DataSource) : StepExecutionListener {

    private val jdbcOperations: JdbcOperations = JdbcTemplate(dataSource)

    override fun beforeStep(stepExecution: StepExecution) {
        countLogging()
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        countLogging()

        return ExitStatus.COMPLETED
    }

    private fun countLogging() {
        val sql = "SELECT count(*) FROM member"
        val count: Int = this.jdbcOperations.queryForObject<Int>(sql, Int::class.java)!!

        logger.info { "count of member: $count" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}