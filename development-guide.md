# CodeFM - Guía de Desarrollo

## Arquitectura del Proyecto

CodeFM sigue una arquitectura hexagonal (Clean Architecture) con separación clara de responsabilidades en módulos Maven:

```
codefm (parent)
├── codefm-domain          # Entidades y contratos (interfaces)
├── codefm-application     # Casos de uso (lógica de negocio)
├── codefm-infrastructure  # Implementaciones (JPA, Mappers, Configuración)
├── codefm-api             # Controllers y DTOs generados desde OpenAPI
├── codefm-kafka           # Productores/Consumidores Kafka
├── codefm-boot            # Módulo ejecutable
├── karate-test            # Tests de integración (Karate)
└── jacoco-report-aggregate # Reportes de cobertura
```

## Flujo de Datos: Endpoint → Base de Datos

### 1. Definición de API (OpenAPI)

**Ubicación**: `codefm-api/src/main/resources/private-api.yaml` o `public-api.yaml`

```yaml
paths:
  /teacher-notebook/schools:
    get:
      tags:
        - TeacherNoteBookSchools
      operationId: schools
      responses:
        '200':
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/SchoolDTO'

components:
  schemas:
    SchoolDTO:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string
```

**Generación automática**: Maven genera interfaces y DTOs en `target/generated-sources/`

### 2. Controller (API Layer)

**Ubicación**: `codefm-api/src/main/java/org.web.codefm.api/controller/`

```java
@RestController
@RequiredArgsConstructor
public class PrivateSchools implements TeacherNoteBookSchoolsApi {
    
    private final SchoolUseCase schoolUseCase;
    private final SchoolDTOMapper schoolDTOMapper;
    
    @Logged
    @Override
    @PreAuthorize("hasRole('" + UserRole.TEACHER.getRoleName() + "')")
    public ResponseEntity<List<SchoolDTO>> schools() {
        List<School> schools = schoolUseCase.getSchoolsByTeacher();
        List<SchoolDTO> dtos = schoolDTOMapper.toDTOList(schools);
        return ResponseEntity.ok(dtos);
    }
    
    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('" + UserRole.TEACHER.getRoleName() + "')")
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

**Anotaciones importantes**:

- `@Logged`: Logging automático de entrada/salida del método
- `@PreAuthorize`: Control de acceso basado en roles
- `@Locale(position)`: **OBLIGATORIO** cuando el endpoint recibe header `Accept-Language`. Indica la posición (
  0-indexed) del parámetro para actualizar el locale de SessionUser
- `@Override`: Implementa el método generado desde OpenAPI

### 3. Mapper DTO (API Layer)

**Ubicación**: `codefm-api/src/main/java/org.web.codefm.api/mapper/`

```java
@Mapper(componentModel = "spring")
public interface SchoolDTOMapper {
    SchoolDTO toDTO(School school);
    List<SchoolDTO> toDTOList(List<School> schools);
}
```

### 4. Caso de Uso - Interfaz (Domain Layer)

**Ubicación**: `codefm-domain/src/main/java/org.web.codefm.domain/usecase/`

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

**IMPORTANTE**: Siempre incluir JavaDoc en inglés:

- Descripción de la interfaz con `/** */`
- Descripción de cada método con `@param`, `@return`, `@throws` si aplica
- Explicar el propósito y comportamiento esperado

### 5. Caso de Uso - Implementación (Application Layer)

**Ubicación**: `codefm-application/src/main/java/org.web.codefm.usecase/`

```java
@Service
@RequiredArgsConstructor
public class SchoolUseCaseImpl implements SchoolUseCase {
    
    private final SchoolService schoolService;
    private final SessionUser sessionUser;
    
