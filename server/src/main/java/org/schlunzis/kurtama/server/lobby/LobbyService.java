package org.schlunzis.kurtama.server.lobby;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.schlunzis.kurtama.common.LobbyInfo;
import org.schlunzis.kurtama.common.messages.IClientMessage;
import org.schlunzis.kurtama.common.messages.IServerMessage;
import org.schlunzis.kurtama.common.messages.lobby.client.CreateLobbyRequest;
import org.schlunzis.kurtama.common.messages.lobby.client.JoinLobbyRequest;
import org.schlunzis.kurtama.common.messages.lobby.client.LeaveLobbyRequest;
import org.schlunzis.kurtama.common.messages.lobby.server.*;
import org.schlunzis.kurtama.server.lobby.exception.LobbyNotFoundException;
import org.schlunzis.kurtama.server.lobby.exception.WrongLobbyPasswordException;
import org.schlunzis.kurtama.server.service.ClientMessageContext;
import org.schlunzis.kurtama.server.service.ServerMessageWrappers;
import org.schlunzis.kurtama.server.user.ServerUser;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LobbyService {

    private final LobbyManagement lobbyManagement;

    @EventListener
    public ServerMessageWrappers onCreateLobbyRequest(ClientMessageContext<CreateLobbyRequest> cmc) {
        CreateLobbyRequest request = cmc.getClientMessage();
        try {
            ServerLobby lobby = lobbyManagement.createLobby(request.name(), request.password(), cmc.getUser());
            cmc.respond(new LobbyCreatedSuccessfullyResponse(lobby.toDTO()));
            updateLobbyListInfo(cmc);
        } catch (Exception e) {
            log.info("Could not create lobby. No user found for session.");
            cmc.respond(new LobbyCreationFailedResponse());
        }
        return cmc.close();
    }

    @EventListener
    public ServerMessageWrappers onJoinLobbyRequest(ClientMessageContext<JoinLobbyRequest> cmc) {
        JoinLobbyRequest request = cmc.getClientMessage();

        try {
            ServerLobby lobby = lobbyManagement.joinLobby(request.lobbyID(), request.password(), cmc.getUser());
            cmc.respond(new JoinLobbySuccessfullyResponse(lobby.toDTO()));

            var userJoinedLobbyMessage = new UserJoinedLobbyMessage(lobby.toDTO());
            informUsersInLobby(userJoinedLobbyMessage, request.lobbyID(), cmc);
            updateLobbyListInfo(cmc);
        } catch (LobbyNotFoundException e) {
            log.info("Could not join lobby. Lobby not found.");
            cmc.respond(new JoinLobbyFailedResponse());
        } catch (WrongLobbyPasswordException e) {
            log.info("Could not join lobby. Wrong password.");
            cmc.respond(new JoinLobbyFailedResponse());
        } catch (Exception e) {
            log.error("Could not join lobby.", e);
            cmc.respond(new JoinLobbyFailedResponse());
        }
        return cmc.close();
    }

    @EventListener
    public ServerMessageWrappers onLeaveLobbyRequest(ClientMessageContext<LeaveLobbyRequest> cmc) {
        LeaveLobbyRequest request = cmc.getClientMessage();
        try {
            boolean lobbyEmpty = lobbyManagement.leaveLobby(request.lobbyID(), cmc.getUser());
            cmc.respond(new LeaveLobbySuccessfullyResponse());
            if (!lobbyEmpty) {
                ServerLobby lobby = lobbyManagement.getLobby(request.lobbyID());
                var userLeftLobbyMessage = new UserLeftLobbyMessage(lobby.toDTO());
                informUsersInLobby(userLeftLobbyMessage, request.lobbyID(), cmc);
            }
            updateLobbyListInfo(cmc);
        } catch (LobbyNotFoundException e) {
            log.info("Could not leave lobby. Lobby not found.");
            cmc.respond(new LobbyLeaveFailedResponse());
        } catch (Exception e) {
            log.error("Could not leave lobby.", e);
            cmc.respond(new LobbyLeaveFailedResponse());
        }
        return cmc.close();
    }

    /**
     * Updates the lobby list info for all users in the main menu.
     */

    private void updateLobbyListInfo(ClientMessageContext<? extends IClientMessage> cmc) {
        Collection<LobbyInfo> lobbyInfos = lobbyManagement.getAll().stream().map(ServerLobby::getInfo).toList();
        cmc.sendToAll(new LobbyListInfoMessage(lobbyInfos));
    }

    private void informUsersInLobby(IServerMessage message, UUID lobbyID, ClientMessageContext<? extends IClientMessage> cmc) {
        try {
            Collection<ServerUser> users = new ArrayList<>(lobbyManagement.getLobby(lobbyID).getUsers());
            users.remove(cmc.getUser());
            cmc.sendToMany(message, users);
        } catch (LobbyNotFoundException e) {
            log.debug("Could not inform other lobby members. Probably the last member in lobby left and the lobby was removed.");
        }
    }

}
