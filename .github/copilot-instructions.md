# Instrucciones para GitHub Copilot - Proyecto CodeFM

Este documento proporciona las directrices que debo seguir cada vez que trabajes conmigo en el proyecto CodeFM.

## Información del Proyecto

**CodeFM** es una aplicación Java con arquitectura hexagonal (Clean Architecture) que sigue un patrón multi-capa con
Maven:

### Módulos Principales

- `codefm-domain`: Entidades y contratos (interfaces) de negocio
- `codefm-application`: Casos de uso e implementación de lógica de negocio
- `codefm-infrastructure`: Implementaciones (JPA, Mappers, Configuración)
- `codefm-api`: Controllers y DTOs generados desde OpenAPI
- `codefm-kafka`: Productores/Consumidores Kafka
- `codefm-boot`: Módulo ejecutable
- `karate-test`: Tests de integración
- `jacoco-report-aggregate`: Reportes de cobertura

---

## Flujo de Datos Obligatorio

El flujo debe seguir estrictamente este patrón:
**API (OpenAPI) → Controller → UseCase → Service → Repository → JPA → Entity JPA**

### Orden de Capas por Módulo

1. **codefm-domain**: Repository interfaces, Service interfaces, UseCase interfaces, Entidades de dominio, Excepciones
2. **codefm-application**: Service impl, UseCase impl, Tests
3. **codefm-infrastructure**: Repository impl, JPA Repository, Entity JPA, Mapper, Configuración
4. **codefm-api**: Controller, DTO Mapper, Tests

---

## Convenciones de Nombres

Aplicar estas convenciones de forma consistente:

| Concepto              | Ejemplo                |
|-----------------------|------------------------|
| Domain entity         | `School`               |
| JPA entity            | `SchoolEntity`         |
| DTO                   | `SchoolDTO`            |
| Repository interface  | `SchoolRepository`     |
| Repository impl       | `SchoolRepositoryImpl` |
| JPA repository        | `SchoolJPARepository`  |
| Service interface     | `SchoolService`        |
| Service impl          | `SchoolServiceImpl`    |
| UseCase interface     | `SchoolUseCase`        |
| UseCase impl          | `SchoolUseCaseImpl`    |
| Infrastructure mapper | `SchoolMapper`         |
| API mapper            | `SchoolDTOMapper`      |

---

## Requerimientos Críticos por Tipo de Archivo

### 1. Entidades de Dominio (codefm-domain/entity/)

```java
@Data
@Builder
@Generated
public class School {
    private Integer id;
    private String name;
}
```

**OBLIGATORIO**:

- Usar `@Data` (NO `@Getter`, `@Setter` por separado)
- Usar `@Builder` (NO `@NoArgsConstructor` + `@AllArgsConstructor`)
- Incluir anotación `@Generated`

### 2. Interfaces de Repository (codefm-domain/repository/)

```java
/**
 * Repository interface for school data access operations.
 * Provides methods to retrieve and manage school information.
 */
public interface SchoolRepository {
    /**
     * Finds all schools associated with a specific teacher.
     *
     * @param teacherId The unique identifier of the teacher
     * @return List of schools belonging to the specified teacher
     */
    List<School> findByTeacherId(Integer teacherId);
}
```

**OBLIGATORIO**:

- JavaDoc en INGLÉS con descripción de clase
- JavaDoc para cada método con `@param`, `@return`, `@throws`

### 3. Interfaces de Service (codefm-domain/service/)

```java
/**
 * Service interface for school business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface SchoolService {
    /**
     * Retrieves all schools associated with a specific teacher.
     *
     * @param teacherId The unique identifier of the teacher
     * @return List of schools belonging to the specified teacher
     */
    List<School> getSchoolsByTeacherId(Integer teacherId);
}
```

**OBLIGATORIO**:

- JavaDoc en INGLÉS con descripción de clase
- JavaDoc para cada método con `@param`, `@return`, `@throws`

### 4. Interfaces de UseCase (codefm-domain/usecase/)

```java
/**
 * Interface that defines school operations for teachers.
 * Handles school data retrieval and management.
 */
public interface SchoolUseCase {
    /**
     * Retrieves all schools associated with the authenticated teacher.
     *
     * @return List of schools belonging to the teacher
     */
    List<School> getSchoolsByTeacher();
}
```

**OBLIGATORIO**:

- JavaDoc en INGLÉS con descripción de clase
- JavaDoc para cada método con `@param`, `@return`, `@throws`

### 5. Implementación de UseCase (codefm-application/usecase/)

