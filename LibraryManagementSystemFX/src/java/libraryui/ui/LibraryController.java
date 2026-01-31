package libraryui.ui;

import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import library.Book;
import library.BorrowedBook;
import library.LibraryReport;
import library.LibraryService;
import library.Member;
import library.SQLiteBorrowedBookHandler;
import library.SQLiteConnectionManager;
import libraryui.ui.dialogs.EditBookDialog;
import libraryui.ui.dialogs.EditMemberDialog;

import java.io.File;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LibraryController {

    // View containers
    @FXML private StackPane contentArea;
    @FXML private VBox booksView;
    @FXML private VBox membersView;
    @FXML private VBox addBookView;
    @FXML private VBox registerMemberView;
    @FXML private VBox borrowReturnView;

    // Books tab
    @FXML private TextField bookSearchField;
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, Integer> bookIdColumn;
    @FXML private TableColumn<Book, String> bookTitleColumn;
    @FXML private TableColumn<Book, String> bookAuthorColumn;
    @FXML private TableColumn<Book, String> bookStatusColumn;
    @FXML private TableColumn<Book, String> bookBorrowerColumn;
    @FXML private TableColumn<Book, String> bookDueDateColumn;
    @FXML private TableColumn<Book, Void> bookCoverColumn;
    @FXML private TableColumn<Book, Void> bookEditColumn;
    @FXML private TableColumn<Book, Void> bookDeleteColumn;

    // Members tab
    @FXML private TextField memberSearchField;
    @FXML private TableView<Member> membersTable;
    @FXML private TableColumn<Member, Integer> memberIdColumn;
    @FXML private TableColumn<Member, String> memberNameColumn;
    @FXML private TableColumn<Member, String> memberPasswordColumn;
    @FXML private TableColumn<Member, Double> memberBalanceColumn;
    @FXML private TableColumn<Member, Void> memberEditColumn;
    @FXML private TableColumn<Member, Void> memberDeleteColumn;

    // Add Book form
    @FXML private TextField bookIdField;
    @FXML private TextField bookTitleField;
    @FXML private TextField bookAuthorField;
    @FXML private TextField bookCoverPathField;

    // Register Member form
    @FXML private TextField memberIdField;
    @FXML private TextField memberNameField;
    @FXML private TextField memberPasswordField;

    // Borrow/Return
    @FXML private TextField borrowMemberIdField;
    @FXML private TextField borrowBookIdField;
    
    // Fine Management
    @FXML private TextField fineMemberIdField;
    @FXML private TextField fineAmountField;
    
    // Due Date Information
    @FXML private TextField dueDateBookIdField;
    @FXML private Label dueDateLabel;

    // Output areas for each view
    @FXML private TextArea outputArea;
    @FXML private TextArea membersOutputArea;
    @FXML private TextArea addBookOutputArea;
    @FXML private TextArea registerMemberOutputArea;
    @FXML private TextArea borrowReturnOutputArea;

    private LibraryService libraryService;
    
    // Timestamp formatter for absolute time logging
    private DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("[HH:mm:ss] ");

    // Backing lists for tables
    private javafx.collections.ObservableList<Book> bookItems;
    private javafx.collections.ObservableList<Member> memberItems;
    private FilteredList<Book> filteredBooks;
    private FilteredList<Member> filteredMembers;
    
    // Session-level borrowing tracking (fallback when borrower not in member's list)
    private final java.util.Map<Integer, Integer> bookBorrowerMap = new java.util.HashMap<>(); // bookId -> memberId
    /** Selected cover image path when adding a book (FileChooser). */
    private String selectedCoverPath;

    @FXML
    private void initialize() {
        // Setup table columns
        bookIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        bookTitleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        bookAuthorColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        bookStatusColumn.setCellValueFactory(data -> {
            String status = data.getValue().isAvailable() ? "Available" : "Borrowed";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        // Show borrower information by looking up who borrowed this book
        bookBorrowerColumn.setCellValueFactory(data -> {
            Book book = data.getValue();
            if (book.isAvailable() || libraryService == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
            
            // First try: Get current borrower directly from database (most reliable)
            SQLiteBorrowedBookHandler borrowedBookHandler = new SQLiteBorrowedBookHandler();
            int currentBorrowerId = borrowedBookHandler.getCurrentBorrowerId(book.getId());
            if (currentBorrowerId != -1) {
                // Find the member by ID
                for (Member member : libraryService.getAllMembers()) {
                    if (member.getId() == currentBorrowerId) {
                        return new javafx.beans.property.SimpleStringProperty(member.getName());
                    }
                }
            }
            
            // Second try: Use session-level tracking map as fallback
            Integer borrowerId = bookBorrowerMap.get(book.getId());
            if (borrowerId != null) {
                // Find the member by ID
                for (Member member : libraryService.getAllMembers()) {
                    if (member.getId() == borrowerId) {
                        return new javafx.beans.property.SimpleStringProperty(member.getName());
                    }
                }
            }
            
            // Third try: Find which member borrowed this book through normal relationships
            for (Member member : libraryService.getAllMembers()) {
                for (BorrowedBook borrowedBook : member.getBorrowedBooks()) {
                    if (borrowedBook.getBook().getId() == book.getId()) {
                        return new javafx.beans.property.SimpleStringProperty(member.getName());
                    }
                }
            }
            
            // Fallback: Book is marked as borrowed but we can't determine the borrower
            if (!book.isAvailable()) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
            
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Due date column: show due date for borrowed books from database
        bookDueDateColumn.setCellValueFactory(data -> {
            Book book = data.getValue();
            if (book.isAvailable() || libraryService == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
            
            // Get due date from database for persistence
            SQLiteBorrowedBookHandler borrowedBookHandler = new SQLiteBorrowedBookHandler();
            LocalDate dueDate = borrowedBookHandler.getDueDateForBook(book.getId());
            if (dueDate != null) {
                return new javafx.beans.property.SimpleStringProperty(dueDate.toString());
            }
            
            return new javafx.beans.property.SimpleStringProperty("");
        });

        memberIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        memberNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        memberPasswordColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPassword()));
        memberBalanceColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getBalance()).asObject());

        // Cover column: display ImageView from cover file path
        setupBookCoverColumn();
        // Setup edit columns
        setupBookEditColumn();
        setupBookDeleteColumn();
        setupMemberEditColumn();
        setupMemberDeleteColumn();
        
        // Show books view by default
        showBooksView();
        // Remove initialization message to start with empty log
    }

    public void setLibraryService(LibraryService libraryService) {
        this.libraryService = libraryService;

        // Load table data from SQLite (persistent DB path shown so user knows where data is stored)
        bookItems = FXCollections.observableArrayList(libraryService.getAllBooks());
        filteredBooks = new FilteredList<>(bookItems, p -> true);
        booksTable.setItems(filteredBooks);

        memberItems = FXCollections.observableArrayList(libraryService.getAllMembers());
        filteredMembers = new FilteredList<>(memberItems, p -> true);
        membersTable.setItems(filteredMembers);

        // Connect search fields
        bookSearchField.textProperty().addListener((obs, old, nw) -> {
            String query = nw == null ? "" : nw.trim().toLowerCase();
            filteredBooks.setPredicate(book -> {
                if (book == null) return false;
                if (query.isEmpty()) return true;
                return book.getTitle().toLowerCase().contains(query) ||
                        book.getAuthor().toLowerCase().contains(query);
            });
        });

        memberSearchField.textProperty().addListener((obs, old, nw) -> {
            String query = nw == null ? "" : nw.trim().toLowerCase();
            filteredMembers.setPredicate(member -> {
                if (member == null) return false;
                if (query.isEmpty()) return true;
                return member.getName().toLowerCase().contains(query);
            });
        });

        // Show where data is stored (persists after closing IDE)
        appendOutputToAll("Data stored at: " + SQLiteConnectionManager.getDatabasePath());
    }

    // View switching methods
    @FXML private void showBooksView() {
        hideAllViews();
        booksView.setVisible(true);
    }

    @FXML private void showMembersView() {
        hideAllViews();
        membersView.setVisible(true);
    }

    @FXML private void showAddBookView() {
        hideAllViews();
        addBookView.setVisible(true);
    }

    @FXML private void showRegisterMemberView() {
        hideAllViews();
        registerMemberView.setVisible(true);
    }

    @FXML private void showBorrowReturnView() {
        hideAllViews();
        borrowReturnView.setVisible(true);
    }

    private void hideAllViews() {
        booksView.setVisible(false);
        membersView.setVisible(false);
        addBookView.setVisible(false);
        registerMemberView.setVisible(false);
        borrowReturnView.setVisible(false);
    }

    // Add book
    @FXML
    private void handleAddBook() {
        try {
            int id = Integer.parseInt(bookIdField.getText());
            String title = bookTitleField.getText();
            String author = bookAuthorField.getText();

            // Prevent duplicate IDs (check both UI list and DB)
            if (libraryService.getAllBooks().stream().anyMatch(b -> b.getId() == id)) {
                appendOutputToAll("Error adding book: A book with ID " + id + " already exists.");
                bookIdField.clear();
                return;
            }

            Book book = new Book(id, title, author);
            // Set cover path if user selected an image
            if (selectedCoverPath != null && !selectedCoverPath.isBlank()) {
                book.setCoverPath(selectedCoverPath.trim());
            }
            libraryService.addBook(book);
            // Reload table from SQLite so TableView and report stay in sync with persisted data
            refreshBooksTable();
            appendOutputToAll("Book added: " + title);

            // Clear input fields and cover selection
            bookIdField.clear();
            bookTitleField.clear();
            bookAuthorField.clear();
            bookCoverPathField.clear();
            selectedCoverPath = null;
        } catch (Exception e) {
            appendOutputToAll("Error adding book: " + e.getMessage());
            bookIdField.clear();
            bookTitleField.clear();
            bookAuthorField.clear();
            bookCoverPathField.clear();
            selectedCoverPath = null;
        }
    }

    // Register member
    @FXML
    private void handleRegisterMember() {
        try {
            int id = Integer.parseInt(memberIdField.getText());
            String name = memberNameField.getText();
            String password = memberPasswordField.getText();

            if (memberItems.stream().anyMatch(m -> m.getId() == id)) {
                appendOutputToAll("Error registering member: A member with ID " + id + " already exists.");
                // Clear the conflicting ID so the user can type a new one
                memberIdField.clear();
                return;
            }

            Member member = new Member(id, name, password);
            libraryService.registerMember(member);
            refreshMembersTable();
            appendOutputToAll("Member registered: " + name);

            memberIdField.clear();
            memberNameField.clear();
            memberPasswordField.clear();
        } catch (Exception e) {
            appendOutputToAll("Error registering member: " + e.getMessage());
            // Clear all input fields on error
            memberIdField.clear();
            memberNameField.clear();
            memberPasswordField.clear();
        }
    }

    // Borrow/Return
    @FXML
    private void handleBorrowBook() {
        try {
            int memberId = Integer.parseInt(borrowMemberIdField.getText());
            int bookId = Integer.parseInt(borrowBookIdField.getText());

            Member member = libraryService.getAllMembers().stream()
                    .filter(m -> m.getId() == memberId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No member found with ID " + memberId));

            Book book = libraryService.getAllBooks().stream()
                    .filter(b -> b.getId() == bookId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No book found with ID " + bookId));

            // Use default due date (14 days from now) - could be enhanced with DatePicker
            java.time.LocalDate dueDate = java.time.LocalDate.now().plusWeeks(2);
            
            // Delegate to LibraryService - NO business logic in UI
            libraryService.borrowBook(member, book, dueDate);
            
            // Track the borrowing relationship for borrower column display
            bookBorrowerMap.put(bookId, memberId);
            
            appendOutputToAll("Book borrowed: " + member.getName() + " (ID " + memberId + ") borrowed \"" + book.getTitle() + "\" (Book ID " + bookId + "). Due: " + dueDate);

            // Refresh books table to reflect availability
            refreshBooksTable();

            borrowMemberIdField.clear();
            borrowBookIdField.clear();
        } catch (Exception e) {
            appendOutputToAll("Error borrowing book: " + e.getMessage());
            borrowMemberIdField.clear();
            borrowBookIdField.clear();
        }
    }

    @FXML
    private void handleReturnBook() {
        try {
            int memberId = Integer.parseInt(borrowMemberIdField.getText());
            int bookId = Integer.parseInt(borrowBookIdField.getText());

            Member member = libraryService.getAllMembers().stream()
                    .filter(m -> m.getId() == memberId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No member found with ID " + memberId));

            Book book = libraryService.getAllBooks().stream()
                    .filter(b -> b.getId() == bookId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No book found with ID " + bookId));

            // Delegate to LibraryService - enforces fine rules automatically
            libraryService.returnBook(member, book);
            
            // Remove the borrowing relationship from tracking map
            bookBorrowerMap.remove(bookId);
            
            appendOutputToAll("Book returned: " + member.getName() + " returned \"" + book.getTitle() + "\".");

            // Refresh books table to reflect availability
            refreshBooksTable();

            borrowMemberIdField.clear();
            borrowBookIdField.clear();
        } catch (IllegalStateException e) {
            // Handle fine-related exceptions specifically
            appendOutputToAll("Cannot return book: " + e.getMessage());
            // Don't clear fields so user can try again after paying fines
        } catch (Exception e) {
            appendOutputToAll("Error returning book: " + e.getMessage());
            borrowMemberIdField.clear();
            borrowBookIdField.clear();
        }
    }

    // Reports: always use fresh data from SQLite (no cached or ObservableList counts)
    @FXML
    private void handleGenerateReport() {
        try {
            refreshBooksTable();
            refreshMembersTable();
            LibraryReport report = libraryService.getReport();
            appendOutputToAll("Report generated:");
            appendOutputToAll("  Total books: " + report.getTotalBooks());
            appendOutputToAll("  Total members: " + report.getTotalMembers());
        } catch (Exception e) {
            appendOutputToAll("Error generating report: " + e.getMessage());
        }
    }

    private void refreshBooksTable() {
        if (libraryService == null) return;
        bookItems.setAll(libraryService.getAllBooks());
        // filteredBooks already wraps bookItems; just keep predicate
        booksTable.setItems(filteredBooks);
        // Force refresh of the table to update borrower column
        booksTable.refresh();
    }

    private void refreshMembersTable() {
        if (libraryService == null) return;
        memberItems.setAll(libraryService.getAllMembers());
        membersTable.setItems(filteredMembers);
        // Force refresh of the table
        membersTable.refresh();
    }

    /** Opens FileChooser to select a book cover image; stores path in field and selectedCoverPath. */
    @FXML
    private void handleChooseCover() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Book Cover Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        File file = chooser.showOpenDialog(bookCoverPathField.getScene().getWindow());
        if (file != null) {
            selectedCoverPath = file.getAbsolutePath();
            bookCoverPathField.setText(selectedCoverPath);
        }
    }

    private void appendOutput(String message) {
        String timestampedMessage = addTimestamp(message);
        outputArea.appendText(timestampedMessage + "\n");
    }

    private void appendOutputToAll(String message) {
        String timestampedMessage = addTimestamp(message);
        outputArea.appendText(timestampedMessage + "\n");
        membersOutputArea.appendText(timestampedMessage + "\n");
        addBookOutputArea.appendText(timestampedMessage + "\n");
        registerMemberOutputArea.appendText(timestampedMessage + "\n");
        borrowReturnOutputArea.appendText(timestampedMessage + "\n");
    }
    
    /**
     * Cover column: displays book cover image from file path (TEXT in DB).
     * Uses ImageView; empty cell if no cover path.
     */
    private void setupBookCoverColumn() {
        if (bookCoverColumn == null) return;
        bookCoverColumn.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(48);
                imageView.setFitHeight(64);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Book book = getTableRow().getItem();
                String path = book.getCoverPath();
                if (path == null || path.isBlank()) {
                    setGraphic(null);
                    return;
                }
                try {
                    Image img = new Image(new File(path).toURI().toString());
                    imageView.setImage(img);
                    setGraphic(imageView);
                } catch (Exception e) {
                    setGraphic(null);
                }
            }
        });
    }

    private void setupBookEditColumn() {
        bookEditColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final ImageView editIcon = new ImageView(new Image("/icons/edit.png"));
            {
                editIcon.setFitWidth(16);
                editIcon.setFitHeight(16);
                editIcon.setPreserveRatio(true);
                editButton.setGraphic(editIcon);
                editButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                editButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    handleEditBook(book);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });
    }
    
    private void setupMemberEditColumn() {
        memberEditColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final ImageView editIcon = new ImageView(new Image("/icons/edit.png"));
            {
                editIcon.setFitWidth(16);
                editIcon.setFitHeight(16);
                editIcon.setPreserveRatio(true);
                editButton.setGraphic(editIcon);
                editButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                editButton.setOnAction(event -> {
                    Member member = getTableView().getItems().get(getIndex());
                    handleEditMember(member);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });
    }
    
    private void handleEditBook(Book book) {
        try {
            EditBookDialog dialog = new EditBookDialog(book);
            Book updatedBook = dialog.showDialog();
            
            if (updatedBook != null) {
                // Remove old book and add updated one
                libraryService.removeBook(book);
                libraryService.addBook(updatedBook);
                refreshBooksTable();
                appendOutputToAll("Book updated: " + updatedBook.getTitle());
            }
        } catch (Exception e) {
            appendOutputToAll("Error editing book: " + e.getMessage());
        }
    }
    
    private void handleEditMember(Member member) {
        try {
            EditMemberDialog dialog = new EditMemberDialog(member);
            Member updatedMember = dialog.showDialog();
            
            if (updatedMember != null) {
                // Update member in repository
                libraryService.registerMember(updatedMember);
                refreshMembersTable();
                appendOutputToAll("Member updated: " + updatedMember.getName());
            }
        } catch (Exception e) {
            appendOutputToAll("Error editing member: " + e.getMessage());
        }
    }
    
    private void setupBookDeleteColumn() {
        bookDeleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button();
            private final ImageView deleteIcon = new ImageView(new Image("/icons/delete.png"));
            {
                deleteIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16);
                deleteIcon.setPreserveRatio(true);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    handleDeleteBook(book);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }
    
    private void setupMemberDeleteColumn() {
        memberDeleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button();
            private final ImageView deleteIcon = new ImageView(new Image("/icons/delete.png"));
            {
                deleteIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16);
                deleteIcon.setPreserveRatio(true);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteButton.setOnAction(event -> {
                    Member member = getTableView().getItems().get(getIndex());
                    handleDeleteMember(member);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }
    
    private void handleDeleteBook(Book book) {
        try {
            // Check if book is borrowed
            if (!book.isAvailable()) {
                appendOutputToAll("Cannot delete book '" + book.getTitle() + "' - it is currently borrowed.");
                return;
            }
            
            // Remove book from library service
            libraryService.removeBook(book);
            refreshBooksTable();
            appendOutputToAll("Book deleted: " + book.getTitle());
        } catch (Exception e) {
            appendOutputToAll("Error deleting book: " + e.getMessage());
        }
    }
    
    private void handleDeleteMember(Member member) {
        try {
            // Check if member has borrowed books
            if (!member.getBorrowedBooks().isEmpty()) {
                appendOutputToAll("Cannot delete member '" + member.getName() + "' - they have " + 
                    member.getBorrowedBooks().size() + " borrowed books.");
                return;
            }
            
            // Remove member from library service
            libraryService.removeMember(member);
            refreshMembersTable();
            appendOutputToAll("Member deleted: " + member.getName());
        } catch (Exception e) {
            appendOutputToAll("Error deleting member: " + e.getMessage());
        }
    }
    
    private String addTimestamp(String message) {
        return LocalDateTime.now().format(timestampFormatter) + message;
    }
    
    // Fine Management Handlers
    @FXML
    private void handleAddFine() {
        try {
            int memberId = Integer.parseInt(fineMemberIdField.getText());
            double amount = Double.parseDouble(fineAmountField.getText());

            Member member = libraryService.getAllMembers().stream()
                    .filter(m -> m.getId() == memberId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No member found with ID " + memberId));

            // Delegate to LibraryService - NO business logic in UI
            member.addFine(amount);
            libraryService.registerMember(member);
            refreshMembersTable();
            
            appendOutputToAll("Fine added: $" + String.format("%.2f", amount) + " to " + member.getName() + " (ID " + memberId + "). New balance: $" + String.format("%.2f", member.getBalance()));

            fineMemberIdField.clear();
            fineAmountField.clear();
        } catch (Exception e) {
            appendOutputToAll("Error adding fine: " + e.getMessage());
            fineMemberIdField.clear();
            fineAmountField.clear();
        }
    }

    @FXML
    private void handlePayFine() {
        try {
            int memberId = Integer.parseInt(fineMemberIdField.getText());
            double amount = Double.parseDouble(fineAmountField.getText());

            Member member = libraryService.getAllMembers().stream()
                    .filter(m -> m.getId() == memberId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No member found with ID " + memberId));

            // Delegate to LibraryService - NO business logic in UI
            libraryService.payFine(member, amount);
            refreshMembersTable();
            
            appendOutputToAll("Fine paid: $" + String.format("%.2f", amount) + " by " + member.getName() + " (ID " + memberId + "). New balance: $" + String.format("%.2f", member.getBalance()));

            fineMemberIdField.clear();
            fineAmountField.clear();
        } catch (Exception e) {
            appendOutputToAll("Error paying fine: " + e.getMessage());
            fineMemberIdField.clear();
            fineAmountField.clear();
        }
    }

    @FXML
    private void handleClearFine() {
        try {
            int memberId = Integer.parseInt(fineMemberIdField.getText());

            Member member = libraryService.getAllMembers().stream()
                    .filter(m -> m.getId() == memberId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No member found with ID " + memberId));

            // Delegate to LibraryService - NO business logic in UI
            libraryService.clearFine(member);
            refreshMembersTable();
            
            appendOutputToAll("Fine cleared for " + member.getName() + " (ID " + memberId + "). Balance reset to $0.00");

            fineMemberIdField.clear();
            fineAmountField.clear();
        } catch (Exception e) {
            appendOutputToAll("Error clearing fine: " + e.getMessage());
            fineMemberIdField.clear();
            fineAmountField.clear();
        }
    }

    // Due Date Handler
    @FXML
    private void handleCheckDueDate() {
        try {
            int bookId = Integer.parseInt(dueDateBookIdField.getText());

            // Get due date from database through handler (could be moved to LibraryService)
            SQLiteBorrowedBookHandler borrowedBookHandler = new SQLiteBorrowedBookHandler();
            LocalDate dueDate = borrowedBookHandler.getDueDateForBook(bookId);
            
            if (dueDate != null) {
                dueDateLabel.setText(dueDate.toString());
                // Get borrower info for complete information
                int borrowerId = borrowedBookHandler.getCurrentBorrowerId(bookId);
                if (borrowerId != -1) {
                    Member member = libraryService.getAllMembers().stream()
                            .filter(m -> m.getId() == borrowerId)
                            .findFirst()
                            .orElse(null);
                    if (member != null) {
                        appendOutputToAll("Due date for Book ID " + bookId + ": " + dueDate + " (Borrowed by: " + member.getName() + ")");
                        return;
                    }
                }
                appendOutputToAll("Due date for Book ID " + bookId + ": " + dueDate);
                return;
            }

            // If not found in database, check if book exists and its status
            Book book = libraryService.getAllBooks().stream()
                    .filter(b -> b.getId() == bookId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No book found with ID " + bookId));

            if (!book.isAvailable()) {
                dueDateLabel.setText("Book borrowed but due date not available in database");
                appendOutputToAll("Book ID " + bookId + " is borrowed but due date information not found in database.");
            } else {
                dueDateLabel.setText("Book is available");
                appendOutputToAll("Book ID " + bookId + " is currently available.");
            }

        } catch (Exception e) {
            appendOutputToAll("Error checking due date: " + e.getMessage());
            dueDateLabel.setText("Error");
        }
    }
}