    @Override
    public List<School> getSchoolsByTeacher() {
        Integer teacherId = Integer.valueOf(sessionUser.getParameters().get(SessionParameter.TEACHER_ID.getClaimName()));
        return schoolService.getSchoolsByTeacherId(teacherId);
    }
}
```

**IMPORTANTE**: Los casos de uso NUNCA llaman directamente a repositories, siempre pasan por servicios

### 6. Service - Interfaz (Domain Layer)

**Ubicación**: `codefm-domain/src/main/java/org.web.codefm.domain/service/`

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

**IMPORTANTE**: Siempre incluir JavaDoc en inglés en las interfaces de servicio

### 7. Service - Implementación (Application Layer)

**Ubicación**: `codefm-application/src/main/java/org.web.codefm.service/`

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

**Responsabilidades**:

- Implementar la lógica de negocio específica
- Orquestar llamadas a uno o más repositories
- Aplicar validaciones y transformaciones
- Actuar como intermediario entre UseCase y Repository

### 8. Entidad de Dominio (Domain Layer)

**Ubicación**: `codefm-domain/src/main/java/org.web.codefm.domain/entity/`

```java
@Data
@Builder
@Generated
public class School {
    private Integer id;
    private Integer teacherId;
    private String name;
    private String town;
    private Integer tlf;
}
```

**IMPORTANTE**: Las entidades de dominio deben usar `@Data` para los métodos habituales (getters, setters, etc.) y
`@Builder` para la construcción de objetos. Esto proporciona un modo de instanciación más legible y robusto,
especialmente en los tests. Evitar el uso de `@NoArgsConstructor` y `@AllArgsConstructor` directamente.

### 9. Repository - Interfaz (Domain Layer)

**Ubicación**: `codefm-domain/src/main/java/org.web.codefm.domain/repository/`

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

**IMPORTANTE**: Siempre incluir JavaDoc en inglés con descripción de parámetros y retorno

### 10. Repository - Implementación (Infrastructure Layer)

**Ubicación**: `codefm-infrastructure/src/main/java/org.web.codefm.infrastructure/[modulo]/`

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

    @Override
    public School softDeleteSchool(Integer schoolId, Integer teacherId) {
        SchoolEntity schoolEntity = schoolJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("School not found or not owned by teacher or already deleted."));

        schoolEntity.setDeletionDate(LocalDate.now());
        SchoolEntity updatedEntity = schoolJPARepository.save(schoolEntity);
        return schoolMapper.toModel(updatedEntity);
    }
}
```

**Patrón de Soft Delete**:

- Buscar la entidad con validación de ownership usando un método JPA específico (ej.
  `findByIdAndTeacherIdAndDeletionDateIsNull`)
- Lanzar `IllegalArgumentException` si no se encuentra o no pertenece al usuario
- Establecer `deletionDate` con `LocalDate.now()`
- Guardar la entidad actualizada con `save()`
- Retornar la entidad mapeada al dominio

### 11. JPA Repository (Infrastructure Layer)

**Ubicación**: `codefm-infrastructure/src/main/java/org.web.codefm.infrastructure/jpa/[schema]/`

```java
@Repository
public interface SchoolJPARepository extends JpaRepository<SchoolEntity, Integer> {
    List<SchoolEntity> findByTeacherId(Integer teacherId);
}
```

### 12. Entity JPA (Infrastructure Layer)

**Ubicación**: `codefm-infrastructure/src/main/java/org.web.codefm.infrastructure/entity/mariadb/[schema]/`

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

**Organización por schema**:

- `mariadb/codefm/` → Schema codefm
- `mariadb/teachernotebook/` → Schema teacher_notebook

### 13. Mapper Infrastructure (Infrastructure Layer)

**Ubicación**: `codefm-infrastructure/src/main/java/org.web.codefm.infrastructure/mapper/`

```java
@Mapper(componentModel = "spring")
public interface SchoolMapper {
    School toModel(SchoolEntity entity);
    List<School> toModelList(List<SchoolEntity> entities);
}
```

## Configuración Multi-DataSource

### DataSource Config

