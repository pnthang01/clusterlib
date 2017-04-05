package io.cluster.http;

import com.google.gson.reflect.TypeToken;
import io.cluster.http.annotation.HttpMethod;
import io.cluster.http.core.ControllerManager;
import io.cluster.http.core.HttpResponseUtil;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringPool;
import io.cluster.util.StringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UriMapper {

    private static final Logger LOGGER = LogManager.getLogger(UriMapper.class.getName());

    public static final String FAVICON_URI = "/favicon.ico";

    private static Type PARAM_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    public static FullHttpResponse responseToUri(ChannelHandlerContext ctx, FullHttpRequest request, String uri) {
        String ipAddress;
        if (request.headers().get("X-Forwarded-For") != null) {
            ipAddress = request.headers().get("X-Forwarded-For");
        } else {
            ipAddress = HttpResponseUtil.getRemoteIP(ctx);
        }
        Object result = handleUri(request, uri);

        if (null == result || result.equals(404) || result.equals(502)) {
            return HttpResponseUtil.theHttpContent(StringPool.BLANK);
        } else {
            return StaticFileHandler.theJSONContent(MethodUtil.toJson(result));
        }
    }

    private static Object handleUri(FullHttpRequest request, String uri) {
        try {
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
            Map<String, List<String>> finalParams = new HashMap();
            finalParams.putAll(queryStringDecoder.parameters());
            String dataStr = request.content().toString(StandardCharsets.UTF_8);
            //
            if (!StringUtil.isNullOrEmpty(dataStr)) {
                Map<String, String> fromJson = MethodUtil.fromJson(dataStr, PARAM_TYPE);
                for (Map.Entry<String, String> entry : fromJson.entrySet()) {
                    finalParams.put(entry.getKey(), Arrays.asList(entry.getValue()));
                }
            }
            HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod().name());
            return ControllerManager.invokeUri(queryStringDecoder.path(), httpMethod, finalParams);
        } catch (Exception ex) {
            LOGGER.error("Cannot handle " + uri + " with following error", ex);
            return 502;
        }
    }
}
