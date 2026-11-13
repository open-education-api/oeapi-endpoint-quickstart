-- =============================================================================
-- oeapi-endpoint-quickstart: database migration from the OEAPI v5 build to v6
-- =============================================================================
--
-- Run ONCE on an existing (MySQL 8) database, with the application STOPPED,
-- and BEFORE the v6 build is started for the first time. If the v6 build starts
-- first, spring.jpa.hibernate.ddl-auto=update creates empty "organisation" and
-- "programme" tables next to the old ones; drop those empty tables, then run
-- this script.
--
-- TAKE A BACKUP FIRST:  mysqldump oeapi_qs > oeapi_qs_v5_backup.sql
--
-- Table and join-table names below are the ones Hibernate generates by default.
-- Check them with SHOW TABLES / SHOW COLUMNS FROM ... if your installation
-- customised the naming strategy, and skip the statements that do not apply.
-- A fresh installation does not need this script.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. British English: organization -> organisation, program -> programme
-- -----------------------------------------------------------------------------

RENAME TABLE organization TO organisation;
RENAME TABLE program TO programme;
RENAME TABLE program_person TO programme_person;
RENAME TABLE program_mode_of_delivery TO programme_mode_of_delivery;
RENAME TABLE course_program TO course_programme;

ALTER TABLE organisation
    RENAME COLUMN organization_id TO organisation_id,
    RENAME COLUMN organization_type_id TO organisation_type_id;

ALTER TABLE programme
    RENAME COLUMN program_id TO programme_id,
    RENAME COLUMN program_type TO programme_type,
    RENAME COLUMN program_level TO programme_level,
    RENAME COLUMN organization_id TO organisation_id,
    RENAME COLUMN teaching_language TO teaching_languages,
    RENAME COLUMN enrollment TO enrolment;

ALTER TABLE course
    RENAME COLUMN organization_id TO organisation_id,
    RENAME COLUMN teaching_language TO teaching_languages,
    RENAME COLUMN enrollment TO enrolment;

ALTER TABLE component
    RENAME COLUMN organization_id TO organisation_id,
    RENAME COLUMN teaching_language TO teaching_languages,
    RENAME COLUMN enrollment TO enrolment;

ALTER TABLE offering
    RENAME COLUMN organization_id TO organisation_id,
    RENAME COLUMN program_id TO programme_id,
    RENAME COLUMN program_offering_id TO programme_offering_id,
    RENAME COLUMN teaching_language TO teaching_languages;

ALTER TABLE ooapi_group RENAME COLUMN organization_id TO organisation_id;
ALTER TABLE association RENAME COLUMN issuer_organization_id TO issuer_organisation_id;

ALTER TABLE course_programme RENAME COLUMN programs_program_id TO programmes_programme_id;
ALTER TABLE programme_person RENAME COLUMN program_program_id TO programme_programme_id;
ALTER TABLE programme_mode_of_delivery RENAME COLUMN program_program_id TO programme_programme_id;

-- Offering classes: the discriminator keeps the Java class name
UPDATE offering SET offering_class = 'ProgrammeOffering' WHERE offering_class = 'ProgramOffering';

-- -----------------------------------------------------------------------------
-- 2. Enumeration values (v6 uses snake_case, extensible enumerations)
-- -----------------------------------------------------------------------------

UPDATE ooapi_unita_enumeration SET name = 'organisationType' WHERE name = 'organizationType';

-- level (course: enumeration table, programme: plain column)
UPDATE ooapi_unita_enumeration
   SET value = REPLACE(REPLACE(value, ' ', '_'), '-', '_')
 WHERE name = 'level';
UPDATE ooapi_unita_enumeration SET value = 'x-microcredential' WHERE name = 'level' AND value = 'microcredential';
UPDATE programme SET programme_level = REPLACE(REPLACE(programme_level, ' ', '_'), '-', '_');
UPDATE programme SET programme_level = 'x-microcredential' WHERE programme_level = 'microcredential';

-- learning component type
UPDATE ooapi_unita_enumeration SET value = 'lecture'           WHERE name = 'componentType' AND value = 'lecturer';
UPDATE ooapi_unita_enumeration SET value = 'independent_study' WHERE name = 'componentType' AND value = 'independent study';
UPDATE ooapi_unita_enumeration SET value = 'skills_training'   WHERE name = 'componentType' AND value = 'skills training';

-- programme type, level of qualification, qualification awarded, mode of study
UPDATE programme SET programme_type = 'programme'      WHERE programme_type = 'program';
UPDATE programme SET programme_type = 'specialisation' WHERE programme_type = 'specialization';

UPDATE programme SET level_of_qualification = 'eqf_3' WHERE level_of_qualification = 'equf_3';
UPDATE programme SET level_of_qualification = CONCAT('eqf_', level_of_qualification)
 WHERE level_of_qualification IN ('1', '2', '3', '4', '5', '6', '7', '8');
