package io.github.hanseter.startup.api;

import javafx.stage.Stage;

/**
 * A service to gain access to the main window of the JavaFx application.
 */
public interface MainWindowProvider {

    Stage getWindow();
}
