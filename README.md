# verifyme-tests
VerifyMe takes a person's identity details, runs them through a set of risk rules, adds up a risk score, and returns a decision: APPROVE, REVIEW, or REJECT. Higher score = riskier = more likely to be rejected. That's the whole product.

## What is VerifyMe?

VerifyMe is a lightweight **identity verification service**. You send it a person's
identity details, it runs them through a set of **risk rules**, adds up a **risk score**,
and returns a **decision**: `APPROVE`, `REVIEW`, or `REJECT`. A higher score means higher
risk, which means the identity is more likely to be rejected.

### API endpoints

| Endpoint | Description |
|----------|-------------|
| `POST /verify` | Verify a single identity. Returns `requestId`, `decision`, `score`, and `reasons`. |
| `GET /status/:requestId` | Look up a previously stored result. Returns the result if found, or `404` if the id is unknown. |
| `POST /verify/batch` | Verify up to 5 identities at once. More than 5 returns `400`. |

### Request fields

`name`, `email`, `dob` (YYYY-MM-DD), `ssn_last4` (4 digits), `country` (ISO 2-letter code).

### Scoring rules (additive)

| Points | Trigger |
|--------|---------|
| +30 | Any of `name`, `email`, or `dob` is missing or empty |
| +25 | Email domain is `test.com` or `fake.io` (disposable) |
| +20 | Country is not US, CA, or GB |
| +15 | Age derived from `dob` is under 18 |
| +10 | `ssn_last4` is `0000` or `1234` |

### Decision bands

| Score | Decision |
|-------|----------|
| 0 – 25 | APPROVE |
| 26 – 55 | REVIEW |
| 56+ | REJECT |


## Setup

```bash
npm install
```

## Run

```bash
npm test
```

```bash
VERIFYME_BASE_URL=http://localhost:3001 npm test
```
