# Instrucciones para GitHub Copilot - Proyecto CodeFM

Este documento proporciona las directrices que debo seguir cada vez que trabajes conmigo en el proyecto CodeFM.

## Reglas Generales de Código

**CRÍTICO - PROHIBICIÓN ABSOLUTA DE COMENTARIOS EN EL CÓDIGO**:

- **NUNCA JAMÁS** agregar comentarios inline en el código Java (ni // ni /* */)
- **NUNCA JAMÁS** agregar comentarios explicativos dentro de métodos
- **NUNCA JAMÁS** agregar comentarios tipo "Given", "When", "Then" en tests
- **NUNCA JAMÁS** agregar comentarios de secciones como "// Setup", "// Arrange", etc.
- **NUNCA JAMÁS** agregar comentarios descriptivos en ninguna parte del código
- El código DEBE ser autoexplicativo mediante nombres descriptivos de variables, métodos y clases
- **ÚNICA EXCEPCIÓN**: JavaDoc obligatorio SOLO en interfaces públicas (Repository, Service, UseCase) según se
  especifica más adelante
- Si necesitas separar lógicamente bloques de código, usa líneas en blanco, NO comentarios
- Los tests deben ser legibles por su estructura y nombres de métodos, NO por comentarios

---

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

---

## **REGLA CRÍTICA: SessionUser y Parámetros de Sesión**

**OBLIGATORIO - NUNCA PASAR PARÁMETROS QUE ESTÉN EN SessionUser**:

Los parámetros que se pueden obtener de `SessionUser` (como `teacherId`, `userId`, etc.) **NUNCA** deben pasarse como
parámetros entre distintas clases o capas.

### ❌ INCORRECTO - NO HACER ESTO:

```java
// UseCase pasando teacherId al Service
public class SchoolUseCaseImpl implements SchoolUseCase {
    @Override
    public List<School> getSchoolsByTeacher() {
        Integer teacherId = getTeacherId(); // ❌ Obtiene teacherId aquí
        return schoolService.getSchoolsByTeacherId(teacherId); // ❌ Lo pasa al service
    }
}

// Service recibiendo teacherId como parámetro
public interface SchoolService {
    List<School> getSchoolsByTeacherId(Integer teacherId); // ❌ Recibe teacherId
}
```

### ✅ CORRECTO - HACER ESTO:

```java
// UseCase NO pasa teacherId
public class SchoolUseCaseImpl implements SchoolUseCase {
    private final SchoolService schoolService;
    
    @Override
    public List<School> getSchoolsByTeacher() {
        return schoolService.getSchoolsByTeacher(); // ✅ No pasa teacherId
    }
}

// Service obtiene teacherId internamente desde SessionUser
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {
    private final SchoolRepository schoolRepository;
    private final SessionUser sessionUser;
    
    @Override
    public List<School> getSchoolsByTeacher() {
        Integer teacherId = getTeacherId(); // ✅ Obtiene teacherId internamente
        return schoolRepository.findByTeacherId(teacherId);
    }
    
    private Integer getTeacherId() {
        return Integer.valueOf(
            sessionUser.getParameters().get(SessionParameter.TEACHER_ID.getClaimName())
        );
    }
}
```

**Razones:**

1. **Single Source of Truth**: SessionUser es la única fuente de verdad para información de sesión
2. **Seguridad**: Evita que se pueda manipular el teacherId/userId entre capas
3. **Cohesión**: Cada Service es responsable de obtener sus propios datos de sesión
4. **Mantenibilidad**: Cambios en SessionUser solo afectan a los Services, no a toda la cadena

**Parámetros que SÍ se pasan entre capas:**

- IDs de entidades de negocio (studentId, schoolId, classId, etc.)
- Datos de dominio (Student, School, Class objects)
- Filtros de búsqueda (name, surnames, etc.)

**Parámetros que NUNCA se pasan (se obtienen de SessionUser):**

- `teacherId` (del token JWT)
- `userId` (del token JWT)
- Cualquier parámetro de sesión del usuario autenticado

---

### 5. Implementación de UseCase (codefm-application/usecase/)

```java
@Service
@RequiredArgsConstructor
public class SchoolUseCaseImpl implements SchoolUseCase {
    private final SchoolService schoolService;
    
    @Override
    public List<School> getSchoolsByTeacher() {
        return schoolService.getSchoolsByTeacher(); // No pasa teacherId
    }
}
```

**OBLIGATORIO**:

- UseCase NUNCA llama directamente a Repository
- UseCase SIEMPRE pasa por Service
- UseCase NO debe obtener ni pasar parámetros de SessionUser (el Service lo hace)
- Inyección por constructor con `@RequiredArgsConstructor`

### 6. Implementación de Service (codefm-application/service/)

```java
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {
    private final SchoolRepository schoolRepository;
    private final SessionUser sessionUser;
    
    @Override
    public List<School> getSchoolsByTeacher() {
        Integer teacherId = getTeacherId(); // Obtiene teacherId internamente
        return schoolRepository.findByTeacherId(teacherId);
    }
    
    private Integer getTeacherId() {
        return Integer.valueOf(
            sessionUser.getParameters().get(SessionParameter.TEACHER_ID.getClaimName())
        );
    }
}
```

**OBLIGATORIO**:

- Service orquesta una o más operaciones de Repository
- Service contiene lógica de negocio específica
- Service actúa como intermediario entre UseCase y Repository
- **Service SIEMPRE inyecta SessionUser y MessageSource**
- **Service obtiene parámetros de sesión (teacherId, userId) internamente con método privado getTeacherId()**
- Service NUNCA recibe teacherId/userId como parámetro de métodos públicos

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

**Ubicación**: Implementación de Repository (para la operación unitaria)

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

### Soft Delete en Cascada (CRÍTICO)

**OBLIGATORIO**: Cada vez que se realice un soft delete de una entidad, se DEBEN dar de baja en cascada **todas las
entidades dependientes** y las dependientes de estas, recursivamente, hasta el final de la cadena.

**La cascada se gestiona SIEMPRE desde Java (ServiceImpl), NUNCA con triggers de base de datos.**

#### Cadena de dependencias actual

```
School
  └── Classes (school_id)
        ├── StudentClasses (id_class)
        │     └── ExerciseStudentGrades (id_student + exercises de la clase) [soft delete]
        ├── SubjectClasses (id_class)
        │     └── Exercises (id_subject_class)
        │           ├── ExerciseStudentGrades (id_exercise) [soft delete]
        │           └── ExerciseDocuments (class_subject_exercise) [hard delete + borrar archivo]
        └── Schedules (class_id)

Subject
  ├── SubjectClasses (id_subject)
  │     └── Exercises (id_subject_class)
  │           ├── ExerciseStudentGrades (id_exercise) [soft delete]
  │           └── ExerciseDocuments (class_subject_exercise) [hard delete + borrar archivo]
  └── Schedules (subject_id)

Student
  ├── ExerciseStudentGrades (id_student) [soft delete]
  └── StudentClasses (student_id)
        └── ExerciseStudentGrades (id_student + exercises de la clase) [soft delete]
```

#### Implementación obligatoria

La cascada se implementa en el método `softDelete` del **ServiceImpl** con `@Transactional`:

```java

@Override
@Transactional
public void softDeleteSchool(Integer schoolId, Integer teacherId) {
    Locale locale = sessionUser.getLocale();
    SchoolValidationUtil.validateSchoolOwnership(schoolId, teacherId, this, messageSource, locale);

    List<Integer> classIds = classRepository.findActiveIdsBySchoolId(schoolId);

    for (Integer classId : classIds) {
        cascadeDeleteClass(classId);
    }

    classRepository.softDeleteBySchoolId(schoolId);
    schoolRepository.softDeleteSchool(schoolId, teacherId);
}

private void cascadeDeleteClass(Integer classId) {
    List<Integer> subjectClassIds = subjectClassRepository.findActiveIdsByClassId(classId);

    if (!subjectClassIds.isEmpty()) {
        exerciseRepository.softDeleteBySubjectClassIds(subjectClassIds);
    }

    studentClassRepository.softDeleteByClassId(classId);
    subjectClassRepository.softDeleteByClassId(classId);
    scheduleRepository.softDeleteByClassId(classId);
}
```

#### Reglas de la cascada

1. **El Service que hace el soft delete** inyecta los repositories de las entidades dependientes
2. **Primero** se dan de baja las entidades más profundas de la cadena (exercises), **después** las intermedias
   (subjectClasses, schedules, studentClasses), y **por último** la entidad principal
3. **`@Transactional`** en el método del Service garantiza que toda la cascada es atómica
4. **Los RepositoryImpl de cascada NO necesitan `@Transactional`** porque participan de la transacción del Service
5. **Cada Repository** debe exponer métodos de soft delete masivo (`softDeleteByClassId`, `softDeleteBySubjectId`, etc.)
   implementados con queries `@Modifying` en el JPA Repository
6. **Siempre que se añada una nueva entidad dependiente**, actualizar la cascada de la entidad padre

#### Métodos necesarios en cada capa

| Capa              | Método                                                                      | Ejemplo                                                  |
|-------------------|-----------------------------------------------------------------------------|----------------------------------------------------------|
| JPA Repository    | `@Modifying @Query("UPDATE ... SET deletionDate = CURRENT_DATE WHERE ...")` | `softDeleteByClassId(Integer classId)`                   |
| Domain Repository | Interfaz con JavaDoc                                                        | `void softDeleteByClassId(Integer classId)`              |
| RepositoryImpl    | Delegación al JPA                                                           | `subjectClassJPARepository.softDeleteByClassId(classId)` |
| Domain Repository | Método para obtener IDs activos                                             | `List<Integer> findActiveIdsByClassId(Integer classId)`  |

#### Tests de cascada

Cada test de soft delete DEBE verificar que se llama a los métodos de cascada de todas las dependencias:

```java

@Test
void softDeleteSchool_shouldCascadeDeleteAllDependencies() {
    when(classRepository.findActiveIdsBySchoolId(schoolId)).thenReturn(Arrays.asList(classId1, classId2));
    when(subjectClassRepository.findActiveIdsByClassId(classId1)).thenReturn(Arrays.asList(100, 101));
    when(subjectClassRepository.findActiveIdsByClassId(classId2)).thenReturn(Collections.emptyList());

    schoolService.softDeleteSchool(schoolId, teacherId);

    verify(exerciseRepository).softDeleteBySubjectClassIds(Arrays.asList(100, 101));
    verify(studentClassRepository).softDeleteByClassId(classId1);
    verify(subjectClassRepository).softDeleteByClassId(classId1);
    verify(scheduleRepository).softDeleteByClassId(classId1);
    verify(studentClassRepository).softDeleteByClassId(classId2);
    verify(subjectClassRepository).softDeleteByClassId(classId2);
    verify(scheduleRepository).softDeleteByClassId(classId2);
    verify(classRepository).softDeleteBySchoolId(schoolId);
    verify(schoolRepository).softDeleteSchool(schoolId, teacherId);
}
```

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
21. [ ] Crear o actualizar colección de Postman en `postman/`
22. [ ] Ejecutar `mvn clean compile`
23. [ ] Ejecutar `mvn test`

---

## Validaciones de Ownership y Permisos del Profesor (CRÍTICO)

**OBLIGATORIO**: Todas las operaciones CRUD deben validar que el profesor autenticado tiene permisos sobre los recursos
que está manipulando. **NUNCA** permitir que un profesor acceda, modifique o elimine recursos de otro profesor.

### Principio General

Cada operación debe verificar que **toda la cadena de pertenencia** del recurso lleva al `teacherId` del profesor
autenticado. Si en cualquier punto de la cadena la validación falla, se debe lanzar una excepción
`NotFoundException` o `ForbiddenException`.

### Cadena de Pertenencia

```
Teacher (teacherId de SessionUser)
  ├── Schools (teacher_id)
  │     └── Classes (school_id → school.teacher_id)
  │           ├── StudentClasses (id_class)
  │           ├── SubjectClasses (id_class)
  │           │     └── Exercises (id_subject_class)
  │           │           ├── ExerciseStudentGrades (id_exercise)
  │           │           └── ExerciseDocuments (class_subject_exercise)
  │           └── Schedules (class_id)
  ├── Subjects (id_teacher)
  └── Students (teacher_id)
```

### Validaciones Obligatorias por Entidad

| Entidad              | Validación de Ownership                                                                                                     |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------|
| School               | `schoolRepository.findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId)`                                           |
| Class                | `classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)`                                             |
| Subject              | `subjectRepository.findByIdAndTeacherId(subjectId, teacherId)`                                                              |
| Student              | `studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)`                                         |
| SubjectClass         | Validar que la **clase** y la **asignatura** pertenecen al profesor                                                         |
| Schedule             | Validar que la **clase** pertenece al profesor                                                                              |
| Exercise             | `exerciseRepository.findByIdAndTeacherId(exerciseId, teacherId)` (valida cadena exercise→subjectClass→class→school→teacher) |
| ExerciseStudentGrade | `exerciseStudentGradeRepository.findByIdAndTeacherId(gradeId, teacherId)` (valida cadena completa)                          |
| ExerciseDocument     | Validar que el **exercise** pertenece al profesor                                                                           |
| StudentClass         | Validar que la **clase** y el **estudiante** pertenecen al profesor                                                         |

### Reglas de Validación por Tipo de Operación

#### Consultas (GET)

- **Siempre** filtrar por `teacherId` en la query o validar ownership antes de devolver resultados
- Si se recibe un `classId` como parámetro, validar que la clase pertenece al profesor ANTES de consultar
- Si se recibe un `studentId` como parámetro, validar que el estudiante pertenece al profesor

#### Creación (POST/PUT)

- Validar que **todos los IDs referenciados** en el body pertenecen al profesor:
    - Si se recibe `classId` → validar que la clase es del profesor
    - Si se recibe `subjectId` → validar que la asignatura es del profesor
    - Si se recibe `studentId` → validar que el estudiante es del profesor
    - Si se recibe `exerciseId` → validar que el ejercicio es del profesor
- Validar que las **asociaciones intermedias** existen:
    - Para crear un grade: validar que el **estudiante está matriculado en la clase** del ejercicio
    - Para crear un schedule: validar que la **asignatura está asignada a la clase** (existe en `subject_classes`)
    - Para crear un exercise: validar que la **subjectClass existe y está activa**

#### Modificación (PUT/PATCH)

- Validar ownership del recurso que se está modificando (buscar con `findByIdAndTeacherId`)
- Si se modifican IDs referenciados, validar ownership de los nuevos IDs

#### Eliminación (DELETE)

- Validar ownership del recurso que se está eliminando (buscar con `findByIdAndTeacherId`)
- Aplicar cascada de soft delete según la cadena de dependencias

### Ejemplo de Validación Completa en Service

```java

@Override
public ExerciseStudentGrade createGrade(Integer exerciseId, ExerciseStudentGrade grade) {
    Integer teacherId = getTeacherId();
    Locale locale = sessionUser.getLocale();

    Exercise exercise = exerciseRepository.findByIdAndTeacherId(exerciseId, teacherId)
            .orElseThrow(() -> new ExerciseStudentGradeNotFoundException(
                    messageSource.getMessage(MessageKeys.EXERCISE_NOT_FOUND, null, locale)));

    validateStudentId(grade.getStudentId(), teacherId, errors, locale);
    validateGrade(grade, exercise, errors, locale);
    validateStudentInExerciseClass(grade.getStudentId(), exercise, teacherId, errors, locale);
    validateNoDuplicate(grade.getStudentId(), exerciseId, teacherId, errors, locale);

    ...
}

private void validateStudentId(Integer studentId, Integer teacherId, List<ErrorMessage> errors, Locale locale) {
    if (studentId == null) {
        errors.add(new ErrorMessage("studentId", messageSource.getMessage(
                MessageKeys.STUDENT_REQUIRED, null, locale)));
        return;
    }

    Optional<Student> student = studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
    if (student.isEmpty()) {
        errors.add(new ErrorMessage("studentId", messageSource.getMessage(
                MessageKeys.STUDENT_NOT_FOUND, null, locale)));
    }
}
```

### Patrón `findByIdAndTeacherId` en Repository

Para validar ownership en queries complejas (entidades que no tienen `teacher_id` directo), usar JOINs en el JPA
Repository:

```java

@Query("SELECT g FROM ExerciseStudentGradeEntity g " +
        "JOIN ExerciseEntity e ON g.exerciseId = e.id " +
        "JOIN SubjectClassEntity sc ON e.subjectClassId = sc.id " +
        "JOIN ClassEntity c ON sc.classId = c.id " +
        "JOIN SchoolEntity s ON c.schoolId = s.id " +
        "WHERE g.id = :id AND s.teacherId = :teacherId AND g.deletionDate IS NULL")
Optional<ExerciseStudentGradeEntity> findByIdAndTeacherId(@Param("id") Integer id, @Param("teacherId") Integer teacherId);
```

### Validaciones de Asociaciones entre Entidades

| Operación                  | Validación requerida                                                                                                         |
|----------------------------|------------------------------------------------------------------------------------------------------------------------------|
| Asignar asignatura a clase | Clase pertenece al profesor + Asignatura pertenece al profesor + No existe duplicado                                         |
| Crear schedule             | Clase pertenece al profesor + Asignatura asignada a la clase (`subject_classes`)                                             |
| Crear exercise             | SubjectClass existe y pertenece al profesor (vía clase)                                                                      |
| Crear grade de alumno      | Exercise pertenece al profesor + Student pertenece al profesor + Student está en la clase del exercise + No existe duplicado |
| Subir documento a exercise | Exercise pertenece al profesor                                                                                               |
| Matricular alumno en clase | Clase pertenece al profesor + Alumno pertenece al profesor                                                                   |

### Tests Obligatorios de Ownership

Para cada operación CRUD, SIEMPRE crear tests que verifiquen:

1. ✅ **Happy path**: recurso pertenece al profesor → operación exitosa
2. ✅ **Recurso no encontrado**: ID no existe → `NotFoundException`
3. ✅ **Recurso de otro profesor**: recurso existe pero pertenece a otro profesor → `NotFoundException` o
   `ForbiddenException`
4. ✅ **Entidades asociadas inválidas**: IDs referenciados no pertenecen al profesor → `ValidationException`

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
13. **Validación de Asociaciones Subject-Class**: Para crear horarios (schedules), la asignatura DEBE estar previamente
    asignada a la clase en la tabla `subject_classes`
14. **Colecciones Postman**: SIEMPRE actualizar la colección de Postman correspondiente en `postman/` al crear o
    modificar endpoints
15. **Ownership del Profesor**: SIEMPRE validar que TODOS los recursos y entidades referenciadas pertenecen al profesor
    autenticado. NUNCA permitir operaciones sobre recursos de otro profesor. Validar también que las asociaciones
    intermedias existen (alumno en clase, asignatura asignada a clase, etc.)

---

## Validaciones de Asociaciones (SubjectClass)

La tabla `subject_classes` gestiona la relación muchos-a-muchos entre asignaturas y clases. Las siguientes reglas
aplican:

### Reglas de Negocio

1. **Una asignatura solo puede asignarse una vez a una clase** (sin fecha de baja activa)
2. **Solo se puede operar sobre clases que pertenezcan al profesor** (validar ownership via `ClassRepository`)
3. **Solo se pueden asignar asignaturas que pertenezcan al profesor** (validar ownership via `SubjectRepository`)
4. **Para crear un horario (schedule), la asignatura DEBE estar asignada a la clase**

### Endpoints de SubjectClass

| Método | Endpoint                                          | Descripción                                              |
|--------|---------------------------------------------------|----------------------------------------------------------|
| GET    | `/teacher-notebook/v1/classes/{classId}/subjects` | Obtener asignaturas de una clase                         |
| GET    | `/teacher-notebook/v1/classes-subjects`           | Obtener todas las clases con sus asignaturas             |
| PUT    | `/teacher-notebook/v1/classes/{classId}/subjects` | Asignar asignaturas a una clase (body: `subjectIds[]`)   |
| DELETE | `/teacher-notebook/v1/classes/{classId}/subjects` | Eliminar asignaturas de una clase (body: `subjectIds[]`) |

### Mensajes de Error con Nombres

Los mensajes de error de validación de asignaturas deben mostrar el **nombre de la asignatura** en lugar del ID:

```java
var subject = subjectRepository.findById(subjectId);
String subjectName = subject.map(Subject::getName).orElse(String.valueOf(subjectId));
String message = messageSource.getMessage(MessageKeys.SUBJECT_CLASS_ALREADY_EXISTS, new Object[]{subjectName}, locale);
```

### Tests de Karate para SubjectClass

Ubicación: `karate-test/src/test/resources/features/teacher-notebook/subject-classes/`

- `getsubjectsbyclass.feature` - Obtener asignaturas de una clase
- `getclasseswithsubjects.feature` - Obtener todas las clases con asignaturas
- `assignsubjectstoclass.feature` - Asignar asignaturas a una clase
- `removesubjectsfromclass.feature` - Eliminar asignaturas de una clase

---

## Registro de Tests de Karate en IndividualKarateTestRunner

**OBLIGATORIO**: Cada vez que se cree un nuevo archivo `.feature` de Karate, se DEBE registrar en la clase
`IndividualKarateTestRunner` ubicada en `karate-test/src/test/java/IndividualKarateTestRunner.java`.

### Formato del método

```java

@Karate.Test
Karate testNombreDescriptivo() {
    return Karate.run("features/ruta/al/archivo").relativeTo(getClass());
}
```

### Ejemplo

Si creas un nuevo archivo `karate-test/src/test/resources/features/teacher-notebook/grades/creategrades.feature`,
debes añadir el siguiente método a `IndividualKarateTestRunner`:

```java

@Karate.Test
Karate testTeacherNotebookCreateGrades() {
    return Karate.run("features/teacher-notebook/grades/creategrades").relativeTo(getClass());
}
```

### Convención de nombres

- El nombre del método debe seguir el patrón: `test` + `Módulo` + `Acción`
- Ejemplos:
    - `testTeacherNotebookGetSubjects`
    - `testTeacherNotebookCreateSchedules`
    - `testTeacherNotebookAssignSubjectsToClass`

---

## Colecciones de Postman

**OBLIGATORIO**: Cada vez que se cree o modifique un endpoint, se DEBE crear o actualizar la colección de Postman
correspondiente ubicada en el directorio `postman/`.

### Ubicación

Las colecciones de Postman se encuentran en el directorio `postman/` del proyecto:

- `Classes.postman_collection.json` - Endpoints de clases
- `Schools.postman_collection.json` - Endpoints de colegios
- `Students.postman_collection.json` - Endpoints de estudiantes
- `Subjects.postman_collection.json` - Endpoints de asignaturas
- `SubjectClasses.postman_collection.json` - Endpoints de asignación asignaturas-clases
- `Schedules.postman_collection.json` - Endpoints de horarios

### Reglas para Colecciones de Postman

1. **Variable de entorno**: Usar `{{host}}` para la URL base
2. **Headers obligatorios**: Incluir `Accept-Language` con valor `es` o `en`
3. **Cuerpos de ejemplo**: Incluir TODOS los campos obligatorios en el body de ejemplo
4. **Descripción**: Añadir descripción explicativa para cada request
5. **Variables de path**: Usar la sintaxis `:id` para parámetros de ruta

### Cuándo Actualizar las Colecciones

- **Nuevo endpoint**: Añadir nuevo request a la colección correspondiente
- **Campo nuevo obligatorio**: Actualizar TODOS los bodies de ejemplo que usen ese DTO
- **Campo eliminado**: Remover el campo de los bodies de ejemplo
- **Cambio en validaciones**: Actualizar la descripción del request

### Ejemplo de Request

```json
{
  "name": "Create Student",
  "request": {
    "method": "PUT",
    "header": [
      {
        "key": "Content-Type",
        "value": "application/json"
      },
      {
        "key": "Accept-Language",
        "value": "es"
      }
    ],
    "body": {
      "mode": "raw",
      "raw": "{\n    \"name\": \"Juan\",\n    \"surnames\": \"García López\",\n    \"dateOfBirth\": \"15/03/2010\",\n    \"gender\": \"M\",\n    \"additionalInfo\": \"Información adicional\"\n}"
    },
    "url": {
      "raw": "{{host}}/teacher-notebook/v1/students",
      "host": [
        "{{host}}"
      ],
      "path": [
        "teacher-notebook",
        "v1",
        "students"
      ]
    },
    "description": "Crea un nuevo estudiante. El campo gender es obligatorio (M=Masculino, F=Femenino)."
  }
}
```

---

## Ejemplos de Referencia

Consultar los siguientes archivos para ver ejemplos completos:

- `development-guide.md`: Guía completa del proyecto
- Módulos existentes como `codefm-application`, `codefm-infrastructure`, `codefm-api`


