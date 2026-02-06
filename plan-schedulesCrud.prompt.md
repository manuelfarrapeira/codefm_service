# Plan: CRUD de Schedules (Horarios de Clase)

**TL;DR**: Crear un CRUD completo para la tabla `schedules` con endpoints para: crear horarios en lote por clase/día,
consultar por clase, actualizar individual, y eliminar en lote. Formato de hora `HH:mm`. Validaciones: día 1-5, end >
start, subject existe, clase pertenece al profesor. En eliminación, si algún ID no pertenece al profesor, no se elimina
nada y se devuelve error.

## Tabla de Referencia

```sql
create table teacher_notebook_pre.schedules (
    id            int auto_increment primary key,
    class_id      int  not null,
    subject_id    int  not null,
    day           int  not null,
    start         time not null,
    end           time not null,
    deletion_date date null,
    constraint schedules_classes_id_fk
        foreign key (class_id) references teacher_notebook_pre.classes (id),
    constraint schedules_subjects_id_fk
        foreign key (subject_id) references teacher_notebook_pre.subjects (id)
);
```

## Steps

### 1. Actualizar OpenAPI (private-api.yaml)

Añadir tag y endpoints en `codefm-api/src/main/resources/private-api.yaml`:

**Tag:**

```yaml
- name: TeacherNoteBookSchedules
  description: Operations related to Teacher Notebook Class Schedules
```

**Endpoints:**

1. `PUT /teacher-notebook/v1/classes/{classId}/schedules` - Crear horarios en lote
    - Path param: `classId`
    - Header: `Accept-Language`
    - Body: `ScheduleCreateRequestDTO` (day + items[])
    - Response 201: array de `ScheduleDTO`

2. `GET /teacher-notebook/v1/classes/{classId}/schedules` - Consultar horarios por clase
    - Path param: `classId`
    - Header: `Accept-Language`
    - Response 200: array de `ScheduleDTO`

3. `PATCH /teacher-notebook/v1/schedules/{id}` - Actualizar un horario
    - Path param: `id`
    - Header: `Accept-Language`
    - Body: `ScheduleUpdateRequestDTO` (day, start, end)
    - Response 200: `ScheduleDTO`

4. `DELETE /teacher-notebook/v1/schedules` - Soft-delete en lote
    - Header: `Accept-Language`
    - Body: `ScheduleDeleteRequestDTO` (ids[])
    - Response 204: No Content

**DTOs:**

```yaml
ScheduleDTO:
  type: 'object'
  properties:
    id:
      type: 'integer'
    classId:
      type: 'integer'
    subjectId:
      type: 'integer'
    day:
      type: 'integer'
      minimum: 1
      maximum: 5
    start:
      type: 'string'
      description: 'Time in format HH:mm'
      example: '08:30'
    end:
      type: 'string'
      description: 'Time in format HH:mm'
      example: '09:30'

ScheduleCreateRequestDTO:
  type: 'object'
  required:
    - day
    - items
  properties:
    day:
      type: 'integer'
      minimum: 1
      maximum: 5
      description: 'Day of week (1=Monday, 5=Friday)'
      example: 1
    items:
      type: 'array'
      items:
        $ref: '#/components/schemas/ScheduleItemDTO'

ScheduleItemDTO:
  type: 'object'
  required:
    - subjectId
    - start
    - end
  properties:
    subjectId:
      type: 'integer'
      example: 1
    start:
      type: 'string'
      description: 'Start time in format HH:mm'
      example: '08:30'
    end:
      type: 'string'
      description: 'End time in format HH:mm'
      example: '09:30'

ScheduleUpdateRequestDTO:
  type: 'object'
  required:
    - day
    - start
    - end
  properties:
    day:
      type: 'integer'
      minimum: 1
      maximum: 5
      description: 'Day of week (1=Monday, 5=Friday)'
      example: 1
    start:
      type: 'string'
      description: 'Start time in format HH:mm'
      example: '08:30'
    end:
      type: 'string'
      description: 'End time in format HH:mm'
      example: '09:30'

ScheduleDeleteRequestDTO:
  type: 'object'
  required:
    - ids
  properties:
    ids:
      type: 'array'
      items:
        type: 'integer'
      description: 'List of schedule IDs to delete'
      example: [1, 2, 3]
```

