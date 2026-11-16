package com.ontime.mediconnect.ui;

import com.ontime.mediconnect.dao.StaffDao;
import com.ontime.mediconnect.model.Staff;
import com.ontime.mediconnect.util.Validator;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Staff login. Locks the form for 30 seconds after 3 wrong attempts. */
public class LoginView extends VBox {
    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_SECONDS = 30;

    private final StaffDao staffDao = new StaffDao();
    private final TextField staffIdField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label messageLabel = new Label();
    private final Button loginButton = new Button("Login");
    private int failedAttempts = 0;

    public LoginView(Consumer<Staff> onSuccess, Runnable onExit) {
        getStyleClass().add("view");
        setSpacing(14);
        setAlignment(Pos.CENTER);
        setMaxWidth(380);
        staffIdField.setPromptText("e.g. S001");
        messageLabel.getStyleClass().add("error-label");

        GridPane form = Ui.formGrid();
        Ui.addRow(form, 0, "Staff ID", staffIdField);
        Ui.addRow(form, 1, "Password", passwordField);

        Button exit = new Button("Exit");
        exit.getStyleClass().add("secondary");
        exit.setOnAction(e -> onExit.run());
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> login(onSuccess));

        getChildren().addAll(Ui.title("MediConnect"), new Label("Philadelphia Medical Center - staff login"),
                form, messageLabel, loginButton, exit);
    }

    private void login(Consumer<Staff> onSuccess) {
        String id = staffIdField.getText() == null ? "" : staffIdField.getText().trim().toUpperCase();
        String password = passwordField.getText();
        String error = Validator.firstError(Validator.staffId(id),
                password == null || password.isEmpty() ? "Password is required." : null);
        if (error != null) {
            messageLabel.setText(error);
            return;
        }
        try {
            Optional<Staff> staff = staffDao.authenticate(id, password);
            if (staff.isPresent()) {
                passwordField.clear();
                onSuccess.accept(staff.get());
                return;
            }
        } catch (SQLException e) {
            messageLabel.setText(Ui.friendly(e));
            return;
        }
        failedAttempts++;
        passwordField.clear();
        if (failedAttempts >= MAX_ATTEMPTS) {
            lock();
        } else {
            messageLabel.setText("Wrong Staff ID or password. Attempts left: " + (MAX_ATTEMPTS - failedAttempts));
        }
    }

    private void lock() {
        loginButton.setDisable(true);
        messageLabel.setText("Too many failed attempts. Try again in " + LOCK_SECONDS + " seconds.");
        PauseTransition wait = new PauseTransition(Duration.seconds(LOCK_SECONDS));
        wait.setOnFinished(e -> {
            failedAttempts = 0;
            loginButton.setDisable(false);
            messageLabel.setText("");
        });
        wait.play();
    }
}
