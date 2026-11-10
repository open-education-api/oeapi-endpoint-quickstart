# Migrating oeapi-endpoint-quickstart from OEAPI v5 to v6

Status: analysis (branch `spec_v6`, base commit `c05f247`)
Sources: v6 spec `https://oeapi.eu/specification/v6.0/oeapi-6.0-rc.3.json` (OpenAPI 3.1.1, info.version `6.0-rc.3`,
79 paths, 114 schemas), "Major changes" page `https://oeapi.eu/v6.0/#/technical/major-changes`,
versioning and error pages of the v6 docs, and the current code base (which implements v5.0.0).

> v6 is still a **release candidate** (rc.3). Field names below are taken from rc.3 and may still move before 6.0 final.

---

## 1. Cross-cutting changes (affect every resource)

| # | Topic | v5 (what the code does today) | v6 | Where in the code |
|---|-------|-------------------------------|----|-------------------|
| 1 | Spelling | `organization`, `program`, `enrollment` | British English: `organisation`, `programme`, `enrolment`, `specialisation`… (paths, property names **and** enum values) | everywhere |
| 2 | Media type | `application/json` | `application/vnd.oeapi+json` for success, `application/problem+json` for errors | every `produces = "application/json"` in `controller/*` |
| 3 | Version negotiation | none (only `X-Oeapi-Quickstart-Version` header) | Client sends `Accept: application/vnd.oeapi+json;version=6.0[;consumer=x;consumer-version=y]`; server echoes the served version in `Content-Type`; minor fallback allowed, major never; otherwise **406** with `ProblemVersionNotAcceptable {requestedVersion, supportedVersions[]}` | new filter next to `VersionHeaderFilter.java` |
| 4 | Errors | `oeapiError {status,title,detail}` | RFC 7807 Problem: `type` (uri, **required**), `title`, `status` (int), `detail`, `instance`; new **409 Conflict** on POST/PUT/PATCH | `oeapiError`, `oeapiAplicationExceptionHandler`, `oeapiException` |
| 5 | References / expand | one property is *either* an id *or* the expanded object (`"organization": "uuid"` / `{…}`), implemented with `xxxObject` swap in `oeapiExpandableMapper` | **two** properties: `organisationId` (uuid) **and** `organisation` (object, only when expanded). Same pattern for `parentId/parent`, `childIds/children`, `coordinatorIds/coordinators`, `programmeIds/programmes`, `courseId/course`, `academicSessionId/academicSession`, `roomIds/rooms`, `learningOutcomeIds/learningOutcomes`… | `oeapiExpandableMapper`, all `*DTO` |
| 6 | Dates | `format: date` (`startDate`, `endDate`, `validFrom`…) | `format: date-time` and usually renamed: `startDateTime`, `endDateTime`, `flexibleEntryPeriodStartDateTime`, `firstStartDateTime` (programme); `validFrom/validTo` keep names but become date-time | `LocalDate` fields in model + DTO, `oeapiDTOMapper` converters |
| 7 | Language | `teachingLanguage: "eng"` (ISO 639-2, single) | `teachingLanguages: ["en-GB"]` (RFC 5646, array, minItems 1). `LanguageTypedString.language` also RFC 5646 | `oeapiEducation`, `Offering`, DTOs, `ooapi.config.defaultTeachingLanguage=eng` |
| 8 | Mode of delivery | `modeOfDelivery` (list, values like `x-coil_based`) | `modesOfDelivery` (array of strings): `blended, coil, hybrid, joint_delivery, online, presential, project_based, research_lab_based, work_based` | `ModeOfDelivery` entity + enum yml |
| 9 | Consumers | `consumers: [Consumer…]` | `consumer: Consumer` (single, nullable) selected with `?consumer=` | every entity with `List<Consumer> consumers` |
| 10 | Study load | single `{studyLoadUnit, value}`, units `contacttime, ects, sbu, sp, hour` | `studyLoad: [StudyLoadDescriptor]`, units `contact_time, ects, sbu, sp, hour` | `StudyLoad`, `Course`, `Program` |
| 11 | Learning outcomes | `List<List<LanguageTypedString>>` inline | first-class resource `LearningOutcome` (`/learning-outcomes`), referenced by `learningOutcomeIds` / `learningOutcomes` | `oeapiEducation.learningOutcomes` |
| 12 | Enumerations | closed enums, values with spaces (`"academic year"`, `"lecture room"`) | **extensible** enums, snake_case values (`academic_year`, `lecture_room`) | `schemas/enum/*.yml`, `oeapiEnumConversionService`, `@ValidEnumYaml` |
| 13 | Pagination | `pageNumber` 0-based internally, default `pageSize=100`, `hasPrevious/NextPage` partly commented out, `ext` | `pageSize` ∈ {10,20,50,100,250} default 10, `pageNumber` ≥ 1; required: `pageSize, pageNumber, hasPreviousPage, hasNextPage, items`; `totalPages` optional | `oeapiResponse`, `oeapiRequestParam` |
| 14 | Query parameters | `q`, `sort`, `primaryCode`, single-field filters (`level`, `teachingLanguage`…) | `q`, `primaryCode`, `consumer`, `fields` (sparse fieldsets), `filter_query[field][op]=value` (deepObject; ops `is,in,not_in,like,not_like,any_in_array,all_in_array,gt_/lt_int,float,date`, plus `__or`), `expand`, `returnTimelineOverrides`. **No `sort`** | `controller/requestparameters/*` |
| 15 | New common props | – | `supplementaryInformation[] {role,type,value}`, `instructorIds/instructors`, `timelineOverrides` (Course/Programme, `returnTimelineOverrides=true`) | DTOs |
| 16 | Service document `GET /` | `contactEmail, specification, documentation, consumers, ext` | `contactEmail`*, `specification`* (uri), `documentation`, `supportedConsumers[{consumerKey,version}]`, `supportedOperations[{verbs,path}]`, `supportedExpands[{expandableObjects,path}]`, `ext` | `Metadata`, `MetadataController`, `application.properties` |
| 17 | Security | `securitySchemes` in spec | removed from spec (implementation concern) | nothing to do; keep `ApplicationSecurity` |
| 18 | Address | `countryCode` string | `countryCode: Country {iso3166-1-alpha2, iso3166-1-alpha3, iso3166-2, iso3166-3}`; `addressType` adds `deliveries, invoicing, teaching`; `geolocation{latitude,longitude}` | `Address`, `address.yml` |
| 19 | Cost | `costType` `"STAP eligible","total costs"`, numeric amount | `costType` `stap_eligible, total_costs`; `amount`, `vatAmount`, `amountWithoutVat` as decimal **strings**; `currency`; `displayAmount[]` | `Cost`, `cost.yaml` |

