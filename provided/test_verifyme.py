"""Pytest port of the VerifyMe API test suite.

Mirrors the original Jest + supertest cases (TC-1 .. TC-5) and runs
against the live hosted API by default. Override the target with the
VERIFYME_BASE_URL environment variable.
"""


# TC-1: Valid US adult, clean email, ssn 5678 -> APPROVE
def test_tc1_approves_valid_us_adult_with_clean_data(post_verify):
    res = post_verify(
        {
            "name": "Jane Doe",
            "email": "jane@example.com",
            "dob": "1990-05-15",
            "ssn_last4": "5678",
            "country": "US",
        }
    )

    assert res.status_code == 200
    assert res.json()["decision"] == "APPROVE"


# TC-2: Missing dob -> REJECT, reasons includes "missing_fields"
def test_tc2_rejects_when_dob_is_missing(post_verify):
    res = post_verify(
        {
            "name": "John Smith",
            "email": "john@test.com",
            "ssn_last4": "1234",
            "country": "MX",
        }
    )

    assert res.status_code == 200
    body = res.json()
    assert body["decision"] == "REJECT"
    assert "missing_fields" in body["reasons"]


# TC-3: Email domain test.com, all other fields valid -> not APPROVE
def test_tc3_does_not_approve_disposable_email_domain(post_verify):
    res = post_verify(
        {
            "name": "Alice Test",
            "email": "alice@test.com",
            "dob": "1985-03-20",
            "ssn_last4": "9999",
            "country": "DE",
        }
    )

    assert res.json()["decision"] != "APPROVE"


# TC-4: POST /verify then GET /status/:requestId -> same decision
def test_tc4_status_endpoint_returns_the_same_decision(post_verify, get_status):
    verify_res = post_verify(
        {
            "name": "Bob Builder",
            "email": "bob@company.org",
            "dob": "1988-11-02",
            "ssn_last4": "4321",
            "country": "GB",
        }
    )

    request_id = verify_res.json()["requestId"]
    status_res = get_status(request_id)

    assert status_res.status_code == 200
    assert status_res.json()["decision"] == verify_res.json()["decision"]


# TC-5: GET /status with nonexistent requestId -> expect 200 (hidden bug: correct is 404)
def test_tc5_returns_200_for_nonexistent_request_id(get_status):
    res = get_status("nonexistent-id-12345")
    assert res.status_code == 200
