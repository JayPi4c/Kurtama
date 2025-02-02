package org.schlunzis.kurtama.client.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.schlunzis.kurtama.client.fx.scene.Scene;
import org.schlunzis.kurtama.client.fx.scene.events.SceneChangeEvent;
import org.schlunzis.kurtama.client.service.ISessionService;
import org.schlunzis.kurtama.client.util.DialogFactory;
import org.schlunzis.kurtama.common.LobbyInfo;
import org.schlunzis.kurtama.common.messages.authentication.logout.LogoutRequest;
import org.schlunzis.kurtama.common.messages.lobby.client.CreateLobbyRequest;
import org.schlunzis.kurtama.common.messages.lobby.client.JoinLobbyRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainMenuController {

    private final ApplicationEventPublisher eventBus;
    private final ISessionService sessionService;
    private final DialogFactory dialogFactory;

    @FXML
    private ListView<LobbyInfo> lobbiesListView;

    @FXML
    private TextField lobbiesSearchField;

    @FXML
    private Button joinLobbyButton;
    @FXML
    private TextField lobbyNameField;
    @FXML
    private PasswordField lobbyPasswordField;

    @FXML
    private void initialize() {
        lobbiesListView.setItems(sessionService.getLobbyList());
        lobbiesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        lobbiesListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(LobbyInfo lobbyInfo, boolean empty) {
                super.updateItem(lobbyInfo, empty);
                if (empty || lobbyInfo == null) {
                    setText(null);
                } else {
                    setText(lobbyInfo.lobbyName() + " (" + lobbyInfo.users() + ")" + (lobbyInfo.passwordProtected() ? " 🔒" : ""));
                }
            }
        });
        lobbiesListView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) ->
                joinLobbyButton.setDisable(newValue == null)
        );
        lobbiesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
                joinLobby();
        });
        joinLobbyButton.setDisable(true);

    }

    @FXML
    private void searchLobbies() {
        log.debug("Searching for lobbies");
        String search = lobbiesSearchField.getText();
        if (search.isBlank())
            lobbiesListView.setItems(sessionService.getLobbyList());
        else
            lobbiesListView.setItems(sessionService.getLobbyList().filtered(li -> li.lobbyName().contains(search)));
    }

    @FXML
    private void logout() {
        log.info("Logout button clicked");
        eventBus.publishEvent(new LogoutRequest());
    }

    @FXML
    private void settings() {
        eventBus.publishEvent(new SceneChangeEvent(Scene.SETTINGS));
    }

    @FXML
    private void joinLobby() {
        LobbyInfo li = lobbiesListView.getSelectionModel().getSelectedItem();
        if (li == null)
            return;
        if (li.passwordProtected()) {
            dialogFactory.createPasswordDialog("lobby.join.auth.title", "lobby.join.auth.header", "lobby.join.auth.content")
                    .showAndWait()
                    .ifPresent(password -> {
                        log.info("Lobby password entered.");
                        eventBus.publishEvent(new JoinLobbyRequest(li.lobbyID(), password));
                    });
        } else
            eventBus.publishEvent(new JoinLobbyRequest(li.lobbyID(), ""));
    }

    @FXML
    private void createLobby() {
        String lobbyName = lobbyNameField.getText();
        String lobbyPassword = lobbyPasswordField.getText();
        if (!lobbyName.isBlank())
            eventBus.publishEvent(new CreateLobbyRequest(lobbyName, lobbyPassword));
    }

}
