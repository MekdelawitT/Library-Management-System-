package libraryui.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import library.*;

import java.net.URL;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Use SQLite database for persistence
        DataHandler<Book> bookDataHandler = new SQLiteBookHandler();
        DataHandler<Member> memberDataHandler = new SQLiteMemberHandler();

        BookRepository bookRepository = new BookRepository(bookDataHandler);
        MemberRepository memberRepository = new MemberRepository(memberDataHandler);
        LibraryService libraryService = new LibraryService(memberRepository, bookRepository);

        // Load FXML: same package as App (libraryui.ui) then classpath root
        URL fxmlUrl = App.class.getResource("library.fxml");
        if (fxmlUrl == null) {
            fxmlUrl = App.class.getResource("/libraryui/ui/library.fxml");
        }
        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML not found: library.fxml. Ensure src/resources is marked as Resources and rebuild.");
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), 1000, 650);

        LibraryController controller = loader.getController();
        controller.setLibraryService(libraryService);

        stage.setTitle("Library Management Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
