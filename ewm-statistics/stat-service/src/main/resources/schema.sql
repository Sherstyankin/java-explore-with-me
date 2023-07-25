CREATE TABLE IF NOT EXISTS hits (
        id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
        app varchar(255) NOT NULL,
        uri varchar(255) NOT NULL,
        ip varchar(255) NOT NULL,
        created timestamp WITHOUT TIME ZONE NOT NULL
);