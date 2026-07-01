package hu.mrflow.labelanalyzer;


import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.List;
import atlantafx.base.theme.PrimerDark;


/**
 * Belépési pont.
 * Betölti a MainView.fxml-t és megjeleníti a főablakot.
 */
public class MainApp extends Application {

    private static final String[] ICON_SIZES = {"16", "24", "32", "48", "64", "128", "256", "512"};

    @Override
    public void start(Stage primaryStage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/hu/mrflow/labelanalyzer/view/MainView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 800);

        primaryStage.setTitle("LabelAnalyzer – Növényvédő Szer Engedélyelemző");
        primaryStage.getIcons().addAll(loadIcons());
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        applyDockOrTaskbarIcon();
    }

    private List<Image> loadIcons() {
        return java.util.Arrays.stream(ICON_SIZES)
                .map(size -> new Image(getClass().getResourceAsStream(
                        "/hu/mrflow/labelanalyzer/icon/icon-" + size + ".png")))
                .toList();
    }

    /**
     * A JavaFX Stage ikon csak az ablak fejlécét/tálcabejegyzését állítja be.
     * macOS-en a Dock ikont (és Windows-on az alkalmazás-tálca ikont futás közben,
     * pl. mvn javafx:run esetén) a java.awt.Taskbar API-n keresztül kell külön beállítani.
     */
    private void applyDockOrTaskbarIcon() {
        if (!Taskbar.isTaskbarSupported()) return;
        Taskbar taskbar = Taskbar.getTaskbar();
        if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) return;
        var image = Toolkit.getDefaultToolkit().createImage(
                getClass().getResource("/hu/mrflow/labelanalyzer/icon/icon-512.png"));
        taskbar.setIconImage(image);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