---

## 2. Resource / endpoint mapping

Legend: ✅ exists today · 🟡 exists, needs rename/reshape · ❌ new in v6 · 🗑 not in v6

| v5 endpoint in the code | v6 endpoint(s) | Action |
|---|---|---|
| `GET /` | `GET /` (Service) | 🟡 new Service shape |
| `/organizations`, `/{id}` | `/organisations`, `/{organisationId}`, **`PUT /{id}`** | 🟡 rename |
| `/organizations/{id}/programs` · `/courses` · `/components` · `/groups` | `/organisations/{id}/programmes` · `/courses` · `/learning-components` · `/test-components` · `/groups` | 🟡 + ❌ test-components |
| `/organizations/{id}/course-offerings`, `/program-offerings`, `/learning-component-offerings` (stubs returning 404) | `/organisations/{id}/course-offerings`, `/programme-offerings`, `/learning-component-offerings`, `/test-component-offerings` | 🟡 implement |
| `/organizations/{id}/education-specifications` | – | 🗑 |
| `/programs`, `/{id}`, `/{id}/courses`, `/{id}/offerings` | `/programmes`, `/{id}` (`ProgrammeExpanded`), `/{id}/courses`, `/{id}/programme-offerings`, **`/{id}/programmes`** (children) | 🟡 + ❌ |
| `/courses`, `/{id}`, `/{id}/offerings`, `/{id}/components` | `/courses`, `/{id}` (`CourseExpanded`), `/{id}/course-offerings`, `/{id}/learning-components`, `/{id}/test-components`, `/{id}/learning-component-offerings`, `/{id}/test-component-offerings` | 🟡 + ❌ |
| `/learning-components` (already v6 path), `/{id}/learning-component-offerings` | same | 🟡 reshape body |
| – | `/test-components`, `/{id}`, `/{id}/test-component-offerings` | ❌ |
| `/offerings` (generic, `offeringType` discriminator), `POST /offerings/courseOffering`, `/programOffering` | **no generic endpoint**. `GET/PUT/PATCH /course-offerings/{id}`, `/programme-offerings/{id}`, `/learning-component-offerings/{id}`, `/test-component-offerings/{id}` + sub-resources `/groups`, `/…-associations`, `/learning-component-offerings`, `/test-component-offerings` | 🟡 split |
| `/academic-sessions`, `/{id}`, `/{id}/offerings` | `/academic-sessions`, `/{id}`, `/{id}/course-offerings`, `/programme-offerings`, `/learning-component-offerings`, `/test-component-offerings` | 🟡 split |
| `/associations/{id}`, `POST /associations/external/me` | `GET/PUT/PATCH /course-offering-associations/{id}`, `/programme-offering-associations/{id}`, `/learning-component-offering-associations/{id}`, `/test-component-offering-associations/{id}`; `POST /course-offering-associations/external/me`, `POST /programme-offering-associations/external/me`; attempts: `/test-component-offering-associations/{id}/test-component-offering-association-attempts`, `/test-component-offering-associations-attempt/{id}`, `…/url` | 🟡 split + ❌ |
| `/persons`, `/me`, `/{id}`, `POST`, `PUT /{id}` | same (+ `filter_query`, `affiliations`) | 🟡 reshape body |
| `/persons/{id}/associations` | `/persons/{id}/course-offering-associations`, `/programme-…`, `/learning-component-…`, `/test-component-offering-associations` | 🟡 split |
| `/persons/{id}/groups` | – (use `/groups/{id}/memberships`) | 🗑 |
| `/groups`, `/{id}`, `/{id}/persons` | `/groups`, `/{id}`, **`PUT /{id}`**, `/{id}/memberships`, **`PUT /{id}/memberships/{personId}`** | 🟡 + ❌ Membership |
| `/buildings`, `/{id}`, `/{id}/rooms` · `/rooms`, `/{id}` | same | 🟡 reshape (ids are currently `WRITE_ONLY`, so never returned!) |
| – | `/learning-outcomes`, `/{id}` | ❌ |
| – | `/documents/{documentId}` | ❌ (optional) |
| `/education-specifications/**` | – | 🗑 (kept in v6 conceptual model only) |
| `/news-feeds`, `/news-item` | – | 🗑 |
| `POST/PUT/DELETE /courses`, `/programs`, `/organizations`, `/loadbyjson`, `/{id}/update`, `/offerings/deleteByCourseId` | not in spec (v6 writes: PUT/PATCH offerings & associations, PUT organisations/groups/persons, POST persons) | keep as **implementation extensions** (admin/loading API used by the SURF view UI) |

