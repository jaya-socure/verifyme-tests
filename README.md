# verifyme-tests

Standalone test suite for the [VerifyMe](https://verify-me-46mk.onrender.com) identity verification API.

The tests run against the live hosted API by default and require no local server.

## Setup

```bash
npm install
```

## Run

```bash
npm test
```

The base URL defaults to `https://verify-me-46mk.onrender.com` and can be overridden:

```bash
VERIFYME_BASE_URL=http://localhost:3001 npm test
```

## Test cases

- **TC-1** — approves a valid US adult with clean data
- **TC-2** — rejects when `dob` is missing
- **TC-3** — does not approve a disposable email domain
- **TC-4** — `/status` returns the same decision as `/verify`
- **TC-5** — `/status` behavior for a nonexistent requestId
