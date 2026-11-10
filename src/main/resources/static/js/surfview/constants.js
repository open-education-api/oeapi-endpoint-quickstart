const Constants = {};

Constants.pageSize = 25;
Constants.loadPageSize = 100;
Constants.offeringConsumer = {
    consumerKey: 'eduxchange',
    allianceNames: ['ewuu', 'lde', 'euroteq', 'kom'],
    defaultAllianceName: 'kom'
};
Constants.booleanOptions = [
    ['true', 'Yes'],
    ['false', 'No']
];
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
    organisations: {
        title: 'All organisations',
        noun: 'organisations',
        endpoint: '/organisations',
        kind: 'organisation',
        idField: 'organisationId',
        detailEndpoint: id => `/organisations/${encodeURIComponent(id)}`,
        columns: [
            ['Name', entity => entityLinkValue('organisation', entity)],
            ['Code', entity => codeValue(entity.primaryCode)],
            ['Short name', entity => entity.shortName],
            ['Type', entity => entity.organisationType],
            ['City', entity => entity.addresses?.[0]?.city || '']
        ]
    },
    academicSessions: {
        title: 'All academic sessions',
        noun: 'academic sessions',
        endpoint: '/academic-sessions',
        kind: 'academicSession',
        idField: 'academicSessionId',
        detailEndpoint: id => `/academic-sessions/${encodeURIComponent(id)}`,
        columns: [
            ['Name', entity => entityLinkValue('academicSession', entity)],
            ['Code', entity => codeValue(entity.primaryCode)],
            ['Type', entity => entity.academicSessionType],
            ['Start date', entity => entity.startDate],
            ['End date', entity => entity.endDate]
        ]
    },
    programmes: {
        title: 'All programmes',
        noun: 'programmes',
        endpoint: '/programmes',
        kind: 'programme',
        idField: 'programmeId',
        detailEndpoint: id => `/programmes/${encodeURIComponent(id)}`,
        offeringsEndpoint: id => `/programmes/${encodeURIComponent(id)}/programme-offerings`,
        columns: [
            ['Name', entity => entityLinkValue('programme', entity)],
            ['Code', entity => codeValue(entity.primaryCode)],
            ['Level', entity => entity.level],
            ['Study load', entity => studyLoadValue(entity.studyLoad)]
        ]
    },
    courses: {
        title: 'All courses',
        noun: 'courses',
        endpoint: '/courses',
        kind: 'course',
        idField: 'courseId',
        detailEndpoint: id => `/courses/${encodeURIComponent(id)}`,
        offeringsEndpoint: id => `/courses/${encodeURIComponent(id)}/course-offerings`,
        columns: [
            ['Name', entity => entityLinkValue('course', entity)],
            ['Code', entity => codeValue(entity.primaryCode)],
            ['Level', entity => entity.level],
            ['Study load', entity => studyLoadValue(entity.studyLoad)]
        ]
    }
};
Constants.enumOptions = {
    personAffiliations: [
        ['student', 'Student'],
        ['employee', 'Employee'],
        ['guest', 'Guest']
    ],
    organisationType: [
        ['root', 'Root'],
        ['institute', 'Institute'],
        ['department', 'Department'],
        ['faculty', 'Faculty'],
        ['branch', 'Branch'],
        ['academy', 'Academy'],
        ['school', 'School']
    ],
    teachingLanguage: [
        ['en', 'English'],
        ['nl', 'Dutch'],
        ['es', 'Spanish'],
        ['eu', 'Basque'],
        ['de', 'German'],
        ['fr', 'French'],
        ['ar', 'Arabic'],
        ['zh', 'Chinese'],
        ['hr', 'Croatian'],
        ['it', 'Italian'],
        ['ja', 'Japanese'],
        ['no', 'Norwegian'],
        ['pap', 'Papiamento'],
        ['pl', 'Polish'],
        ['pt', 'Portuguese'],
        ['ro', 'Romanian'],
        ['ru', 'Russian'],
        ['sv', 'Swedish'],
        ['tr', 'Turkish']
    ],
    modeOfDelivery: [
        ['presential', 'Presential'],
        ['online', 'Online'],
        ['hybrid', 'Hybrid'],
        ['blended', 'Blended'],
        ['coil', 'COIL'],
        ['joint_delivery', 'Joint delivery'],
        ['project_based', 'Project based'],
        ['research_lab_based', 'Research lab based'],
        ['work_based', 'Work based']
    ],
    costType: [
        ['total_costs', 'Total costs'],
        ['stap_eligible', 'STAP eligible']
    ],
    addressType: [
        ['postal', 'Postal'],
        ['visit', 'Visit'],
        ['deliveries', 'Deliveries'],
        ['invoicing', 'Invoicing'],
        ['teaching', 'Teaching']
    ],
    resultValueType: [
        ['pass_or_fail', 'Pass or fail'],
        ['insufficient_satisfactory_good', 'Insufficient / satisfactory / good'],
        ['us_letter', 'US letter'],
        ['uk_letter', 'UK letter'],
        ['de_grade', 'German grade'],
        ['grade_0_100', '0-100'],
        ['grade_0_10', '0-10'],
        ['grade_0_10_one_decimal', '0-10 (one decimal)'],
        ['reference_level_europass', 'Europass reference level'],
        ['other', 'Other']
    ],
    codeType: [
        ['identifier', 'Identifier'],
        ['uuid', 'UUID'],
        ['institution_code', 'Institution code'],
        ['programme_code', 'Programme code'],
        ['component_code', 'Component code'],
        ['offering_code', 'Offering code'],
        ['organisation_id', 'Organisation id'],
        ['national_identity_number', 'National identity number']
    ],
    academicSessionType: [
        ['academic_year', 'Academic year'],
        ['semester', 'Semester'],
        ['trimester', 'Trimester'],
        ['quarter', 'Quarter'],
        ['testing_period', 'Testing period'],
        ['period', 'Period']
    ],
    programmeType: [
        ['programme', 'Programme'],
        ['minor', 'Minor'],
        ['honours', 'Honours'],
        ['specialisation', 'Specialisation'],
        ['track', 'Track'],
        ['specification', 'Specification']
    ],
    level: [
        ['pre_vocational', 'Pre-vocational'],
        ['secondary_vocational_education', 'Secondary vocational education'],
        ['secondary_vocational_education_1', 'Secondary vocational education 1'],
        ['secondary_vocational_education_2', 'Secondary vocational education 2'],
        ['secondary_vocational_education_3', 'Secondary vocational education 3'],
        ['secondary_vocational_education_4', 'Secondary vocational education 4'],
        ['associate_degree', 'Associate degree'],
        ['bachelor', 'Bachelor'],
        ['master', 'Master'],
        ['doctoral', 'Doctoral'],
        ['post_doctoral', 'Post-doctoral'],
        ['undefined', 'Undefined'],
        ['undivided', 'Undivided'],
        ['nt2_1', 'NT2-1'],
        ['nt2_2', 'NT2-2']
    ],
    studyLoadType: [
        ['contact_time', 'Contact time'],
        ['ects', 'ECTS'],
        ['sbu', 'SBU'],
        ['sp', 'SP'],
        ['hour', 'Hour']
    ]
};

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
