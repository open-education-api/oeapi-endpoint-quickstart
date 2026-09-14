package oeapi.testingweb;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.AfterAll;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CourseComposeEntitiesTest {

    Logger logger = LoggerFactory.getLogger(CourseComposeEntitiesTest.class);

    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired    
    private TestUtilCUDRest TUCudRest;    
    
    @Autowired    
    private TestUtilGetRest TUGetRest;      
    
    String restResource = "courses";
    String templateAbrev = "COURSE";
    String entity = "course";
    
    @Test
    void whenCreateCourse_CheckComponents() throws IOException {

        String randomId = TUCudRest.whenPost_testOk(restResource, entity, templateAbrev, webTestClient);
        // Testing Components
        logger.info("Course [" + randomId + "] load...");
      // Spec v6  String uri = "/" + restResource + "/" + randomId + "/learning-components";
        String uri = "/" + restResource + "/" + randomId + "/components";
        logger.info("Testing components ..." + uri);
        TUGetRest.getPages_testTotalPages(uri, webTestClient);
        
        // Deleting course created for test                
        TUCudRest.delete_test(restResource, randomId, webTestClient);


    }

    @Test
    void whenCreateCourse_CheckOfferings() throws IOException {

        String randomId = TUCudRest.whenPost_testOk(restResource, entity, templateAbrev, webTestClient);
        logger.info("Course [" + randomId + "] load...");
        // Spec v6   String uri = "/" + restResource + "/" + randomId + "/course-offerings";
        String uri = "/" + restResource + "/" + randomId + "/offerings";
        logger.info("Testing offerings ..." + uri);
        TUGetRest.getPages_testTotalPages(uri, webTestClient);

        // Deleting course created for test                
        TUCudRest.delete_test(restResource, randomId, webTestClient);
        
    }


    /**
     *
     * This runs even when a test fails, and it never asserts - teardown must not turn a
     * passing run red.
     */
    @AfterAll
    void cleanUpCreatedData() {
        TUCudRest.cleanupCreated(webTestClient);
    }

}
