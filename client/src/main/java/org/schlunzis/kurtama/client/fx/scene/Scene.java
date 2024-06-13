package org.schlunzis.kurtama.client.fx.scene;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.schlunzis.kurtama.client.fx.controller.LobbyController;
import org.schlunzis.kurtama.client.fx.controller.LoginController;
import org.schlunzis.kurtama.client.fx.controller.MainMenuController;
import org.schlunzis.kurtama.client.fx.controller.RegisterController;

/**
 * This enum lists all scenes of the application. Each entry stores information about the title key and the controller class of the scene.
 *
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Scene {

    LOGIN("Login", LoginController.class, "login.fxml"),
    MAIN("Main Menu", MainMenuController.class, "main.fxml"),
    REGISTER("Register", RegisterController.class, "register.fxml"),
    LOBBY("Lobby", LobbyController.class, "lobby.fxml");

    /**
     * Contains the lookup key for the title of the scene.
     */
    private final String titleKey;
    /**
     * Contains the controller class for the scene.
     */
    private final Class<?> controllerClass;

    private final String fxml;

}
