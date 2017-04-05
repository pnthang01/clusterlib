package io.cluster.http;

import io.cluster.util.StringPool;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StaticFileHandler {

    private static final Logger LOGGER = LogManager.getLogger(StaticFileHandler.class.getName());

    static final String HTTP_HEADER_CACHE = "must_revalidate, private, max-age=";
    static final String HEADER_CONNECTION_CLOSE = "Close";

    static final byte[] BASE64GIF_BYTES = StringPool.BASE64_GIF_BLANK.getBytes();
    static byte[] CROSSDOMAINXML_BYTES;
    static byte[] ADXJAVASCRIPTTRACKING_BYTES;
    static byte[] FACEBOOKJAVASCRIPTTRACKING_BYTES;
    static byte[] SOCIALJAVASCRIPTTRACKING_BYTES;
    static int cacheHttpMaxAge = 7200;
    static String httpHeaderCache = "";

    static {
        httpHeaderCache = HTTP_HEADER_CACHE + cacheHttpMaxAge;
    }

    public static FullHttpResponse theBase64Image1pxGif() {
        ByteBuf byteBuf = Base64.decode(Unpooled.copiedBuffer(BASE64GIF_BYTES));
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, byteBuf);
        response.headers().set(CONTENT_TYPE, StringPool.MIME_TYPE_GIF);
        response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
        response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
        return response;
    }

    public static FullHttpResponse staticCrossdomainFileContent() {
        ByteBuf byteBuf = Unpooled.copiedBuffer(CROSSDOMAINXML_BYTES);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, byteBuf);
        response.headers().set(CONTENT_TYPE, StringPool.MIME_TYPE_XML);
        response.headers().set("cache-control", httpHeaderCache);
        response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
        response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
        return response;
    }

    public static FullHttpResponse staticADXJavascriptTrackingFileContent() {
        ByteBuf byteBuf = Unpooled.copiedBuffer(ADXJAVASCRIPTTRACKING_BYTES);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                byteBuf);
        response.headers().set(CONTENT_TYPE, StringPool.MIME_TYPE_JS);
        response.headers().set("cache-control", httpHeaderCache);
        response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
        response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
        return response;
    }

    public static FullHttpResponse staticFacebookJavascriptTrackingFileContent() {
        System.out.println("call staticFacebookJavascriptTrackingFileContent");
        ByteBuf byteBuf = Unpooled.copiedBuffer(FACEBOOKJAVASCRIPTTRACKING_BYTES);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                byteBuf);
        response.headers().set(CONTENT_TYPE, StringPool.MIME_TYPE_JS);
        response.headers().set("cache-control", httpHeaderCache);
        response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
        response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
        return response;
    }

    public static FullHttpResponse staticSocialJavascriptTrackingFileContent() {
        System.out.println("call static sicial script");
        ByteBuf byteBuf = Unpooled.copiedBuffer(SOCIALJAVASCRIPTTRACKING_BYTES);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                byteBuf);
        response.headers().set(CONTENT_TYPE, StringPool.MIME_TYPE_JS);
        response.headers().set("cache-control", httpHeaderCache);
        response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
        response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
        return response;
    }

    public static FullHttpResponse theJavaScriptContent(String str) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(str.getBytes());
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, byteBuf);
        response.headers().set(CONTENT_TYPE, StringPool.MIME_TYPE_JS);
        response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
        response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
        return response;
    }

    public static FullHttpResponse theJSONContent(String str) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(str.getBytes());
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, byteBuf);
        response.headers().set(CONTENT_TYPE, StringPool.MIME_TYPE_UTF8_JSON);
        response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
        response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
        return response;
    }
}
