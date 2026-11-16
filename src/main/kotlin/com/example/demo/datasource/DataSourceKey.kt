package com.example.demo.datasource

object DataSourceKey {
    
    private val DATA_SOURCE_KEY = ThreadLocal<Int?>()

    var dataSourceKey: Int?
        get() = DATA_SOURCE_KEY.get()
        set(key) {
            DATA_SOURCE_KEY.set(key)
        }

    fun clearDataSourceKey() {
        DATA_SOURCE_KEY.remove()
    }
}