### 2. Crear Entidad de Dominio

Crear `codefm-domain/src/main/java/org/web/codefm/domain/entity/teachernotebook/Schedule.java`:

```java
@Data
@Builder
@Generated
public class Schedule {
    private Integer id;
    private Integer classId;
    private Integer subjectId;
    private Integer day;
    private LocalTime start;
    private LocalTime end;
    private LocalDate deletionDate;
}
```

### 3. Crear Interfaces de Dominio

#### ScheduleRepository

`codefm-domain/src/main/java/org/web/codefm/domain/repository/teachernotebook/ScheduleRepository.java`

Métodos:

- `List<Schedule> findByClassId(Integer classId)`
- `Optional<Schedule> findById(Integer id)`
- `Optional<Schedule> findByIdAndTeacherId(Integer id, Integer teacherId)`
- `List<Schedule> saveAll(List<Schedule> schedules)`
- `Schedule save(Schedule schedule)`
- `Schedule update(Schedule schedule)`
- `void softDeleteSchedules(List<Integer> ids, Integer teacherId)`
- `boolean allSchedulesBelongToTeacher(List<Integer> ids, Integer teacherId)`

#### ScheduleService

`codefm-domain/src/main/java/org/web/codefm/domain/service/teachernotebook/ScheduleService.java`

Métodos:

- `List<Schedule> getSchedulesByClassId(Integer classId)`
- `List<Schedule> createSchedules(Integer classId, Integer day, List<Schedule> schedules)`
- `Schedule updateSchedule(Integer scheduleId, Schedule schedule)`
- `void softDeleteSchedules(List<Integer> ids)`

#### ScheduleUseCase

`codefm-domain/src/main/java/org/web/codefm/domain/usecase/teachernotebook/ScheduleUseCase.java`

Métodos:

- `List<Schedule> getSchedulesByClassId(Integer classId)`
- `List<Schedule> createSchedules(Integer classId, Integer day, List<Schedule> schedules)`
- `Schedule updateSchedule(Integer scheduleId, Schedule schedule)`
- `void softDeleteSchedules(List<Integer> ids)`

### 4. Crear Excepciones

`codefm-domain/src/main/java/org/web/codefm/domain/exception/teachernotebook/ScheduleNotFoundException.java`:

```java
public class ScheduleNotFoundException extends ErrorMessageBaseException {
    public ScheduleNotFoundException(String message) {
        super(ErrorCodeEnum.RESOURCE_NOT_FOUND, message);
    }
}
```

`codefm-domain/src/main/java/org/web/codefm/domain/exception/teachernotebook/ScheduleValidationException.java`:

```java
public class ScheduleValidationException extends ListErrorMessageBaseException {
    public ScheduleValidationException(List<ErrorMessage> errors) {
        super(ErrorCodeEnum.VALIDATION_ERROR, errors);
    }
}
```

### 5. Añadir MessageKeys e i18n

En `codefm-domain/src/main/java/org/web/codefm/domain/i18n/MessageKeys.java`:

```java
public static final String SCHEDULE_NOT_FOUND = "schedule.not.found";
public static final String SCHEDULE_VALIDATION_DAY_REQUIRED = "schedule.validation.day.required";
public static final String SCHEDULE_VALIDATION_DAY_INVALID = "schedule.validation.day.invalid";
public static final String SCHEDULE_VALIDATION_START_REQUIRED = "schedule.validation.start.required";
public static final String SCHEDULE_VALIDATION_END_REQUIRED = "schedule.validation.end.required";
public static final String SCHEDULE_VALIDATION_END_BEFORE_START = "schedule.validation.end.before.start";
public static final String SCHEDULE_VALIDATION_SUBJECT_NOT_FOUND = "schedule.validation.subject.not.found";
public static final String SCHEDULE_VALIDATION_CLASS_NOT_FOUND = "schedule.validation.class.not.found";
public static final String SCHEDULE_VALIDATION_IDS_REQUIRED = "schedule.validation.ids.required";
public static final String SCHEDULE_VALIDATION_IDS_NOT_OWNED = "schedule.validation.ids.not.owned";
public static final String SCHEDULE_VALIDATION_ITEMS_REQUIRED = "schedule.validation.items.required";
```

En `codefm-boot/src/main/resources/messages_en.properties`:

