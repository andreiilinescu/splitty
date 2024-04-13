package client.services;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;

import java.util.Optional;

public class NotificationHelper implements NotificationService {
    /**
     * Show an error message, in a new window.
     * @param title title of the error message
     * @param message message of the error
     */
    @Override
    public void showError(String title, String message) {
        Alert errorAlert = new Alert(AlertType.ERROR);
        errorAlert.initModality(Modality.APPLICATION_MODAL);
        errorAlert.setTitle(I18N.get("general.error"));
        errorAlert.setHeaderText(title);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
    /**
     * Show an error message, in a new window.
     * @param title title of the error message
     * @param message message of the error
     * @param header defines what type of information is being provided to the user
     */
    @Override
    public void informUser(String title, String message, String header) {
        Alert feedbackAlert = new Alert(AlertType.INFORMATION);
        feedbackAlert.initModality(Modality.APPLICATION_MODAL);
        feedbackAlert.setTitle(header);
        feedbackAlert.setHeaderText(title);
        feedbackAlert.setContentText(message);
        feedbackAlert.show();
    }

    /**
     * Show a confirmation message, in a new window.
     * @param title title of the confirmation message
     * @param message message of the confirmation
     * @return true if the user confirms the action, false otherwise
     */
    @Override
    public boolean showConfirmation(String title, String message) {
        Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
        confirmationAlert.initModality(Modality.APPLICATION_MODAL);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText(title);
        confirmationAlert.setContentText(message);

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
