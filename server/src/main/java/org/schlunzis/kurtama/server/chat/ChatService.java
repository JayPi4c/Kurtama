package org.schlunzis.kurtama.server.chat;

import lombok.extern.slf4j.Slf4j;
import org.schlunzis.kurtama.common.messages.chat.ClientChatMessage;
import org.schlunzis.kurtama.common.messages.chat.ServerChatMessage;
import org.schlunzis.kurtama.server.auth.AuthenticationService;
import org.schlunzis.kurtama.server.net.ClientMessageWrapper;
import org.schlunzis.kurtama.server.service.AbstractService;
import org.schlunzis.kurtama.server.user.ServerUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class ChatService extends AbstractService {

    public ChatService(ApplicationEventPublisher eventBus, AuthenticationService authenticationService) {
        super(eventBus, authenticationService);
    }

    @EventListener
    public void onClientChatMessage(ClientMessageWrapper<ClientChatMessage> cmw) {
        ClientChatMessage ccm = cmw.clientMessage();
        log.debug("Processing chat message {}", ccm);
        Optional<ServerUser> optionalUser = authenticationService.getUserForSession(cmw.session());
        if (optionalUser.isPresent())
            sendToAll(new ServerChatMessage(ccm.getChatID(), ccm.getNickname(), optionalUser.get().toDTO(), ccm.getMessage()));
        else
            log.warn("received ClientChatMessage without user for session!");
    }

}
