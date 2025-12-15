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