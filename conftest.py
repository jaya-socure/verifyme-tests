import os

import pytest
import requests

BASE_URL = os.environ.get("VERIFYME_BASE_URL", "https://verify-me-46mk.onrender.com").rstrip("/")
TIMEOUT = int(os.environ.get("VERIFYME_TIMEOUT", "30"))


@pytest.fixture(scope="session")
def base_url():
    return BASE_URL


@pytest.fixture
def post_verify(base_url):
    def _post(payload):
        return requests.post(f"{base_url}/verify", json=payload, timeout=TIMEOUT)

    return _post


@pytest.fixture
def get_status(base_url):
    def _get(request_id):
        return requests.get(f"{base_url}/status/{request_id}", timeout=TIMEOUT)

    return _get
