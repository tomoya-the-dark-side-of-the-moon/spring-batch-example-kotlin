package com.example.demo.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
class DataSourceConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    fun batchDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    @BatchDataSource
    fun batchDataSource(): DataSource {
        return batchDataSourceProperties().initializeDataSourceBuilder().build()
    }

    @Bean
    @ConfigurationProperties("spring.business.datasource")
    fun businessDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean("businessDataSource")
    fun businessDataSource(): DataSource {
        return businessDataSourceProperties().initializeDataSourceBuilder().build()
    }

    @Bean("businessEntityManagerFactory")
    fun businessEntityManagerFactory(
        @Qualifier("businessDataSource") businessDataSource: DataSource?
    ): LocalEntityManagerFactoryBean {
        val localEntityManagerFactoryBean = LocalEntityManagerFactoryBean()
        localEntityManagerFactoryBean.setPackagesToScan("com.example.demo.domain")
        localEntityManagerFactoryBean.dataSource = businessDataSource
        localEntityManagerFactoryBean.jpaVendorAdapter = HibernateJpaVendorAdapter()

        return localEntityManagerFactoryBean
    }

    @Bean("businessPlatformTransactionManager")
    fun businessPlatformTransactionManager(
        @Qualifier("businessDataSource") businessDataSource: DataSource?
    ): PlatformTransactionManager {
        return JpaTransactionManager(businessEntityManagerFactory(businessDataSource).getObject()!!)
    }

}