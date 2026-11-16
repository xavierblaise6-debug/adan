package com.ontime.mediconnect.ui;

import com.ontime.mediconnect.model.Staff;
import java.util.function.Supplier;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Main window shown after login: a menu on the left and the selected screen in the centre. */
public class MainView extends BorderPane {
    private final StackPane content = new StackPane();

    public MainView(Staff staff, Runnable onLogout, Runnable onExit) {
        Label welcome = new Label("Logged in as " + staff.name() + " (" + staff.id() + ")");
        welcome.getStyleClass().add("status-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, Ui.title("MediConnect"), spacer, welcome);
        header.getStyleClass().add("header-bar");

        VBox menu = new VBox(6,
                navButton("Doctors", DoctorView::new),
                navButton("Patients", PatientView::new),
                navButton("Visits & Treatment", VisitView::new),
                navButton("Billing", BillView::new),
                navButton("Display Information", QueryView::new));
        Region fill = new Region();
        VBox.setVgrow(fill, Priority.ALWAYS);
        Button logout = new Button("Log out");
        logout.setOnAction(e -> {
            if (Ui.confirm("Log out?")) {
                onLogout.run();
            }
        });
        Button exit = new Button("Exit");
        exit.setOnAction(e -> {
            if (Ui.confirm("Exit MediConnect?")) {
                onExit.run();
            }
        });
        menu.getChildren().addAll(fill, logout, exit);
        menu.getStyleClass().add("nav-bar");

        content.getChildren().add(new Label("Choose an option from the menu."));
        setTop(header);
        setLeft(menu);
        setCenter(content);
    }

    private Button navButton(String text, Supplier<Node> screen) {
        Button button = new Button(text);
        button.setOnAction(e -> content.getChildren().setAll(screen.get()));   // fresh screen = fresh data
        return button;
    }
}
