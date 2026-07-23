import request from "supertest";

const BASE_URL =
  process.env.VERIFYME_BASE_URL || "https://verify-me-46mk.onrender.com";
const api = () => request(BASE_URL);

type Decision = "APPROVE" | "REVIEW" | "REJECT";

interface VerifyResponse {
  requestId: string;
  decision: Decision;
  score: number;
  reasons: string[];
}

interface StatusResponse {
  requestId: string;
  decision: Decision;
  score: number;
  reasons: string[];
}

interface VerifyRequest {
  name?: string;
  email?: string;
  dob?: string;
  ssn_last4?: string;
  country?: string;
}

describe("VerifyMe API", () => {
  // TC-1: Valid US adult, clean email, ssn 5678 → APPROVE
  test("TC-1: approves a valid US adult with clean data", async () => {
    const payload: VerifyRequest = {
      name: "Jane Doe",
      email: "jane@example.com",
      dob: "1990-05-15",
      ssn_last4: "5678",
      country: "US",
    };
    const res = await api().post("/verify").send(payload);
    const body = res.body as VerifyResponse;

    expect(res.status).toBe(200);
    expect(body.decision).toBe("APPROVE");
  });

  // TC-2: Missing dob → REJECT, reasons includes "missing_fields"
  test("TC-2: rejects when dob is missing", async () => {
    const payload: VerifyRequest = {
      name: "John Smith",
      email: "john@test.com",
      ssn_last4: "1234",
      country: "MX",
    };
    const res = await api().post("/verify").send(payload);
    const body = res.body as VerifyResponse;

    expect(res.status).toBe(200);
    expect(body.decision).toBe("REJECT");
    expect(body.reasons).toContain("missing_fields");
  });

  // TC-3: Email domain test.com, all other fields valid → not APPROVE
  test("TC-3: does not approve disposable email domain", async () => {
    const payload: VerifyRequest = {
      name: "Alice Test",
      email: "alice@test.com",
      dob: "1985-03-20",
      ssn_last4: "9999",
      country: "DE",
    };
    const res = await api().post("/verify").send(payload);
    const body = res.body as VerifyResponse;

    expect(body.decision).not.toBe("APPROVE");
  });

  // TC-4: POST /verify then GET /status/:requestId → same decision
  test("TC-4: status endpoint returns the same decision", async () => {
    const payload: VerifyRequest = {
      name: "Bob Builder",
      email: "bob@company.org",
      dob: "1988-11-02",
      ssn_last4: "4321",
      country: "GB",
    };
    const verifyRes = await api().post("/verify").send(payload);
    const verifyBody = verifyRes.body as VerifyResponse;

    const requestId = verifyBody.requestId;
    const statusRes = await api().get(`/status/${requestId}`);
    const statusBody = statusRes.body as StatusResponse;

    expect(statusRes.status).toBe(200);
    expect(statusBody.decision).toBe(verifyBody.decision);
  });

  // TC-5: GET /status with nonexistent requestId → expect 200
  test("TC-5: returns 200 for nonexistent requestId", async () => {
    const res = await api().get("/status/nonexistent-id-12345");
    expect(res.status).toBe(200);
  });
});
