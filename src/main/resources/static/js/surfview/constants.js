const Constants = {};

Constants.pageSize = 25;
Constants.loadPageSize = 100;
Constants.offeringConsumer = {
    consumerKey: 'eduxchange',
    allianceNames: ['ewuu', 'lde', 'euroteq', 'kom'],
    defaultAllianceName: 'kom'
};
Constants.tabs = {
    persons: {
        title: 'All persons',
        noun: 'persons',
        endpoint: '/persons',
        kind: 'person',
        idField: 'personId',
        detailEndpoint: id => `/persons/${encodeURIComponent(id)}`,
        columns: [
            ['Name', entity => entityLinkValue('person', entity)],
            ['Code', entity => codeValue(entity.primaryCode)],
            ['Email', entity => entity.mail],
            ['Affiliations', entity => Array.isArray(entity.affiliations) ? entity.affiliations.join(', ') : ''],
            ['Active enrollment', entity => entity.activeEnrollment ? 'Yes' : 'No']
        ]
    },
    organizations: {
        title: 'All organizations',
        noun: 'organizations',
        endpoint: '/organizations',
        kind: 'organization',
        idField: 'organizationId',
        detailEndpoint: id => `/organizations/${encodeURIComponent(id)}`,
        columns: [
            ['Name', entity => entityLinkValue('organization', entity)],
            ['Code', entity => codeValue(entity.primaryCode)],
            ['Short name', entity => entity.shortName],
            ['Type', entity => entity.organizationType],
            ['City', entity => entity.addresses?.[0]?.city || '']
        ]
    },
    academicSessions: {
        title: 'All academic sessions',
        noun: 'academic sessions',
        endpoint: '/academic-sessions',
        columns: [
            ['Name', entity => textValue(entity.name)],
            ['Code', entity => codeValue(entity.primaryCode)],
            ['Type', entity => entity.academicSessionType],
            ['Start date', entity => entity.startDate],
            ['End date', entity => entity.endDate]
        ]
    },
    programs: {
        title: 'All programs',
        noun: 'programs',
        endpoint: '/programs',
        kind: 'program',
        idField: 'programId',
        detailEndpoint: id => `/programs/${encodeURIComponent(id)}`,
        offeringsEndpoint: id => `/programs/${encodeURIComponent(id)}/offerings?pageSize=100&pageNumber=1`,
        columns: [
            ['Name', entity => entityLinkValue('program', entity)],
            ['Code', entity => codeValue(entity.primaryCode)],
            ['Level', entity => entity.level],
            ['Study load', entity => studyLoadValue(entity.studyLoad)],
            ['Nr offerings', entity => offeringCountCell('program', entity)]
        ]
    },
    courses: {
        title: 'All courses',
        noun: 'courses',
        endpoint: '/courses',
        kind: 'course',
        idField: 'courseId',
        detailEndpoint: id => `/courses/${encodeURIComponent(id)}`,
        offeringsEndpoint: id => `/courses/${encodeURIComponent(id)}/offerings?pageSize=100&pageNumber=1`,
        columns: [
            ['Name', entity => entityLinkValue('course', entity)],
            ['Code', entity => codeValue(entity.primaryCode)],
            ['Level', entity => entity.level],
            ['Study load', entity => studyLoadValue(entity.studyLoad)],
            ['Nr offerings', entity => offeringCountCell('course', entity)]
        ]
    }
};
Constants.enumOptions = {
    personAffiliations: [
        ['student', 'Student'],
        ['employee', 'Employee'],
        ['guest', 'Guest']
    ],
    organizationType: [
        ['root', 'Root'],
        ['institute', 'Institute'],
        ['department', 'Department'],
        ['faculty', 'Faculty'],
        ['branch', 'Branch'],
        ['academy', 'Academy'],
        ['school', 'School']
    ],
    teachingLanguage: [
        ['eng', 'English'],
        ['nld', 'Dutch'],
        ['spa', 'Spanish'],
        ['deu', 'German'],
        ['fra', 'French'],
        ['ara', 'Arabic'],
        ['chi', 'Chinese'],
        ['hrv', 'Croatian'],
        ['ita', 'Italian'],
        ['jpn', 'Japanese'],
        ['nor', 'Norwegian'],
        ['pap', 'Papiamento'],
        ['pol', 'Polish'],
        ['por', 'Portuguese'],
        ['ron', 'Romanian'],
        ['rus', 'Russian'],
        ['swe', 'Swedish'],
        ['tur', 'Turkish']
    ],
    modeOfDelivery: [
        ['distance-learning', 'Distance learning'],
        ['online', 'Online'],
        ['on campus', 'On campus'],
        ['hybrid', 'Hybrid'],
        ['situated', 'Situated']
    ],
    costType: [
        ['total costs', 'Total costs'],
        ['STAP eligible', 'STAP eligible']
    ],
    addressType: [
        ['postal', 'Postal'],
        ['visit', 'Visit'],
        ['deliveries', 'Deliveries'],
        ['billing', 'Billing'],
        ['teaching', 'Teaching']
    ],
    resultExpected: [
        ['true', 'Yes'],
        ['false', 'No']
    ],
    resultValueType: [
        ['pass-or-fail', 'Pass or fail'],
        ['US letter', 'US letter'],
        ['UK letter', 'UK letter'],
        ['0-100', '0-100'],
        ['1-10', '1-10']
    ],
    offeringCodeType: [
        ['identifier', 'Identifier'],
        ['otherType', 'Other type']
    ],
    academicSessionCodeType: [
        ['identifier', 'Identifier'],
        ['uuid', 'UUID']
    ],
    academicSessionType: [
        ['academic year', 'Academic year'],
        ['semester', 'Semester'],
        ['trimester', 'Trimester'],
        ['quarter', 'Quarter'],
        ['testing period', 'Testing period'],
        ['period', 'Period']
    ],
    programType: [
        ['program', 'Program'],
        ['minor', 'Minor'],
        ['honours', 'Honours'],
        ['specialization', 'Specialization'],
        ['track', 'Track'],
        ['specification', 'Specification']
    ],
    level: [
        ['secondary vocational education', 'Secondary vocational education'],
        ['secondary vocational education 1', 'Secondary vocational education 1'],
        ['secondary vocational education 2', 'Secondary vocational education 2'],
        ['secondary vocational education 3', 'Secondary vocational education 3'],
        ['secondary vocational education 4', 'Secondary vocational education 4'],
        ['associate degree', 'Associate degree'],
        ['bachelor', 'Bachelor'],
        ['master', 'Master'],
        ['doctoral', 'Doctoral'],
        ['microcredential', 'Microcredential'],
        ['undefined', 'Undefined'],
        ['undivided', 'Undivided'],
        ['nt2-1', 'NT2-1'],
        ['nt2-2', 'NT2-2']
    ],
    studyLoadType: [
        ['contacttime', 'Contact time'],
        ['ects', 'ECTS'],
        ['sbu', 'SBU'],
        ['sp hour', 'SP hour']
    ],
    sector: [
        ['secondary vocational education', 'Secondary vocational education'],
        ['higher professional education', 'Higher professional education'],
        ['university education', 'University education']
    ]
};