```properties
schedule.not.found=Schedule not found.
schedule.validation.day.required=Day is required.
schedule.validation.day.invalid=Day must be between 1 (Monday) and 5 (Friday).
schedule.validation.start.required=Start time is required.
schedule.validation.end.required=End time is required.
schedule.validation.end.before.start=End time must be after start time.
schedule.validation.subject.not.found=Subject not found.
schedule.validation.class.not.found=Class not found or does not belong to teacher.
schedule.validation.ids.required=At least one schedule ID is required.
schedule.validation.ids.not.owned=One or more schedules do not belong to the teacher.
schedule.validation.items.required=At least one schedule item is required.
```

En `codefm-boot/src/main/resources/messages_es.properties`:

```properties
schedule.not.found=Horario no encontrado.
schedule.validation.day.required=El d\u00eda es obligatorio.
schedule.validation.day.invalid=El d\u00eda debe estar entre 1 (lunes) y 5 (viernes).
schedule.validation.start.required=La hora de inicio es obligatoria.
schedule.validation.end.required=La hora de fin es obligatoria.
schedule.validation.end.before.start=La hora de fin debe ser posterior a la hora de inicio.
schedule.validation.subject.not.found=Asignatura no encontrada.
schedule.validation.class.not.found=Clase no encontrada o no pertenece al profesor.
schedule.validation.ids.required=Se requiere al menos un ID de horario.
schedule.validation.ids.not.owned=Uno o m\u00e1s horarios no pertenecen al profesor.
schedule.validation.items.required=Se requiere al menos un elemento de horario.
```

### 6. Crear Entity JPA

`codefm-infrastructure/src/main/java/org/web/codefm/infrastructure/entity/mariadb/teachernotebook/ScheduleEntity.java`:

```java
@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
@Where(clause = "deletion_date IS NULL")
public class ScheduleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @Column(name = "subject_id", nullable = false)
    private Integer subjectId;

    @Column(nullable = false)
    private Integer day;

    @Column(nullable = false)
    private LocalTime start;

    @Column(name = "end", nullable = false)
    private LocalTime end;

    @Column(name = "deletion_date")
    private LocalDate deletionDate;
}
```

### 7. Crear Infraestructura

#### ScheduleJPARepository

`codefm-infrastructure/src/main/java/org/web/codefm/infrastructure/jpa/teachernotebook/ScheduleJPARepository.java`:

```java
@Repository
public interface ScheduleJPARepository extends JpaRepository<ScheduleEntity, Integer> {
    List<ScheduleEntity> findByClassIdAndDeletionDateIsNull(Integer classId);
    Optional<ScheduleEntity> findByIdAndDeletionDateIsNull(Integer id);
    
    @Query("SELECT s FROM ScheduleEntity s " +
           "JOIN ClassEntity c ON s.classId = c.id " +
           "JOIN SchoolEntity sc ON c.schoolId = sc.id " +
           "WHERE s.id = :id AND sc.teacherId = :teacherId AND s.deletionDate IS NULL")
    Optional<ScheduleEntity> findByIdAndTeacherId(@Param("id") Integer id, @Param("teacherId") Integer teacherId);
    
    @Query("SELECT COUNT(s) FROM ScheduleEntity s " +
           "JOIN ClassEntity c ON s.classId = c.id " +
           "JOIN SchoolEntity sc ON c.schoolId = sc.id " +
           "WHERE s.id IN :ids AND sc.teacherId = :teacherId AND s.deletionDate IS NULL")
    long countByIdsAndTeacherId(@Param("ids") List<Integer> ids, @Param("teacherId") Integer teacherId);
    
    @Modifying
    @Query("UPDATE ScheduleEntity s SET s.deletionDate = CURRENT_DATE WHERE s.id IN :ids")
    void softDeleteByIds(@Param("ids") List<Integer> ids);
}
```

#### ScheduleMapper

`codefm-infrastructure/src/main/java/org/web/codefm/infrastructure/mapper/ScheduleMapper.java`:

```java
@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    Schedule toModel(ScheduleEntity entity);
    List<Schedule> toModelList(List<ScheduleEntity> entities);
    ScheduleEntity toEntity(Schedule schedule);
    List<ScheduleEntity> toEntityList(List<Schedule> schedules);
}
```

