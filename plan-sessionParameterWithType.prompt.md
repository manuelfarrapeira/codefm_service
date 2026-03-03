# Plan: Refactorizar SessionParameter con tipo embebido

Modificar `SessionParameter` para incluir el tipo de dato en el propio enum, crear un nuevo método
`getParameter(SessionParameter)` en `SessionUser` que use ese tipo, eliminar el método antiguo y actualizar todos los
usos en producción y tests.

## Steps

1. Modificar [
   `SessionParameter.java`](c:\proyectos_github\codefm\codefm-domain\src\main\java\org\web\codefm\domain\session\SessionParameter.java)
   añadiendo `Class<?> type` → `TEACHER_ID("teacher_id", Integer.class)`.

2. Reemplazar método `getParameter(SessionParameter, Class<T>)` en [
   `SessionUser.java`](c:\proyectos_github\codefm\codefm-domain\src\main\java\org\web\codefm\domain\session\SessionUser.java)
   por `getParameter(SessionParameter)` que obtenga el tipo del enum.

3. Actualizar **UseCases** (2 archivos): [
   `ClassUseCaseImpl.java`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\usecase\teachernotebook\ClassUseCaseImpl.java)
   y [
   `SchoolUseCaseImpl.java`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\usecase\teachernotebook\SchoolUseCaseImpl.java) -
   cambiar `getParameter(SessionParameter.TEACHER_ID, Integer.class)` → `getParameter(SessionParameter.TEACHER_ID)`.

4. Actualizar **Services** con patrón `getParameter`: [
   `CalendarAlertServiceImpl`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\service\teachernotebook\CalendarAlertServiceImpl.java), [
   `ExerciseStudentGradeServiceImpl`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\service\teachernotebook\ExerciseStudentGradeServiceImpl.java), [
   `SubjectClassServiceImpl`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\service\teachernotebook\SubjectClassServiceImpl.java), [
   `SubjectServiceImpl`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\service\teachernotebook\SubjectServiceImpl.java).

5. Actualizar **Services** con patrón `getParameters().get()`: [
   `ExerciseDocumentServiceImpl`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\service\teachernotebook\ExerciseDocumentServiceImpl.java), [
   `ScheduleServiceImpl`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\service\teachernotebook\ScheduleServiceImpl.java), [
   `StudentServiceImpl`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\service\teachernotebook\StudentServiceImpl.java), [
   `StudentClassServiceImpl`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\service\teachernotebook\StudentClassServiceImpl.java), [
   `ExerciseServiceImpl`](c:\proyectos_github\codefm\codefm-application\src\main\java\org\web\codefm\service\teachernotebook\ExerciseServiceImpl.java) -
   cambiar `Integer.valueOf(sessionUser.getParameters().get(...))` →
   `sessionUser.getParameter(SessionParameter.TEACHER_ID)`.

6. Actualizar **tests de dominio**: [
   `SessionUserTest.java`](c:\proyectos_github\codefm\codefm-domain\src\test\java\org\web\codefm\domain\session\SessionUserTest.java), [
   `SessionParameterTest.java`](c:\proyectos_github\codefm\codefm-domain\src\test\java\org\web\codefm\domain\session\SessionParameterTest.java) -
   adaptar tests al nuevo método sin segundo parámetro.

7. Actualizar **tests de application**: `ClassUseCaseImplTest`, `SchoolUseCaseImplTest`, `CalendarAlertServiceImplTest`,
   `SubjectServiceImplTest`, `SubjectClassServiceImplTest`, `ExerciseStudentGradeServiceImplTest` - cambiar mocks de
   `.getParameter(SessionParameter.TEACHER_ID, Integer.class)` → `.getParameter(SessionParameter.TEACHER_ID)`.

## Further Considerations

1. **Tests de tipos diferentes**: Los tests actuales prueban conversión a `String`, `Long`, `Boolean`, etc. con
   `TEACHER_ID`. Se deben crear nuevos `SessionParameter` de prueba para diferentes tipos en los tests o simplificar
   tests solo con `TEACHER_ID` como `Integer`. **Recomendación**: Crear constantes de test adicionales como
   `TEST_STRING_PARAM`, `TEST_LONG_PARAM` si se necesita probar otros tipos, o bien añadir más constantes al enum si son
   parámetros reales del JWT.