```java
@Service
@RequiredArgsConstructor
public class SchoolUseCaseImpl implements SchoolUseCase {
    private final SchoolService schoolService;
    private final SessionUser sessionUser;
    
    @Override
    public List<School> getSchoolsByTeacher() {
        Integer teacherId = Integer.valueOf(
            sessionUser.getParameters().get(SessionParameter.TEACHER_ID.getClaimName())
        );
        return schoolService.getSchoolsByTeacherId(teacherId);
    }
}
```

**OBLIGATORIO**:

- UseCase NUNCA llama directamente a Repository
- UseCase SIEMPRE pasa por Service
- Inyección por constructor con `@RequiredArgsConstructor`

### 6. Implementación de Service (codefm-application/service/)

```java
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {
    private final SchoolRepository schoolRepository;
    
    @Override
    public List<School> getSchoolsByTeacherId(Integer teacherId) {
        return schoolRepository.findByTeacherId(teacherId);
    }
}
```

**OBLIGATORIO**:

- Service orquesta una o más operaciones de Repository
- Service contiene lógica de negocio específica
- Service actúa como intermediario entre UseCase y Repository

### 7. Implementación de Repository (codefm-infrastructure/[module]/)

```java
@Repository
@RequiredArgsConstructor
public class SchoolRepositoryImpl implements SchoolRepository {
    private final SchoolJPARepository schoolJPARepository;
    private final SchoolMapper schoolMapper;
    
    @Override
    public List<School> findByTeacherId(Integer teacherId) {
        return schoolMapper.toModelList(
            schoolJPARepository.findByTeacherId(teacherId)
        );
    }
}
```

**OBLIGATORIO**:

- Mapear resultados de JPA a entidades de dominio usando Mapper
- Usar inyección por constructor
- Nunca retornar JPA entities directamente

### 8. JPA Repository (codefm-infrastructure/jpa/[schema]/)

```java
@Repository
public interface SchoolJPARepository extends JpaRepository<SchoolEntity, Integer> {
    List<SchoolEntity> findByTeacherId(Integer teacherId);
}
```

**OBLIGATORIO**:

- Extender `JpaRepository<Entity, ID>`
- Una para cada schema (codefm, teacher_notebook, etc.)

### 9. Entity JPA (codefm-infrastructure/entity/mariadb/[schema]/)

```java
@Entity
@Table(name = "schools")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class SchoolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;
    
    @Column(nullable = false, length = 200)
    private String name;
}
```

**OBLIGATORIO**:

- Usar `@NoArgsConstructor` + `@AllArgsConstructor` (diferente a Domain entities)
- Usar `@Data` para getters/setters
- Incluir anotación `@Generated`
- Organizar por schema: `mariadb/codefm/`, `mariadb/teacher_notebook/`

### 10. Mapper Infrastructure (codefm-infrastructure/mapper/)

```java
@Mapper(componentModel = "spring")
public interface SchoolMapper {
    School toModel(SchoolEntity entity);
    List<School> toModelList(List<SchoolEntity> entities);
}
```

**OBLIGATORIO**:

- Usar `@Mapper(componentModel = "spring")`
- Métodos de conversión de JPA entity → Domain entity
- Ejecutar `mvn clean compile` después de crear

### 11. DTO Mapper (codefm-api/mapper/)

```java

@Mapper(componentModel = "spring")
public interface SchoolDTOMapper {
    SchoolDTO toDTO(School school);

    List<SchoolDTO> toDTOList(List<School> schools);
}
```

**OBLIGATORIO**:

- Usar `@Mapper(componentModel = "spring")`
- Métodos de conversión de Domain entity → DTO
- Ejecutar `mvn clean compile` después de crear

### 12. Controller (codefm-api/controller/)

```java

@RestController
@RequiredArgsConstructor
public class PrivateSchools implements TeacherNoteBookSchoolsApi {

    private final SchoolUseCase schoolUseCase;
    private final SchoolDTOMapper schoolDTOMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SchoolDTO> createSchool(
            SchoolRequestDTO dto,           // posición 0
            String acceptLanguage           // posición 1
    ) {
        School schoolToCreate = schoolRequestMapper.toDomain(dto);
        School createdSchool = schoolUseCase.createSchool(schoolToCreate);
        return new ResponseEntity<>(schoolDTOMapper.toDTO(createdSchool), HttpStatus.CREATED);
    }
}
```

**OBLIGATORIO**:

