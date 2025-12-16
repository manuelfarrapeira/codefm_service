package org.web.codefm.infrastructure.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "org.web.codefm.infrastructure.jpa.codefm",
        entityManagerFactoryRef = "codefmEntityManagerFactory",
        transactionManagerRef = "codefmTransactionManager"
)
public class CodefmDataSourceConfig {

    @Primary
    @Bean(name = "codefmDataSourceProperties")
    @ConfigurationProperties("spring.datasource.codefm")
    public DataSourceProperties codefmDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "codefmDataSource")
    public DataSource codefmDataSource(@Qualifier("codefmDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name = "codefmEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean codefmEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("codefmDataSource") DataSource dataSource) {

        java.util.Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        properties.put("hibernate.hbm2ddl.auto", "none");

        return builder
                .dataSource(dataSource)
                .packages("org.web.codefm.infrastructure.entity.mariadb.codefm")
                .persistenceUnit("codefm")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "codefmTransactionManager")
    public PlatformTransactionManager codefmTransactionManager(
            @Qualifier("codefmEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
