package com.ontime.mediconnect;

import com.ontime.mediconnect.dao.StaffDao;
import com.ontime.mediconnect.model.Staff;
import com.ontime.mediconnect.ui.LoginView;
import com.ontime.mediconnect.ui.MainView;
import com.ontime.mediconnect.ui.Ui;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** MediConnect entry point (OnTime Systems for Philadelphia Medical Center). */
public class MediConnectApp extends Application {
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("MediConnect - Philadelphia Medical Center");
        try {
            new StaffDao().ensureDefaultAdmin();
        } catch (SQLException e) {
            Ui.error(Ui.friendly(e));   // login screen will keep reporting the problem
        }
        showLogin();
        stage.show();
    }

    private void showLogin() {
        LoginView login = new LoginView(this::showMain, Platform::exit);
        StackPane root = new StackPane(login);
        root.getStyleClass().add("view");
        setScene(new Scene(root, 520, 420));
    }

    private void showMain(Staff staff) {
        setScene(new Scene(new MainView(staff, this::showLogin, Platform::exit), 1100, 720));
    }

    private void setScene(Scene scene) {
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
