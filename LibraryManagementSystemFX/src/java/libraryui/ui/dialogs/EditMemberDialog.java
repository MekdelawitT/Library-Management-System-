package libraryui.ui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import library.Member;

public class EditMemberDialog {
    private final Member member;
    private final Stage dialog;
    private boolean confirmed = false;
    
    private TextField nameField;
    private PasswordField passwordField;
    
    public EditMemberDialog(Member member) {
        this.member = member;
        this.dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Member");
        dialog.setResizable(false);
        
        createContent();
    }
    
    private void createContent() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Member ID (read-only)
        Label idLabel = new Label("Member ID:");
        TextField idField = new TextField(String.valueOf(member.getId()));
        idField.setEditable(false);
        idField.setDisable(true);
        grid.add(idLabel, 0, 0);
        grid.add(idField, 1, 0);
        
        // Name (editable)
        Label nameLabel = new Label("Name:");
        nameField = new TextField(member.getName());
        grid.add(nameLabel, 0, 1);
        grid.add(nameField, 1, 1);
        
        // Password (editable)
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setText(member.getPassword());
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        
        // Buttons
        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #4caf7c; -fx-text-fill: white;");
        saveButton.setOnAction(e -> handleSave());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, 3);
        
        Scene scene = new Scene(grid);
        dialog.setScene(scene);
    }
    
    private void handleSave() {
        String name = nameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (name.isEmpty()) {
            showAlert("Error", "Name cannot be empty!");
            return;
        }
        
        if (password.isEmpty()) {
            showAlert("Error", "Password cannot be empty!");
            return;
        }
        
        confirmed = true;
        dialog.close();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Member showDialog() {
        dialog.showAndWait();
        return confirmed ? createUpdatedMember() : null;
    }
    
    private Member createUpdatedMember() {
        return new Member(member.getId(), nameField.getText().trim(), passwordField.getText().trim());
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
