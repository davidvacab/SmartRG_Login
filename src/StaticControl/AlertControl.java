package StaticControl;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertControl {
    public static Optional<ButtonType> askConfirmation(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel running units");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to cancel?");
        return alert.showAndWait();
    }

    public static void errorMessage(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invalid Quantity");
        alert.setHeaderText(null);
        alert.setContentText("Number of tabs must be between 1 and 20");
        alert.showAndWait();
    }
}
