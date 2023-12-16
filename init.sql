USE mydatabase;

CREATE TABLE IF NOT EXISTS urlServices (
    url VARCHAR(255) PRIMARY KEY,
    urlAcortada VARCHAR(255),
    qrurl VARCHAR(255),
    qr BOOLEAN,
    alcanzable INT
);
