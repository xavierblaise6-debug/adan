package com.ontime.mediconnect.ui;

import com.ontime.mediconnect.dao.DoctorDao;
import com.ontime.mediconnect.model.Doctor;
import com.ontime.mediconnect.util.Validator;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Add, view, update and delete doctors. */
public class DoctorView extends VBox {
    private final DoctorDao dao = new DoctorDao();
    private final TableView<Doctor> table = new TableView<>();
    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField specializationField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField searchField = new TextField();

    public DoctorView() {
        getStyleClass().add("view");
        setSpacing(10);
        idField.setEditable(false);
        idField.setPromptText("Assigned automatically");
        searchField.setPromptText("Search by ID, name or specialization");

        GridPane form = Ui.formGrid();
        Ui.addRow(form, 0, "Doctor ID", idField);
        Ui.addRow(form, 1, "Name", nameField);
        Ui.addRow(form, 2, "Specialization", specializationField);
        Ui.addRow(form, 3, "Phone", phoneField);
        Ui.addRow(form, 4, "Email", emailField);

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
        HBox buttons = new HBox(8, add, update, delete, clear);

        table.getColumns().add(Ui.<Doctor>column("ID", 70, Doctor::id));
        table.getColumns().add(Ui.<Doctor>column("Name", 180, Doctor::name));
        table.getColumns().add(Ui.<Doctor>column("Specialization", 180, Doctor::specialization));
        table.getColumns().add(Ui.<Doctor>column("Phone", 130, Doctor::phone));
        table.getColumns().add(Ui.<Doctor>column("Email", 220, Doctor::email));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, doctor) -> {
            if (doctor != null) {
                fill(doctor);
            }
        });
        searchField.textProperty().addListener((obs, old, text) -> load(text));
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(Ui.title("Doctors"), form, buttons, searchField, table);
        setPadding(new Insets(16));
        load("");
    }

    private void add() {
        String error = validate();
        if (error != null) {
            Ui.error(error);
            return;
        }
        try {
            Doctor saved = dao.insert(read(null));
            load(searchField.getText());
            clearForm();
            Ui.info("Doctor saved with ID " + saved.id() + ".");
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private void update() {
        if (idField.getText().isEmpty()) {
            Ui.error("Select a doctor from the table first.");
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
                Ui.info("Doctor updated.");
            } else {
                Ui.error("That doctor no longer exists.");
            }
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private void delete() {
        if (idField.getText().isEmpty()) {
            Ui.error("Select a doctor from the table first.");
            return;
        }
        if (!Ui.confirm("Delete doctor " + idField.getText() + "?")) {
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
                Validator.text("Specialization", specializationField.getText(), 50),
                Validator.phone(phoneField.getText()),
                Validator.email(emailField.getText()));
    }

    private Doctor read(String id) {
        return new Doctor(id, nameField.getText(), specializationField.getText(),
                phoneField.getText(), emailField.getText());
    }

    private void fill(Doctor d) {
        idField.setText(d.id());
        nameField.setText(d.name());
        specializationField.setText(d.specialization());
        phoneField.setText(d.phone());
        emailField.setText(d.email());
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        idField.clear();
        nameField.clear();
        specializationField.clear();
        phoneField.clear();
        emailField.clear();
    }

    private void load(String text) {
        try {
            table.setItems(FXCollections.observableArrayList(dao.search(text == null ? "" : text)));
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }
}
