# Eventra AI Service

## Overview

This project contains the AI microservice for the Eventra Graduation Project.

The service is built using **Python**, **FastAPI**, and **Hugging Face Transformers**.

Currently implemented AI features:

- Personalized Event Recommendation
- Event Similarity Recommendation
- Sentiment Analysis

The service exposes REST APIs that will later be integrated with the Java Spring Boot backend.

---

# Project Structure

```
Eventra_AI/
│
├── main.py
├── models.py
├── recommender.py
├── similarity.py
├── sentiment.py
├── data.py
├── events.json
├── requirements.txt
├── tests/
│   ├── test_recommendation.py
│   ├── test_similarity.py
│   ├── test_sentiment.py
│   └── __init__.py
└── README.md
```

---

# Requirements

Install:

- Python 3.11 or newer
- pip

You can verify the installation by running:

```
python --version
```

---

# Setup Instructions

## Step 1 - Open the project

Open **Command Prompt**, **PowerShell**, or **Visual Studio Terminal**.

Navigate to the project folder.

Example:

```
cd C:\Users\YourName\Desktop\Eventra_AI
```

You should now be inside the folder that contains:

```
main.py
requirements.txt
```

---

## Step 2 - Create a Virtual Environment

Run:

```
python -m venv venv
```

This creates a new folder named:

```
venv/
```

which contains the project's isolated Python environment.

---

## Step 3 - Activate the Virtual Environment

### Windows PowerShell

```
venv\Scripts\activate
```

### Windows Command Prompt

```
venv\Scripts\activate.bat
```

If successful, your terminal will look similar to:

```
(venv) PS C:\Users\...
```

---

## Step 4 - Install Dependencies

Run:

```
pip install -r requirements.txt
```

This installs:

- FastAPI
- Uvicorn
- Sentence Transformers
- Transformers
- Torch
- Scikit-learn
- Pytest

The first installation may take several minutes.

---

## Step 5 - Run the AI Service

Run:

```
uvicorn main:app --reload
```

If successful, you should see something similar to:

```
INFO: Uvicorn running on http://127.0.0.1:8000
```

---

# API Documentation

Open your browser:

```
http://127.0.0.1:8000/docs
```

Swagger UI will open automatically.

All endpoints can be tested directly from the browser.

---

# Available Endpoints

## Home

```
GET /
```

Returns:

```
{
    "message": "Eventra AI Service Running"
}
```

---

## User Recommendation

```
POST /recommend-user?user_id=1
```

Returns personalized event recommendations.

---

## Event Similarity

```
GET /recommend/{event_id}
```

Example:

```
GET /recommend/1
```

Returns events similar to the selected event.

---

## Sentiment Analysis

```
POST /sentiment
```

Body:

```json
{
    "text":"Amazing event!"
}
```

Returns:

```json
[
  {
    "label":"POSITIVE",
    "score":0.99
  }
]
```

---

# Running Tests

This project includes automated tests using **pytest**.

Run:

```
python -m pytest
```

Expected result:

```
16 passed
```

---

# Current Implementation

Implemented:

- Personalized Event Recommendation
- Event Similarity
- Sentiment Analysis
- Automated Testing

---

# Current Limitations

The Recommendation and Similarity features currently use mock data stored in:

```
data.py
events.json
```

These will later be replaced with data received from the Spring Boot backend and database.

The Sentiment Analysis endpoint already accepts real user text input.

---

# Backend Integration

The Java Spring Boot backend will call these REST APIs.

Example:

```
POST http://localhost:8000/sentiment
```

with

```json
{
    "text":"Amazing event!"
}
```

The AI service returns the prediction as JSON.

The backend is responsible for:

- Reading data from the database
- Calling the AI service
- Receiving predictions
- Storing results if needed
- Returning responses to the frontend

---

# Notes

- Do not upload or share the `venv` folder.
- If the AI models are downloaded for the first time, the startup may take a few minutes.
- Internet access is required only for the first model download. Afterwards, the models are cached locally.

---

# Authors

Eventra Graduation Project

AI Module

Developed by:

Mohamed Farag