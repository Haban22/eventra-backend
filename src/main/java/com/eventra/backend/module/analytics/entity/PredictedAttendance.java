package com.eventra.backend.module.analytics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "predicted_attendance")
public class PredictedAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}