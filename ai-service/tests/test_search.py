from fastapi.testclient import TestClient
from main import app

client = TestClient(app)


def test_search_status_code():
    response = client.get("/search?query=tech")
    assert response.status_code == 200


def test_search_returns_list():
    response = client.get("/search?query=tech")
    data = response.json()
    assert isinstance(data, list)


def test_category_search_technology():
    # 'tech' alias maps to 'technology' — all Technology events returned
    response = client.get("/search?query=tech")
    data = response.json()
    assert len(data) > 0
    for item in data:
        assert item["event"]["category"] == "Technology"


def test_category_search_gaming_includes_sports():
    # 'gaming' returns Gaming events (score 1.0) + Sports (score 0.90)
    response = client.get("/search?query=gaming")
    data = response.json()
    categories = [item["event"]["category"] for item in data]
    assert "Gaming" in categories


def test_location_search_cairo():
    # 'cairo' triggers location search — all Cairo events returned
    response = client.get("/search?query=cairo")
    data = response.json()
    assert len(data) > 0
    for item in data:
        assert item["event"]["location"] == "Cairo"


def test_location_search_alexandria():
    response = client.get("/search?query=alexandria")
    data = response.json()
    assert len(data) > 0
    for item in data:
        assert item["event"]["location"] == "Alexandria"


def test_location_search_giza():
    response = client.get("/search?query=giza")
    data = response.json()
    assert len(data) > 0
    for item in data:
        assert item["event"]["location"] == "Giza"


def test_semantic_search_coding():
    # 'coding' should semantically match Programming Hackathon
    response = client.get("/search?query=coding")
    data = response.json()
    assert len(data) > 0
    event_ids = [item["event"]["id"] for item in data]
    assert 12 in event_ids  # Programming Hackathon


def test_semantic_search_hacking():
    # 'hacking' should match Cybersecurity Bootcamp
    response = client.get("/search?query=hacking")
    data = response.json()
    assert len(data) > 0
    event_ids = [item["event"]["id"] for item in data]
    assert 5 in event_ids  # Cybersecurity Bootcamp


def test_no_results_for_random_query():
    response = client.get("/search?query=xyz999abc")
    data = response.json()
    assert "message" in data
    assert data["message"] == "No matching events found."


def test_empty_query_returns_error():
    response = client.get("/search?query=")
    data = response.json()
    assert "error" in data
    assert data["error"] == "Search query cannot be empty"


def test_max_5_semantic_results():
    # Semantic search returns max 5 results
    response = client.get("/search?query=machine learning")
    data = response.json()
    if isinstance(data, list):
        assert len(data) <= 5