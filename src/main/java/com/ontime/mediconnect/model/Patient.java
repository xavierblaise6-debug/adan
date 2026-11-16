package com.ontime.mediconnect.model;

/** A patient registered at PMC. */
public record Patient(String id, String name, String address, String gender, int age,
                      String bloodGroup, double weight, double height) {
    @Override
    public String toString() {
        return id + " - " + name;
    }
}
