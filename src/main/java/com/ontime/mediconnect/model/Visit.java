package com.ontime.mediconnect.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One patient visit: diagnosis and treatment details (table PatientDetails). */
public record Visit(String id, String doctorId, String patientId, String ailment, String description,
                    int visitNumber, LocalDate checkupDate, BigDecimal consFees, BigDecimal otherExpenses) {

    public BigDecimal total() {
        return consFees.add(otherExpenses);
    }

    @Override
    public String toString() {
        return id + " - " + patientId + " (" + checkupDate + ")";
    }
}
