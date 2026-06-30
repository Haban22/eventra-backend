from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_sentiment_status_code():

    response = client.post(
        "/sentiment",
        json={"text": "Amazing event!"}
    )

    assert response.status_code == 200


def test_positive_sentiment():

    response = client.post(
        "/sentiment",
        json={"text": "Amazing event!"}
    )

    data = response.json()

    assert data["label"] == "POSITIVE"


def test_negative_sentiment():

    response = client.post(
        "/sentiment",
        json={"text": "Terrible event. Very bad organization."}
    )

    data = response.json()

    assert data["label"] == "NEGATIVE"


def test_empty_text():

    response = client.post(
        "/sentiment",
        json={"text": ""}
    )

    assert response.status_code == 400

    assert response.json()["detail"] == "Text cannot be empty"