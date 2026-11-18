package com.example.demo.config

import com.example.demo.datasource.MemberRoutingDataSource
import com.example.demo.domain.FullNameMember
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.configuration.annotation.StepScope
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
import org.springframework.orm.jpa.vendor.Database
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

    @Bean("batchDataSource")
    @Primary
    @BatchDataSource
    fun batchDataSource(): DataSource {
        return batchDataSourceProperties().initializeDataSourceBuilder().build()
    }

    @Bean("batchTransactionManager")
    @Primary
    fun batchTransactionManager(@Qualifier("batchDataSource") batchDataSource: DataSource): PlatformTransactionManager {
        return DataSourceTransactionManager(batchDataSource)
    }

    @Bean("memberRoutingDataSource")
    fun memberRoutingDataSource(): MemberRoutingDataSource {
        return MemberRoutingDataSource()
    }

    @Bean("memberImportDataSource")
    @StepScope
    fun memberImportDataSource(): DataSource {
        return memberRoutingDataSource().determineTargetDataSource()
    }

    @Bean("memberImportEntityManagerFactory")
    fun memberImportEntityManagerFactory(
        @Qualifier("memberImportDataSource") memberImportDataSource: DataSource?
    ): LocalEntityManagerFactoryBean {
        val localEntityManagerFactoryBean = LocalEntityManagerFactoryBean()
        // ネイティブイメージでは実行時のクラスパススキャンが効かないため、エンティティを明示的に登録する
        localEntityManagerFactoryBean.persistenceConfiguration.managedClass(FullNameMember::class.java)
        localEntityManagerFactoryBean.dataSource = memberImportDataSource
        val jpaVendorAdapter = HibernateJpaVendorAdapter()
        jpaVendorAdapter.setDatabase(Database.POSTGRESQL)
        localEntityManagerFactoryBean.jpaVendorAdapter = jpaVendorAdapter
        localEntityManagerFactoryBean.jpaPropertyMap["hibernate.boot.allow_jdbc_metadata_access"] = "false"
        localEntityManagerFactoryBean.jpaPropertyMap["hibernate.hbm2ddl.auto"] = "none"

        return localEntityManagerFactoryBean
    }

    @Bean("memberImportTransactionManager")
    fun memberImportTransactionManager(
        @Qualifier("memberImportEntityManagerFactory") memberImportEntityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(memberImportEntityManagerFactory)
    }

}