---

## 3. Schema changes per entity (code field → v6 property)

### Organisation (`Organization`, `OrganizationDTO`)
- `organizationId` → `organisationId`; `organizationType` → `organisationType` (values unchanged).
- `parent` (id) → `parentId` + `parent`; `children` (ids) → `childIds` + `children`; **new** `rootId/root`.
- `consumers[]` → `consumer`.

### Programme (`Program`, `ProgramDTO`)
- `programId` → `programmeId`; `programType` values: `program`→`programme`, `specialization`→`specialisation`.
- `educationSpecification` removed; its data now lives on the programme (`formalDocument`, `levelOfQualification`, `fieldsOfStudy` ≤6 chars, `studyLoad[]`, `learningOutcomeIds`).
- `sector` **removed**. `level` values become snake_case (`associate_degree`, `secondary_vocational_education_1`, `nt2_1`…; `microcredential` gone, `pre_vocational`, `post_doctoral` added).
- `levelOfQualification`: `eqf_0…eqf_8`, `nlqf_4plus` (drop `"1".."8"`, typo `equf_3`).
- `qualificationAwarded`: `diploma, vocational_diploma, certificate, associate_degree, bachelor, master, doctoral, none` (v5 used `ba, bsc, msc…`).
- new: `qualificationDesignations[]`, `assessment[]`, `modesOfDelivery[]`, `duration`, `firstStartDateTime`, `instructorIds`, `supplementaryInformation`, `timelineOverrides`.
- `parent/children` → `parentId/parent`, `childIds/children`; `coordinators` → `coordinatorIds/coordinators`; `organization` → `organisationId/organisation`.
- `enrollment` → `enrolment`.

