import pandas as pd
import numpy as np
import joblib
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score

# ===============================
# Load Dataset
# ===============================
df = pd.read_excel("data/event_size_data_500.xlsx")

le = LabelEncoder()
df["category_enc"] = le.fit_transform(df["category"])

X = df[[
    "category_enc",
    "is_free",
    "days_until_event",
    "avg_past_attendees",
    "promotion_score",
    "venue_capacity",
]]
y = df["actual_attendees"]

# ===============================
# Split
# ===============================
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# ===============================
# Train
# ===============================
model = RandomForestRegressor(n_estimators=200, max_depth=15, random_state=42)
model.fit(X_train, y_train)

# ===============================
# Evaluation
# ===============================
preds = model.predict(X_test)
mae   = mean_absolute_error(y_test, preds)
rmse  = np.sqrt(mean_squared_error(y_test, preds))
r2    = r2_score(y_test, preds)
cv_r2 = cross_val_score(model, X, y, cv=5, scoring="r2")

print("\n========== EVENT SIZE MODEL EVALUATION ==========\n")
print(f"MAE  (Mean Absolute Error) : {mae:.2f} attendees")
print(f"RMSE (Root Mean Sq Error)  : {rmse:.2f} attendees")
print(f"R²   (Explained Variance)  : {r2:.3f}")
print(f"CV R² (5-fold)             : {cv_r2.mean():.3f} ± {cv_r2.std():.3f}")

print("\nFeature Importance:")
for feat, score in sorted(
    zip(X.columns, model.feature_importances_),
    key=lambda x: x[1],
    reverse=True,
):
    print(f"  {feat:30} {score:.3f}")

# ===============================
# Save
# ===============================
joblib.dump(model, "models/event_size_model.pkl")
joblib.dump(le, "models/category_encoder.pkl")
print("\nSaved: models/event_size_model.pkl, models/category_encoder.pkl")