**Ubicación**: `codefm-infrastructure/src/main/java/org.web.codefm.infrastructure/config/`

```java
@Configuration
@EnableJpaRepositories(
    basePackages = "org.web.codefm.infrastructure.jpa",
    entityManagerFactoryRef = "codefmEntityManagerFactory",
    transactionManagerRef = "codefmTransactionManager"
)
public class CodefmDataSourceConfig {
    
    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.codefm")
    public DataSourceProperties codefmDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Primary
    @Bean
    public DataSource codefmDataSource(
            @Qualifier("codefmDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
```

### Configuración en application.yml

```yaml
spring:
  datasource:
    codefm:
      url: jdbc:p6spy:mariadb://192.168.18.10:3306/codefm
      username: codefm
      password: ${MARIADB_PASSWORD}
    teachernotebook:
      url: jdbc:p6spy:mariadb://192.168.18.10:3306/teacher_notebook
      username: codefm
      password: ${MARIADB_PASSWORD}
```

## Seguridad y Sesión de Usuario

### SessionUser

```java
@Data
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class SessionUser implements Serializable {
    private String id;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permisos;
    private Map<String, String> parameters = new HashMap<>();
}
```

**Uso de parameters**: Almacenar datos adicionales del JWT como `teacher_id`

## Manejo de Errores y Excepciones

Para devolver errores de negocio o de validación a la API de forma consistente, se debe seguir un patrón específico que
se integra con el manejador de excepciones global.

### Flujo de Creación de una Excepción

#### Paso 1: Añadir un Código de Error

Toda excepción de negocio debe tener un código único. Añade una nueva entrada al `enum` ubicado en
`codefm-domain/src/main/java/org.web.codefm.domain/exception/ErrorCodeEnum.java`.

```java
// ErrorCodeEnum.java
public enum ErrorCodeEnum {
   // ... otros errores
   VALIDATION_ERROR("1006", "VALIDATION_ERROR");
}
```

**IMPORTANTE - Reutilización de Códigos Genéricos**:

- Antes de crear nuevos códigos de error, verifica si existen códigos genéricos que puedan reutilizarse.
- Códigos genéricos disponibles:
    - `RESOURCE_NOT_FOUND("1003", "RESOURCE_NOT_FOUND")` - Para recursos no encontrados (School, Class, etc.)
    - `RESOURCE_FORBIDDEN("1004", "RESOURCE_FORBIDDEN")` - Para accesos denegados a recursos
    - `VALIDATION_ERROR("1006", "VALIDATION_ERROR")` - Para errores de validación
- Solo crea códigos específicos cuando el error requiera un tratamiento o comportamiento diferenciado.
- Ejemplo de reutilización:
  ```java
  // Usar RESOURCE_NOT_FOUND para diferentes tipos de recursos
  public class SchoolNotFoundException extends BaseException {
      public SchoolNotFoundException(String message) {
          super(ErrorCodeEnum.RESOURCE_NOT_FOUND, message);
      }
  }
  
  public class ClassNotFoundException extends BaseException {
      public ClassNotFoundException(String message) {
          super(ErrorCodeEnum.RESOURCE_NOT_FOUND, message);
      }
  }
  ```

#### Paso 2: Crear la Clase de Excepción

Crea una nueva clase de excepción en el módulo `codefm-domain`, típicamente en un subpaquete de `exception`.

- **Para errores con múltiples detalles (ej. validación):** La excepción debe extender `ListErrorMessageBaseException`.
- **Para errores simples:** Puede extender `BaseException` o `ErrorMessageBaseException`.

```java
// SchoolValidationException.java
public class SchoolValidationException extends ListErrorMessageBaseException {

   public SchoolValidationException(List<ErrorMessage> errors) {
      super(ErrorCodeEnum.VALIDATION_ERROR, errors);
   }
}
```

**IMPORTANTE**: La excepción debe usar `org.web.codefm.domain.entity.exception.ErrorMessage` para los detalles, **NUNCA
** un DTO de la capa de API.

