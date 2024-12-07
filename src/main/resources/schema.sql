CREATE TABLE IF NOT EXISTS people (
     id_peo INTEGER PRIMARY KEY AUTOINCREMENT,
     name VARCHAR2(50),
     surname VARCHAR2(50),
     email VARCHAR2(50)
);
CREATE INDEX IF NOT EXISTS idx_people_name ON people(name, surname);

CREATE TABLE IF NOT EXISTS secret_santa (
    id_sea INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR2(100),
    creation_date TIMESTAMP,
    last_update TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_santa_date ON secret_santa(creation_date);

CREATE TABLE IF NOT EXISTS secret_santa_run (
    id_ser INTEGER PRIMARY KEY AUTOINCREMENT,
    id_sea INTEGER,
    creation_date TIMESTAMP,
    last_update TIMESTAMP,
    FOREIGN KEY (id_sea) REFERENCES secret_santa (id_sea) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_run_fk ON secret_santa_run(id_sea);
CREATE INDEX IF NOT EXISTS idx_run_date ON secret_santa_run(last_update);

CREATE TABLE IF NOT EXISTS secret_santa_run_people (
    id_srp INTEGER PRIMARY KEY AUTOINCREMENT,
    id_ser INTEGER,
    id_peo INTEGER,
    id_peo_to INTEGER,
    mail_sent VARCHAR2(1),
    FOREIGN KEY (id_ser) REFERENCES secret_santa_run (id_ser) ON DELETE CASCADE,
    FOREIGN KEY (id_peo) REFERENCES people (id_peo) ON DELETE CASCADE,
    FOREIGN KEY (id_peo_to) REFERENCES people (id_peo) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_run_people_fk1 ON secret_santa_run_people(id_ser);
CREATE INDEX IF NOT EXISTS idx_run_people_fk2 ON secret_santa_run_people(id_peo);
CREATE INDEX IF NOT EXISTS idx_run_people_fk3 ON secret_santa_run_people(id_peo_to);

CREATE TABLE IF NOT EXISTS secret_santa_run_exclusion (
    id_srp INTEGER,
    id_peo INTEGER,
    FOREIGN KEY (id_srp) REFERENCES secret_santa_run_people(id_srp) ON DELETE CASCADE,
    FOREIGN KEY (id_peo) REFERENCES people (id_peo) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_run_exclusion ON secret_santa_run_exclusion(id_srp, id_peo);