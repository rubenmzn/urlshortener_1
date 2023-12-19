-- SQLBook: Code
-- init.sql

USE mydatabase;

CREATE TABLE IF NOT EXISTS urlServices (
    url VARCHAR(255) PRIMARY KEY,
    urlAcortada VARCHAR(255),
    qrurl VARCHAR(255),
    qr BOOLEAN,
    alcanzable INT,
    qrCode BLOB,
    idQr VARCHAR(255)
);

-- Puedes agregar más comandos SQL según sea necesario