#### ScheduleRepositoryImpl

`codefm-infrastructure/src/main/java/org/web/codefm/infrastructure/teachernotebook/ScheduleRepositoryImpl.java`:

```java
@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepository {
    private final ScheduleJPARepository scheduleJPARepository;
    private final ScheduleMapper scheduleMapper;

    @Override
    public List<Schedule> findByClassId(Integer classId) {
        return scheduleMapper.toModelList(
            scheduleJPARepository.findByClassIdAndDeletionDateIsNull(classId)
        );
    }

    @Override
    public Optional<Schedule> findById(Integer id) {
        return scheduleJPARepository.findByIdAndDeletionDateIsNull(id)
            .map(scheduleMapper::toModel);
    }

    @Override
    public Optional<Schedule> findByIdAndTeacherId(Integer id, Integer teacherId) {
        return scheduleJPARepository.findByIdAndTeacherId(id, teacherId)
            .map(scheduleMapper::toModel);
    }

    @Override
    public List<Schedule> saveAll(List<Schedule> schedules) {
        List<ScheduleEntity> entities = scheduleMapper.toEntityList(schedules);
        List<ScheduleEntity> saved = scheduleJPARepository.saveAll(entities);
        return scheduleMapper.toModelList(saved);
    }

    @Override
    public Schedule save(Schedule schedule) {
        ScheduleEntity entity = scheduleMapper.toEntity(schedule);
        ScheduleEntity saved = scheduleJPARepository.save(entity);
        return scheduleMapper.toModel(saved);
    }

    @Override
    public Schedule update(Schedule schedule) {
        ScheduleEntity entity = scheduleMapper.toEntity(schedule);
        ScheduleEntity saved = scheduleJPARepository.save(entity);
        return scheduleMapper.toModel(saved);
    }

    @Override
    @Transactional
    public void softDeleteSchedules(List<Integer> ids, Integer teacherId) {
        scheduleJPARepository.softDeleteByIds(ids);
    }

    @Override
    public boolean allSchedulesBelongToTeacher(List<Integer> ids, Integer teacherId) {
        long count = scheduleJPARepository.countByIdsAndTeacherId(ids, teacherId);
        return count == ids.size();
    }
}
```

### 8. Crear Implementaciones de Application

#### ScheduleServiceImpl

`codefm-application/src/main/java/org/web/codefm/application/service/teachernotebook/ScheduleServiceImpl.java`:

Validaciones:

- Día entre 1 y 5
- End > Start
- Subject existe y pertenece al profesor
- Class existe y pertenece al profesor
- En delete: todos los IDs pertenecen al profesor

#### ScheduleUseCaseImpl

`codefm-application/src/main/java/org/web/codefm/application/usecase/teachernotebook/ScheduleUseCaseImpl.java`:

Delega al service.

### 9. Crear Mappers de API

#### ScheduleDTOMapper

`codefm-api/src/main/java/org/web/codefm/api/mapper/ScheduleDTOMapper.java`:

```java
@Mapper(componentModel = "spring")
public interface ScheduleDTOMapper {
    @Mapping(target = "start", source = "start", qualifiedByName = "localTimeToString")
    @Mapping(target = "end", source = "end", qualifiedByName = "localTimeToString")
    ScheduleDTO toDTO(Schedule schedule);
    
    List<ScheduleDTO> toDTOList(List<Schedule> schedules);
    
    @Named("localTimeToString")
    default String localTimeToString(LocalTime time) {
        return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
    }
}
```

#### ScheduleRequestMapper

`codefm-api/src/main/java/org/web/codefm/api/mapper/ScheduleRequestMapper.java`:

```java
@Mapper(componentModel = "spring")
public abstract class ScheduleRequestMapper {
    
    @Named("stringToLocalTime")
    protected LocalTime stringToLocalTime(String time) {
        return time != null ? LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")) : null;
    }
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classId", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    @Mapping(target = "day", ignore = true)
    @Mapping(target = "start", source = "start", qualifiedByName = "stringToLocalTime")
    @Mapping(target = "end", source = "end", qualifiedByName = "stringToLocalTime")
    public abstract Schedule toDomain(ScheduleItemDTO dto);
    
    public abstract List<Schedule> toDomainList(List<ScheduleItemDTO> dtos);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classId", ignore = true)
    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    @Mapping(target = "start", source = "start", qualifiedByName = "stringToLocalTime")
    @Mapping(target = "end", source = "end", qualifiedByName = "stringToLocalTime")
    public abstract Schedule toDomainForUpdate(ScheduleUpdateRequestDTO dto);
}
```

