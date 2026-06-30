from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_search_status_code():

    response = client.post("/search", json={"query": "technology", "limit": 5})

    assert response.status_code == 200


def test_search_returns_list():

    response = client.post("/search", json={"query": "technology", "limit": 5})

    data = response.json()

    assert isinstance(data, list)


def test_search_respects_limit():

    response = client.post("/search", json={"query": "technology", "limit": 2})

    data = response.json()

    assert len(data) <= 2


def test_search_alias_resolves_to_category():

    response = client.post("/search", json={"query": "tech", "limit": 5})

    data = response.json()

    assert all(item["event"]["category"] == "Technology" for item in data)


def test_search_empty_query():

    response = client.post("/search", json={"query": "", "limit": 5})

    data = response.json()

    assert data == {"error": "Search query cannot be empty"}


def test_search_no_matches():

    response = client.post(
        "/search", json={"query": "underwater basket weaving", "limit": 5}
    )

    data = response.json()

    assert data == {"message": "No matching events found."}


def test_search_missing_query_rejected():

    response = client.post("/search", json={})

    assert response.status_code == 422
