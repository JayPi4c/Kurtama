package org.schlunzis.kurtama.client.net;

import lombok.RequiredArgsConstructor;
import org.schlunzis.kurtama.client.events.ClientReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NetworkStartThread extends Thread {

    private final INetworkClient networkClient;

    @EventListener
    public void onClientReadyEvent(ClientReadyEvent event) {
        this.start();
    }

    @Override
    public void run() {
        networkClient.start();
    }

}