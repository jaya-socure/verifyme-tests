package com.verifyme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RestAssured port of the VerifyMe API test suite.
 * Runs against the live hosted API by default.
 */
class VerifyMeApiTest {

    private static final String DEFAULT_BASE_URL = "https://verify-me-46mk.onrender.com";

    @BeforeAll
    static void setUp() {
        String baseUrl = System.getenv("VERIFYME_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_BASE_URL;
        }
        RestAssured.baseURI = baseUrl.replaceAll("/$", "");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private static Response postVerify(Map<String, Object> payload) {
        return given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/verify")
                .then()
                .extract()
                .response();
    }

    private static Response getStatus(String requestId) {
        return given()
                .when()
                .get("/status/{requestId}", requestId)
                .then()
                .extract()
                .response();
    }

    // TC-1: Valid US adult, clean email, ssn 5678 → APPROVE
    @Test
    @DisplayName("TC-1: approves a valid US adult with clean data")
    void tc1ApprovesValidUsAdultWithCleanData() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Jane Doe");
        payload.put("email", "jane@example.com");
        payload.put("dob", "1990-05-15");
        payload.put("ssn_last4", "5678");
        payload.put("country", "US");

        Response res = postVerify(payload);

        assertEquals(200, res.statusCode());
        assertEquals("APPROVE", res.jsonPath().getString("decision"));
    }

    // TC-2: Missing dob → REJECT, reasons includes "missing_fields"
    @Test
    @DisplayName("TC-2: rejects when dob is missing")
    void tc2RejectsWhenDobIsMissing() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "John Smith");
        payload.put("email", "john@test.com");
        payload.put("ssn_last4", "1234");
        payload.put("country", "MX");

        Response res = postVerify(payload);
        List<String> reasons = res.jsonPath().getList("reasons", String.class);

        assertEquals(200, res.statusCode());
        assertEquals("REJECT", res.jsonPath().getString("decision"));
        assertTrue(reasons.contains("missing_fields"), "reasons should include missing_fields");
    }

    // TC-3: Email domain test.com, all other fields valid → not APPROVE
    @Test
    @DisplayName("TC-3: does not approve disposable email domain")
    void tc3DoesNotApproveDisposableEmailDomain() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Alice Test");
        payload.put("email", "alice@test.com");
        payload.put("dob", "1985-03-20");
        payload.put("ssn_last4", "9999");
        payload.put("country", "DE");

        Response res = postVerify(payload);

        assertNotEquals("APPROVE", res.jsonPath().getString("decision"));
    }

    // TC-4: POST /verify then GET /status/:requestId → same decision
    @Test
    @DisplayName("TC-4: status endpoint returns the same decision")
    void tc4StatusEndpointReturnsTheSameDecision() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Bob Builder");
        payload.put("email", "bob@company.org");
        payload.put("dob", "1988-11-02");
        payload.put("ssn_last4", "4321");
        payload.put("country", "GB");

        Response verifyRes = postVerify(payload);
        String requestId = verifyRes.jsonPath().getString("requestId");
        String verifyDecision = verifyRes.jsonPath().getString("decision");

        Response statusRes = getStatus(requestId);

        assertEquals(200, statusRes.statusCode());
        assertEquals(verifyDecision, statusRes.jsonPath().getString("decision"));
    }

    // TC-5: GET /status with nonexistent requestId → 404
    @Test
    @DisplayName("TC-5: returns 404 for nonexistent requestId")
    void tc5Returns404ForNonexistentRequestId() {
        Response res = getStatus("nonexistent-id-12345");
        assertEquals(404, res.statusCode());
    }
}