- Implementar interfaz generada desde OpenAPI
- Usar `@Logged` para logging automático
- Usar `@PreAuthorize` con `UserRole` string para control de acceso
- SI el endpoint recibe header `Accept-Language`, USAR `@Locale(position)` indicando la posición (0-indexed) del
  parámetro
- Mapear DTO request → Domain entity
- Llamar a UseCase (NUNCA directamente a Service o Repository)
- Mapear Domain entity → DTO response
- Inyección por constructor con `@RequiredArgsConstructor`

---

## OpenAPI y Generación de Código

### Ubicaciones

- API privada: `codefm-api/src/main/resources/private-api.yaml`
- API pública: `codefm-api/src/main/resources/public-api.yaml`

**OBLIGATORIO**:

- Regenerar código con `mvn compile` tras cambios en YAML
- Si el endpoint tiene header `Accept-Language`, incluirlo en OpenAPI:
  ```yaml
  parameters:
    - name: Accept-Language
      in: header
      required: false
      schema:
        type: string
  ```

---

## Manejo de Errores y Excepciones

### Paso 1: Códigos de Error (ErrorCodeEnum)

Ubicación: `codefm-domain/src/main/java/org.web.codefm.domain/exception/ErrorCodeEnum.java`

**IMPORTANTE - Reutilizar códigos genéricos antes de crear nuevos**:

- `RESOURCE_NOT_FOUND("1003", "RESOURCE_NOT_FOUND")` - Recursos no encontrados
- `RESOURCE_FORBIDDEN("1004", "RESOURCE_FORBIDDEN")` - Acceso denegado
- `VALIDATION_ERROR("1006", "VALIDATION_ERROR")` - Errores de validación

Solo crear códigos específicos si requieren tratamiento diferenciado.

### Paso 2: Clase de Excepción (codefm-domain/exception/)

```java
public class SchoolValidationException extends ListErrorMessageBaseException {
    public SchoolValidationException(List<ErrorMessage> errors) {
        super(ErrorCodeEnum.VALIDATION_ERROR, errors);
    }
}
```

**OBLIGATORIO**:

- Usar `org.web.codefm.domain.entity.exception.ErrorMessage` (NUNCA DTOs de API)
- Extender `ListErrorMessageBaseException` para múltiples errores o `ErrorMessageBaseException` para simples

### Paso 3: Lanzar desde Service (codefm-application/service/)

**CRÍTICO - USO OBLIGATORIO DE MessageSource**:

```java
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {
    private final SchoolRepository schoolRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;
    
    @Override
    public School createSchool(School school) {
        List<ErrorMessage> errors = new ArrayList<>();
        
        if (school.getName() == null || school.getName().trim().isEmpty()) {
            String message = messageSource.getMessage(
                MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED,
                null,
                sessionUser.getLocale()
            );
            errors.add(new ErrorMessage("name", message));
        }
        
        if (!errors.isEmpty()) {
            throw new SchoolValidationException(errors);
        }
        
        return schoolRepository.save(school);
    }
}
```

**OBLIGATORIO**:

- ✅ **SIEMPRE inyectar `MessageSource` y `SessionUser`** en los Services
- ✅ **NUNCA pasar directamente `MessageKeys.XXX`** a las excepciones
- ✅ **SIEMPRE usar `messageSource.getMessage(MessageKeys.XXX, null, sessionUser.getLocale())`**
- ✅ El mensaje se internacionaliza según el locale del usuario
- ✅ Usar constantes de `MessageKeys` (NUNCA strings literales)
- ✅ Lanzar excepciones desde Service (lógica de negocio)

**Ejemplo CORRECTO**:

```java
String message = messageSource.getMessage(MessageKeys.SCHOOL_NOT_FOUND, null, sessionUser.getLocale());
throw new SchoolNotFoundException(message);
```

**Ejemplo INCORRECTO** ❌:

```java
throw new SchoolNotFoundException(MessageKeys.SCHOOL_NOT_FOUND); // ❌ NO HACER ESTO
```

### Paso 3.1: Uso de MessageSource en Mappers (codefm-api/mapper/)

Los mappers que necesiten lanzar excepciones también deben usar `MessageSource`:

```java
@Mapper(componentModel = "spring")
public abstract class SchoolRequestMapper {

    @Autowired
    protected MessageSource messageSource;
    
    @Autowired
    protected SessionUser sessionUser;

    @Mapping(target = "id", ignore = true)
    public abstract School toDomain(SchoolRequestDTO dto);

    protected void validateSchool(SchoolRequestDTO dto) {
        if (dto.getName() == null) {
            List<ErrorMessage> errors = new ArrayList<>();
            String message = messageSource.getMessage(
                MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED,
                null,
                sessionUser.getLocale()
            );
            errors.add(new ErrorMessage("name", message));
            throw new SchoolValidationException(errors);
        }
    }
}
```

