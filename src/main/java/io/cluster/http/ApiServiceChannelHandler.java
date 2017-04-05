package io.cluster.http;

import io.cluster.http.core.HttpResponseUtil;
import io.cluster.util.StringPool;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApiServiceChannelHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LogManager.getLogger(ApiChannelHanderAdapter.class.getName());


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            //filter DDOS/bad/attacking requests       

            String uri = request.getUri();
            //System.out.println("===> uri: " + uri);
            if (uri.equalsIgnoreCase(UriMapper.FAVICON_URI)) {
                returnImage1pxGifResponse(ctx);
            } else {
                FullHttpResponse response = null;
                try {
                    response = UriMapper.responseToUri(ctx, null, uri);
                } catch (Exception e) {
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    LOGGER.error("AdxMobileLogChannelHandler responseToUri error", e);
                }
                if (response == null) {
                    response = HttpResponseUtil.theHttpContent(StringPool.BLANK);
                }

                // Write the response.				
                ChannelFuture future = ctx.write(response);
                ctx.flush();
                ctx.close();

                //Close the non-keep-alive connection after the write operation is done.
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        if (msg instanceof HttpContent) {
            if (msg instanceof LastHttpContent) {
                returnImage1pxGifResponse(ctx);
            }
        }
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

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
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

}
