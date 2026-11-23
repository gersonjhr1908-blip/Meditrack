-- SQLCMD: proporciona MEDITRACK_DB_PASSWORD localmente antes de ejecutar el bloque de login.
USE master;
GO

IF EXISTS (
    SELECT 1
    FROM sys.sql_logins
    WHERE name = 'meditrack_app'
)
    ALTER LOGIN meditrack_app
    WITH PASSWORD = '$(MEDITRACK_DB_PASSWORD)';
ELSE
    CREATE LOGIN meditrack_app
    WITH PASSWORD = '$(MEDITRACK_DB_PASSWORD)';
GO

USE MediTrack;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.database_principals
    WHERE name = 'meditrack_app'
)
    CREATE USER meditrack_app
    FOR LOGIN meditrack_app;
GO

ALTER ROLE db_datareader ADD MEMBER meditrack_app;
ALTER ROLE db_datawriter ADD MEMBER meditrack_app;
GO



SELECT
    u.id_usuario,
    u.dni,
    u.nombres,
    u.apellidos,
    u.correo,
    u.rol,
    p.id_paciente,
    p.codigo_paciente,
    p.fecha_nacimiento
FROM dbo.USUARIO u
INNER JOIN dbo.PACIENTE p
    ON p.id_usuario = u.id_usuario
ORDER BY u.id_usuario DESC;

SELECT * FROM dbo.USUARIO;
SELECT * FROM dbo.PACIENTE;



SELECT TOP (10)
    u.dni,
    u.nombres,
    u.apellidos,
    u.correo,
    u.estado,
    p.codigo_paciente
FROM MediTrack.dbo.USUARIO AS u
JOIN MediTrack.dbo.PACIENTE AS p ON p.id_usuario = u.id_usuario
ORDER BY u.id_usuario DESC;

SELECT * FROM [dbo].[MEDICO]
SELECT * FROM [dbo].[USUARIO]


USE MediTrack;
GO

SELECT
    u.id_usuario,
    u.nombres,
    u.apellidos,
    u.dni,
    u.correo,
    u.estado,
    p.id_personal,
    m.id_medico,
    m.numero_colegiatura,
    e.nombre AS especialidad
FROM USUARIO u
INNER JOIN PERSONAL p ON p.id_usuario = u.id_usuario
INNER JOIN MEDICO m ON m.id_personal = p.id_personal
LEFT JOIN MEDICO_ESPECIALIDAD me ON me.id_medico = m.id_medico
LEFT JOIN ESPECIALIDAD e ON e.id_especialidad = me.id_especialidad
WHERE u.rol = 'MEDICO'
ORDER BY e.nombre, u.apellidos;

SELECT e.nombre AS especialidad, COUNT(*) AS cantidad
FROM MEDICO_ESPECIALIDAD me
JOIN ESPECIALIDAD e ON e.id_especialidad = me.id_especialidad
GROUP BY e.nombre;



USE MediTrack;
GO
-- Completa datos faltantes de m�dicos y registra el acceso de Yudy al iniciar sesi�n.
UPDATE u SET telefono = v.telefono, sexo = v.sexo
FROM dbo.USUARIO u
JOIN (VALUES
('98000001','900000002','Masculino'),('98000003','900000004','Femenino'),('98000005','900000006','Masculino'),
('98000007','900000008','Femenino'),('98000009','900000010','Masculino'),('98000011','900000012','Femenino'),
('98000013','900000014','Femenino'),('98000015','900000016','Masculino'),('98000017','900000018','Femenino'),
('98000019','900000020','Masculino'),('98000021','900000022','Femenino'),('98000023','900000024','Masculino'),
('98000025','900000026','Femenino'),('98000027','900000028','Masculino'),('98000029','900000030','Femenino'),
('98000031','900000032','Masculino'),('98000033','900000034','Femenino'),('98000035','900000036','Masculino')
) v(dni,telefono,sexo) ON v.dni=u.dni
WHERE u.rol='MEDICO';
GO
SELECT u.id_usuario,u.nombres,u.apellidos,u.dni,u.telefono,u.sexo,u.ultimo_acceso,u.correo,u.rol,u.estado
FROM dbo.USUARIO u WHERE u.rol='MEDICO' OR LOWER(u.correo)='paciente@meditrack.example';
GO

--verificar acceso
SELECT nombres, correo, ultimo_acceso
FROM USUARIO
WHERE correo = 'paciente@meditrack.example';


