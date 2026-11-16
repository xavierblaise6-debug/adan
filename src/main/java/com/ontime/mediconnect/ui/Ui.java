package com.ontime.mediconnect.ui;

import java.sql.SQLException;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.GridPane;

/** Small helpers shared by every screen. */
public final class Ui {
    private Ui() { }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Error", message);
    }

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "MediConnect", message);
    }

    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Please confirm");
        return alert.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    public static Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("title-label");
        return label;
    }

    public static Label caption(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("caption");
        return label;
    }

    /** Adds "caption + control" as one row of a form grid. */
    public static void addRow(GridPane grid, int row, String caption, Node control) {
        grid.add(caption(caption), 0, row);
        grid.add(control, 1, row);
    }

    public static GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        return grid;
    }

    public static <T> TableColumn<T, String> column(String title, double width, Function<T, String> getter) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
        return column;
    }

    /** Turns a database exception into a message a receptionist can act on. */
    public static String friendly(SQLException e) {
        int code = e.getErrorCode();
        String state = e.getSQLState();
        if (code == 547) {
            return "This record is used by other records (visits or bills), so it cannot be deleted.";
        }
        if (code == 2627 || code == 2601) {
            return "That record already exists.";
        }
        if (code == 18456) {
            return "The database rejected the login in db.properties. Check the user name and password.";
        }
        if (state != null && state.startsWith("08")) {
            return "Cannot connect to the database. Check that SQL Server is running and db.properties is correct.";
        }
        return "Database error: " + e.getMessage();
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