### Course (`Course`, `CourseDTO`)
- `programs` → `programmeIds/programmes`; `coordinators` → `coordinatorIds/coordinators`; new `instructorIds/instructors`.
- `educationSpecification` and `sector` removed; `level` snake_case values; `fieldsOfStudy` string ≤6.
- `firstStartDate` stays named `firstStartDate` but becomes date-time.
- `teachingLanguage` → `teachingLanguages[]`, `studyLoad` → array, `enrollment` → `enrolment`.

### LearningComponent / TestComponent (`Component`, `ComponentDTO`)
- `Component` becomes `LearningComponent` (`componentType` ∈ `consultation, excursion, external, independent_study, learning_community, lecture, practical, project, skills_training, tutorial, workshop`; v5 had `lecturer`, `independent study`, `skills training`).
- **new** `TestComponent` (`componentType` ∈ `unknown, test_on_paper, digital_test, life_skills_test, oral_test, portfolio_assessment`; `resultValueType`, `attempts`, `passFrom`, `state`, `extraDuration`).
- `course` → `courseId/course`; new `parentId/childIds` hierarchy; `organisationId`.

### Offerings (`Offering` + `CourseOffering`/`ProgramOffering`/`ComponentOffering`)
The single-table inheritance already matches v6 well; what changes is the JSON:
- `offeringId` + `offeringType` → typed ids `courseOfferingId`, `programmeOfferingId`, `learningComponentOfferingId`, `testComponentOfferingId`; no discriminator in the payload.
- `startDate/endDate` → `startDateTime/endDateTime`; `flexibleEntryPeriodStart/End` → `flexibleEntryPeriodStartDateTime/EndDateTime`.
- `enrollStartDate/enrollEndDate` → `enrolmentPeriods[] {startDateTime, endDateTime, targetGroups, enrolmentType, enrolmentUrl, queueEnabled, …}`.
- new: `state` (`concept, cancelled, active, inactive`), `rosteringState`, `groupIds`, `supplementaryInformation`.
- `academicSession` → `academicSessionId/academicSession`; `organization` → `organisationId/organisation`.
- CourseOffering: single `programOffering` → `programmeOfferingIds[]` (many-to-many).
- LearningComponentOffering: single `room` → `roomIds[]`; single `courseOffering` → `courseOfferingIds[]`; `resultWeight` nullable.
- ProgrammeOffering: `priceInformation` minItems 1.
- New TestComponentOffering (`documents[]`).

### AcademicSession
- `startDate/endDate` → `startDateTime/endDateTime` (both **required**); `parent`, `year` → `parentId/parent`, `yearId/year`; new `childIds/children`, `abbreviation`, `otherCodes`.
- `academicSessionType` values snake_case.

### Person
- `mail` → `email`, `secondaryMail` → `secondaryEmail`, `activeEnrollment` → `activeEnrolment` (**required**).
- `countryOfBirth`, `nationality` become objects (`Country`, `Nationality`).
- new: `preferredName`, `alternateName`, `surnamePrefix`, `idCheckName`, `languageOfChoice[]`, `assignedNeeds[]`, `address`, `otherCodes`.
- At least one of `surname`/`givenName`/`preferredName` + `primaryCode` + `activeEnrolment`.

### Group / Membership
- `startDate/endDate` → date-time; `organisationId`, `academicSessionId`, `offeringIds[]`.
- `members` (ManyToMany Person) → `Membership {personId, groupId, state(active|cancelled), role, startDateTime, endDateTime}`.

### Associations (`Association`)
- generic `Association` + `associationType` → 4 types with `associationId` and common `AssociationProperties {role, state, remoteState, startDateTime, expectedEndDateTime, actualEndDateTime}`.
- `associationRole` → `role` (values snake_case: `teaching_assistant`), states `cancelled` (British, v5 `canceled`).
- Result per type (`CourseOfferingAssociationResult` requires `studyLoad`; component ones carry `weight`), `Result.resultDate` → `resultDateTime`, `resultState` → `state`, new `rawScore`, `maxRawScore`, `final`, `assessorId`, `documents`.
- Attempts for test-component associations (new).

### Room / Building
- `roomId`/`buildingId` are currently `@JsonProperty(access = WRITE_ONLY)` → must be readable (required in v6).
- Room: `building` → `buildingId/building`; `roomType` snake_case.

---

## 4. Enumerations to replace (`src/main/resources/schemas/enum`)

