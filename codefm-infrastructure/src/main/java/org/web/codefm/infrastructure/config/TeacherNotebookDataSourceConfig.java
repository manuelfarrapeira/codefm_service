package org.web.codefm.infrastructure.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "org.web.codefm.infrastructure.jpa.teachernotebook",
        entityManagerFactoryRef = "teacherNotebookEntityManagerFactory",
        transactionManagerRef = "teacherNotebookTransactionManager"
)
public class TeacherNotebookDataSourceConfig {

    @Bean(name = "teacherNotebookDataSourceProperties")
    @ConfigurationProperties("spring.datasource.teachernotebook")
    public DataSourceProperties teacherNotebookDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "teacherNotebookDataSource")
    public DataSource teacherNotebookDataSource(@Qualifier("teacherNotebookDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "teacherNotebookEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean teacherNotebookEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("teacherNotebookDataSource") DataSource dataSource) {

        java.util.Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        properties.put("hibernate.hbm2ddl.auto", "none");

        return builder
                .dataSource(dataSource)
                .packages("org.web.codefm.infrastructure.entity.mariadb.teachernotebook")
                .persistenceUnit("teachernotebook")
                .properties(properties)
                .build();
    }

    @Bean(name = "teacherNotebookTransactionManager")
    public PlatformTransactionManager teacherNotebookTransactionManager(
            @Qualifier("teacherNotebookEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "teacherNotebookJdbcTemplate")
    public JdbcTemplate teacherNotebookJdbcTemplate(
            @Qualifier("teacherNotebookDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
