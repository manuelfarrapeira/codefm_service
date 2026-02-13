CREATE TRIGGER trg_soft_delete_classes_on_school_delete
    AFTER UPDATE
    ON schools
    FOR EACH ROW
BEGIN
    -- Este trigger se activará después de una actualización en la tabla 'schools'.
    -- Queremos que actúe solo si la 'deletion_date' de la escuela ha sido establecida (no es NULL)
    -- y si ha cambiado (o si antes era NULL y ahora tiene un valor).
    IF NEW.deletion_date IS NOT NULL AND (OLD.deletion_date IS NULL OR NEW.deletion_date <> OLD.deletion_date) THEN
        -- Actualizamos la 'deletion_date' en las clases asociadas a esta escuela.
    UPDATE classes
    SET deletion_date = NEW.deletion_date
    WHERE school_id = NEW.id
      AND deletion_date IS NULL; -- Opcional: solo actualiza las clases que aún no han sido dadas de baja
END IF;
END;

CREATE TRIGGER trg_soft_delete_students_classes_on_class_delete
    AFTER UPDATE
    ON classes
    FOR EACH ROW
BEGIN
    -- Este trigger se activará después de una actualización en la tabla 'classes'.
    -- Queremos que actúe solo si la 'deletion_date' de la clase ha sido establecida (no es NULL)
    -- y si ha cambiado (o si antes era NULL y ahora tiene un valor).
    IF NEW.deletion_date IS NOT NULL AND (OLD.deletion_date IS NULL OR NEW.deletion_date <> OLD.deletion_date) THEN
        -- Actualizamos la 'deletion_date' en las relaciones estudiante-clase asociadas a esta clase.
        UPDATE students_classes
        SET deletion_date = NEW.deletion_date
        WHERE id_class = NEW.id
          AND deletion_date IS NULL; -- Solo actualiza las relaciones que aún no han sido dadas de baja
    END IF;
END;

CREATE TRIGGER trg_soft_delete_subject_classes_on_class_delete
    AFTER UPDATE
    ON classes
    FOR EACH ROW
BEGIN
    -- Este trigger se activará después de una actualización en la tabla 'classes'.
    -- Queremos que actúe solo si la 'deletion_date' de la clase ha sido establecida (no es NULL)
    -- y si ha cambiado (o si antes era NULL y ahora tiene un valor).
    IF NEW.deletion_date IS NOT NULL AND (OLD.deletion_date IS NULL OR NEW.deletion_date <> OLD.deletion_date) THEN
        -- Actualizamos la 'deletion_date' en las relaciones asignatura-clase asociadas a esta clase.
        UPDATE subjerct_classes
        SET deletion_date = NEW.deletion_date
        WHERE id_class = NEW.id
          AND deletion_date IS NULL; -- Solo actualiza las relaciones que aún no han sido dadas de baja
    END IF;
END;

CREATE TRIGGER trg_soft_delete_schedules_on_class_delete
    AFTER UPDATE
    ON classes
    FOR EACH ROW
BEGIN
    -- Este trigger se activará después de una actualización en la tabla 'classes'.
    -- Queremos que actúe solo si la 'deletion_date' de la clase ha sido establecida (no es NULL)
    -- y si ha cambiado (o si antes era NULL y ahora tiene un valor).
    IF NEW.deletion_date IS NOT NULL AND (OLD.deletion_date IS NULL OR NEW.deletion_date <> OLD.deletion_date) THEN
        -- Actualizamos la 'deletion_date' en los horarios asociados a esta clase.
        UPDATE schedules
        SET deletion_date = NEW.deletion_date
        WHERE class_id = NEW.id
          AND deletion_date IS NULL; -- Solo actualiza los horarios que aún no han sido dados de baja
    END IF;
END;

CREATE TRIGGER trg_soft_delete_subject_classes_on_subject_delete
    AFTER UPDATE
    ON subjects
    FOR EACH ROW
BEGIN
    -- Este trigger se activará después de una actualización en la tabla 'subjects'.
    -- Queremos que actúe solo si la 'deletion_date' de la asignatura ha sido establecida (no es NULL)
    -- y si ha cambiado (o si antes era NULL y ahora tiene un valor).
    IF NEW.deletion_date IS NOT NULL AND (OLD.deletion_date IS NULL OR NEW.deletion_date <> OLD.deletion_date) THEN
        -- Actualizamos la 'deletion_date' en las relaciones asignatura-clase asociadas a esta asignatura.
        UPDATE subjerct_classes
        SET deletion_date = NEW.deletion_date
        WHERE id_subject = NEW.id
          AND deletion_date IS NULL; -- Solo actualiza las relaciones que aún no han sido dadas de baja
    END IF;
END;

CREATE TRIGGER trg_soft_delete_schedules_on_subject_delete
    AFTER UPDATE
    ON subjects
    FOR EACH ROW
BEGIN
    -- Este trigger se activará después de una actualización en la tabla 'subjects'.
    -- Queremos que actúe solo si la 'deletion_date' de la asignatura ha sido establecida (no es NULL)
    -- y si ha cambiado (o si antes era NULL y ahora tiene un valor).
    IF NEW.deletion_date IS NOT NULL AND (OLD.deletion_date IS NULL OR NEW.deletion_date <> OLD.deletion_date) THEN
        -- Actualizamos la 'deletion_date' en los horarios asociados a esta asignatura.
        UPDATE schedules
        SET deletion_date = NEW.deletion_date
        WHERE subject_id = NEW.id
          AND deletion_date IS NULL; -- Solo actualiza los horarios que aún no han sido dados de baja
    END IF;
END;


SHOW TRIGGERS;
