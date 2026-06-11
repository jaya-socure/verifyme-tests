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
