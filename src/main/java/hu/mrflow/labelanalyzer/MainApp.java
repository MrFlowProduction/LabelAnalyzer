package hu.mrflow.labelanalyzer;


import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import atlantafx.base.theme.PrimerDark;


/**
 * Belépési pont.
 * Betölti a MainView.fxml-t és megjeleníti a főablakot.
 */
public class MainApp extends Application {



    @Override
    public void start(Stage primaryStage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/hu/mrflow/labelanalyzer/view/MainView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 800);

        primaryStage.setTitle("LabelAnalyzer – Növényvédő Szer Engedélyelemző");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

