package com.example.demo.listener

import com.example.demo.datasource.DataSourceKey.clearDataSourceKey
import com.example.demo.datasource.DataSourceKey.dataSourceKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution

class MemberImportStepExecutionListener : StepExecutionListener {

    override fun beforeStep(stepExecution: StepExecution) {
        val executionContext = stepExecution.executionContext

        val id = executionContext.getInt("id")
        val fileName = executionContext.getString("fileName")

        logger.info("id: {}, fileName: {}", id, fileName)

        dataSourceKey = id
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        clearDataSourceKey()

        return stepExecution.exitStatus
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MemberImportStepExecutionListener::class.java)
    }
}