package com.ontime.mediconnect.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A bill raised for one visit. */
public record Bill(int id, String visitId, String patientId, LocalDateTime billDate,
                   BigDecimal amount, String modeOfPayment) { }