#### Paso 3: Lanzar la Excepción desde el Service

En la capa de servicio (`codefm-application`), donde se realiza la lógica de negocio, se debe instanciar y lanzar la
excepción cuando una validación falla.

```java
// SchoolServiceImpl.java
@Override
public School createSchool(School school) {
   List<ErrorMessage> errors = new ArrayList<>();

   if (school.getName() == null || school.getName().trim().isEmpty()) {
      errors.add(new ErrorMessage("name", "school.validation.name.required"));
   }

   if (!errors.isEmpty()) {
      throw new SchoolValidationException(errors);
   }

   return schoolRepository.save(school);
}
```

#### Paso 4: Mapeo de Excepciones a HttpStatus en la API

Para que el `RestExceptionHandler` devuelva el `HttpStatus` correcto para cada excepción de negocio, es necesario
registrar la excepción y su `HttpStatus` asociado en el `ExceptionStatusEnum`.

**Ubicación**: `codefm-api/src/main/java/org.web.codefm.api/exception/ExceptionStatusEnum.java`

```java
// ExceptionStatusEnum.java
@Getter
@AllArgsConstructor
public enum ExceptionStatusEnum {

   USER_NOT_FOUND(UserNotFound.class, HttpStatus.NOT_FOUND),
   VALIDATION_ERROR(SchoolValidationException.class, HttpStatus.BAD_REQUEST); // Ejemplo: Mapeo de SchoolValidationException a 400 Bad Request

   private final Class<?> exceptionClazz;
   private final HttpStatus status;

   public static <T extends Throwable> ExceptionStatusEnum getExceptionEnum(final Class<T> obj) {
      return Arrays.stream(ExceptionStatusEnum.values())
              .filter(ex -> (obj.equals(ex.getExceptionClazz()))).findFirst().orElse(null);
   }
}
```

**Instrucciones para añadir nuevas excepciones**:

1. Asegúrate de que tu excepción de dominio extienda de `BaseException` o una de sus subclases (
   `ErrorMessageBaseException`, `ListErrorMessageBaseException`).
2. Añade una nueva entrada al `ExceptionStatusEnum` con un nombre descriptivo, la clase de tu excepción y el
   `HttpStatus` deseado.
3. Importa la clase de tu excepción en `ExceptionStatusEnum.java`.

## Internacionalización (i18n) de Mensajes de Error

Para soportar múltiples idiomas en los mensajes de error de la API, se sigue una estrategia de internacionalización (
i18n) basada en claves.

### Configuración de Spring MessageSource

Para que Spring Boot cargue los ficheros de propiedades de mensajes, se debe añadir la siguiente configuración en
`codefm-boot/src/main/resources/application.yml`:

```yaml
spring:
   messages:
      basename: messages # Esto indica a Spring que busque messages.properties, messages_en.properties, messages_es.properties, etc.
      encoding: UTF-8
```

### Flujo de Internacionalización

#### Paso 1: Añadir Mensajes a los Ficheros de Propiedades

Los textos de los mensajes de error se almacenan en ficheros de propiedades en `codefm-boot/src/main/resources/`.

- `messages_en.properties` (para inglés)
- `messages_es.properties` (para español)

Cada mensaje tiene una clave única.

```properties
# messages_en.properties
school.validation.name.required=School name is required.
school.validation.tlf.invalid=Telephone number must be 9 digits.
```

```properties
# messages_es.properties
school.validation.name.required=El nombre del colegio es obligatorio.
school.validation.tlf.invalid=El n\u00famero de tel\u00e9fono debe tener 9 d\u00edgitos.
```

**IMPORTANTE - Codificación de Caracteres**:

- Los archivos `messages_es.properties` deben usar **codificación ASCII** con escape de caracteres especiales.
- Los caracteres especiales del español (á, é, í, ó, ú, ñ, ¿, ¡) deben codificarse usando secuencias Unicode (`\uXXXX`).
- Ejemplos de codificación:
    - `ñ` → `\u00f1`
    - `á` → `\u00e1`
    - `é` → `\u00e9`
    - `í` → `\u00ed`
    - `ó` → `\u00f3`
    - `ú` → `\u00fa`
- Esta codificación garantiza la compatibilidad entre diferentes sistemas y evita problemas de visualización.

#### Paso 2: Centralizar las Claves de Mensaje

Para evitar errores de tipeo y facilitar la refactorización, las claves de los mensajes se deben definir como constantes
en una clase `MessageKeys` en el dominio.

```java
// codefm-domain/src/main/java/org.web.codefm.domain/i18n/MessageKeys.java
@UtilityClass
public class MessageKeys {
   public static final String SCHOOL_VALIDATION_NAME_REQUIRED = "school.validation.name.required";
   public static final String SCHOOL_VALIDATION_TLF_INVALID = "school.validation.tlf.invalid";
}
```

#### Paso 3: Usar Claves de Mensaje en el Dominio

La capa de dominio debe permanecer independiente del idioma. Por lo tanto, la clase `ErrorMessage` no contiene el
mensaje final, sino la **clave del mensaje**.

```java
// ErrorMessage.java
public class ErrorMessage {
   private final String param;
   private String messageKey; // Contiene la clave, ej: "school.validation.name.required"
}
```

Cuando se lanza una excepción en el `Service`, se usan las constantes de `MessageKeys`.

```java
// SchoolServiceImpl.java
if(school.getName() ==null||school.

getName().

trim().

isEmpty()){
        errors.

add(new ErrorMessage("name", MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED));
        }
```

#### Paso 4: Resolución de Mensajes en la Capa de API

La traducción de la clave al mensaje final ocurre en la capa de API, específicamente en el `RestExceptionHandler`.

1. **Inyección de `MessageSource`**: El `RestExceptionHandler` inyecta el `MessageSource` de Spring.
2. **Detección del Idioma**: El handler inspecciona la cabecera `Accept-Language` de la petición para determinar el
   `Locale` del usuario.
3. **Traducción**: Al mapear la excepción a un `ErrorResponseDTO`, se utiliza
   `messageSource.getMessage(messageKey, null, locale)` para obtener el texto traducido correspondiente a la clave.

Este proceso asegura que el dominio se mantenga limpio y que la responsabilidad de la internacionalización recaiga en la
capa de API, que es la más cercana al usuario.

## Testing Unitario

### Estructura de Tests

Los tests se organizan en los mismos módulos que el código de producción:

- `codefm-application/src/test/` - Tests de UseCases y Services
- `codefm-infrastructure/src/test/` - Tests de Repositories
- `codefm-api/src/test/` - Tests de Controllers (si es necesario)

### Test de Service

**Ubicación**: `codefm-application/src/test/java/org.web.codefm.service/`

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

**Características**:

- Usar `@ExtendWith(MockitoExtension.class)`
- Mockear dependencias con `@Mock`
- Inyectar clase bajo test con `@InjectMocks`
- Verificar llamadas a dependencias con `verify()`
- Probar casos exitosos y casos límite

### Test de UseCase

**Ubicación**: `codefm-application/src/test/java/org.web.codefm.usecase/`

```java
@ExtendWith(MockitoExtension.class)
class SchoolUseCaseImplTest {

    @Mock
    private SchoolService schoolService;

    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private SchoolUseCaseImpl schoolUseCase;

    @Test
    void getSchoolsByTeacher_shouldReturnSchools() {
        Integer teacherId = 1;
        Map<String, String> parameters = new HashMap<>();
        parameters.put("teacher_id", String.valueOf(teacherId));
        
        School school1 = School.builder().id(1).teacherId(teacherId).name("School A").build();
        List<School> expectedSchools = Arrays.asList(school1);

        when(sessionUser.getParameters()).thenReturn(parameters);
        when(schoolService.getSchoolsByTeacherId(teacherId)).thenReturn(expectedSchools);

        List<School> result = schoolUseCase.getSchoolsByTeacher();

        assertNotNull(result);
        assertEquals(1, result.size());
       verify(schoolService, times(1)).findByTeacherId(teacherId);
    }
}
```