Constants.supportedLanguages = new Map([
    ['english', {
        label: 'English',
        languagePrefixes: ['en-'],
        teachingLanguageCodes: ['eng']
    }],
    ['dutch', {
        label: 'Dutch',
        languagePrefixes: ['nl-'],
        teachingLanguageCodes: ['nld']
    }],
    ['spanish', {
        label: 'Spanish',
        languagePrefixes: ['es-'],
        teachingLanguageCodes: ['spa']
    }]
]);

Constants.languageNames = {
    'bg-BG': 'Bulgarian',
    'hr-HR': 'Croatian',
    'cs-CZ': 'Czech',
    'da-DK': 'Danish',
    'nl-NL': 'Dutch',
    'en-GB': 'English',
    'en-IE': 'English',
    'et-EE': 'Estonian',
    'fi-FI': 'Finnish',
    'fr-FR': 'French',
    'fr-BE': 'French',
    'fr-LU': 'French',
    'de-DE': 'German',
    'de-AT': 'German',
    'de-LU': 'German',
    'el-GR': 'Greek',
    'hu-HU': 'Hungarian',
    'ga-IE': 'Irish',
    'it-IT': 'Italian',
    'lv-LV': 'Latvian',
    'lt-LT': 'Lithuanian',
    'mt-MT': 'Maltese',
    'pl-PL': 'Polish',
    'pt-PT': 'Portuguese',
    'ro-RO': 'Romanian',
    'sk-SK': 'Slovak',
    'sl-SI': 'Slovenian',
    'es-ES': 'Spanish',
    'sv-SE': 'Swedish',
    'ca-ES': 'Catalan',
    'eu-ES': 'Basque',
    'gl-ES': 'Galician'
};
