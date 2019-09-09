CREATE TABLE ${TABLE_NAME}
(
  CHECK(${CHECK})
) INHERITS (${BASE_NAME});

INSERT INTO geometry_columns VALUES('','public','${TABLE_NAME}','objectspatial',${DIMENSION},${SRID},'GEOMETRY');
CREATE INDEX objecthash_${TABLE_NAME} ON ${TABLE_NAME} USING btree (md5(objectstring));
CREATE INDEX objectlanguage_${TABLE_NAME} ON ${TABLE_NAME} USING btree (objectlanguage);
CREATE INDEX objectspatial_${TABLE_NAME} ON ${TABLE_NAME} USING gist (objectspatial);
CREATE INDEX objecttsvector_${TABLE_NAME} ON ${TABLE_NAME} USING gin (objecttsvector);
CREATE INDEX objecttype_${TABLE_NAME} ON ${TABLE_NAME} USING btree (objecttype);
CREATE INDEX objecturi_${TABLE_NAME} ON ${TABLE_NAME} USING btree (objecturi);
CREATE INDEX predicate_${TABLE_NAME} ON ${TABLE_NAME} USING btree (predicate);
CREATE INDEX subject_${TABLE_NAME} ON ${TABLE_NAME} USING btree (subject);
CREATE OR REPLACE FUNCTION fulltext_trigger_${TABLE_NAME}() RETURNS trigger AS $$ begin new.objecttsvector := CASE WHEN new.objecttsvectorconfig IS NULL THEN NULL ELSE to_tsvector(new.objecttsvectorconfig::regconfig, new.objectstring) END; return new; END $$ LANGUAGE plpgsql;
CREATE TRIGGER tsvectorupdate_${TABLE_NAME} BEFORE INSERT OR UPDATE ON ${TABLE_NAME} FOR EACH ROW EXECUTE PROCEDURE fulltext_trigger_${TABLE_NAME}();