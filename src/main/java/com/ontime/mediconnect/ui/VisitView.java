package com.ontime.mediconnect.ui;

import com.ontime.mediconnect.dao.DoctorDao;
import com.ontime.mediconnect.dao.PatientDao;
import com.ontime.mediconnect.dao.VisitDao;
import com.ontime.mediconnect.model.Doctor;
import com.ontime.mediconnect.model.Patient;
import com.ontime.mediconnect.model.Visit;
import com.ontime.mediconnect.util.Validator;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Enter visits, diagnosis and treatment (table PatientDetails). */
public class VisitView extends VBox {
    private final VisitDao dao = new VisitDao();
    private final TableView<Visit> table = VisitTable.create();
    private final TextField idField = new TextField();
    private final ComboBox<Patient> patientBox = new ComboBox<>();
    private final ComboBox<Doctor> doctorBox = new ComboBox<>();
    private final Label visitNumberLabel = new Label("-");
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TextField ailmentField = new TextField();
    private final TextField descriptionField = new TextField();
    private final TextField consFeesField = new TextField();
    private final TextField otherField = new TextField("0");

    public VisitView() {
        getStyleClass().add("view");
        setSpacing(10);
        idField.setEditable(false);
        idField.setPromptText("Assigned automatically");
        loadChoices();

        patientBox.valueProperty().addListener((obs, old, patient) -> {
            // The visit number is only calculated while adding a new visit.
            if (patient != null && table.getSelectionModel().getSelectedItem() == null) {
                try {
                    visitNumberLabel.setText(String.valueOf(dao.nextVisitNumber(patient.id())));
                } catch (SQLException e) {
                    Ui.error(Ui.friendly(e));
                }
            }
        });

        GridPane form = Ui.formGrid();
        Ui.addRow(form, 0, "Visit ID", idField);
        Ui.addRow(form, 1, "Patient", patientBox);
        Ui.addRow(form, 2, "Doctor", doctorBox);
        Ui.addRow(form, 3, "Visit number", visitNumberLabel);
        Ui.addRow(form, 4, "Checkup date", datePicker);
        Ui.addRow(form, 5, "Ailment", ailmentField);
        Ui.addRow(form, 6, "Description", descriptionField);
        Ui.addRow(form, 7, "Consultation fees", consFeesField);
        Ui.addRow(form, 8, "Other expenses", otherField);

        Button add = new Button("Add");
        add.setOnAction(e -> add());
        Button update = new Button("Update");
        update.setOnAction(e -> update());
        Button delete = new Button("Delete");
        delete.getStyleClass().add("danger");
        delete.setOnAction(e -> delete());
        Button clear = new Button("Clear");
        clear.getStyleClass().add("secondary");
        clear.setOnAction(e -> clearForm());

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, visit) -> {
            if (visit != null) {
                fill(visit);
            }
        });
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(Ui.title("Visits, Diagnosis and Treatment"), form,
                new HBox(8, add, update, delete, clear), table);
        loadTable();
    }

    private void add() {
        String error = validate();
        if (error != null) {
            Ui.error(error);
            return;
        }
        try {
            int number = dao.nextVisitNumber(patientBox.getValue().id());
            Visit saved = dao.insert(read(null, number));
            loadTable();
            clearForm();
            Ui.info("Visit saved with ID " + saved.id() + ".");
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private void update() {
        if (idField.getText().isEmpty()) {
            Ui.error("Select a visit from the table first.");
            return;
        }
        String error = validate();
        if (error != null) {
            Ui.error(error);
            return;
        }
        try {
            int number = Integer.parseInt(visitNumberLabel.getText());
            if (dao.update(read(idField.getText(), number))) {
                loadTable();
                Ui.info("Visit updated.");
            } else {
                Ui.error("That visit no longer exists.");
            }
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private void delete() {
        if (idField.getText().isEmpty()) {
            Ui.error("Select a visit from the table first.");
            return;
        }
        if (!Ui.confirm("Delete visit " + idField.getText() + "?")) {
            return;
        }
        try {
            dao.delete(idField.getText());
            loadTable();
            clearForm();
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private String validate() {
        String date = null;
        if (datePicker.getValue() == null) {
            date = "Checkup date is required.";
        } else if (datePicker.getValue().isAfter(LocalDate.now())) {
            date = "Checkup date cannot be in the future.";
        }
        return Validator.firstError(
                patientBox.getValue() == null ? "Select a patient." : null,
                doctorBox.getValue() == null ? "Select a doctor." : null,
                date,
                Validator.text("Ailment", ailmentField.getText(), 50),
                Validator.optionalText("Description", descriptionField.getText(), 150),
                Validator.money("Consultation fees", consFeesField.getText()),
                Validator.money("Other expenses", otherField.getText()));
    }

    private Visit read(String id, int visitNumber) {
        String description = descriptionField.getText().trim().isEmpty() ? null : descriptionField.getText();
        return new Visit(id, doctorBox.getValue().id(), patientBox.getValue().id(), ailmentField.getText(),
                description, visitNumber, datePicker.getValue(),
                new BigDecimal(consFeesField.getText().trim()), new BigDecimal(otherField.getText().trim()));
    }

    private void fill(Visit v) {
        idField.setText(v.id());
        patientBox.getItems().stream().filter(p -> p.id().equals(v.patientId()))
                .findFirst().ifPresent(patientBox.getSelectionModel()::select);
        doctorBox.getItems().stream().filter(d -> d.id().equals(v.doctorId()))
                .findFirst().ifPresent(doctorBox.getSelectionModel()::select);
        patientBox.setDisable(true);   // the patient of an existing visit cannot be changed
        visitNumberLabel.setText(String.valueOf(v.visitNumber()));
        datePicker.setValue(v.checkupDate());
        ailmentField.setText(v.ailment());
        descriptionField.setText(v.description() == null ? "" : v.description());
        consFeesField.setText(v.consFees().toPlainString());
        otherField.setText(v.otherExpenses().toPlainString());
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        idField.clear();
        patientBox.setDisable(false);
        patientBox.setValue(null);
        doctorBox.setValue(null);
        visitNumberLabel.setText("-");
        datePicker.setValue(LocalDate.now());
        ailmentField.clear();
        descriptionField.clear();
        consFeesField.clear();
        otherField.setText("0");
    }

    private void loadChoices() {
        try {
            patientBox.setItems(FXCollections.observableArrayList(new PatientDao().findAll()));
            doctorBox.setItems(FXCollections.observableArrayList(new DoctorDao().findAll()));
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private void loadTable() {
        try {
            table.setItems(FXCollections.observableArrayList(dao.findAll()));
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }
}
