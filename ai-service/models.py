from sentence_transformers import SentenceTransformer
from transformers import pipeline

# Load embedding model once
embedding_model = SentenceTransformer("all-MiniLM-L6-v2")

# Load sentiment model once
sentiment_model = pipeline("sentiment-analysis")