--citas
SELECT * FROM [dbo].[CITA]
SELECT * FROM [dbo].[USUARIO]
USE MediTrack;

SELECT id_usuario, nombres, apellidos, dni, correo, rol, estado
FROM USUARIO
WHERE LOWER(correo) = 'paciente@meditrack.example';





---RECORDARTORIO
USE MediTrack;
IF OBJECT_ID('dbo.RECORDATORIO','U') IS NULL
BEGIN
 CREATE TABLE dbo.RECORDATORIO(
  id_recordatorio INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
  id_paciente INT NOT NULL,
  nombre VARCHAR(150) NOT NULL,
  tipo VARCHAR(30) NOT NULL,
  detalle VARCHAR(500) NULL,
  frecuencia VARCHAR(50) NOT NULL,
  hora TIME(0) NOT NULL,
  fecha DATE NOT NULL,
  activo BIT NOT NULL CONSTRAINT DF_RECORDATORIO_ACTIVO DEFAULT(1),
  fecha_creacion DATETIME NOT NULL CONSTRAINT DF_RECORDATORIO_FECHA DEFAULT(GETDATE()),
  CONSTRAINT FK_RECORDATORIO_PACIENTE FOREIGN KEY(id_paciente) REFERENCES dbo.PACIENTE(id_paciente)
 );
END;
GO


---verificar recordatorio
USE MediTrack;

SELECT *
FROM RECORDATORIO
ORDER BY id_recordatorio DESC;

----medicamentos y tratamientos limpios
USE MediTrack;
GO

SELECT *
FROM dbo.MEDICAMENTO
ORDER BY 1 DESC;

SELECT *
FROM dbo.TRATAMIENTO
ORDER BY 1 DESC;

SELECT *
FROM dbo.TRATAMIENTO_MEDICAMENTO
ORDER BY 1 DESC;

SELECT *
FROM dbo.ATENCION_TRATAMIENTO
ORDER BY 1 DESC;

---confirmar ID prueba
USE MediTrack;
GO

SELECT id_paciente, id_usuario
FROM dbo.PACIENTE;

SELECT id_medico, id_personal
FROM dbo.MEDICO;

SELECT id_usuario, nombres, apellidos, rol
FROM dbo.USUARIO
WHERE rol IN ('PACIENTE', 'MEDICO');
--columnas 
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME IN (
    'MEDICAMENTO',
    'TRATAMIENTO',
    'TRATAMIENTO_MEDICAMENTO'
)
ORDER BY TABLE_NAME, ORDINAL_POSITION;

--insertar medicamentos prueba
USE MediTrack;
GO

INSERT INTO dbo.MEDICAMENTO
(
    nombre,
    forma_farmaceutica,
    via_administracion,
    estado
)
VALUES
(
    'Paracetamol',
    'Tableta',
    'Oral',
    'ACTIVO'
);

DECLARE @id_medicamento INT = SCOPE_IDENTITY();

INSERT INTO dbo.TRATAMIENTO
(
    id_paciente,
    id_medico,
    nombre,
    diagnostico_referencia,
    fecha_inicio,
    estado,
    observaciones
)
VALUES
(
    1,
    1,
    'Tratamiento de prueba',
    'Dolor de cabeza',
    CAST(GETDATE() AS DATE),
    'ACTIVO',
    'Registro de prueba para validar Android'
);

DECLARE @id_tratamiento INT = SCOPE_IDENTITY();

SELECT @id_medicamento AS id_medicamento,
       @id_tratamiento AS id_tratamiento;

--ver 
SELECT *
FROM dbo.MEDICAMENTO;

SELECT *
FROM dbo.TRATAMIENTO;

---relacionar 
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'TRATAMIENTO_MEDICAMENTO'
ORDER BY ORDINAL_POSITION;

--relacionar medicamento
USE MediTrack;
GO

INSERT INTO dbo.TRATAMIENTO_MEDICAMENTO
(
    id_tratamiento,
    id_medicamento,
    dosis,
    unidad_dosis,
    frecuencia,
    via_administracion,
    fecha_inicio,
    estado
)
VALUES
(
    1,
    1,
    500,
    'mg',
    'Cada 8 horas',
    'Oral',
    CAST(GETDATE() AS DATE),
    'ACTIVO'
);

SELECT *
FROM dbo.TRATAMIENTO_MEDICAMENTO;


----xd
USE MediTrack;
GO

SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME IN ('TUTOR', 'TUTOR_PACIENTE')
ORDER BY TABLE_NAME, ORDINAL_POSITION;


