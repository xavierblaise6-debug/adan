package com.ontime.mediconnect.ui;

import com.ontime.mediconnect.model.Visit;
import javafx.scene.control.TableView;

/** Factory for the visits table that several screens display. */
final class VisitTable {
    private VisitTable() { }

    static TableView<Visit> create() {
        TableView<Visit> table = new TableView<>();
        table.getColumns().add(Ui.<Visit>column("Visit ID", 70, Visit::id));
        table.getColumns().add(Ui.<Visit>column("Patient", 70, Visit::patientId));
        table.getColumns().add(Ui.<Visit>column("Doctor", 70, Visit::doctorId));
        table.getColumns().add(Ui.<Visit>column("Date", 95, v -> v.checkupDate().toString()));
        table.getColumns().add(Ui.<Visit>column("Visit #", 60, v -> String.valueOf(v.visitNumber())));
        table.getColumns().add(Ui.<Visit>column("Ailment", 170, Visit::ailment));
        table.getColumns().add(Ui.<Visit>column("Description", 200,
                v -> v.description() == null ? "" : v.description()));
        table.getColumns().add(Ui.<Visit>column("Consultation", 95, v -> v.consFees().toPlainString()));
        table.getColumns().add(Ui.<Visit>column("Other", 80, v -> v.otherExpenses().toPlainString()));
        table.getColumns().add(Ui.<Visit>column("Total", 90, v -> v.total().toPlainString()));
        return table;
    }
}
