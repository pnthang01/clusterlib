package io.cluster.http.core;

import io.cluster.util.StringPool;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

public class HttpResponseUtil {

    static final String HEADER_LOCATION_NAME = "Location";
    static final String HEADER_CONNECTION_CLOSE = "Close";

    public static FullHttpResponse redirectPath(String uri)
            throws UnsupportedEncodingException {
        int i = uri.indexOf("/http");
        if (i > 0) {
			// String metaUri = uri.substring(0, i);
            // do something with metaUri, E.g:
            // /r/13083/142/zizgzlzmzqzlzizhzizrzoziznzhzozizgzjzrzgzozizizgzdzizlzhzdzizkzmzdzmzgzozjzm21zjzmzq1t1u1t20201v21zjzjzr

            String url = uri.substring(i + 1);
            // System.out.println(metaUri + " " + url) ;
            return redirect(URLDecoder.decode(url, StringPool.UTF_8));
        }
        return null;
    }

    public static FullHttpResponse redirect(String url) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                HttpResponseStatus.MOVED_PERMANENTLY);
        response.headers().set(HEADER_LOCATION_NAME, url);
        return response;
    }

    public static FullHttpResponse theHttpContent(String str) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(str.getBytes());
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, byteBuf);
        response.headers().set(CONTENT_TYPE, StringPool.MIME_TYPE_UTF8_TEXT);
        response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
        response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
        return response;
    }

    public static String getParamValue(String name, Map<String, List<String>> params) {
        return getParamValue(name, params, StringPool.BLANK);
    }

    public static String getParamValue(String name, Map<String, List<String>> params, String defaultVal) {
        List<String> vals = params.get(name);
        if (vals != null) {
            if (!vals.isEmpty()) {
                return vals.get(0);
            }
        }
        return defaultVal;
    }

    public static String getRemoteIP(ChannelHandlerContext ctx) {
        try {
            SocketAddress remoteAddress = ctx.channel().remoteAddress();
            String ip = remoteAddress.toString().split("/")[1].split(":")[0];
            return ip;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "0.0.0.0";
    }

    public static String getRequestIP(ChannelHandlerContext ctx, HttpRequest request) {
        String ipAddress;
        if (request.headers().get("X-Forwarded-For") != null) {
            ipAddress = request.headers().get("X-Forwarded-For");
        }

        ipAddress = HttpResponseUtil.getRemoteIP(ctx);

        return ipAddress;
    }
}
