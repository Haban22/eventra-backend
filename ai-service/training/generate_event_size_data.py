"""
generate_event_size_data.py

Generates the synthetic training dataset for the Attendance Prediction
feature (event_size_data_500.xlsx).

No LLM or AI tool is used here. Every row is produced using NumPy
probability distributions. Distribution PARAMETERS (not the rows
themselves) are grounded in real published statistics where cited below;
remaining parameters are domain-informed design choices made and tuned
by the development team.

References:
  [1] Grace, J. (2019). Predicting Meetup Event Size Using a
      Classification Model. Medium / NYC Meetup API data, 134,060 events.
      https://medium.com/@grace01/predicting-meetup-event-size-using-a-classification-model-77d782202e9b
  [2] Cvent. (2026). 390 Event Statistics Shaping the Industry in 2026.
      https://www.cvent.com/en/blog/events/event-statistics

Run:
    python generate_event_size_data.py

Output:
    event_size_data_500.xlsx
"""

import pandas as pd
import numpy as np

np.random.seed(42)
N_ROWS = 1500

# ================================================================
# category — weighted random choice
# Weights = real category distribution from Grace J. (2019) [1],
# mapped from Meetup's group categories to our 7 Eventra categories.
# ================================================================
CATEGORIES = ["technology", "music", "sports", "arts", "business", "health", "education"]
CATEGORY_WEIGHTS = [0.28, 0.12, 0.10, 0.08, 0.20, 0.10, 0.12]  # cited [1]

# ================================================================
# venue_capacity — uniform range per category
# Ranges are NOT cited from a paper; they reflect general industry
# venue-size norms for each event type (small meetup -> large
# conference/concert), informed by Cvent (2026) [2] event-size
# context. Design choice made by the development team.
# ================================================================
CATEGORY_CAPACITY_RANGE = {
    "technology": (30,  600),
    "music":      (50,  1000),
    "sports":     (50,  800),
    "arts":       (20,  300),
    "business":   (30,  600),
    "health":     (20,  300),
    "education":  (20,  400),
}


def generate_row():
    # --- category: cited weights [1] ---
    category = np.random.choice(CATEGORIES, p=CATEGORY_WEIGHTS)

    # --- is_free: cited probability split [1] ---
    # Grace J. (2019) measured has_event_fee across real Meetup events:
    # 68% free, 32% paid.
    is_free = np.random.choice([0, 1], p=[0.32, 0.68])

    # --- days_until_event: cited mean, our distribution shape ---
    # Grace J. (2019) reports created_to_event_days averaging 14 days.
    # We chose an exponential distribution (our design choice) because
    # it produces the right-skewed pattern (many events planned soon,
    # a long tail planned further out) consistent with her description.
    days_until_event = int(np.clip(np.random.exponential(14), 1, 90))

    # --- venue_capacity: our design choice, uniform per category ---
    cap_min, cap_max = CATEGORY_CAPACITY_RANGE[category]
    venue_capacity = int(np.random.uniform(cap_min, cap_max))

    # --- avg_past_attendees: our design choice ---
    # Generated proportionally to venue_capacity (5 .. 90% of capacity)
    # rather than a fixed small range. This was a deliberate correction
    # after testing showed a fixed narrow range caused the model to fail
    # to extrapolate for organizers with large real-world history values.
    avg_past_attendees = int(np.random.uniform(5, venue_capacity * 0.9))

    # --- promotion_score: our design choice ---
    # Beta(2,2) bounded to [0.1, 1.0] — a symmetric bell shape between
    # 0 and 1, chosen to represent typical moderate promotion effort
    # with few events at the extremes (no promotion / max promotion).
    promotion_score = round(np.clip(np.random.beta(2, 2), 0.1, 1.0), 2)

    # --- actual_attendees: domain-informed formula ---
    # Direction of each relationship is supported by the cited research
    # (more history -> more attendees, free events attract more people,
    # more promotion helps, more lead time slightly reduces urgency).
    # The exact numeric coefficients (0.65, 0.10, 0.15, 0.5) were tuned
    # by the development team through iterative testing, not taken from
    # a paper.
    attend = (
        avg_past_attendees * 0.65
        + (venue_capacity * 0.10 if is_free else 0)
        + promotion_score * venue_capacity * 0.15
        - days_until_event * 0.5
        + np.random.normal(0, venue_capacity * 0.05)
    )
    actual_attendees = max(1, int(np.clip(attend, 1, venue_capacity)))

    return {
        "category":           category,
        "is_free":            is_free,
        "days_until_event":   days_until_event,
        "avg_past_attendees": avg_past_attendees,
        "promotion_score":    promotion_score,
        "venue_capacity":     venue_capacity,
        "actual_attendees":   actual_attendees,
    }


def main():
    rows = [generate_row() for _ in range(N_ROWS)]
    df = pd.DataFrame(rows)

    print("=== GENERATED DATASET SUMMARY ===")
    print(f"Rows: {len(df)}")
    print(df.describe().round(2))
    print("\nCategory distribution:")
    print(df["category"].value_counts())

    df.to_excel("data/event_size_data_500.xlsx", index=False)
    print("\nSaved: data/event_size_data_500.xlsx")


if __name__ == "__main__":
    main()
