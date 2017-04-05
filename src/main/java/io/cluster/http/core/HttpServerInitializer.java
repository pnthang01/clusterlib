package io.cluster.http.core;

import io.cluster.http.ApiChannelHanderAdapter;
import io.cluster.http.ApiServiceChannelHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    ChannelHandler getLogChannelHandler() {
        return new ApiServiceChannelHandler();
    }

    public HttpServerInitializer() {
        super();
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline p = ch.pipeline();
        // Provides support for http objects:
        p.addLast("codec", new HttpServerCodec());
        // Deals with fragmentation in http traffic: 
        p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
        // Uncomment the following line if you want HTTPS
        //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        //engine.setUseClientMode(false);
        //p.addLast("ssl", new SslHandler(engine));
//        p.addLast("decoder", new HttpRequestDecoder());
//        // Uncomment the following line if you don't want to handle HttpChunks.
//        //p.addLast("aggregator", new HttpObjectAggregator(1048576));
//        p.addLast("encoder", new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content compression.
        //p.addLast("deflater", new HttpContentCompressor());
        p.addLast("handler", new ApiChannelHanderAdapter());
    }
}
