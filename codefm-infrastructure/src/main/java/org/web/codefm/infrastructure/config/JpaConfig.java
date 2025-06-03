package org.web.codefm.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "org.web.codefm.infrastructure.jpa")
public class JpaConfig {

}

