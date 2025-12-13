# CodeFM - Contexto del Proyecto

## Información General

- **Versión**: Definida en `pom.xml` raíz
- **Java**: 17
- **Spring Boot**: 3.2.3
- **Build Tool**: Maven (multi-módulo)
- **Gestión de versiones**: Todos los módulos heredan versión del parent POM

## Arquitectura de Módulos

```
codefm (parent)
├── codefm-domain          # Entidades y lógica de dominio
├── codefm-application     # Casos de uso
├── codefm-infrastructure  # Repositorios, JPA, Consul, Security
├── codefm-api             # Controllers y OpenAPI specs
├── codefm-kafka           # Productores/Consumidores Kafka + Avro
├── codefm-boot            # Módulo ejecutable principal
└── jacoco-report-aggregate # Reportes de cobertura
```

## Stack Tecnológico

### Backend

- Spring Boot 3.2.3 (Web, Data JPA, Actuator)
- MariaDB + P6Spy (logging SQL)
- Lombok 1.18.30
- MapStruct 1.5.5.Final

### Mensajería

- Apache Kafka + Spring Kafka
- Avro 1.11.3 para serialización
- Confluent Schema Registry 7.4.0

### Seguridad

- Spring Security + OAuth2 Resource Server
- Keycloak (autenticación/autorización)
- Infisical (gestión de secretos)

### Configuración

- Spring Cloud Consul (Config + Discovery) 2023.0.3

### Observabilidad

- Spring Boot Actuator
- Micrometer + Prometheus
- Grafana + Loki
- Logback con Logstash encoder

### Calidad de Código

- JaCoCo 0.8.10 (cobertura)
- SonarQube
- Karate (tests de integración)

### Documentación

- SpringDoc OpenAPI 2.1.0
- Swagger UI

## Configuraciones Importantes

### SonarQube Exclusions

```
**/codefm/api/controller/**
**/config/**
**/domain/exception/**
**/generated/**
```

### Workflows CI/CD

- `.github/workflows/karate-test.yml` - Tests automáticos
- Comandos: `deploy-pre`, `deploy-pro nombre-rama`

## Estructura de Dependencias

### codefm-domain

- Sin dependencias externas (solo Lombok)

### codefm-application

- Depende de: domain, infrastructure

### codefm-infrastructure

- Depende de: domain
- Incluye: JPA, MariaDB, Consul, Security OAuth2

### codefm-api

- Depende de: domain, application
- Genera código desde: `private-api.yaml`, `public-api.yaml`

### codefm-kafka

- Depende de: domain
- Genera clases Avro desde: `src/main/resources/avro/`

### codefm-boot

- Depende de: api, kafka
- Módulo ejecutable con Spring Boot Maven Plugin

## Endpoints Principales

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

## Notas de Desarrollo

1. **OpenAPI**: Las APIs se generan automáticamente desde YAML en `codefm-api/src/main/resources/`
2. **Avro**: Los esquemas están en `codefm-kafka/src/main/resources/avro/`
3. **Tests**: No agregar tests automáticamente a menos que se solicite explícitamente
4. **Versiones**: Usar `${project.version}` para dependencias entre módulos
5. **Repackage**: Deshabilitado en todos los módulos excepto `codefm-boot`
