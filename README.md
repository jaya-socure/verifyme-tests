# verifyme-tests (Python / pytest)

Standalone pytest suite for the [VerifyMe](https://verify-me-46mk.onrender.com) identity verification API.

The tests run against the live hosted API by default and require no local server.

> This is the **Python** branch. Other languages (e.g. JavaScript) live on their own branches.

## Setup & Run

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
pytest
```

## Configuration

The base URL defaults to `https://verify-me-46mk.onrender.com` and can be overridden:

```bash
VERIFYME_BASE_URL=http://localhost:3001 pytest
```
## Scoring Rules

The risk score is additive. Each rule that triggers adds to the total score:

| Rule | Points | Trigger |
|------|--------|---------|
| Missing fields | +30 | Any of name, email, or dob is missing or empty |
| Disposable email | +25 | Email domain is `test.com` or `fake.io` |
| Unsupported country | +20 | Country is not US, CA, or GB |
| Underage | +15 | Age derived from dob is under 18 |
| Suspicious SSN | +10 | ssn_last4 is "0000" or "1234" |

## Decision Bands

| Score Range | Decision |
|-------------|----------|
| 0 - 25 | APPROVE |
| 26 - 55 | REVIEW |
| 56+ | REJECT |
