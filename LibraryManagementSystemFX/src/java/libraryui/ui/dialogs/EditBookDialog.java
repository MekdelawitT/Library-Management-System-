package libraryui.ui.dialogs;

import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import library.Book;

public class EditBookDialog {
    private final Book book;
    private final Stage dialog;
    private boolean confirmed = false;
    
    private TextField titleField;
    private TextField authorField;
    private TextField coverPathField;
    
    public EditBookDialog(Book book) {
        this.book = book;
        this.dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Book");
        dialog.setResizable(false);
        
        createContent();
    }
    
    private void createContent() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Book ID (read-only)
        Label idLabel = new Label("Book ID:");
        TextField idField = new TextField(String.valueOf(book.getId()));
        idField.setEditable(false);
        idField.setDisable(true);
        grid.add(idLabel, 0, 0);
        grid.add(idField, 1, 0);
        
        // Title (editable)
        Label titleLabel = new Label("Title:");
        titleField = new TextField(book.getTitle());
        grid.add(titleLabel, 0, 1);
        grid.add(titleField, 1, 1);
        
        // Author (editable)
        Label authorLabel = new Label("Author:");
        authorField = new TextField(book.getAuthor());
        grid.add(authorLabel, 0, 2);
        grid.add(authorField, 1, 2);
        
        // Cover path (editable via Choose)
        Label coverLabel = new Label("Cover:");
        coverPathField = new TextField(book.getCoverPath() != null ? book.getCoverPath() : "");
        coverPathField.setEditable(false);
        coverPathField.setPromptText("No image");
        Button chooseCoverButton = new Button("Choose image...");
        chooseCoverButton.setOnAction(e -> handleChooseCover());
        HBox coverBox = new HBox(8, coverPathField, chooseCoverButton);
        coverBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(coverLabel, 0, 3);
        grid.add(coverBox, 1, 3);
        
        // Buttons
        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #4caf7c; -fx-text-fill: white;");
        saveButton.setOnAction(e -> handleSave());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, 4);
        
        Scene scene = new Scene(grid);
        dialog.setScene(scene);
    }
    
    private void handleSave() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        
        if (title.isEmpty()) {
            showAlert("Error", "Title cannot be empty!");
            return;
        }
        
        if (author.isEmpty()) {
            showAlert("Error", "Author cannot be empty!");
            return;
        }
        
        confirmed = true;
        dialog.close();
    }
    
    private void handleChooseCover() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Book Cover Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        File file = chooser.showOpenDialog(dialog);
        if (file != null) {
            coverPathField.setText(file.getAbsolutePath());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Book showDialog() {
        dialog.showAndWait();
        return confirmed ? createUpdatedBook() : null;
    }
    
    private Book createUpdatedBook() {
        Book updated = new Book(book.getId(), titleField.getText().trim(), authorField.getText().trim());
        updated.setAvailable(book.isAvailable());
        String path = coverPathField.getText();
        if (path != null && !path.isBlank()) updated.setCoverPath(path.trim());
        return updated;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
