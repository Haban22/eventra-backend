from fastapi.testclient import TestClient
from main import app

client = TestClient(app)


def test_sentiment_status_code():
    response = client.post(
        "/sentiment",
        json={"text": "Amazing event!"}
    )
    assert response.status_code == 200


def test_sentiment_returns_list():
    response = client.post(
        "/sentiment",
        json={"text": "Amazing event!"}
    )
    data = response.json()
    assert isinstance(data, list)


def test_sentiment_has_label_and_score():
    response = client.post(
        "/sentiment",
        json={"text": "Amazing event!"}
    )
    data = response.json()
    assert "label" in data[0]
    assert "score" in data[0]


def test_positive_sentiment():
    response = client.post(
        "/sentiment",
        json={"text": "Amazing event! I loved every moment."}
    )
    data = response.json()
    assert data[0]["label"] == "POSITIVE"


def test_negative_sentiment():
    response = client.post(
        "/sentiment",
        json={"text": "Terrible event. Very bad organization."}
    )
    data = response.json()
    assert data[0]["label"] == "NEGATIVE"


def test_mixed_sentiment_is_negative():
    # Mixed reviews with negative words lean NEGATIVE
    response = client.post(
        "/sentiment",
        json={"text": "Some parts were good but overall boring and disappointing."}
    )
    data = response.json()
    assert data[0]["label"] == "NEGATIVE"


def test_confidence_score_is_float():
    response = client.post(
        "/sentiment",
        json={"text": "Great experience!"}
    )
    data = response.json()
    assert isinstance(data[0]["score"], float)


def test_confidence_score_between_0_and_1():
    response = client.post(
        "/sentiment",
        json={"text": "Great experience!"}
    )
    data = response.json()
    assert 0.0 <= data[0]["score"] <= 1.0


def test_empty_text_returns_400():
    response = client.post(
        "/sentiment",
        json={"text": ""}
    )
    assert response.status_code == 400
    assert response.json()["detail"] == "Text cannot be empty"


def test_event_specific_positive():
    # Realistic Eventra use case — post-event feedback
    response = client.post(
        "/sentiment",
        json={"text": "The AI Workshop was incredibly well organized and informative!"}
    )
    data = response.json()
    assert data[0]["label"] == "POSITIVE"


def test_event_specific_negative():
    response = client.post(
        "/sentiment",
        json={"text": "The venue was awful and the speakers were unprepared."}
    )
    data = response.json()
    assert data[0]["label"] == "NEGATIVE"