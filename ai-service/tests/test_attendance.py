from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

# Base valid input for reuse across tests
VALID_INPUT = {
    "category": "technology",
    "is_free": 1,
    "days_until_event": 13,
    "avg_past_attendees": 80,
    "promotion_score": 0.7,
    "venue_capacity": 150
}


def test_attendance_status_code():
    response = client.post("/predict-attendance", json=VALID_INPUT)
    assert response.status_code == 200


def test_attendance_returns_required_fields():
    response = client.post("/predict-attendance", json=VALID_INPUT)
    data = response.json()
    assert "predicted_attendees" in data
    assert "range_low" in data
    assert "range_high" in data
    assert "confidence_note" in data
    assert "used_category_average" in data


def test_prediction_is_positive():
    response = client.post("/predict-attendance", json=VALID_INPUT)
    data = response.json()
    assert data["predicted_attendees"] >= 1


def test_prediction_does_not_exceed_capacity():
    response = client.post("/predict-attendance", json=VALID_INPUT)
    data = response.json()
    assert data["predicted_attendees"] <= VALID_INPUT["venue_capacity"]


def test_range_low_less_than_predicted():
    response = client.post("/predict-attendance", json=VALID_INPUT)
    data = response.json()
    assert data["range_low"] <= data["predicted_attendees"]


def test_range_high_greater_than_predicted():
    response = client.post("/predict-attendance", json=VALID_INPUT)
    data = response.json()
    assert data["range_high"] >= data["predicted_attendees"]


def test_range_high_does_not_exceed_capacity():
    response = client.post("/predict-attendance", json=VALID_INPUT)
    data = response.json()
    assert data["range_high"] <= VALID_INPUT["venue_capacity"]


def test_new_organizer_uses_category_average():
    # avg_past_attendees = 0 triggers cold-start fallback
    new_organizer_input = {**VALID_INPUT, "avg_past_attendees": 0}
    response = client.post("/predict-attendance", json=new_organizer_input)
    data = response.json()
    assert data["used_category_average"] is True


def test_experienced_organizer_no_category_average():
    response = client.post("/predict-attendance", json=VALID_INPUT)
    data = response.json()
    assert data["used_category_average"] is False


def test_invalid_category_returns_400():
    invalid_input = {**VALID_INPUT, "category": "gaming"}
    response = client.post("/predict-attendance", json=invalid_input)
    assert response.status_code == 400
    assert "Unknown category" in response.json()["detail"]


def test_confidence_note_contains_range():
    response = client.post("/predict-attendance", json=VALID_INPUT)
    data = response.json()
    assert str(data["range_low"])  in data["confidence_note"]
    assert str(data["range_high"]) in data["confidence_note"]


def test_large_organizer_predicts_large_attendance():
    # Organizer with 300 past attendees, large venue
    large_input = {
        "category": "sports",
        "is_free": 1,
        "days_until_event": 10,
        "avg_past_attendees": 300,
        "promotion_score": 0.9,
        "venue_capacity": 500
    }
    response = client.post("/predict-attendance", json=large_input)
    data = response.json()
    # Should predict significantly more than a small event
    assert data["predicted_attendees"] > 50


def test_promotion_score_validation():
    # promotion_score > 1.0 should fail validation
    invalid_input = {**VALID_INPUT, "promotion_score": 1.5}
    response = client.post("/predict-attendance", json=invalid_input)
    assert response.status_code == 422


def test_venue_capacity_minimum():
    # venue_capacity < 1 should fail validation
    invalid_input = {**VALID_INPUT, "venue_capacity": 0}
    response = client.post("/predict-attendance", json=invalid_input)
    assert response.status_code == 422