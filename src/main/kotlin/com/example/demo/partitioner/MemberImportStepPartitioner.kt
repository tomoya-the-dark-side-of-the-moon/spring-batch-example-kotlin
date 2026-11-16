package com.example.demo.partitioner

import com.example.demo.datasource.MemberRoutingDataSource
import com.example.demo.domain.Reservation
import org.springframework.batch.core.partition.Partitioner
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.datasource.DriverManagerDataSource


class MemberImportStepPartitioner(

    @param:Value("#{jobExecutionContext['reservations']}") private val reservations: MutableList<Reservation>,
    private val memberRoutingDataSource: MemberRoutingDataSource
) : Partitioner {
    override fun partition(gridSize: Int): Map<String, ExecutionContext> {
        val map: MutableMap<String, ExecutionContext> = HashMap()
        var i = 0
        for ((id, fileName, host, username, dbName, password) in this.reservations) {
            val executionContext: ExecutionContext = ExecutionContext()
            executionContext.put("fileName", fileName)
            executionContext.put("id", id)
            map["reservation-$i"] = executionContext

            val url = String.format("jdbc:postgresql://%s:5432/%s", host, dbName)
            val dataSource = DriverManagerDataSource(url, username!!, password!!)
            dataSource.setDriverClassName("org.postgresql.Driver")
            this.memberRoutingDataSource.addDataSource(id, dataSource)

            i++
        }

        return map
    }
}