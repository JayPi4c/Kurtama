package org.schlunzis.kurtama.server.chat;

import lombok.RequiredArgsConstructor;
import org.schlunzis.kurtama.common.messages.authentication.login.LoginSuccessfulResponse;
import org.schlunzis.kurtama.common.messages.chat.ServerChatMessage;
import org.schlunzis.kurtama.server.service.SecondaryRequestContext;
import org.schlunzis.kurtama.server.service.ServerMessageWrappers;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecondaryChatService {

    @EventListener
    public ServerMessageWrappers onLoginSuccessfulResponse(SecondaryRequestContext<LoginSuccessfulResponse> src) {
        src.respondAdditionally(new ServerChatMessage(new UUID(0, 0), "SERVER", null, "Welcome to the chat!"));
        return src.close();
    }

}
