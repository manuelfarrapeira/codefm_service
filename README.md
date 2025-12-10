# CodeFM - Aplicación Spring Boot con Stack DevOps Completo

## 🚀 Descripción

CodeFM es una aplicación Spring Boot que implementa un stack completo de herramientas DevOps para desarrollo, seguridad,
monitoreo y despliegue automatizado.

## 🛠️ Stack Tecnológico

### Backend

- **Java 17** con Spring Boot 3.2.3
- **MariaDB** como base de datos principal
- **Maven** para gestión de dependencias
- **Apache Kafka** para mensajería asíncrona y event streaming

### Seguridad

- **OAuth2** con Keycloak para autenticación y autorización
- **Infisical** para gestión segura de secretos y variables de entorno

### Monitoreo y Observabilidad

- **Prometheus** para métricas de aplicación
- **Grafana** para dashboards y visualización
- **Loki** para agregación y análisis de logs
- **Spring Boot Actuator** para health checks y métricas

### Calidad de Código

- **SonarQube** para análisis estático de código
- **JaCoCo** para cobertura de código
- **Karate** para tests de integración y API

### Gestión de Configuración

- **Consul** para manejo de variables en caliente
- **Spring Cloud Config** para configuración distribuida

### Mensajería y Event Streaming

- **Apache Kafka** para comunicación asíncrona entre servicios
- **Avro** para serialización de mensajes
- **Schema Registry** para gestión de esquemas
- **Spring Kafka** para integración con Spring Boot

### Documentación

- **Swagger/OpenAPI 3** para documentación automática de APIs

## 📋 APIs y Documentación

### Swagger UI

La documentación interactiva de la API está disponible en:

```
http://localhost:8080/swagger-ui.html
```

### Endpoints de Actuator

- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

## 🔐 Seguridad y Secretos

### Keycloak OAuth2

La aplicación utiliza Keycloak para:

- Autenticación de usuarios
- Autorización basada en roles
- Single Sign-On (SSO)
- Gestión de sesiones

### Infisical

Gestión segura de secretos:

- Variables de entorno encriptadas
- Rotación automática de secretos
- Auditoría de acceso a secretos
- Integración con CI/CD

## 📊 Monitoreo

### Prometheus + Grafana

- Métricas de aplicación en tiempo real
- Alertas automáticas
- Dashboards personalizados
- Monitoreo de performance

### Loki

- Agregación centralizada de logs
- Búsqueda y filtrado avanzado
- Correlación de logs con métricas

## 📨 Kafka

### Configuración

La aplicación utiliza Kafka para:

- Publicación de eventos de usuario
- Consumo de mensajes asíncronos
- Serialización con Avro
- Integración con Schema Registry

### Topics

- **user-topic**: Eventos de usuarios (creación, actualización)

### Producers

- **ExampleKafkaProducer**: Publica eventos de usuario al topic configurado

### Consumers

- **ExampleKafkaConsumer**: Consume y procesa eventos de usuario

## 🧪 Testing

### Karate Tests

Tests automatizados de API que incluyen:

- Tests de regresión
- Validación de contratos
- Tests de integración
- Ejecución en CI/CD

## ⚙️ CI/CD Workflows

### 🚀 Deploy PRE

**Comando en PR:**

```
deploy-pre
```

**Comando en Issue (rama específica):**

```
deploy-pre nombre-rama
```

### 🏭 Deploy PRO

**Comando en Issue:**

```
deploy-pro nombre-rama
```

### 🧪 Ejecutar Tests Karate

**Trigger automático:** Pull Requests a `master` o `develop`

**Ejecución manual:**

- Workflow dispatch con selección de rama
- Comentarios automáticos en PR con resultados

### 🔍 Detección de Secretos

- Escaneo automático en cada push
- Prevención de commits con secretos
- Alertas de seguridad

### 📊 Análisis SonarQube

- Análisis automático en PRs
- Métricas de calidad de código
- Detección de vulnerabilidades
- Cobertura de código con JaCoCo

## 🏗️ Arquitectura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   codefm-api    │    │ codefm-application│    │  codefm-domain  │
│   (Controllers) │────│   (Use Cases)    │────│   (Entities)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │codefm-infrastructure│
                    │  (Repositories)  │
                    └─────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  codefm-kafka   │  │   codefm-boot   │  │ jacoco-report   │
│ (Kafka Events)  │  │ (Spring Boot)   │  │  (Coverage)     │
└─────────────────┘  └─────────────────┘  └─────────────────┘
         │
    Apache Kafka
```