**OBLIGATORIO para Mappers**:

- ✅ Cambiar de `interface` a `abstract class` para poder inyectar dependencias
- ✅ Usar `@Autowired` para inyectar `MessageSource` y `SessionUser`
- ✅ Usar `messageSource.getMessage()` en lugar de pasar claves directamente

### Paso 4: Mapeo a HttpStatus (ExceptionStatusEnum)

Ubicación: `codefm-api/src/main/java/org.web.codefm.api/exception/ExceptionStatusEnum.java`

```java
@Getter
@AllArgsConstructor
public enum ExceptionStatusEnum {
    VALIDATION_ERROR(SchoolValidationException.class, HttpStatus.BAD_REQUEST);
    
    private final Class<?> exceptionClazz;
    private final HttpStatus status;
}
```

**OBLIGATORIO**:

- Registrar TODAS las excepciones personalizadas
- Mapear al `HttpStatus` apropiado

---

## Internacionalización (i18n)

### Configuración en application.yml

```yaml
spring:
  messages:
    basename: messages
    encoding: UTF-8
```

### Archivos de Propiedades

- `codefm-boot/src/main/resources/messages_en.properties` (Inglés)
- `codefm-boot/src/main/resources/messages_es.properties` (Español)

**OBLIGATORIO**:

- Archivos en Spanish DEBEN estar en ASCII con escape Unicode
- Caracteres españoles: `ñ` → `\u00f1`, `á` → `\u00e1`, `é` → `\u00e9`, `í` → `\u00ed`, `ó` → `\u00f3`, `ú` → `\u00fa`

Ejemplo:

```properties
# messages_es.properties
school.validation.name.required=El nombre del colegio es obligatorio.
school.validation.tlf.invalid=El n\u00famero de tel\u00e9fono debe tener 9 d\u00edgitos.
```

### MessageKeys (codefm-domain/i18n/MessageKeys.java)

```java
@UtilityClass
public class MessageKeys {
    public static final String SCHOOL_VALIDATION_NAME_REQUIRED = "school.validation.name.required";
    public static final String SCHOOL_VALIDATION_TLF_INVALID = "school.validation.tlf.invalid";
}
```

**OBLIGATORIO**:

- NUNCA usar strings literales en código
- SIEMPRE crear constantes en `MessageKeys`

---

## Patrón de Soft Delete

**Ubicación**: Implementación de Repository

```java
@Override
public School softDeleteSchool(Integer schoolId, Integer teacherId) {
    SchoolEntity schoolEntity = schoolJPARepository
        .findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId)
        .orElseThrow(() -> new IllegalArgumentException("School not found or not owned by teacher or already deleted."));
    
    schoolEntity.setDeletionDate(LocalDate.now());
    SchoolEntity updatedEntity = schoolJPARepository.save(schoolEntity);
    return schoolMapper.toModel(updatedEntity);
}
```

**OBLIGATORIO**:

- Buscar con validación de ownership: `findByIdAndTeacherIdAndDeletionDateIsNull`
- Lanzar `IllegalArgumentException` si no se encuentra
- Establecer `deletionDate` a `LocalDate.now()`
- Guardar y retornar entidad mapeada al dominio

---

## Testing

### Estructura

- Tests unitarios de Service: `codefm-application/src/test/java/org.web.codefm.service/`
- Tests unitarios de UseCase: `codefm-application/src/test/java/org.web.codefm.usecase/`
- Tests unitarios de Repository: `codefm-infrastructure/src/test/java/org.web.codefm.infrastructure/`
- Tests de integración Karate: `karate-test/src/test/resources/features/`

### Convención de Nombres de Tests

```
methodName_shouldExpectedBehavior_whenCondition
```

Ejemplo:

```java
@Test
void getSchoolsByTeacherId_shouldReturnSchools_whenTeacherExists() { }

@Test
void createSchool_shouldThrowValidationException_whenNameIsEmpty() { }
```

### Test de Service

