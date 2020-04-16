package com.github.hydra.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class NettyServer {


    private final ServerBootstrap bootstrap;

    private final ServerConfig config;

    private final EventLoopGroup parentGroup;

    private final EventLoopGroup childGroup;


    public NettyServer(ServerConfig config) {

        this.bootstrap = new ServerBootstrap();
        this.config = config;

        this.parentGroup = this.config.isEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        this.childGroup = this.config.isEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        this.bootstrap.group(this.parentGroup, this.childGroup);
        this.bootstrap.channel(this.config.isEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class);

        this.bootstrap.option(ChannelOption.AUTO_CLOSE, true);
        this.bootstrap.option(ChannelOption.SO_BACKLOG, 1024);

        this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {


            @Override protected void initChannel(SocketChannel socketChannel) throws Exception {

                ChannelPipeline cp = socketChannel.pipeline();
                cp.addLast("netty-log", new LoggingHandler("NettyLog", LogLevel.TRACE));
                cp.addLast("http-codec", new HttpServerCodec());
                cp.addLast("http-chunked", new ChunkedWriteHandler());
                cp.addLast("http-aggregator", new HttpObjectAggregator(64 * 1024));
                //cp.addLast("ws-compression", new WebSocketServerCompressionHandler()); 会导致内存泄露
                cp.addLast("ws-aggregator", new WebSocketFrameAggregator(64 * 1024));
                cp.addLast("ws-protocol", new WebSocketServerProtocolHandler(config.getPath(), null, true, 64 * 1024));
                cp.addLast("ws-text", new TextFrameHandler());
            }
        });
        this.bootstrap.childOption(ChannelOption.TCP_NODELAY, false);
    }


    public void start() {

        try {
            String url = String.format("%s://%s:%s", this.config.getSchema().schema, this.config.getHost(), this.config.getPort());
            log.info("WebSocketServer Starting . " + url);
            ChannelFuture future = this.bootstrap.bind(this.config.getPort());
            Channel channel = future.sync().channel();
            log.info("WebSocketServer Started .");
            channel.closeFuture().sync();
            log.info("WebSocketServer Closing .");
        } catch (Exception e) {
            log.info("WebSocketServer Error : " + e);
        } finally {
            this.parentGroup.shutdownGracefully();
            this.childGroup.shutdownGracefully();
            log.info("WebSocketServer Closed .");
        }
    }
}
