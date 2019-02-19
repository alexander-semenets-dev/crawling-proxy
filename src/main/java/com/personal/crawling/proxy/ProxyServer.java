package com.personal.crawling.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;
import spark.Spark;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ProxyServer {

    private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0";

    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    public static void main(String[] args) {
        Spark.get("/crawl-link", crawlLink());
    }

    private static Route crawlLink() {
        return (request, response) -> {
            final Map<String, String> parameters = request.params();
            final String crawlingUrl = parameters.get("url");

            return getCrawledContentJsonObject(crawlingUrl).toString();
        };
    }

    private static ObjectNode getCrawledContentJsonObject(String crawlingUrl) throws IOException {
        final okhttp3.Request request = new okhttp3.Request.Builder()
                .url(crawlingUrl)
                .header("User-Agent", USER_AGENT)
                .get()
                .build();

        final okhttp3.Response response = httpClient.newCall(request)
                .execute();

        final ObjectNode objectNode = objectMapper.createObjectNode()
                .put("statusCode", response.code())
                .put("statusMessage", response.message());

        if (response.isSuccessful()) {
            final byte[] contentBytes = Optional.ofNullable(response.body())
                    .map(responseBody -> {
                        try {
                            return responseBody.bytes();
                        } catch (IOException e) {
                            return new byte[0];
                        }
                    })
                    .orElse(new byte[0]);

            objectNode.put("contentBytes", contentBytes);
        }

        return objectNode;
    }

}