| file | v6 values |
|---|---|
| `modeOfDeliveryType` | blended, coil, hybrid, joint_delivery, online, presential, project_based, research_lab_based, work_based |
| `studyLoadType` | contact_time, ects, sbu, sp, hour |
| `componentType` → `learningComponentType` | consultation, excursion, external, independent_study, learning_community, lecture, practical, project, skills_training, tutorial, workshop |
| new `testComponentType` | unknown, test_on_paper, digital_test, life_skills_test, oral_test, portfolio_assessment |
| `programType` | programme, minor, honours, specialisation, track, specification |
| `levelType` | pre_vocational, secondary_vocational_education[_1.._4], associate_degree, bachelor, master, doctoral, post_doctoral, undefined, undivided, nt2_1, nt2_2 |
| `levelOfQualificationType` | eqf_0 … eqf_8, nlqf_4plus |
| `qualificationAwardedType` | diploma, vocational_diploma, certificate, associate_degree, bachelor, master, doctoral, none |
| `formalDocumentType` | certificate, diploma, micro_credential_certificate, school_advice, testimonial, no_official_document |
| `academicSessionType` | academic_year, semester, trimester, quarter, testing_period, period |
| `costType` | stap_eligible, total_costs |
| `roomType` | general_purpose, lecture_room, computer_room, laboratory, office, workspace, exam_location, study_room, examination_room, conference_room |
| `associationRole` | student, lecturer, teaching_assistant, coordinator, invigilator, assessor, guest |
| `state` (association) | pending, cancelled, denied, associated, queued, finished |
| `remoteState` | pending, cancelled, denied, associated, queued |
| new | offeringState, rosteringState, resultState, passState, componentState, attemptState, attendance, membershipState, membershipRole, groupType (class, team, group), addressType, codeType (+`organisation_id`, `programme_code`…), supplementaryRole/Type, documentType, personalNeed, learningOutcomeLevel |
| drop | `offeringType`, `sectorType`, `educationSpecificationType`, `newsFeedType` |

The existing `*_ooapiV6.yml` files were drafted against an earlier v6 draft and are **out of date**
(e.g. `programType_ooapiV6.yml` still says `program`, `specialization`).

---

## 5. Impact on persistence

Entity/table names (`organization`, `program`, `organization_id` columns, enumeration rows) do not have to change: v6 is an API
contract. Two strategies:

- **A. API-layer migration (recommended)** – keep JPA entities and tables, rename only JSON properties in DTOs and add
  the new columns needed (e.g. `offering.state`, `teaching_languages`, `enrolment_periods`, join tables for
  `programmeOfferingIds`/`roomIds`, `learning_outcome`, `test_component`, `membership`). Hibernate `ddl-auto=update` can add
  columns; enum values stored in `oeapi_unita_enumeration` need a one-off data migration (`"academic year"` → `academic_year` …).
- **B. Full rename** – rename entities, tables and columns to British English. Cleaner, but requires SQL migration
  scripts for existing installations and touches every repository/service/test.

## 6. Impact outside Java

- SURF view UI (`static/js/surfview/*.js`, `constants.js`, `oeapi.js`) calls `/organizations`, `/programs`, `/offerings`,
  `/courses/{id}/offerings`, `POST /offerings/programOffering` and reads v5 property names.
- Seed files `1-Organization.json` … `4-Offering.json`, `program-demo04*.json`, `examples/*.json`, `docs/kom/**/*.http|json`.
- JSON-schema validation files `schemas/*.yml` (one per entity) used by `oeapiObjectsValidator`.
- Tests in `src/test/java/oeapi/testingweb/*` hard-code v5 paths and payloads.
- README / `application.properties` (`ooapi.metadata.*`, `defaultTeachingLanguage=eng`).

## 7. Suggested order of work

1. Infrastructure: media type + version negotiation filter (406), Problem+json errors, Pagination, Service document.
2. Enumerations (v6 YAMLs + DB value migration).
3. Organisation → Programme → Course → LearningComponent (rename + id/object split + new fields).
4. Offerings split into the four typed controllers; AcademicSession sub-resources.
5. Persons, Groups + Membership, Rooms/Buildings.
6. New: TestComponent(+Offering), LearningOutcome, typed Associations (+attempts).
7. Query features: `filter_query`, `fields`, `expand` per spec `supportedExpands`.
8. UI, seed data, docs, tests.
