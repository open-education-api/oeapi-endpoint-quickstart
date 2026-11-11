/*
 * OEAPI v6 adapter for the SURF view.
 *
 * The dashboard code works with a flat, single-valued model (teachingLanguage,
 * organisation, parent, startDate, offeringId...). The endpoint speaks OEAPI v6
 * (teachingLanguages[], organisationId, parentId, startDateTime, typed offering
 * ids...). callEndpoint() runs every OEAPI request through toV6() and every
 * OEAPI response through fromV6(), so the rest of the dashboard is unaware of
 * the difference.
 */
const OeapiV6 = (() => {

    // Matched anywhere in the path, so an endpoint published below a prefix
    // (https://host/oeapi/courses) is recognised too. Nested paths come first.
    const RESOURCE_KINDS = [
        [/\/course-offerings(\/|$)/, 'course_offering'],
        [/\/programme-offerings(\/|$)/, 'programme_offering'],
        [/\/learning-component-offerings(\/|$)/, 'learning_component_offering'],
        [/\/academic-sessions(\/|$)/, 'academic_session'],
        [/\/organisations\/[^/]+\/programmes(\/|$)/, 'programme'],
        [/\/organisations\/[^/]+\/courses(\/|$)/, 'course'],
        [/\/programmes\/[^/]+\/courses(\/|$)/, 'course'],
        [/\/organisations(\/|$)/, 'organisation'],
        [/\/programmes(\/|$)/, 'programme'],
        [/\/courses(\/|$)/, 'course'],
        [/\/persons(\/|$)/, 'person']
    ];

    const OFFERING_ID = {
        course_offering: 'courseOfferingId',
        programme_offering: 'programmeOfferingId',
        learning_component_offering: 'learningComponentOfferingId'
    };

    function kindOf(url) {
        let path;
        try {
            path = new URL(url, window.location.origin).pathname;
        } catch (e) {
            return null;
        }
        const match = RESOURCE_KINDS.find(([pattern]) => pattern.test(path));
        return match ? match[1] : null;
    }

    const isObject = value => value !== null && typeof value === 'object' && !Array.isArray(value);
    const day = value => typeof value === 'string' && value.length >= 10 ? value.substring(0, 10) : value;
    const dateTime = value => typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value) ? `${value}T00:00:00Z` : value;

    function move(obj, from, to, convert = value => value) {
        if (Object.prototype.hasOwnProperty.call(obj, from)) {
            const value = obj[from];
            delete obj[from];
            if (value !== undefined) {
                obj[to] = convert(value);
            }
        }
    }

    // v6 Country / Nationality objects <-> the ISO code the dashboard edits
    const isoCode = value => isObject(value)
        ? (value['iso3166-1-alpha2'] || value['iso3166-1-alpha3'] || value['iso3166-3'] || '')
        : value;
    const isoObject = value => {
        if (typeof value !== 'string' || value === '') {
            return isObject(value) ? value : undefined;
        }
        return value.length === 3 ? {'iso3166-1-alpha3': value} : {'iso3166-1-alpha2': value};
    };

    // ---- v6 -> dashboard -----------------------------------------------------

    function fromV6Entity(kind, entity) {
        if (!isObject(entity)) {
            return entity;
        }
        const e = {...entity};
        if (kind === 'person') {
            move(e, 'email', 'mail');
            move(e, 'secondaryEmail', 'secondaryMail');
            move(e, 'activeEnrolment', 'activeEnrollment');
            move(e, 'countryOfBirth', 'countryOfBirth', isoCode);
            move(e, 'nationality', 'nationality', isoCode);
            return e;
        }
        move(e, 'teachingLanguages', 'teachingLanguage', list => Array.isArray(list) ? (list[0] || '') : list);
        move(e, 'modesOfDelivery', 'modeOfDelivery');
        move(e, 'enrolment', 'enrollment');
        move(e, 'organisationId', 'organisation');
        move(e, 'parentId', 'parent');
        move(e, 'childIds', 'children');
        move(e, 'coordinatorIds', 'coordinators');
        move(e, 'programmeIds', 'programmes');
        move(e, 'validFrom', 'validFrom', day);
        move(e, 'validTo', 'validTo', day);
        if (Array.isArray(e.studyLoad)) {
            e.studyLoad = e.studyLoad[0] || null;
        }
        if (kind === 'programme') {
            move(e, 'firstStartDateTime', 'firstStartDate', day);
        } else {
            move(e, 'firstStartDate', 'firstStartDate', day);
        }
        if (kind === 'academic_session') {
            move(e, 'startDateTime', 'startDate', day);
            move(e, 'endDateTime', 'endDate', day);
            move(e, 'yearId', 'year');
        }
        if (OFFERING_ID[kind]) {
            move(e, OFFERING_ID[kind], 'offeringId');
            e.offeringType = kind.replace('_offering', '').replace('learning_component', 'component');
            move(e, 'courseId', 'course');
            move(e, 'programmeId', 'programme');
            move(e, 'learningComponentId', 'component');
            move(e, 'academicSessionId', 'academicSession');
            move(e, 'startDateTime', 'startDate', day);
            move(e, 'endDateTime', 'endDate', day);
            move(e, 'flexibleEntryPeriodStartDateTime', 'flexibleEntryPeriodStart', day);
            move(e, 'flexibleEntryPeriodEndDateTime', 'flexibleEntryPeriodEnd', day);
            const periods = e.enrolmentPeriods;
            delete e.enrolmentPeriods;
            if (Array.isArray(periods) && periods.length > 0) {
                e.enrollStartDate = day(periods[0].startDateTime);
                e.enrollEndDate = day(periods[0].endDateTime);
            }
        }
        return e;
    }

    function fromV6(kind, body) {
        if (!kind || !isObject(body)) {
            return body;
        }
        if (Array.isArray(body.items)) {
            return {...body, items: body.items.map(item => fromV6Entity(kind, item))};
        }
        return fromV6Entity(kind, body);
    }

    // ---- dashboard -> v6 -----------------------------------------------------

    function toV6(kind, entity) {
        if (!kind || !isObject(entity)) {
            return entity;
        }
        const e = {...entity};
        if (kind === 'person') {
            move(e, 'mail', 'email');
            move(e, 'secondaryMail', 'secondaryEmail');
            move(e, 'activeEnrollment', 'activeEnrolment');
            move(e, 'countryOfBirth', 'countryOfBirth', isoObject);
            move(e, 'nationality', 'nationality', isoObject);
            return e;
        }
        move(e, 'teachingLanguage', 'teachingLanguages', value => value ? [value] : []);
        move(e, 'modeOfDelivery', 'modesOfDelivery');
        move(e, 'enrollment', 'enrolment');
        if (typeof e.organisation === 'string' || e.organisation === null) {
            move(e, 'organisation', 'organisationId');
        }
        if (typeof e.parent === 'string' || e.parent === null) {
            move(e, 'parent', 'parentId');
        }
        if (Array.isArray(e.children) && e.children.every(child => typeof child === 'string')) {
            move(e, 'children', 'childIds');
        }
        if (Array.isArray(e.coordinators) && e.coordinators.every(c => typeof c === 'string')) {
            move(e, 'coordinators', 'coordinatorIds');
        }
        if (Array.isArray(e.programmes) && e.programmes.every(p => typeof p === 'string')) {
            move(e, 'programmes', 'programmeIds');
        }
        move(e, 'validFrom', 'validFrom', dateTime);
        move(e, 'validTo', 'validTo', dateTime);
        delete e.sector;
        if (isObject(e.studyLoad)) {
            e.studyLoad = [e.studyLoad];
        } else if (e.studyLoad === null) {
            delete e.studyLoad;
        }
        if (kind === 'programme') {
            move(e, 'firstStartDate', 'firstStartDateTime', dateTime);
        } else {
            move(e, 'firstStartDate', 'firstStartDate', dateTime);
        }
        if (kind === 'academic_session') {
            move(e, 'startDate', 'startDateTime', dateTime);
            move(e, 'endDate', 'endDateTime', dateTime);
            if (typeof e.year === 'string') {
                move(e, 'year', 'yearId');
            }
        }
        if (OFFERING_ID[kind]) {
            move(e, 'offeringId', OFFERING_ID[kind]);
            delete e.offeringType;
            if (typeof e.course === 'string') {
                move(e, 'course', 'courseId');
            }
            if (typeof e.programme === 'string') {
                move(e, 'programme', 'programmeId');
            }
            if (typeof e.component === 'string') {
                move(e, 'component', 'learningComponentId');
            }
            if (typeof e.academicSession === 'string') {
                move(e, 'academicSession', 'academicSessionId');
            }
            move(e, 'startDate', 'startDateTime', dateTime);
            move(e, 'endDate', 'endDateTime', dateTime);
            move(e, 'flexibleEntryPeriodStart', 'flexibleEntryPeriodStartDateTime', dateTime);
            move(e, 'flexibleEntryPeriodEnd', 'flexibleEntryPeriodEndDateTime', dateTime);
            const start = e.enrollStartDate;
            const end = e.enrollEndDate;
            delete e.enrollStartDate;
            delete e.enrollEndDate;
            if (start) {
                e.enrolmentPeriods = [end ? {startDateTime: dateTime(start), endDateTime: dateTime(end)}
                                          : {startDateTime: dateTime(start)}];
            }
        }
        return e;
    }

    /** Converts a JSON request body (string) for the given URL. */
    function requestBody(url, body) {
        const kind = kindOf(url);
        if (!kind || typeof body !== 'string' || body === '') {
            return body;
        }
        try {
            const parsed = JSON.parse(body);
            const converted = Array.isArray(parsed) ? parsed.map(item => toV6(kind, item)) : toV6(kind, parsed);
            return JSON.stringify(converted);
        } catch (e) {
            return body;
        }
    }

    /** Wraps a JSON response so that response.json() answers in the dashboard model. */
    async function response(url, original) {
        const kind = kindOf(url);
        const type = original.headers.get('Content-Type') || '';
        if (!kind || !original.ok || !/json/i.test(type) || /problem/i.test(type)) {
            return original;
        }
        const text = await original.text();
        let body = text;
        try {
            body = JSON.stringify(fromV6(kind, JSON.parse(text)));
        } catch (e) {
            // not JSON after all: pass it through untouched
        }
        return new Response(body, {
            status: original.status,
            statusText: original.statusText,
            headers: original.headers
        });
    }

    return {kindOf, fromV6, toV6, requestBody, response};
})();
