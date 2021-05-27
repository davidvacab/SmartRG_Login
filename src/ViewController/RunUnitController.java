package ViewController;

import Model.Regular;
import Model.SR400;
import StaticControl.*;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class RunUnitController implements Initializable {
    public TextField usernameField;
    public TextField passwordField;
    public Button cancelButton;
    public Button loginButton;
    public CheckBox factoryResetCheckBox;
    public ChoiceBox<String> unitChoiceBox;
    public ChoiceBox<String> httpProtocolChoiceBox;
    public ProgressIndicator loadingBar;
    public AnchorPane mainAnchorPane;
    public ComboBox<Integer> fromComboBox;
    public ComboBox<Integer> toComboBox;
    public ContextMenu usernameContextMenu;
    public ContextMenu passwordContextMenu;
    public ContextMenu fromComboBoxContextMenu;
    public ContextMenu toComboBoxContextMenu;

    private ObservableList<Integer> startTabs = FXCollections.observableArrayList();
    private ObservableList<Integer> endTabs = FXCollections.observableArrayList();

    private ArrayList<String> usernameSuggestions = Suggestions.readData("username.ser");
    private ArrayList<String> passwordSuggestions = Suggestions.readData("password.ser");;

    private String fromFilter = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        unitChoiceBox.setItems(FXCollections.observableArrayList("SR400/900", "Regular"));
        httpProtocolChoiceBox.setItems(FXCollections.observableArrayList("HTTP", "HTTP/admin", "HTTPS", "HTTPS/admin"));
        unitChoiceBox.setValue("SR400/900");
        httpProtocolChoiceBox.setValue("HTTP");
        passwordField.setText("admin");
        fromComboBox.setItems(startTabs);
        listeners();
        searchUsernameSuggestions();
        searchPasswordSuggestions();
    }

    private void listeners(){
        BooleanBinding sr400Selected = unitChoiceBox.getSelectionModel().selectedIndexProperty().isEqualTo(0);
        httpProtocolChoiceBox.disableProperty().bind(sr400Selected);
        usernameField.disableProperty().bind(sr400Selected);
        factoryResetCheckBox.disableProperty().bind(sr400Selected.not());

        unitChoiceBox.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            if (t1.matches("SR400/900")){
                usernameField.setText("admin");
            }
        });

        BooleanBinding emptyFields =
                passwordField.textProperty().isEmpty()
                        .or(usernameField.textProperty().isEmpty())
                        .or(fromComboBox.getSelectionModel().selectedItemProperty().isNull())
                        .or(toComboBox.getSelectionModel().selectedItemProperty().isNull());
        loginButton.disableProperty().bind(emptyFields);

        BooleanBinding startComboBoxSelected = fromComboBox.getSelectionModel().selectedItemProperty().isNull();
        toComboBox.disableProperty().bind(startComboBoxSelected);

        for (int i = 1; i < 21; i++){
            startTabs.add(i);
        }
        fromComboBox.getSelectionModel().selectedItemProperty().addListener((observableValue, integer, t1) -> {
            endTabs = FXCollections.observableArrayList();
            for (int tab: startTabs){
                if (tab > t1){
                    endTabs.add(tab);
                }
            }
            toComboBox.setItems(endTabs);
        });

        usernameField.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1){
                usernameContextMenu.hide();
            }
        });
        passwordField.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1){
                passwordContextMenu.hide();
            }
        });
    }

    public void onFactoryReset() {
        passwordField.requestFocus();
    }

    public void onUsernameField() {
        String username = usernameField.getText();
        if (!usernameSuggestions.contains(username)){
            usernameSuggestions.add(username);
            Suggestions.writeData(usernameSuggestions, "username.ser");
        }
        passwordField.requestFocus();
    }

    public void onPasswordField() {
        String password = passwordField.getText();
        if (!passwordSuggestions.contains(password)){
            passwordSuggestions.add(password);
            Suggestions.writeData(passwordSuggestions, "password.ser");
        }
        fromComboBox.requestFocus();
        fromComboBox.show();
    }


    public void onCancel(){
        Optional<ButtonType> result = AlertControl.askConfirmation();
        if (result.isPresent() && result.get().equals(ButtonType.OK)){
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
    }

    public void onClose(WindowEvent event) {
        Optional<ButtonType> result = AlertControl.askConfirmation();
        if (result.isPresent() && result.get().equals(ButtonType.OK)){
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        } else {
            event.consume();
        }
    }

    private void loadingAnimation(boolean state){
        mainAnchorPane.setDisable(state);
        loadingBar.setVisible(state);
        if (!state){
            loginButton.requestFocus();
        }
    }

    public void onRun() {
        loadingAnimation(true);
        String username = usernameField.getText();
        String password = passwordField.getText();
        Thread.UncaughtExceptionHandler handler = (th, ex) -> {
            loadingAnimation(false);
            System.out.println(ex.getMessage());
        };
        ArrayList<Integer> tabsToOpen = new ArrayList<>();
        for (int tab: startTabs){
            if (tab >= fromComboBox.getSelectionModel().getSelectedItem() && tab <= toComboBox.getSelectionModel().getSelectedItem()){
                tabsToOpen.add(tab);
            }
        }
        switch (unitChoiceBox.getSelectionModel().getSelectedItem()){
            case "SR400/900":
                SR400 sr400 = new SR400(password, tabsToOpen);
                if(factoryResetCheckBox.isSelected()){
                    ThreadRunner.runTask(() -> {
                        sr400.runAndFactoryReset();
                        Platform.runLater(() -> loadingAnimation(false));
                    }, handler);
                } else {
                    ThreadRunner.runTask(() -> {
                        sr400.runUnits();
                        Platform.runLater(() -> loadingAnimation(false));
                    },handler);
                }
                break;
            case "Regular":
                Regular regular = new Regular(username, password, tabsToOpen, httpProtocolChoiceBox.getSelectionModel().getSelectedIndex());
                ThreadRunner.runTask(() -> {
                    regular.runUnits();
                    Platform.runLater(() -> loadingAnimation(false));
                }, handler);
                break;
        }
    }

    private void searchUsernameSuggestions(){
        FilteredList<String> filteredSuggestions = new FilteredList<>(FXCollections.observableArrayList(usernameSuggestions), p -> true);
        usernameField.textProperty().addListener((observable, oldValue, newValue )-> {
            filteredSuggestions.setPredicate(suggestion -> {
                if (newValue == null || newValue.isEmpty()){
                    return false;
                } else {
                    String lowerCaseFilter = newValue.toLowerCase();
                    return suggestion.toLowerCase().contains(lowerCaseFilter);
                }
            });
            SortedList<String> sortedSuggestions = new SortedList<>(filteredSuggestions);
            ArrayList<CustomMenuItem> itemsList = new ArrayList<>();
            for (String suggestion: sortedSuggestions){
                Label entry = new Label(suggestion);
                CustomMenuItem item = new CustomMenuItem(entry, true);
                itemsList.add(item);
            }
            if (!itemsList.isEmpty()){
                usernameContextMenu.getItems().clear();
                usernameContextMenu.getItems().addAll(itemsList);
                usernameContextMenu.show(usernameField, Side.BOTTOM, 0,0);
            } else {
                usernameContextMenu.hide();
            }
        });
    }

    private void searchPasswordSuggestions(){
        FilteredList<String> filteredSuggestions = new FilteredList<>(FXCollections.observableArrayList(passwordSuggestions), p -> true);
        passwordField.textProperty().addListener((observable, oldValue, newValue )-> {
            filteredSuggestions.setPredicate(suggestion -> {
                if (newValue == null || newValue.isEmpty()){
                    return false;
                } else {
                    String lowerCaseFilter = newValue.toLowerCase();
                    return suggestion.toLowerCase().contains(lowerCaseFilter);
                }
            });
            SortedList<String> sortedSuggestions = new SortedList<>(filteredSuggestions);
            ArrayList<CustomMenuItem> itemsList = new ArrayList<>();
            for (String suggestion: sortedSuggestions){
                Label entry = new Label(suggestion);
                CustomMenuItem item = new CustomMenuItem(entry, true);
                itemsList.add(item);
            }
            if (!itemsList.isEmpty()){
                passwordContextMenu.getItems().clear();
                passwordContextMenu.getItems().addAll(itemsList);
                passwordContextMenu.show(passwordField, Side.BOTTOM, 0,0);
            } else {
                passwordContextMenu.hide();
            }
        });
    }

    public void onUsernameContextMenu(ActionEvent actionEvent) {
        usernameField.setText(((Label)((CustomMenuItem)actionEvent.getTarget()).getContent()).getText());
        passwordField.requestFocus();
    }

    public void onPasswordContextMenu(ActionEvent actionEvent) {
        passwordField.setText(((Label)((CustomMenuItem)actionEvent.getTarget()).getContent()).getText());
        fromComboBox.requestFocus();
        fromComboBox.show();
    }

    public void onKeyFromComboBox(KeyEvent keyEvent) {
        KeyCode code = keyEvent.getCode();
        if (code.isDigitKey() && fromFilter.length() < 3){
            fromFilter += keyEvent.getText();
            int value = Integer.parseInt(fromFilter);
            if (startTabs.contains(value)){
                fromComboBox.setValue(value);
            } else {
                if (fromFilter.length() != 0){
                    fromFilter = fromFilter.substring(0, fromFilter.length() - 1);
                }
            }
        } else {
            fromFilter = "";
        }
        if (code == KeyCode.ENTER){
            toComboBox.requestFocus();
            toComboBox.show();
        }
    }

    public void onKeyToComboBox(KeyEvent keyEvent) {

    }

    public void onFromComboBox(ActionEvent actionEvent) {
    }

    public void onFromComboBoxMenu(ContextMenuEvent contextMenuEvent) {
        contextMenuEvent.consume();
        SortedList<Integer> sortedSuggestions = new SortedList<>(startTabs);
        ArrayList<CustomMenuItem> itemsList = new ArrayList<>();
        for (int tab: sortedSuggestions){
            Label entry = new Label(Integer.toString(tab));
            CustomMenuItem item = new CustomMenuItem(entry, true);
            itemsList.add(item);
        }
        if (!itemsList.isEmpty()){
            fromComboBoxContextMenu.getItems().clear();
            fromComboBoxContextMenu.getItems().addAll(itemsList);
            fromComboBoxContextMenu.show(passwordField, Side.BOTTOM, 0,0);
        } else {
            fromComboBoxContextMenu.hide();
        }
    }
}