UPDATE programme SET level_of_qualification = 'nlqf_4plus' WHERE level_of_qualification = '4+';

UPDATE programme SET qualification_awarded = 'associate_degree' WHERE LOWER(qualification_awarded) = 'ad';
UPDATE programme SET qualification_awarded = 'bachelor' WHERE LOWER(qualification_awarded) IN ('ba', 'bsc', 'llb');
UPDATE programme SET qualification_awarded = 'master'   WHERE LOWER(qualification_awarded) IN ('ma', 'msc', 'llm');
UPDATE programme SET qualification_awarded = 'doctoral' WHERE LOWER(qualification_awarded) = 'phd';

UPDATE programme SET mode_of_study = 'full_time'    WHERE mode_of_study = 'full-time';
UPDATE programme SET mode_of_study = 'part_time'    WHERE mode_of_study = 'part-time';
UPDATE programme SET mode_of_study = 'dual_training' WHERE mode_of_study = 'dual';
UPDATE programme SET mode_of_study = 'self_paced'   WHERE mode_of_study = 'self-paced';

-- academic session, room, result value type
UPDATE academic_session SET academic_session_type = REPLACE(academic_session_type, ' ', '_');
UPDATE room SET room_type = REPLACE(room_type, ' ', '_');

UPDATE offering SET result_value_type = 'pass_or_fail' WHERE result_value_type = 'pass-or-fail';
UPDATE offering SET result_value_type = 'us_letter'    WHERE result_value_type = 'US letter';
UPDATE offering SET result_value_type = 'uk_letter'    WHERE result_value_type = 'UK letter';
UPDATE offering SET result_value_type = 'grade_0_100'  WHERE result_value_type = '0-100';
UPDATE offering SET result_value_type = 'grade_0_10'   WHERE result_value_type = '1-10';

-- modes of delivery and study load units
UPDATE mode_of_delivery SET name = 'coil'       WHERE name = 'x-coil_based';
UPDATE mode_of_delivery SET name = 'presential' WHERE name IN ('situated', 'on campus');
UPDATE mode_of_delivery SET name = 'online'     WHERE name = 'distance-learning';
UPDATE study_load SET study_load_unit = 'contact_time' WHERE study_load_unit = 'contacttime';

-- cost types inside the priceInformation JSON column
UPDATE offering SET price_information = REPLACE(REPLACE(price_information,
       '"STAP eligible"', '"stap_eligible"'), '"total costs"', '"total_costs"')
 WHERE price_information IS NOT NULL;

-- -----------------------------------------------------------------------------
-- 3. Teaching languages: ISO 639-2 (eng) -> RFC 5646 (en)
-- -----------------------------------------------------------------------------

UPDATE course     SET teaching_languages = CASE teaching_languages
    WHEN 'eng' THEN 'en' WHEN 'nld' THEN 'nl' WHEN 'dut' THEN 'nl' WHEN 'spa' THEN 'es'
    WHEN 'fra' THEN 'fr' WHEN 'fre' THEN 'fr' WHEN 'ita' THEN 'it' WHEN 'por' THEN 'pt'
    WHEN 'ron' THEN 'ro' WHEN 'rum' THEN 'ro' WHEN 'deu' THEN 'de' WHEN 'ger' THEN 'de'
    WHEN 'eus' THEN 'eu' WHEN 'baq' THEN 'eu' ELSE teaching_languages END;
UPDATE programme  SET teaching_languages = CASE teaching_languages
    WHEN 'eng' THEN 'en' WHEN 'nld' THEN 'nl' WHEN 'dut' THEN 'nl' WHEN 'spa' THEN 'es'
    WHEN 'fra' THEN 'fr' WHEN 'fre' THEN 'fr' WHEN 'ita' THEN 'it' WHEN 'por' THEN 'pt'
    WHEN 'ron' THEN 'ro' WHEN 'rum' THEN 'ro' WHEN 'deu' THEN 'de' WHEN 'ger' THEN 'de'
    WHEN 'eus' THEN 'eu' WHEN 'baq' THEN 'eu' ELSE teaching_languages END;
UPDATE component  SET teaching_languages = CASE teaching_languages
    WHEN 'eng' THEN 'en' WHEN 'nld' THEN 'nl' WHEN 'dut' THEN 'nl' WHEN 'spa' THEN 'es'
    WHEN 'fra' THEN 'fr' WHEN 'fre' THEN 'fr' WHEN 'ita' THEN 'it' WHEN 'por' THEN 'pt'
    WHEN 'ron' THEN 'ro' WHEN 'rum' THEN 'ro' WHEN 'deu' THEN 'de' WHEN 'ger' THEN 'de'
    WHEN 'eus' THEN 'eu' WHEN 'baq' THEN 'eu' ELSE teaching_languages END;
