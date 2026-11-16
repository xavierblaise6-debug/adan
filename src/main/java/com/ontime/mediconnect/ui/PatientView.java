package com.ontime.mediconnect.ui;

import com.ontime.mediconnect.dao.PatientDao;
import com.ontime.mediconnect.model.Patient;
import com.ontime.mediconnect.util.Validator;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Add, view, update and delete patients. */
public class PatientView extends VBox {
    private final PatientDao dao = new PatientDao();
    private final TableView<Patient> table = new TableView<>();
    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField addressField = new TextField();
    private final ComboBox<String> genderBox = new ComboBox<>(FXCollections.observableArrayList("M", "F", "O"));
    private final TextField ageField = new TextField();
    private final ComboBox<String> bloodBox = new ComboBox<>(FXCollections.observableArrayList(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
    private final TextField weightField = new TextField();
    private final TextField heightField = new TextField();
    private final TextField searchField = new TextField();

    public PatientView() {
        getStyleClass().add("view");
        setSpacing(10);
        idField.setEditable(false);
        idField.setPromptText("Assigned automatically");
        weightField.setPromptText("kg");
        heightField.setPromptText("cm");
        searchField.setPromptText("Search by ID, name or address");

        GridPane form = Ui.formGrid();
        Ui.addRow(form, 0, "Patient ID", idField);
        Ui.addRow(form, 1, "Name", nameField);
        Ui.addRow(form, 2, "Address", addressField);
        Ui.addRow(form, 3, "Gender (M/F/O)", genderBox);
        Ui.addRow(form, 4, "Age", ageField);
        Ui.addRow(form, 5, "Blood group", bloodBox);
        Ui.addRow(form, 6, "Weight", weightField);
        Ui.addRow(form, 7, "Height", heightField);

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

        table.getColumns().add(Ui.<Patient>column("ID", 60, Patient::id));
        table.getColumns().add(Ui.<Patient>column("Name", 160, Patient::name));
        table.getColumns().add(Ui.<Patient>column("Address", 180, Patient::address));
        table.getColumns().add(Ui.<Patient>column("Gender", 60, Patient::gender));
        table.getColumns().add(Ui.<Patient>column("Age", 50, p -> String.valueOf(p.age())));
        table.getColumns().add(Ui.<Patient>column("Blood", 60, Patient::bloodGroup));
        table.getColumns().add(Ui.<Patient>column("Weight", 70, p -> String.valueOf(p.weight())));
        table.getColumns().add(Ui.<Patient>column("Height", 70, p -> String.valueOf(p.height())));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, patient) -> {
            if (patient != null) {
                fill(patient);
            }
        });
        searchField.textProperty().addListener((obs, old, text) -> load(text));
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(Ui.title("Patients"), form, new HBox(8, add, update, delete, clear),
                searchField, table);
        load("");
    }

    private void add() {
        String error = validate();
        if (error != null) {
            Ui.error(error);
            return;
        }
        try {
            Patient saved = dao.insert(read(null));
            load(searchField.getText());
            clearForm();
            Ui.info("Patient saved with ID " + saved.id() + ".");
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private void update() {
        if (idField.getText().isEmpty()) {
            Ui.error("Select a patient from the table first.");
            return;
        }
        String error = validate();
        if (error != null) {
            Ui.error(error);
            return;
        }
        try {
            if (dao.update(read(idField.getText()))) {
                load(searchField.getText());
                Ui.info("Patient updated.");
            } else {
                Ui.error("That patient no longer exists.");
            }
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private void delete() {
        if (idField.getText().isEmpty()) {
            Ui.error("Select a patient from the table first.");
            return;
        }
        if (!Ui.confirm("Delete patient " + idField.getText() + "?")) {
            return;
        }
        try {
            dao.delete(idField.getText());
            load(searchField.getText());
            clearForm();
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private String validate() {
        return Validator.firstError(
                Validator.name("Name", nameField.getText()),
                Validator.text("Address", addressField.getText(), 40),
                Validator.gender(genderBox.getValue()),
                Validator.age(ageField.getText()),
                Validator.bloodGroup(bloodBox.getValue()),
                Validator.positiveNumber("Weight", weightField.getText()),
                Validator.positiveNumber("Height", heightField.getText()));
    }

    private Patient read(String id) {
        return new Patient(id, nameField.getText(), addressField.getText(), genderBox.getValue(),
                Integer.parseInt(ageField.getText().trim()), bloodBox.getValue(),
                Double.parseDouble(weightField.getText().trim()),
                Double.parseDouble(heightField.getText().trim()));
    }

    private void fill(Patient p) {
        idField.setText(p.id());
        nameField.setText(p.name());
        addressField.setText(p.address());
        genderBox.setValue(p.gender());
        ageField.setText(String.valueOf(p.age()));
        bloodBox.setValue(p.bloodGroup());
        weightField.setText(String.valueOf(p.weight()));
        heightField.setText(String.valueOf(p.height()));
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        idField.clear();
        nameField.clear();
        addressField.clear();
        genderBox.setValue(null);
        ageField.clear();
        bloodBox.setValue(null);
        weightField.clear();
        heightField.clear();
    }

    private void load(String text) {
        try {
            table.setItems(FXCollections.observableArrayList(dao.search(text == null ? "" : text)));
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }
}
