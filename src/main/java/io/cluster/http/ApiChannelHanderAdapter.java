/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http;

import io.cluster.http.core.HttpResponseUtil;
import io.cluster.util.StringPool;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ApiChannelHanderAdapter extends ChannelInboundHandlerAdapter {
    
    private static final Logger LOGGER = LogManager.getLogger(ApiChannelHanderAdapter.class.getName());
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        // Handle requests as switch cases. GET, POST,...
        // This post helps you to understanding switch case usage on strings:
        // http://stackoverflow.com/questions/338206/switch-statement-with-strings-in-java
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            String uri = fullHttpRequest.getUri();
            if (uri.equalsIgnoreCase(UriMapper.FAVICON_URI)) {
                returnImage1pxGifResponse(ctx);
            } else {
                FullHttpResponse response = null;
                try {
                    response = UriMapper.responseToUri(ctx, fullHttpRequest, uri);
                } catch (Exception e) {
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    LOGGER.error("ApiChannelHanderAdapter responseToUri error", e);
                }
                if (response == null) {
                    response = HttpResponseUtil.theHttpContent(StringPool.BLANK);
                }

                // Write the response.				
                ChannelFuture future = ctx.write(response);

                //Close the non-keep-alive connection after the write operation is done.
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } else if (msg instanceof HttpContent) {
            if (msg instanceof LastHttpContent) {
                returnImage1pxGifResponse(ctx);
            }
        }
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.flush();
        ctx.close();
    }
    
    void returnResponseByUri(ChannelHandlerContext ctx, FullHttpResponse response) {
        // Decide whether to close the connection or not.

    	//HttpHeaders requestHeaders = request.headers();
        //HttpHeaders responseHeaders = response.headers();
        // Write the response.
        ChannelFuture future = ctx.write(response);
        ctx.flush();

        //Close the non-keep-alive connection after the write operation is done.
        future.addListener(ChannelFutureListener.CLOSE);
    }

    void returnImage1pxGifResponse(ChannelHandlerContext ctx) {
        // Encode the cookie.
        //HttpHeaders requestHeaders = request.headers();

        // Build the response object.
        FullHttpResponse response = StaticFileHandler.theBase64Image1pxGif();

        // HttpHeaders responseHeaders = response.headers();
        // Browser sent no cookie.  Add some.
        // Write the response.
        ChannelFuture future = ctx.write(response);
        ctx.flush();
        ctx.close();

        //Close the non-keep-alive connection after the write operation is done.
        future.addListener(ChannelFutureListener.CLOSE);
    }
}
