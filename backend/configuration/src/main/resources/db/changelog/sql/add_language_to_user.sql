CREATE TABLE IF NOT EXISTS language (
  code TEXT PRIMARY KEY
);
INSERT INTO language VALUES ('sv'), ('en');
ALTER TABLE mvp_user ADD COLUMN language TEXT NOT NULL REFERENCES language(code) DEFAULT 'en';