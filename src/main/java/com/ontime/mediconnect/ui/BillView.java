package com.ontime.mediconnect.ui;

import com.ontime.mediconnect.dao.BillDao;
import com.ontime.mediconnect.dao.DoctorDao;
import com.ontime.mediconnect.dao.PatientDao;
import com.ontime.mediconnect.dao.VisitDao;
import com.ontime.mediconnect.model.Bill;
import com.ontime.mediconnect.model.Doctor;
import com.ontime.mediconnect.model.Patient;
import com.ontime.mediconnect.model.Visit;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/** Generate, view and print bills. */
public class BillView extends VBox {
    private final BillDao billDao = new BillDao();
    private final VisitDao visitDao = new VisitDao();
    private final ComboBox<Visit> visitBox = new ComboBox<>();
    private final Label totalLabel = new Label("-");
    private final ComboBox<String> modeBox = new ComboBox<>(
            FXCollections.observableArrayList("Cash", "Card", "Cheque", "Online"));
    private final TextField patientField = new TextField();
    private final TableView<Bill> billTable = new TableView<>();
    private final TextArea receiptArea = new TextArea();

    public BillView() {
        getStyleClass().add("view");
        setSpacing(10);
        receiptArea.setEditable(false);
        receiptArea.getStyleClass().add("receipt");
        receiptArea.setPrefRowCount(16);
        patientField.setPromptText("Patient ID, e.g. P001");
        loadUnbilled();

        visitBox.valueProperty().addListener((obs, old, visit) ->
                totalLabel.setText(visit == null ? "-" : visit.total().toPlainString()));

        GridPane form = Ui.formGrid();
        Ui.addRow(form, 0, "Unbilled visit", visitBox);
        Ui.addRow(form, 1, "Total amount", totalLabel);
        Ui.addRow(form, 2, "Mode of payment", modeBox);
        Button generate = new Button("Generate bill");
        generate.setOnAction(e -> generate());

        Button find = new Button("Find bills");
        find.setOnAction(e -> findBills());
        Button show = new Button("Show selected bill");
        show.setOnAction(e -> showSelected());
        Button print = new Button("Print");
        print.setOnAction(e -> print());

        billTable.getColumns().add(Ui.<Bill>column("Bill #", 70, b -> String.valueOf(b.id())));
        billTable.getColumns().add(Ui.<Bill>column("Visit", 70, Bill::visitId));
        billTable.getColumns().add(Ui.<Bill>column("Patient", 70, Bill::patientId));
        billTable.getColumns().add(Ui.<Bill>column("Date", 150,
                b -> b.billDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        billTable.getColumns().add(Ui.<Bill>column("Amount", 90, b -> b.amount().toPlainString()));
        billTable.getColumns().add(Ui.<Bill>column("Paid by", 80, Bill::modeOfPayment));
        billTable.setPrefHeight(130);
        VBox.setVgrow(receiptArea, Priority.ALWAYS);

        getChildren().addAll(Ui.title("Billing"), form, generate,
                Ui.caption("Find existing bills"), new HBox(8, patientField, find, show),
                billTable, receiptArea, print);
    }

    private void generate() {
        Visit visit = visitBox.getValue();
        if (visit == null) {
            Ui.error("Select a visit to bill.");
            return;
        }
        if (modeBox.getValue() == null) {
            Ui.error("Select a mode of payment.");
            return;
        }
        try {
            Bill bill = billDao.generate(visit.id(), modeBox.getValue());
            receiptArea.setText(receipt(bill, visit));
            loadUnbilled();
            modeBox.setValue(null);
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private void findBills() {
        String id = patientField.getText().trim();
        if (id.isEmpty()) {
            Ui.error("Enter a patient ID.");
            return;
        }
        try {
            billTable.setItems(FXCollections.observableArrayList(billDao.findByPatient(id)));
            if (billTable.getItems().isEmpty()) {
                Ui.info("No bills found for " + id + ".");
            }
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private void showSelected() {
        Bill bill = billTable.getSelectionModel().getSelectedItem();
        if (bill == null) {
            Ui.error("Select a bill in the table.");
            return;
        }
        try {
            Visit visit = visitDao.findByPatient(bill.patientId()).stream()
                    .filter(v -> v.id().equals(bill.visitId())).findFirst().orElse(null);
            if (visit == null) {
                Ui.error("The visit for this bill could not be found.");
                return;
            }
            receiptArea.setText(receipt(bill, visit));
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }

    private void print() {
        if (receiptArea.getText().isEmpty()) {
            Ui.error("Generate or show a bill first.");
            return;
        }
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Ui.error("No printer is available.");
            return;
        }
        if (job.showPrintDialog(getScene().getWindow())) {
            Text page = new Text(receiptArea.getText());
            page.setFont(Font.font("Monospaced", 11));
            if (job.printPage(page)) {
                job.endJob();
            } else {
                Ui.error("Printing failed.");
            }
        }
    }

    private String receipt(Bill bill, Visit visit) throws SQLException {
        Patient patient = new PatientDao().search(bill.patientId()).stream()
                .filter(p -> p.id().equals(bill.patientId())).findFirst().orElse(null);
        Doctor doctor = new DoctorDao().search(visit.doctorId()).stream()
                .filter(d -> d.id().equals(visit.doctorId())).findFirst().orElse(null);
        String line = "-".repeat(44);
        return "      PHILADELPHIA MEDICAL CENTER\n"
                + "      Great Lakes Region, USA\n" + line + "\n"
                + String.format("Bill No   : %d%n", bill.id())
                + "Date      : " + bill.billDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n"
                + "Patient   : " + (patient == null ? bill.patientId() : patient) + "\n"
                + "Doctor    : " + (doctor == null ? visit.doctorId()
                        : doctor.name() + " (" + doctor.specialization() + ")") + "\n"
                + "Visit     : " + visit.id() + " (#" + visit.visitNumber() + ", " + visit.checkupDate() + ")\n"
                + "Ailment   : " + visit.ailment() + "\n" + line + "\n"
                + String.format("Consultation fees : %10s%n", visit.consFees().toPlainString())
                + String.format("Other expenses    : %10s%n", visit.otherExpenses().toPlainString())
                + String.format("TOTAL             : %10s%n", bill.amount().toPlainString())
                + "Paid by           : " + bill.modeOfPayment() + "\n" + line + "\n"
                + "      Thank you. Wishing you good health.\n";
    }

    private void loadUnbilled() {
        try {
            visitBox.setItems(FXCollections.observableArrayList(visitDao.findUnbilled()));
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));
        }
    }
}