----codigo vinculacion
USE MediTrack;
GO

ALTER TABLE dbo.TUTOR
ADD codigo_vinculacion VARCHAR(20) NULL,
    codigo_vencimiento DATETIME NULL;


--invitacion al tutor
USE MediTrack;
GO

CREATE TABLE dbo.INVITACION_TUTOR (
    id_invitacion INT IDENTITY(1,1) PRIMARY KEY,
    id_paciente INT NOT NULL,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    fecha_creacion DATETIME NOT NULL DEFAULT GETDATE(),
    fecha_vencimiento DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT FK_INVITACION_PACIENTE
        FOREIGN KEY (id_paciente) REFERENCES dbo.PACIENTE(id_paciente)
);


--- verificar vinculacion
USE MediTrack;
GO

SELECT *
FROM dbo.TUTOR_PACIENTE
ORDER BY id_tutor_paciente DESC;

SELECT *
FROM dbo.TUTOR_PACIENTE
WHERE id_paciente = 2;

SELECT *
FROM dbo.TUTOR_PACIENTE
WHERE id_tutor = 1;


USE MediTrack;
GO

UPDATE dbo.TUTOR_PACIENTE
SET estado = 'PENDIENTE',
    fecha_confirmacion = NULL
WHERE id_tutor = 1
  AND id_paciente = 2;

  SELECT *
FROM dbo.TUTOR_PACIENTE
WHERE id_paciente = 2;

SELECT *
FROM dbo.TUTOR_PACIENTE
WHERE id_paciente = 2;

UPDATE dbo.TUTOR_PACIENTE
SET estado = 'CONFIRMADA',
    fecha_confirmacion = GETDATE()
WHERE id_tutor = 1
  AND id_paciente = 2;

SELECT * FROM [dbo].[USUARIO]




----a�adir enfermeros
USE MediTrack;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @usuario INT, @personal INT;
    DECLARE @correo VARCHAR(100) = 'lucia.rojas@meditrack.example';
    DECLARE @dni VARCHAR(8) = '98000037';

    SELECT @usuario = id_usuario
    FROM dbo.USUARIO WITH (UPDLOCK, HOLDLOCK)
    WHERE correo = @correo AND dni = @dni AND rol = 'ENFERMERO';

    IF @usuario IS NULL
    BEGIN
        IF EXISTS (SELECT 1 FROM dbo.USUARIO WHERE correo = @correo OR dni = @dni)
            THROW 50001, 'El correo o el DNI pertenece a otra cuenta. Revisa los datos.', 1;

        INSERT INTO dbo.USUARIO
            (dni, nombres, apellidos, correo, telefono, sexo,
             password_hash, rol, estado, fecha_creacion)
        VALUES
            (@dni, 'Luc�a', 'Rojas', @correo, '900000038', 'Femenino',
             '$2a$12$bd0EJl8oovssP6MlgTgJQeJAxMB.kclRIu2dQZBzz0Gj3UIlEeLAi',
             'ENFERMERO', 'ACTIVO', GETDATE());

        SET @usuario = SCOPE_IDENTITY();
    END
    ELSE
        UPDATE dbo.USUARIO
        SET telefono = '900000038', sexo = 'Femenino'
        WHERE id_usuario = @usuario;

    SELECT @personal = id_personal
    FROM dbo.PERSONAL
    WHERE id_usuario = @usuario;

    IF @personal IS NULL
    BEGIN
        INSERT INTO dbo.PERSONAL (id_usuario, fecha_ingreso, estado_laboral)
        VALUES (@usuario, CAST(GETDATE() AS DATE), 'ACTIVO');

        SET @personal = SCOPE_IDENTITY();
    END;

    IF NOT EXISTS (SELECT 1 FROM dbo.ENFERMERO WHERE id_personal = @personal)
        INSERT INTO dbo.ENFERMERO (id_personal, codigo_profesional)
        VALUES (@personal, 'ENF-0001');

    COMMIT;

    SELECT u.nombres, u.apellidos, u.correo, u.telefono, u.sexo,
           u.rol, e.codigo_profesional
    FROM dbo.USUARIO AS u
    JOIN dbo.PERSONAL AS p ON p.id_usuario = u.id_usuario
    JOIN dbo.ENFERMERO AS e ON e.id_personal = p.id_personal
    WHERE u.id_usuario = @usuario;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;


SELECT * FROM [dbo].[USUARIO]
SELECT * FROM [dbo].[ENFERMERO]