```java
@ExtendWith(MockitoExtension.class)
class SchoolServiceImplTest {
    
    @Mock
    private SchoolRepository schoolRepository;
    
    @InjectMocks
    private SchoolServiceImpl schoolService;
    
    @Test
    void getSchoolsByTeacherId_shouldReturnSchools() {
        Integer teacherId = 1;
        School school1 = School.builder().id(1).teacherId(teacherId).name("School A").build();
        List<School> expectedSchools = Arrays.asList(school1);
        
        when(schoolRepository.findByTeacherId(teacherId)).thenReturn(expectedSchools);
        
        List<School> result = schoolService.getSchoolsByTeacherId(teacherId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(schoolRepository, times(1)).findByTeacherId(teacherId);
    }
}
```

### Test de Karate

```gherkin
Feature: School Endpoints

Background:
  Given url baseHttpsUrl
  And cookie SESSION = authTokens.karateuseradmin

Scenario: Get schools by teacher
  * def schoolSchema = { id: '#number', name: '#string', town: '#string' }
  Given path '/private/v1/schools'
  When method GET
  Then status 200
  And match each response == schoolSchema
```

---

## Checklist para Crear un Nuevo Endpoint

1. [ ] Definir API en OpenAPI (private-api.yaml o public-api.yaml)
2. [ ] Crear Entidad de Dominio con `@Data`, `@Builder`, `@Generated`
3. [ ] Crear Repository Interface con JavaDoc en inglés
4. [ ] Crear Service Interface con JavaDoc en inglés
5. [ ] Crear UseCase Interface con JavaDoc en inglés
6. [ ] Crear Entity JPA con `@NoArgsConstructor`, `@AllArgsConstructor`, `@Data`, `@Generated`
7. [ ] Crear JPA Repository
8. [ ] Crear Mapper Infrastructure
9. [ ] Implementar Repository
10. [ ] Crear Test de Repository
11. [ ] Implementar Service (lógica de negocio)
12. [ ] Crear Test de Service
13. [ ] Implementar UseCase
14. [ ] Crear Test de UseCase
15. [ ] Crear Mapper DTO
16. [ ] Implementar Controller
    - [ ] Agregar `@Logged` para logging
    - [ ] Agregar `@PreAuthorize` con `UserRole` enum
    - [ ] SI recibe `Accept-Language`, agregar `@Locale(position)` correcto
    - [ ] Mapear DTO request → Domain → DTO response
17. [ ] Crear excepciones personalizadas si es necesario (siguiendo pasos de Manejo de Errores)
18. [ ] Crear claves i18n en MessageKeys
19. [ ] Agregar mensajes en messages_en.properties y messages_es.properties
20. [ ] Crear Test de Karate
21. [ ] Ejecutar `mvn clean compile`
22. [ ] Ejecutar `mvn test`

---

## Comandos Frecuentes

```bash
# Compilar y generar código desde OpenAPI y Mappers
mvn clean compile

# Ejecutar todos los tests unitarios
mvn test

# Ejecutar tests de un módulo específico
mvn test -pl codefm-application

# Ejecutar un test específico
mvn test -Dtest=SchoolServiceImplTest

# Ejecutar tests de integración Karate
mvn test -pl karate-test

# Compilar todo el proyecto
mvn clean install
```

---

## Notas Importantes

1. **Inyección de Dependencias**: SIEMPRE usar constructor con `@RequiredArgsConstructor`
2. **UseCase → Service → Repository**: NUNCA saltar capas
3. **JavaDoc en Inglés**: OBLIGATORIO en todas las interfaces (UseCase, Service, Repository)
4. **MapStruct**: Ejecutar `mvn clean compile` después de crear mappers
5. **Multi-DataSource**: Organizar entities JPA por schema en directorios separados
6. **SessionUser.parameters**: Usar este Map para datos del JWT (ej. teacher_id)
7. **Soft Delete**: SIEMPRE validar `DeletionDateIsNull` al buscar
8. **ErrorMessage**: Usar `org.web.codefm.domain.entity.exception.ErrorMessage` (NUNCA DTOs)
9. **MessageKeys**: NUNCA strings literales en código, SIEMPRE constantes
10. **@Locale**: OBLIGATORIO cuando endpoint recibe header `Accept-Language`
11. **Anotaciones de Seguridad**: Usar `UserRole` enum en `@PreAuthorize` para mejor mantenibilidad
12. **Soft Delete en Queries**: Filtrar por `DeletionDateIsNull` en todas las queries de lectura

---

## Ejemplos de Referencia

Consultar los siguientes archivos para ver ejemplos completos:

- `development-guide.md`: Guía completa del proyecto
- Módulos existentes como `codefm-application`, `codefm-infrastructure`, `codefm-api`


