package com.ontime.mediconnect.ui;

import com.ontime.mediconnect.dao.BillDao;
import com.ontime.mediconnect.dao.CountDao;
import com.ontime.mediconnect.dao.VisitDao;
import com.ontime.mediconnect.model.Visit;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Display information: counts, visits on a date, and a patient's history. */
public class QueryView extends VBox {
    private final CountDao countDao = new CountDao();
    private final VisitDao visitDao = new VisitDao();
    private final BillDao billDao = new BillDao();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TextField patientField = new TextField();
    private final Label resultLabel = new Label("Choose a query.");
    private final TableView<Visit> table = VisitTable.create();

    public QueryView() {
        getStyleClass().add("view");
        setSpacing(10);
        patientField.setPromptText("Patient ID, e.g. P001");
        resultLabel.getStyleClass().add("caption");

        Button doctors = new Button("Number of doctors");
        doctors.setOnAction(e -> run(() -> {
            resultLabel.setText("Doctors registered: " + countDao.doctors());
            table.getItems().clear();
        }));
        Button patients = new Button("Number of patients");
        patients.setOnAction(e -> run(() -> {
            resultLabel.setText("Patients registered: " + countDao.patients());
            table.getItems().clear();
        }));

        Button onDate = new Button("Patients who visited");
        onDate.setOnAction(e -> run(this::visitsOnDate));
        Button revenue = new Button("Amount billed");
        revenue.setOnAction(e -> run(this::revenueOnDate));
        Button history = new Button("Patient history");
        history.setOnAction(e -> run(this::patientHistory));

        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(Ui.title("Display Information"),
                new HBox(8, doctors, patients),
                new HBox(8, Ui.caption("Date"), datePicker, onDate, revenue),
                new HBox(8, Ui.caption("Patient"), patientField, history),
                resultLabel, table);
    }

    private void visitsOnDate() throws SQLException {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            Ui.error("Pick a date.");
            return;
        }
        List<Visit> visits = visitDao.findByDate(date);
        resultLabel.setText("Patients who visited on " + date + ": " + countDao.patientsOn(date)
                + " (" + visits.size() + " visit records)");
        table.setItems(FXCollections.observableArrayList(visits));
    }

    private void revenueOnDate() throws SQLException {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            Ui.error("Pick a date.");
            return;
        }
        resultLabel.setText("Amount billed on " + date + ": " + billDao.revenueOn(date).toPlainString());
        table.getItems().clear();
    }

    private void patientHistory() throws SQLException {
        String id = patientField.getText().trim();
        if (id.isEmpty()) {
            Ui.error("Enter a patient ID.");
            return;
        }
        List<Visit> visits = visitDao.findByPatient(id);
        resultLabel.setText("Treatment history of " + id + ": " + visits.size() + " visit(s)");
        table.setItems(FXCollections.observableArrayList(visits));
    }

    /** A database action that may fail. */
    private interface DbAction {
        void run() throws SQLException;
    }

    private void run(DbAction action) {
        try {
            action.run();
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }
}