### Test de Repository

**Ubicación**: `codefm-infrastructure/src/test/java/org.web.codefm.infrastructure/`

```java
@ExtendWith(MockitoExtension.class)
class SchoolRepositoryImplTest {

    @Mock
    private SchoolJPARepository schoolJPARepository;

    @Mock
    private SchoolMapper schoolMapper;

    @InjectMocks
    private SchoolRepositoryImpl schoolRepository;

    @Test
    void findByTeacherId_shouldReturnSchools() {
        Integer teacherId = 1;
        SchoolEntity entity1 = new SchoolEntity(1, teacherId, "School A", "Town A", 123456);
        List<SchoolEntity> entities = Arrays.asList(entity1);

        School school1 = School.builder().id(1).teacherId(teacherId).name("School A").build();
        List<School> expectedSchools = Arrays.asList(school1);

        when(schoolJPARepository.findByTeacherId(teacherId)).thenReturn(entities);
        when(schoolMapper.toModelList(entities)).thenReturn(expectedSchools);

        List<School> result = schoolRepository.findByTeacherId(teacherId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(schoolJPARepository, times(1)).findByTeacherId(teacherId);
        verify(schoolMapper, times(1)).toModelList(entities);
    }
}
```

### Convenciones de Testing

1. **Nombres de métodos**: `methodName_shouldExpectedBehavior_whenCondition`
2. **Estructura**: Given-When-Then (opcional pero recomendado)
3. **Assertions**: Usar JUnit 5 assertions (`assertNotNull`, `assertEquals`, `assertTrue`)
4. **Verificaciones**: Siempre verificar llamadas a mocks con `verify()`
5. **Casos de prueba**:
    - Caso exitoso (happy path)
    - Caso con lista vacía
    - Casos límite o excepcionales

## Tests de Integración con Karate

El módulo `karate-test` contiene los tests de integración que validan la API de extremo a extremo.

### Resumen del Funcionamiento

1. **Configuración Central (`karate-config.js`)**: Este archivo se ejecuta antes que todos los tests. Su función es:
    * Definir las URLs base para los diferentes entornos (local, dev, etc.).
    * Cargar secretos (como contraseñas de usuario) de forma segura usando un gestor de secretos (Infisical).
    * Realizar una autenticación previa para un conjunto de usuarios de prueba. Llama a `features/common/auth.feature` y
      guarda las cookies o tokens de sesión en una variable global `authTokens`.
    * Esto permite que los tests individuales no necesiten repetir los pasos de login.

2. **Estructura de Ficheros**:
    * `src/test/resources/features`: Contiene los archivos `.feature` con los tests.
    * `src/test/resources/features/common`: Contiene features reutilizables como `auth.feature` e `infisical.feature`.
    * `src/test/java`: Contiene los "Runners" de Java que ejecutan los tests.

### Cómo Crear un Nuevo Test de Karate

1. **Crear el archivo `.feature`**:
    * Crea un nuevo archivo, por ejemplo, `new-endpoint.feature`, dentro de `src/test/resources/features/` (o en un
      subdirectorio si quieres agruparlos).

2. **Definir el `Feature` y `Background`**:
    * El `Background` es crucial para establecer las pre-condiciones. Como mínimo, debes definir la URL base.
    * Si el endpoint requiere autenticación, añade la cookie/token obtenido en la configuración.

   ```gherkin
   Feature: Probar el nuevo endpoint de X

   Background:
     # Establece la URL base (definida en karate-config.js)
     Given url baseHttpsUrl 
     
     # (Opcional) Añade la cookie de autenticación para un usuario específico
     # El token para 'karateuseradmin' fue pre-cargado en karate-config.js
     And cookie SESSION = authTokens.karateuseradmin
   ```

