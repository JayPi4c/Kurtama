package org.schlunzis.kurtama.client.net.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.schlunzis.kurtama.client.events.ConnectionLostEvent;
import org.schlunzis.kurtama.client.events.ConnectionStatusEvent;
import org.schlunzis.kurtama.client.net.INetworkClient;
import org.schlunzis.kurtama.client.net.ServerMessageDispatcher;
import org.schlunzis.kurtama.common.messages.IClientMessage;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public final class NettyClient implements INetworkClient {

    private final ApplicationEventPublisher eventBus;
    private final EventLoopGroup group;
    private final Bootstrap b;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int port;
    private final String host;
    private ChannelFuture f;
    @Getter
    private volatile boolean intentionallyStopped = false;

    public NettyClient(ServerMessageDispatcher serverMessageDispatcher, ApplicationEventPublisher eventBus, String host, int port) {
        this.eventBus = eventBus;
        this.host = host;
        this.port = port;
        group = new NioEventLoopGroup();

        b = new Bootstrap();
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        b.group(group) // Set EventLoopGroup to handle all events for client.
                .channel(NioSocketChannel.class)// Use NIO to accept new connections.
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        p.addLast(new LengthFieldPrepender(4));
                        p.addLast(new StringDecoder());
                        p.addLast(new StringEncoder());
                        // This is our custom client handler which will have logic for chat.
                        p.addLast(new ClientHandler(serverMessageDispatcher) {
                            @Override
                            public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                                super.handlerRemoved(ctx);
                                if (!intentionallyStopped) {
                                    eventBus.publishEvent(new ConnectionStatusEvent(ConnectionStatusEvent.Status.FAILED));
                                    eventBus.publishEvent(new ConnectionLostEvent());
                                }
                            }
                        });
                    }
                });
    }

    @Override
    public void start() {
        eventBus.publishEvent(new ConnectionStatusEvent(ConnectionStatusEvent.Status.CONNECTING));
        f = b.connect(host, port);
        f.addListener(_ -> {
            if (f.isCancelled()) {
                log.info("Connection cancelled by user.");
                close(ConnectionStatusEvent.Status.NOT_CONNECTED);
            } else if (!f.isSuccess()) {
                log.error("Connection failed!", f.cause());
                close(ConnectionStatusEvent.Status.FAILED);
            } else {
                log.info("Connected to server.");
                eventBus.publishEvent(new ConnectionStatusEvent(ConnectionStatusEvent.Status.CONNECTED));
            }
        });
    }

    @Override
    public void close(ConnectionStatusEvent.Status status) {
        log.debug("Closing network client");
        intentionallyStopped = true;
        eventBus.publishEvent(new ConnectionStatusEvent(status));
        f.channel().close();
        group.shutdownGracefully();
    }

    @Override
    public void sendMessage(IClientMessage clientMessage) {
        try {
            String msg = objectMapper.writeValueAsString(clientMessage);
            log.info("Sending message {}", msg);
            f.sync().channel().writeAndFlush(msg);
        } catch (JsonProcessingException e) {
            log.error("Failed to send message. Could not create JSON from object.", e);
        } catch (InterruptedException e) {
            log.error("Failed to send message", e);
            Thread.currentThread().interrupt();
        }
    }
}
