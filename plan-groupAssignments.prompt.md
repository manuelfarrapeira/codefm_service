# Plan: CRUD completo para Trabajos de Grupo (Group Assignments)
Tres tablas nuevas: `group_assignments` (trabajos por trimestre 1-3 vinculados a clase), `group_assignment_grades` (nota unica 0-10 por grupo+trabajo, con soft delete), y `group_assignment_documents` (hard delete + borrado de fichero, max 2MB en `data/group-documents`). Los documentos incluyen un campo booleano `group_document` para distinguir entre documentos del trabajo en si (`false`) y documentos de un grupo para ese trabajo (`true`); cuando es documento de grupo, `group_id` es NOT NULL. Al eliminar un trabajo: cascade soft-delete grades + hard-delete documentos. Al eliminar un grupo (`student_groups`): cascade soft-delete sus grades + hard-delete sus documentos.
## Steps
### 1. SQL - crear `database_scripts/teacher_notebook/V17__create_group_assignments.sql` y ejecutar en BD
- `group_assignments`: id PK, class_id FK->classes, title VARCHAR(200) NOT NULL, description TEXT, quarter INT NOT NULL, deletion_date DATE
- `group_assignment_grades`: id PK, group_assignment_id FK->group_assignments, group_id FK->student_groups, grade DECIMAL(4,2) NOT NULL, deletion_date DATE
- `group_assignment_documents`: id PK, group_assignment_id FK->group_assignments, group_id FK->student_groups NULL, document VARCHAR(255) NOT NULL, description TEXT, group_document BOOLEAN NOT NULL DEFAULT FALSE
En `group_assignment_documents`: si `group_document = false` -> documento del trabajo (group_id NULL); si `group_document = true` -> documento de un grupo para ese trabajo (group_id NOT NULL).
### 2. Domain layer en `codefm-domain`
**Entidades** `entity/teachernotebook/`:
- `GroupAssignment` (id, classId, title, description, quarter, deletionDate)
- `GroupAssignmentGrade` (id, groupAssignmentId, groupId, grade, groupName, deletionDate)
- `GroupAssignmentDocument` (id, groupAssignmentId, groupId, document, description, groupDocument)
**Repositories** `repository/teachernotebook/` (con JavaDoc):
- `GroupAssignmentRepository`: findByClassId, findByIdAndTeacherId, save, softDeleteById, softDeleteByClassId, findActiveIdsByClassId
- `GroupAssignmentGradeRepository`: findByAssignmentId, findByAssignmentIdAndGroupId, save, update, softDeleteById, softDeleteByGroupAssignmentId, softDeleteByGroupAssignmentIds, softDeleteByGroupId, softDeleteByGroupIds
- `GroupAssignmentDocumentRepository`: findByAssignmentId, findByAssignmentIdAndGroupId, findById, save, deleteById, deleteByGroupAssignmentId, deleteByGroupAssignmentIds, deleteByGroupId, deleteByGroupIds, findByGroupAssignmentIds, findByGroupIds - **hard delete** de registros + los ficheros se borran en el service
**Services** `service/teachernotebook/`:
- `GroupAssignmentService`: CRUD trabajos + CRUD notas (getAssignments, createAssignment, updateAssignment, softDeleteAssignment, getGrades, createOrUpdateGrade, deleteGrade)
- `GroupAssignmentDocumentService`: upload, download, getFilename, delete, deleteByGroupAssignmentId, deleteByGroupAssignmentIds, deleteByGroupId, deleteByGroupIds - siguiendo patron de `ExerciseDocumentService`
**UseCases** `usecase/teachernotebook/`:
- `GroupAssignmentUseCase`: delega a service, `softDeleteAssignment` llama cascade + service con `@Transactional`
- `GroupAssignmentDocumentUseCase`: delega a service
**Excepciones** `exception/teachernotebook/`:
- `GroupAssignmentNotFoundException` (extends `ErrorMessageBaseException`)
- `GroupAssignmentValidationException` (extends `ListErrorMessageBaseException`)
- `GroupAssignmentDocumentNotFoundException` (extends `ErrorMessageBaseException`)
- `GroupAssignmentDocumentUploadException` (extends `ErrorMessageBaseException`)
**MessageKeys** - anadir:
- `GROUP_ASSIGNMENT_NOT_FOUND`
- `GROUP_ASSIGNMENT_VALIDATION_TITLE_REQUIRED`
- `GROUP_ASSIGNMENT_VALIDATION_QUARTER_REQUIRED`
- `GROUP_ASSIGNMENT_VALIDATION_QUARTER_INVALID`
- `GROUP_ASSIGNMENT_GRADE_REQUIRED`
- `GROUP_ASSIGNMENT_GRADE_INVALID` (0-10)
- `GROUP_ASSIGNMENT_GROUP_NOT_FOUND`
- `GROUP_ASSIGNMENT_GROUP_NOT_IN_CLASS`
- `GROUP_ASSIGNMENT_GRADE_ALREADY_EXISTS`
- `GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND`
- `GROUP_ASSIGNMENT_DOCUMENT_EMPTY`
- `GROUP_ASSIGNMENT_DOCUMENT_UPLOAD_ERROR`
- `GROUP_ASSIGNMENT_DOCUMENT_DELETE_ERROR`
- `GROUP_ASSIGNMENT_DOCUMENT_INVALID_EXTENSION`
- `GROUP_ASSIGNMENT_DOCUMENT_SIZE_EXCEEDED`
### 3. Infrastructure layer en `codefm-infrastructure`
**JPA Entities** `entity/mariadb/teachernotebook/`:
- `GroupAssignmentEntity`: id, classId, title, description, quarter, deletionDate (patron `ExerciseEntity`)
- `GroupAssignmentGradeEntity`: id, groupAssignmentId, groupId, grade (Double), deletionDate
- `GroupAssignmentDocumentEntity`: id, groupAssignmentId, groupId (nullable), document, description, groupDocument (Boolean)
**JPA Repositories** `jpa/teachernotebook/`:
- `GroupAssignmentJPARepository`: `findByClassIdAndDeletionDateIsNull`, `findByIdAndTeacherId` con JOINs (assignment->class->school.teacherId), `@Modifying softDeleteById`, `softDeleteByClassId`, `findActiveIdsByClassId`
- `GroupAssignmentGradeJPARepository`: `findByGroupAssignmentIdAndDeletionDateIsNull`, `findByGroupAssignmentIdAndGroupIdAndDeletionDateIsNull`, `@Modifying softDeleteById`, `softDeleteByGroupAssignmentId`, `softDeleteByGroupAssignmentIds`, `softDeleteByGroupId`, `softDeleteByGroupIds`
- `GroupAssignmentDocumentJPARepository`: queries estandar + `deleteByGroupAssignmentId`, `deleteByGroupAssignmentIds`, `deleteByGroupId`, `deleteByGroupIds`, `findByGroupAssignmentIdIn`, `findByGroupIdIn`
**Mappers** `mapper/`: `GroupAssignmentMapper`, `GroupAssignmentGradeMapper`, `GroupAssignmentDocumentMapper`
**Repository Impls**: delegacion JPA -> mapper
### 4. Application layer en `codefm-application`
**`GroupAssignmentServiceImpl`**: inyecta `ClassRepository`, `SavedStudentGroupRepository`, `GroupAssignmentRepository`, `GroupAssignmentGradeRepository`, `SessionUser`, `MessageSource`. Validaciones: ownership clase, titulo obligatorio, quarter 1-3 (patron `ExerciseServiceImpl` L130-143), grade 0-10, grupo existe y pertenece a la clase, nota unica por grupo+trabajo (`findByAssignmentIdAndGroupIdAndDeletionDateIsNull`).
**`GroupAssignmentDocumentServiceImpl`**: inyecta `GroupAssignmentRepository`, `GroupAssignmentDocumentRepository`, `SavedStudentGroupRepository`, `SessionUser`, `MessageSource`. Config `@Value("${group.documents.directory:./data/group-documents}")`. Patron `ExerciseDocumentServiceImpl`: max 2MB, extensiones `MimeTypeEnum`, UUID en nombre fichero. Upload: si `groupDocument=true` valida que `groupId` existe y pertenece a la clase; si `groupDocument=false` el `groupId` se ignora/null. Cascade: `deleteDocumentsByGroupAssignmentId(id)`, `deleteDocumentsByGroupAssignmentIds(ids)`, `deleteDocumentsByGroupId(id)`, `deleteDocumentsByGroupIds(ids)`.
**`GroupAssignmentUseCaseImpl`**: para delete: `@Transactional` -> `cascadeSoftDeleteService.cascadeDeleteChildrenOfGroupAssignment(id)` -> `groupAssignmentService.softDeleteAssignment(id)`.
**`GroupAssignmentDocumentUseCaseImpl`**: delega a service.
**Actualizar `CascadeSoftDeleteService`**: anadir `cascadeDeleteChildrenOfGroupAssignment(Integer assignmentId)`.
**Actualizar `CascadeSoftDeleteServiceImpl`**:
- Nuevo: `cascadeDeleteChildrenOfGroupAssignment(id)` -> `groupAssignmentGradeRepository.softDeleteByGroupAssignmentId(id)` + `groupAssignmentDocumentService.deleteDocumentsByGroupAssignmentId(id)`.
- Actualizar `cascadeDeleteChildrenOfClass(classId)`: tras las lineas existentes, anadir: obtener `assignmentIds` activos -> para cada uno cascade hijos -> `softDeleteByClassId` los assignments. Tambien al procesar los `savedGroupIds`: antes del hard-delete members, llamar `groupAssignmentGradeRepository.softDeleteByGroupIds(savedGroupIds)` + `groupAssignmentDocumentService.deleteDocumentsByGroupIds(savedGroupIds)`.
**Tests unitarios**: `GroupAssignmentServiceImplTest`, `GroupAssignmentDocumentServiceImplTest`, `GroupAssignmentUseCaseImplTest`, `GroupAssignmentDocumentUseCaseImplTest`, actualizar `CascadeSoftDeleteServiceImplTest`.
### 5. API layer en `codefm-api`
**OpenAPI** en `private-api.yaml`: nuevo tag `TeacherNoteBookGroupAssignments`, endpoints:
- `GET /teacher-notebook/v1/classes/{classId}/group-assignments` - listar trabajos de clase
- `POST /teacher-notebook/v1/classes/{classId}/group-assignments` - crear trabajo (title, description?, quarter 1-3)
- `PATCH /teacher-notebook/v1/group-assignments/{assignmentId}` - actualizar
- `DELETE /teacher-notebook/v1/group-assignments/{assignmentId}` - soft-delete + cascade
- `GET /teacher-notebook/v1/group-assignments/{assignmentId}/grades` - listar notas
- `PUT /teacher-notebook/v1/group-assignments/{assignmentId}/groups/{groupId}/grade` - crear/actualizar nota (grade 0-10, upsert)
- `DELETE /teacher-notebook/v1/group-assignments/{assignmentId}/groups/{groupId}/grade` - soft-delete nota
- `POST /teacher-notebook/v1/group-assignments/{assignmentId}/documents` - upload documento del trabajo (`groupDocument=false`, sin groupId)
- `POST /teacher-notebook/v1/group-assignments/{assignmentId}/groups/{groupId}/documents` - upload documento de grupo (`groupDocument=true`, con groupId)
- `GET /teacher-notebook/v1/group-assignments/{assignmentId}/documents/{documentId}/download` - download
- `DELETE /teacher-notebook/v1/group-assignments/{assignmentId}/documents/{documentId}` - hard-delete + borrar fichero
**DTOs**:
- `GroupAssignmentDTO` (id, classId, title, description, quarter)
- `GroupAssignmentRequestDTO` (title, description, quarter)
- `GroupAssignmentGradeDTO` (id, groupAssignmentId, groupId, grade, groupName)
- `GroupAssignmentGradeRequestDTO` (grade)
- `GroupAssignmentDocumentDTO` (id, groupAssignmentId, groupId, document, description, groupDocument)
**DTO Mappers**: `GroupAssignmentDTOMapper`, `GroupAssignmentGradeDTOMapper`, `GroupAssignmentDocumentDTOMapper`
**Controllers**: `PrivateGroupAssignments` implementando la API generada, con `@PreAuthorize("hasRole('TEACHER')")`, `@Logged`, `@Locale(position)`
**Actualizar `ExceptionStatusEnum`**:
- `GROUP_ASSIGNMENT_NOT_FOUND(GroupAssignmentNotFoundException.class, HttpStatus.NOT_FOUND)`
- `GROUP_ASSIGNMENT_VALIDATION_ERROR(GroupAssignmentValidationException.class, HttpStatus.BAD_REQUEST)`
- `GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND(GroupAssignmentDocumentNotFoundException.class, HttpStatus.NOT_FOUND)`
- `GROUP_ASSIGNMENT_DOCUMENT_UPLOAD_ERROR(GroupAssignmentDocumentUploadException.class, HttpStatus.INTERNAL_SERVER_ERROR)`
### 6. Config + i18n + Karate + Postman
**`application.yml`**: anadir seccion `group: documents: directory: ./data/group-documents`
**docker-compose.yml** y **docker-compose-pre.yml**: anadir volumen `./data/group-documents:/app/data/group-documents`
**Crear directorio** `data/group-documents/` con `.gitkeep`
**i18n**: mensajes en `messages_en.properties` y `messages_es.properties` (con escapes Unicode) para todas las `MessageKeys` del paso 2
**Karate**: crear `features/teacher-notebook/group-assignments/` con:
- `getgroupassignments.feature`
- `creategroupassignment.feature`
- `updategroupassignment.feature`
- `deletegroupassignment.feature`
- `getgroupassignmentgrades.feature`
- `upsertgroupassignmentgrade.feature`
- `deletegroupassignmentgrade.feature`
- `uploadgroupassignmentdocument.feature`
- `uploadgroupassignmentgroupdocument.feature`
- `downloadgroupassignmentdocument.feature`
- `deletegroupassignmentdocument.feature`
Registrar cada uno en `IndividualKarateTestRunner.java`.
**Postman**: crear `GroupAssignments.postman_collection.json` en `postman/`
## Further Considerations
1. **Constraint de nota unica**: En la tabla `group_assignment_grades` conviene anadir un unique index `UNIQUE(group_assignment_id, group_id)` filtrado por `deletion_date IS NULL` (o gestionarlo solo a nivel de servicio con la query `findByAssignmentIdAndGroupIdAndDeletionDateIsNull` ya que MariaDB no soporta filtered unique indexes nativamente).
2. **Validacion de `groupDocument`**: Cuando `group_document = true` es obligatorio que `group_id` no sea null y que el grupo pertenezca a la misma clase del trabajo. Cuando `group_document = false`, se fuerza `group_id = null` independientemente de lo que envie el cliente.
3. **Datos de seed para Karate**: Se necesitaran INSERTs en la BD de test con al menos un `group_assignment` y un `student_group` en la clase 4 (clase usada en otros tests de Karate), o crear datos dinamicamente con POST dentro de los features.