3. **Escribir el `Scenario` con Validación de Esquema**:
    * Dentro del `Scenario`, define variables para los esquemas que esperas en la respuesta. Esto hace el test mucho más
      robusto que simplemente comprobar tipos básicos.
    * Usa `match each` para aplicar el esquema a cada elemento de un array.

   ```gherkin
   Scenario: Obtener X correctamente
     # 1. Definir los esquemas para la validación
     * def classSchema = { id: '#number', schoolId: '#number', name: '#string', schoolYear: '#string' }
     * def schoolSchema = { id: '#number', name: '#string', town: '#string', tlf: '#number', classes: '#[] classSchema' }

     # 2. Definir el path del endpoint
     Given path '/private/v1/new-endpoint'
     
     # 3. Ejecutar la petición
     When method GET
     
     # 4. Verificar el estado y el esquema de la respuesta
     Then status 200
     # 'match each' aplica la validación a cada objeto del array.
     # También funciona correctamente si el array está vacío.
     And match each response == schoolSchema
   ```

4. **Ejecutar el Test**:
    * **Para un solo test**: Abre el archivo `IndividualKarateTestRunner.java`. Añade un nuevo método de test que apunte
      a tu feature (p.ej.,
      `@Karate.Test Karate testMyNewFeature() { return Karate.run("features/my-feature").relativeTo(getClass()); }`) y
      ejecuta ese método desde el IDE.
    * **Para todos los tests**: Ejecuta la clase `KarateTestRunner.java` o usa el comando de Maven:
      `mvn test -pl karate-test`.

## Ejecución de Tests

```bash
# Ejecutar todos los tests unitarios
mvn test

# Ejecutar tests de un módulo específico
mvn test -pl codefm-application

# Ejecutar un test unitario específico
mvn test -Dtest=SchoolServiceImplTest

# Ejecutar todos los tests de Karate
mvn test -pl karate-test
```

## Checklist para Crear un Nuevo Endpoint

1. [ ] Definir API en OpenAPI (private-api.yaml o public-api.yaml)
2. [ ] Crear Entidad de Dominio (codefm-domain/entity/)
3. [ ] Crear Repository Interface con JavaDoc en inglés (codefm-domain/repository/)
4. [ ] Crear Service Interface con JavaDoc en inglés (codefm-domain/service/)
5. [ ] Crear UseCase Interface con JavaDoc en inglés (codefm-domain/usecase/)
6. [ ] Crear Entity JPA (codefm-infrastructure/entity/mariadb/[schema]/)
7. [ ] Crear JPA Repository (codefm-infrastructure/jpa/[schema]/)
8. [ ] Crear Mapper Infrastructure (codefm-infrastructure/mapper/)
9. [ ] Implementar Repository (codefm-infrastructure/[modulo]/)
10. [ ] Crear Test de Repository (codefm-infrastructure/src/test/)
11. [ ] Implementar Service (codefm-application/service/)
12. [ ] Crear Test de Service (codefm-application/src/test/service/)
13. [ ] Implementar UseCase (codefm-application/usecase/)
14. [ ] Crear Test de UseCase (codefm-application/src/test/usecase/)
15. [ ] Crear Mapper DTO (codefm-api/mapper/)
16. [ ] Implementar Controller (codefm-api/controller/)
    - [ ] Si el endpoint tiene header `Accept-Language`, añadir anotación `@Locale(position)` con la posición correcta
      del parámetro
    - [ ] Añadir anotación `@Logged` para logging automático
    - [ ] Añadir anotación `@PreAuthorize` para control de acceso
