create database BIBLIOTECA;
use BIBLIOTECA;
CREATE TABLE TIPO
(
    ID          INTEGER     NOT NULL PRIMARY KEY,
    DESCRIPCION VARCHAR(20) NOT NULL
);

CREATE TABLE LECTOR
(
    CI        INTEGER     NOT NULL PRIMARY KEY,
    NOMBRE    VARCHAR(50) NOT NULL,
    TELEFONO  INTEGER     NOT NULL,
    CORREO    VARCHAR(30),
    DIRECCION VARCHAR(30) NOT NULL,
    IDTIPO    INTEGER     NOT NULL,
    FOREIGN KEY (IDTIPO) REFERENCES TIPO (ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE MATERIALBIBLIOGRAFICO
(
    CODIGO INTEGER           NOT NULL PRIMARY KEY,
    TITULO VARCHAR(50)       NOT NULL,
    NROPAG SMALLINT          NOT NULL,
    AÑO    SMALLINT UNSIGNED NOT NULL,
    TIPO   VARCHAR(1)        NOT NULL
);

CREATE TABLE FICHAPRESTAMO
(
    NRO      INTEGER NOT NULL PRIMARY KEY,
    FECHA    DATE    NOT NULL,
    DIAS     tinyint NOT NULL,
    CILECTOR INTEGER NOT NULL,
    FOREIGN KEY (CILECTOR) REFERENCES LECTOR (CI)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CODIGOMG INTEGER NOT NULL,
    FOREIGN KEY (CODIGOMG) REFERENCES MATERIALBIBLIOGRAFICO (CODIGO)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE LIBRO
(
    CODIGO    INTEGER     NOT NULL PRIMARY KEY,
    FOREIGN KEY (CODIGO) REFERENCES MATERIALBIBLIOGRAFICO (CODIGO)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    EDITORIAL VARCHAR(50) NOT NULL,
    EDICION   TINYINT     NOT NULL,
    ISBN      VARCHAR(12)
);

CREATE TABLE REVISTA
(
    CODIGO    INTEGER           NOT NULL PRIMARY KEY,
    FOREIGN KEY (CODIGO) REFERENCES MATERIALBIBLIOGRAFICO (CODIGO)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CATEGORIA VARCHAR(25)       NOT NULL,
    NRO       SMALLINT UNSIGNED NOT NULL
);

CREATE TABLE TESIS
(
    CODIGO INTEGER     NOT NULL PRIMARY KEY,
    FOREIGN KEY (CODIGO) REFERENCES MATERIALBIBLIOGRAFICO (CODIGO)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    AREA   VARCHAR(30) NOT NULL
);

INSERT INTO TIPO (ID, DESCRIPCION)
VALUES (1, 'ESTUDIANTE'),
       (2, 'DOCENTE'),
       (3, 'ADMINISTRATIVO');

INSERT INTO LECTOR (CI, NOMBRE, TELEFONO, CORREO, DIRECCION, IDTIPO)
VALUES (111111, 'Joaquin Chunacero', 77102030, 'JCHUMA@GMAIL.COM', 'URB. Las perlas', 2),
       (222222, 'Saturnino Mamani', 72345612, 'smamani@gmail.com', 'av. Landivar 300', 1),
       (444222, 'Luis Fernando Díaz', 72345678, 'luis.diaz@hotmail.com', 'Calle Luna 456', 2),
       (555333, 'María José Pérez', 73456789, 'maria.perez@outlook.com', 'Av. del Sol 789', 1),
       (666444, 'Carlos Alberto Ruiz', 74567890, 'carlos.ruiz@gmail.com', 'Zona Norte 321', 3),
       (777555, 'Paola Fernanda Gómez', 75678901, 'paola.gomez@yahoo.com', 'Barrio Central 654', 2),
       (888666, 'Jorge Luis Vargas', 76789012, 'jorge.vargas@u.edu.bo', 'Av. Las Flores 987', 1),
       (999777, 'Tatiana Ríos', 77890123, 'tatiana.rios@gmail.com', 'Calle 7 Oeste', 2),
       (111888, 'Ernesto Salazar', 78901234, 'ernesto.salazar@protonmail.com', 'Zona Sur 123', 3);


INSERT INTO MATERIALBIBLIOGRAFICO (CODIGO, TITULO, NROPAG, AÑO, TIPO)
VALUES (1000, 'Bases de datos principiantes', 120, 2022, 'L'),
       (3009, 'Sistema de Inf. Geografica', 150, 2015, 'T'),
       (1010, 'Piensa en Haskell', 428, 2012, 'L'),
       (1001, 'Cien años de soledad', 471, 1970, 'L'),
       (1002, 'Don Quijote de la Mancha', 863, 1605, 'L'),
       (1003, 'La sombra del viento', 565, 2001, 'L'),
       (1004, '1984', 328, 1949, 'L'),
       (1005, 'Ficciones', 174, 1944, 'L'),
       (1006, 'Rayuela', 601, 1963, 'L'),
       (1007, 'El Aleph', 146, 1949, 'L'),
       (1008, 'Pedro Páramo', 124, 1955, 'L'),
       (1009, 'El amor en los tiempos del cólera', 348, 1985, 'L'),
       (2001, 'Ciencia Hoy', 125, 2023, 'R'),
       (2002, 'National Geographic', 140, 2022, 'R'),
       (2003, 'Investigación y Ciencia', 132, 2021, 'R'),
       (2004, 'Muy Interesante', 110, 2024, 'R'),
       (2005, 'Nature', 180, 2023, 'R'),
       (2006, 'The Lancet', 150, 2023, 'R'),
       (2007, 'IEEE Spectrum', 160, 2024, 'R'),
       (2008, 'Scientific American', 145, 2022, 'R'),
       (3001, 'Inteligencia Artificial en la educación', 120, 2020, 'T'),
       (3002, 'Análisis de Algoritmos Genéticos', 95, 2019, 'T'),
       (3003, 'Desarrollo de videojuegos educativos', 150, 2021, 'T'),
       (3004, 'Redes neuronales aplicadas a la medicina', 175, 2022, 'T'),
       (3005, 'Big Data en sistemas gubernamentales', 110, 2023, 'T'),
       (3006, 'Blockchain y sus aplicaciones', 140, 2024, 'T'),
       (3007, 'Ciberseguridad en entornos escolares', 130, 2023, 'T'),
       (3008, 'Realidad aumentada en la enseñanza', 100, 2021, 'T');

INSERT INTO FICHAPRESTAMO (NRO, FECHA, DIAS, CILECTOR, CODIGOMG)
VALUES (100, '2025-10-05', 2, 111111, 1001),
       (101, '2025-06-06', 3, 222222, 1002),
       (102, '2025-06-06', 5, 555333, 1006),
       (103, '2025-06-07', 7, 666444, 3004),
       (104, '2025-06-07', 2, 777555, 2003),
       (105, '2025-06-08', 4, 888666, 1009),
       (106, '2025-06-02', 3, 555333, 2002),
       (107, '2025-06-03', 5, 666444, 1001),
       (108, '2025-06-03', 2, 777555, 3001),
       (109, '2025-06-04', 7, 888666, 1005),
       (110, '2025-06-04', 4, 111888, 2004),
       (111, '2025-06-05', 3, 222222, 3003),
       (112, '2025-06-05', 6, 444222, 1007),
       (113, '2025-06-06', 5, 111111, 2008),
       (114, '2025-06-06', 2, 555333, 3006),
       (115, '2025-06-07', 4, 999777, 1004);

INSERT INTO LIBRO (CODIGO, EDITORIAL, EDICION, ISBN)
VALUES (1000, 'ARBOL', 1, '1-20-580'),
       (1010, 'Grupo de Lógica Computacional', 1, '978-84-546'),
       (1001, 'Sudamericana', 1, '978-84-376'),
       (1002, 'Francisco de Robles', 1, '978-84-376'),
       (1003, 'Planeta', 1, '84-08-0378'),
       (1004, 'Secker & Warburg', 1, '978-0-452-2'),
       (1005, 'Sur', 1, '5-339-1129-4'),
       (1006, 'Sudamericana', 1, '978-84-415-8'),
       (1007, 'Losada', 1, '978-841425-7'),
       (1008, 'Fondo de Cultura Económica', 1, '978-968091-6'),
       (1009, 'Oveja Negra', 1, '978-84-495-4');

INSERT INTO REVISTA (CODIGO, CATEGORIA, NRO)
VALUES (2001, 'Divulgación Científica', 125),
       (2002, 'Historia', 340),
       (2003, 'Ciencia', 410),
       (2004, 'Tecnología', 233),
       (2005, 'Biología', 501),
       (2006, 'Medicina', 701),
       (2007, 'Ingeniería', 150),
       (2008, 'Ciencia', 600);

INSERT INTO TESIS (CODIGO, AREA)
VALUES (3009, 'Sistemas de informacion'),
       (3001, 'Sistemas de informacion'),
       (3002, 'Computación'),
       (3003, 'Ingeniería de Software'),
       (3004, 'Bioinformática'),
       (3005, 'Sistemas'),
       (3006, 'Tecnología'),
       (3007, 'Seguridad Informática'),
       (3008, 'Educación y Tecnología');

-- MOSTRAR TODAS LAS TESIS PUBLICADAS ENTRE EL AÑO 2017 - 2020
SELECT MATERIALBIBLIOGRAFICO.CODIGO, TITULO, NROPAG, AÑO, TIPO, AREA
FROM MATERIALBIBLIOGRAFICO,
     TESIS
WHERE AÑO BETWEEN 2017 AND 2021
  AND MATERIALBIBLIOGRAFICO.CODIGO = TESIS.CODIGO
  AND TIPO = 'T';

SELECT *
FROM MATERIALBIBLIOGRAFICO;

-- MOSTRAR LA CANTIDAD DE MATERIAL BIBLIOGRAFICO LIBRO, POR CADA LECTOR
SELECT LECTOR.NOMBRE, LECTOR.CI, COUNT(*)
FROM LECTOR,
     FICHAPRESTAMO,
     MATERIALBIBLIOGRAFICO
WHERE LECTOR.CI = FICHAPRESTAMO.CILECTOR
  AND MATERIALBIBLIOGRAFICO.CODIGO = FICHAPRESTAMO.CODIGOMG
  AND TIPO = 'L'
GROUP BY LECTOR.CI;

-- MOSTRAR EL MATERIAL BIBLIOGRAFICO QUE NO HA SIDO SACADO DE LA BIBLIOTECA
SELECT DISTINCT MATERIALBIBLIOGRAFICO.*
FROM MATERIALBIBLIOGRAFICO
WHERE MATERIALBIBLIOGRAFICO.CODIGO NOT IN (SELECT FICHAPRESTAMO.CODIGOMG FROM FICHAPRESTAMO);

-- MOSTRAR LOS MATERIALES BIBLIOGRAFICOS QUE SE HAN SACADO MAS DE UNA VEZ
SELECT MATERIALBIBLIOGRAFICO.*, COUNT(*) AS CATIDAD
FROM MATERIALBIBLIOGRAFICO,
     FICHAPRESTAMO
WHERE MATERIALBIBLIOGRAFICO.CODIGO = FICHAPRESTAMO.CODIGOMG
GROUP BY FICHAPRESTAMO.CODIGOMG
HAVING COUNT(*) > 1;

