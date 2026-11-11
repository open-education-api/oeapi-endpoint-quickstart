package oeapi.testingweb;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * OEAPI v6 persons, groups with memberships and course offering associations.
 */
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AssociationsAndMembershipsTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestUtil TU;

    private String organisationId;
    private String personId;
    private String courseId;
    private String offeringId;
    private String groupId;
    private String associationId;

    private String read(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/" + file)), StandardCharsets.UTF_8);
    }

    private WebTestClient.ResponseSpec send(String method, String uri, String body) {
        WebTestClient.RequestBodySpec spec = webTestClient.method(org.springframework.http.HttpMethod.valueOf(method))
                .uri(uri)
                .header("Authorization", TU.authHeaderForTest())
                .contentType(MediaType.APPLICATION_JSON);
        return body == null ? spec.exchange() : spec.bodyValue(body).exchange();
    }

    @BeforeEach
    void createData() throws Exception {
        organisationId = UUID.randomUUID().toString();
        send("POST", "/organisations", read("organisation_template.json")
                .replace("--ORG_ID_TOBEINFORMED--", organisationId)
                .replace("--ORG_CODE_TOBEINFORMED--", TU.genRandomCode()))
                .expectStatus().isOk();

        personId = UUID.randomUUID().toString();
        send("POST", "/persons", read("person_template.json")
                .replace("--PERSON_ID_TOBEINFORMED--", personId)
                .replace("--PERSON_CODE_TOBEINFORMED--", TU.genRandomCode()))
                .expectStatus().isOk();

        courseId = UUID.randomUUID().toString();
        send("POST", "/courses", TU.getPayload("course_template", "COURSE", courseId, TU.genRandomCode()))
                .expectStatus().isOk();

        offeringId = UUID.randomUUID().toString();
        send("POST", "/course-offerings", read("courseOffering_template.json")
                .replace("--OFFERING_ID_TOBEINFORMED--", offeringId)
                .replace("--OFFERING_CODE_TOBEINFORMED--", TU.genRandomCode())
                .replace("--COURSE_ID_TOBEINFORMED--", courseId))
                .expectStatus().isCreated();

        groupId = UUID.randomUUID().toString();
        associationId = UUID.randomUUID().toString();
    }

    @AfterEach
    void deleteData() {
        send("DELETE", "/course-offering-associations/" + associationId, null);
        send("DELETE", "/course-offerings/" + offeringId, null);
        send("DELETE", "/courses/" + courseId, null);
    }

    @Test
    void personHasV6Shape() {
        send("GET", "/persons/" + personId, null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.personId").isEqualTo(personId)
                .jsonPath("$.email").isEqualTo("vandamme.mcw@universiteitvanharderwijk.nl")
                .jsonPath("$.activeEnrolment").isEqualTo(false)
                .jsonPath("$.gender").isEqualTo("f")
                .jsonPath("$.countryOfBirth.iso3166-1-alpha2").isEqualTo("NL")
                .jsonPath("$.languageOfChoice[0]").isEqualTo("nl-NL")
                .jsonPath("$.mail").doesNotExist()
                .jsonPath("$.activeEnrollment").doesNotExist();
    }

    @Test
    void groupWithMembership() {
        String group = """
                {
                  "groupId": "%s",
                  "primaryCode": {"codeType": "identifier", "code": "%s"},
                  "groupType": "class",
                  "name": [{"language": "en-GB", "value": "Statistics students"}],
                  "startDateTime": "2026-09-01T00:00:00Z",
                  "organisationId": "%s"
                }
                """.formatted(groupId, TU.genRandomCode(), organisationId);

        send("PUT", "/groups/" + groupId, group).expectStatus().is2xxSuccessful();

        send("GET", "/groups/" + groupId + "?expand=organisation", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.groupId").isEqualTo(groupId)
                .jsonPath("$.startDateTime").isEqualTo("2026-09-01T00:00:00Z")
                .jsonPath("$.organisationId").isEqualTo(organisationId)
                .jsonPath("$.organisation.organisationId").isEqualTo(organisationId);

        send("PUT", "/groups/" + groupId + "/memberships/" + personId, "{\"role\": \"student\"}")
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.personId").isEqualTo(personId)
                .jsonPath("$.groupId").isEqualTo(groupId)
                .jsonPath("$.state").isEqualTo("active");

        send("PUT", "/groups/" + groupId + "/memberships/" + personId, "{\"role\": \"lecturer\", \"state\": \"cancelled\"}")
                .expectStatus().isOk();

        send("GET", "/groups/" + groupId + "/memberships", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].role").isEqualTo("lecturer")
                .jsonPath("$.items[0].state").isEqualTo("cancelled");
    }

    @Test
    void courseOfferingAssociationLifecycle() {
        String association = """
                {
                  "associationId": "%s",
                  "courseOfferingId": "%s",
                  "personId": "%s",
                  "role": "student",
                  "state": "pending",
                  "studyLoad": {"studyLoadUnit": "ects", "value": 6}
                }
                """.formatted(associationId, offeringId, personId);

        send("PUT", "/course-offering-associations/" + associationId, association)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.associationId").isEqualTo(associationId)
                .jsonPath("$.courseOfferingId").isEqualTo(offeringId)
                .jsonPath("$.studyLoad.value").isEqualTo(6.0);

        send("PATCH", "/course-offering-associations/" + associationId,
                "{\"state\": \"associated\", \"result\": {\"state\": \"completed\", \"pass\": \"passed\", \"score\": \"8\", \"resultDateTime\": \"2027-01-20T00:00:00Z\"}}")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.state").isEqualTo("associated")
                .jsonPath("$.role").isEqualTo("student")
                .jsonPath("$.result.pass").isEqualTo("passed")
                .jsonPath("$.result.resultDateTime").isEqualTo("2027-01-20T00:00:00Z");

        send("GET", "/course-offering-associations/" + associationId + "?expand=person,course_offering", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.person.personId").isEqualTo(personId)
                .jsonPath("$.courseOffering.courseOfferingId").isEqualTo(offeringId);

        send("GET", "/course-offerings/" + offeringId + "/course-offering-associations?state=associated", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1);

        send("GET", "/persons/" + personId + "/course-offering-associations", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].associationId").isEqualTo(associationId);

        send("GET", "/persons/" + personId + "/programme-offering-associations", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(0);

        // the same id is not a programme offering association
        send("GET", "/programme-offering-associations/" + associationId, null)
                .expectStatus().isNotFound();
    }
}
