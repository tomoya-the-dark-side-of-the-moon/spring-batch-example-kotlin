package com.example.demo.tasklet

import com.example.demo.domain.Reservation
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

class FindReservationTasklet(dataSource: DataSource) : Tasklet {
    private val jdbcOperations: JdbcOperations = JdbcTemplate(dataSource)

    @Throws(Exception::class)
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val sql =
            "SELECT b.id, b.file_name, a.host, a.user_name, a.db_name, a.password FROM server a INNER JOIN reservation b ON a.id = b.id ORDER BY a.id"
        val reservations = this.jdbcOperations.query(sql, DataClassRowMapper(Reservation::class.java))

        val executionContext = chunkContext.stepContext.stepExecution.jobExecution
            .executionContext
        executionContext.put("reservations", reservations)

        return RepeatStatus.FINISHED
    }
}