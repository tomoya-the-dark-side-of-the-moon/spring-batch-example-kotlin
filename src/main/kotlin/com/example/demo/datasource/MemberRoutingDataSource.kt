package com.example.demo.datasource

import com.example.demo.datasource.DataSourceKey.dataSourceKey
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.sql.DataSource

class MemberRoutingDataSource : AbstractRoutingDataSource() {

    private val dataSources: ConcurrentMap<Int?, DataSource?> = ConcurrentHashMap<Int, DataSource>()

    override fun initialize() {
        // no op
    }

    public override fun determineTargetDataSource(): DataSource {
        val key = this.determineCurrentLookupKey()
        val dataSource = if (key != null) this.dataSources.get(determineCurrentLookupKey()) else null
        checkNotNull(dataSource) { "datasource is not found. key = " }

        return dataSource
    }

    override fun determineCurrentLookupKey(): Any? {
        return dataSourceKey
    }

    fun addDataSource(key: Int?, dataSource: DataSource?) {
        this.dataSources[key] = dataSource
    }
}