### 10. Crear Controller

`codefm-api/src/main/java/org/web/codefm/api/controller/PrivateSchedules.java`:

```java
@RestController
@RequiredArgsConstructor
public class PrivateSchedules implements TeacherNoteBookSchedulesApi {

    private final ScheduleUseCase scheduleUseCase;
    private final ScheduleDTOMapper scheduleDTOMapper;
    private final ScheduleRequestMapper scheduleRequestMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ScheduleDTO>> createSchedules(
            Integer classId,
            ScheduleCreateRequestDTO scheduleCreateRequestDTO,
            String acceptLanguage
    ) {
        List<Schedule> schedules = scheduleRequestMapper.toDomainList(scheduleCreateRequestDTO.getItems());
        List<Schedule> created = scheduleUseCase.createSchedules(
            classId, 
            scheduleCreateRequestDTO.getDay(), 
            schedules
        );
        return new ResponseEntity<>(scheduleDTOMapper.toDTOList(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByClass(
            Integer classId,
            String acceptLanguage
    ) {
        List<Schedule> schedules = scheduleUseCase.getSchedulesByClassId(classId);
        return ResponseEntity.ok(scheduleDTOMapper.toDTOList(schedules));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ScheduleDTO> updateSchedule(
            Integer id,
            ScheduleUpdateRequestDTO scheduleUpdateRequestDTO,
            String acceptLanguage
    ) {
        Schedule schedule = scheduleRequestMapper.toDomainForUpdate(scheduleUpdateRequestDTO);
        Schedule updated = scheduleUseCase.updateSchedule(id, schedule);
        return ResponseEntity.ok(scheduleDTOMapper.toDTO(updated));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteSchedules(
            ScheduleDeleteRequestDTO scheduleDeleteRequestDTO,
            String acceptLanguage
    ) {
        scheduleUseCase.softDeleteSchedules(scheduleDeleteRequestDTO.getIds());
        return ResponseEntity.noContent().build();
    }
}
```

### 11. Registrar Excepciones

En `codefm-api/src/main/java/org/web/codefm/api/exception/ExceptionStatusEnum.java`:

```java
SCHEDULE_NOT_FOUND(ScheduleNotFoundException.class, HttpStatus.NOT_FOUND),
SCHEDULE_VALIDATION(ScheduleValidationException.class, HttpStatus.BAD_REQUEST),
```

### 12. Tests

#### Tests Unitarios

- `ScheduleServiceImplTest` en `codefm-application/src/test/java/`
- `ScheduleUseCaseImplTest` en `codefm-application/src/test/java/`
- `ScheduleRepositoryImplTest` en `codefm-infrastructure/src/test/java/`

#### Tests Karate

`karate-test/src/test/resources/features/schedules.feature`:

```gherkin
Feature: Schedule Endpoints

Background:
  Given url baseHttpsUrl
  And cookie SESSION = authTokens.karateuseradmin

Scenario: Create schedules for a class
  * def scheduleSchema = { id: '#number', classId: '#number', subjectId: '#number', day: '#number', start: '#string', end: '#string' }
  Given path '/private/v1/classes/1/schedules'
  And request { day: 1, items: [{ subjectId: 1, start: '08:30', end: '09:30' }] }
  When method PUT
  Then status 201
  And match each response == scheduleSchema

Scenario: Get schedules by class
  Given path '/private/v1/classes/1/schedules'
  When method GET
  Then status 200

Scenario: Update a schedule
  Given path '/private/v1/schedules/1'
  And request { day: 2, start: '09:00', end: '10:00' }
  When method PATCH
  Then status 200

Scenario: Delete schedules
  Given path '/private/v1/schedules'
  And request { ids: [1, 2] }
  When method DELETE
  Then status 204
```

## Confirmaciones

- ✅ Formato de hora: `HH:mm` (ej: "08:30")
- ✅ DELETE: Si algún ID no pertenece al profesor, no se elimina nada y se devuelve error
- ✅ Estructura de DTOs aprobada
