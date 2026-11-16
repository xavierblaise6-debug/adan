package com.ontime.mediconnect.model;

/** A doctor practising at PMC. */
public record Doctor(String id, String name, String specialization, String phone, String email) {
    @Override
    public String toString() {
        return id + " - " + name;
    }
}