UPDATE offering   SET teaching_languages = CASE teaching_languages
    WHEN 'eng' THEN 'en' WHEN 'nld' THEN 'nl' WHEN 'dut' THEN 'nl' WHEN 'spa' THEN 'es'
    WHEN 'fra' THEN 'fr' WHEN 'fre' THEN 'fr' WHEN 'ita' THEN 'it' WHEN 'por' THEN 'pt'
    WHEN 'ron' THEN 'ro' WHEN 'rum' THEN 'ro' WHEN 'deu' THEN 'de' WHEN 'ger' THEN 'de'
    WHEN 'eus' THEN 'eu' WHEN 'baq' THEN 'eu' ELSE teaching_languages END;

-- -----------------------------------------------------------------------------
-- 3b. Persons, groups and memberships
-- -----------------------------------------------------------------------------

ALTER TABLE person
    RENAME COLUMN mail TO email,
    RENAME COLUMN secondary_mail TO secondary_email,
    RENAME COLUMN active_enrollment TO active_enrolment;

-- If the v6 build has ALREADY been started on this database, Hibernate has added
-- email, secondary_email, active_enrolment and gender_code next to the v5 columns,
-- the RENAMEs above fail, and every insert into person fails with
-- "Field 'active_enrollment' doesn't have a default value". In that case run this
-- block instead of the ALTER TABLE above (and skip the ADD COLUMN gender_code below):
--
--   UPDATE person SET email = COALESCE(email, mail),
--                     secondary_email = COALESCE(secondary_email, secondary_mail),
--                     active_enrolment = active_enrollment;
--   ALTER TABLE person DROP COLUMN mail, DROP COLUMN secondary_mail, DROP COLUMN active_enrollment;
--   UPDATE person SET gender_code = CASE gender WHEN 0 THEN 'm' WHEN 1 THEN 'f' WHEN 2 THEN 'x' WHEN 3 THEN 'u' END
--    WHERE gender IS NOT NULL AND gender_code IS NULL;

-- v5 kept gender as an ordinal (0=M, 1=F, 2=X, 3=U); v6 uses m, f, x, o, u, n
ALTER TABLE person ADD COLUMN gender_code VARCHAR(255);
UPDATE person SET gender_code = CASE gender WHEN 0 THEN 'm' WHEN 1 THEN 'f' WHEN 2 THEN 'x' WHEN 3 THEN 'u' END
 WHERE gender IS NOT NULL;

-- v5 "nationality" was free text in places (e.g. "Dutch"); v6 publishes ISO 3166 codes only.
-- Values that are not a 2 or 3 letter code are not published; correct them by hand if needed:
-- SELECT person_id, nationality, country_of_birth FROM person
--  WHERE LENGTH(nationality) NOT IN (2, 3) OR LENGTH(country_of_birth) NOT IN (2, 3);

-- Group members become v6 memberships (state active, role student)
CREATE TABLE IF NOT EXISTS membership (
    membership_id   VARCHAR(255) NOT NULL PRIMARY KEY,
    group_id        VARCHAR(255) NOT NULL,
    person_id       VARCHAR(255) NOT NULL,
    start_date_time DATETIME(6),
    end_date_time   DATETIME(6),
    state           VARCHAR(255),
    role            VARCHAR(255),
    consumers       TEXT,
    ext             TEXT,
    UNIQUE KEY uk_membership_group_person (group_id, person_id),
    FOREIGN KEY (group_id) REFERENCES ooapi_group (group_id),
    FOREIGN KEY (person_id) REFERENCES person (person_id)
);
INSERT IGNORE INTO membership (membership_id, group_id, person_id, state, role)
SELECT UUID(), group_id, person_id, 'active', 'student' FROM person_group;

-- Associations: v6 uses the new table offering_association (created at start-up).
-- The v5 table "association" did not store a usable link to its offering, so its
-- rows cannot be carried over; it is left in place and can be dropped (section 4).

-- Learning outcomes: nothing to run. On start-up the v6 build turns the inline
-- texts of the learning_outcomes columns (course, programme, component) into rows
-- of the new learning_outcome table and empties those columns.

-- -----------------------------------------------------------------------------
-- 4. Optional clean-up of what v6 no longer uses (education specifications,
--    sector). Hibernate ignores these columns, so they can also stay.
--    Foreign keys on education_specification_id must be dropped first:
--    SHOW CREATE TABLE course;  ALTER TABLE course DROP FOREIGN KEY <name>;
-- -----------------------------------------------------------------------------
-- ALTER TABLE course    DROP COLUMN education_specification_id;
-- ALTER TABLE programme DROP COLUMN education_specification_id, DROP COLUMN sector;
-- DROP TABLE education_specification;
-- DROP TABLE person_group;          -- after checking the membership rows
-- DROP TABLE association;           -- v5 associations, see section 3b
-- DROP TABLE news_feed;             -- news feeds are not part of OEAPI v6
-- ALTER TABLE person DROP COLUMN gender;   -- the v5 ordinal, replaced by gender_code
