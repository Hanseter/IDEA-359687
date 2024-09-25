package io.github.hanseter.startup;

import io.github.hanseter.startup.api.MainWindowProvider;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class FxApp extends Application {


    @SuppressWarnings("java:S2696")
    @Override
    public void start(Stage stage) throws InterruptedException {
        stage.onCloseRequestProperty().set(e -> {
            try {
                AppStarter.stopAndWait();
            } catch (BundleException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });
        stage.setTitle("Test App");
        stage.setScene(new Scene(new VBox(), 1080, 840));

        //don't do this in production :)
        while (AppStarter.framework.getState() != Bundle.ACTIVE) {
            Thread.sleep(200);
        }
        Platform.runLater(() ->
                AppStarter.framework.getBundleContext()
                        .registerService(MainWindowProvider.class, () -> stage, null)
        );
        stage.show();
    }


}
