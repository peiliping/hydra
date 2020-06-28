package com.github.hydra.client;


import com.github.hydra.constant.WebSocketSchema;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;


@Slf4j
public class NettyClient {


    private final Bootstrap bootstrap;

    private final ClientConfig config;

    private final EventLoopGroup eventLoopGroup;

    private final String url;


    public NettyClient(ClientConfig config) {

        this.bootstrap = new Bootstrap();
        this.config = config;
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap.group(this.eventLoopGroup);
        this.bootstrap.channel(NioSocketChannel.class);

        this.url = String.format("%s://%s:%s%s", this.config.getSchema().schema, this.config.getHost(), this.config.getPort(), this.config.getPath());

        this.bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {


            protected void initChannel(SocketChannel socketChannel) throws Exception {

                ChannelPipeline cp = socketChannel.pipeline();

                if (WebSocketSchema.WSS == config.getSchema()) {
                    SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                    cp.addLast("ssl", sslCtx.newHandler(socketChannel.alloc(), config.getHost(), config.getPort()));
                }

                cp.addLast("netty-log", new LoggingHandler("NettyLog", LogLevel.TRACE));
                cp.addLast("http-codec", new HttpClientCodec());
                cp.addLast("http-chunked", new ChunkedWriteHandler());
                cp.addLast("http-aggregator", new HttpObjectAggregator(64 * 1024));
                cp.addLast("ws-aggregator", new WebSocketFrameAggregator(64 * 1024));

                WebSocketClientProtocolConfig protocolConfig = WebSocketClientProtocolConfig.newBuilder()
                        .webSocketUri(URI.create(url))
                        .subprotocol(null)
                        .version(WebSocketVersion.V13)
                        .allowExtensions(true)
                        .customHeaders(EmptyHttpHeaders.INSTANCE)
                        .maxFramePayloadLength(64 * 1024)
                        .performMasking(true)
                        .allowMaskMismatch(false)
                        .handleCloseFrames(true)
                        .sendCloseFrame(WebSocketCloseStatus.NORMAL_CLOSURE)
                        .dropPongFrames(false)
                        .handshakeTimeoutMillis(10 * 1000L)
                        .forceCloseTimeoutMillis(-1)
                        .absoluteUpgradeUrl(false)
                        .build();
                cp.addLast("ws-protocol", new WebSocketClientProtocolHandler(protocolConfig));
                cp.addLast("ws-text", new TextFrameHandler(config.isUnCompressJSON()));
                cp.addLast("ws-binary", new BinaryFrameHandler(config.isUnCompressGzip()));
            }
        });
    }


    public void connect() {

        ChannelFuture channelFuture = this.bootstrap.connect(this.config.getHost(), this.config.getPort());
        ChannelManager.addChannelFuture(channelFuture);
        channelFuture.addListener((ChannelFutureListener) future -> {

            if (future.isSuccess()) {
                ChannelManager.addChannel(future.channel());
            } else {
                log.error("netty connect error : ", future.cause());
            }
        });
    }
}