17. [ ] Crear Test de Karate (karate-test/)
18. [ ] Ejecutar `mvn clean compile`
19. [ ] Ejecutar `mvn test` para verificar todos los tests

## Convenciones de Nombres

- Domain entity: `School`
- JPA entity: `SchoolEntity`
- DTO: `SchoolDTO`
- Repository interface: `SchoolRepository`
- Repository impl: `SchoolRepositoryImpl`
- JPA repository: `SchoolJPARepository`
- Service interface: `SchoolService`
- Service impl: `SchoolServiceImpl`
- UseCase interface: `SchoolUseCase`
- UseCase impl: `SchoolUseCaseImpl`
- Infrastructure mapper: `SchoolMapper`
- API mapper: `SchoolDTOMapper`

## Notas Importantes

1. **Capa de Servicio**: Los UseCases NUNCA llaman directamente a Repositories, siempre pasan por Services
    - UseCase → Service → Repository
    - Los Services contienen la lógica de negocio
    - Los UseCases orquestan servicios y gestionan el flujo
2. **JavaDoc**: SIEMPRE incluir documentación en inglés en todas las interfaces (UseCase, Service, Repository)
    - Descripción de la interfaz con `/** */`
    - Descripción de cada método con `@param`, `@return`, `@throws`
    - Explicar propósito y comportamiento esperado
3. **MapStruct**: Siempre ejecutar `mvn clean compile` después de crear mappers
4. **Multi-DataSource**: Organizar entities por schema en directorios separados
5. **SessionUser**: Usar `parameters` Map para datos adicionales del JWT
6. **Tests**: Crear tests unitarios para Service, UseCase y Repository
    - Usar Mockito para mockear dependencias
    - Seguir convención: `methodName_shouldExpectedBehavior_whenCondition`
    - Verificar llamadas a mocks con `verify()`
    - Probar casos exitosos y casos límite
7. **OpenAPI**: Regenerar código con `mvn compile` tras cambios en YAML
8. **Roles de Seguridad**: Para las anotaciones `@PreAuthorize("hasRole('...')")`, usar el
   `enum org.web.codefm.domain.security.UserRole` para definir los roles (ej.
   `hasRole('"+ UserRole.ADMIN.getRoleName() +"')`). Esto mejora la seguridad, legibilidad y mantenibilidad.
9. **Ordenamiento de Clases**: Al obtener `List<School>`, las `List<Class>` dentro de cada `School` deben ser ordenadas
   por el campo `schoolYear` de mayor a menor. El formato "YY/YY" de `schoolYear` debe ser convertido a un número (ej.
   2425) para el ordenamiento.
10. **Anotación @Locale**: Cuando un endpoint recibe el header `Accept-Language`, es **OBLIGATORIO** añadir la anotación
    `@Locale(position)` sobre el método del controller. Esta anotación indica la posición (0-indexed) del parámetro
    `Accept-Language` en la firma del método, y es necesaria para actualizar el campo `locale` de `SessionUser`, que se
    utiliza para definir el idioma de los mensajes de error y validación.
    - La posición se cuenta desde 0, empezando por el primer parámetro:
      ```java
      @Locale(2)  // Accept-Language está en posición 2
      public ResponseEntity<ClassDTO> createClass(
          Integer schoolId,        // posición 0
          ClassRequestDTO dto,     // posición 1
          String acceptLanguage    // posición 2
      )
      ```
    - **Ejemplo completo**:
      ```java
      @Logged
      @Override
      @Locale(1)
      @PreAuthorize("hasRole('TEACHER')")
      public ResponseEntity<SchoolDTO> createSchool(
          SchoolRequestDTO dto,           // posición 0
          String acceptLanguage           // posición 1
      ) {
          // implementación
      }
      ```
    - **Siempre** añadir el parámetro `Accept-Language` en el OpenAPI YAML como header:
      ```yaml
      parameters:
        - name: Accept-Language
          in: header
          required: false
          schema:
            type: string
      ```
