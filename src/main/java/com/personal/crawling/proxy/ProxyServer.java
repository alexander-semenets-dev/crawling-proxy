package com.personal.crawling.proxy;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;
import spark.Spark;

import java.io.IOException;
import java.text.MessageFormat;

import static java.util.Objects.isNull;

public class ProxyServer {

    private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0";

    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    public static void main(String[] args) {
        Spark.get("/crawl-link", crawlLink());
        Spark.exception(CrawlingException.class, (exception, request, response) -> {
            response.status(500);
            response.body(exception.getMessage());
        });
    }

    private static Route crawlLink() {
        return (request, response) -> {
            final String crawlingUrl = request.queryParams("url");

            return getCrawledContentJsonObject(crawlingUrl);
        };
    }

    private static byte[] getCrawledContentJsonObject(String crawlingUrl) {
        try {
            final okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(crawlingUrl)
                    .header("User-Agent", USER_AGENT)
                    .get()
                    .build();

            final okhttp3.Response response = httpClient.newCall(request)
                    .execute();

            throwCrawlingExceptionIfResponseNotSuccessful(crawlingUrl, response);

            return getResponseBodyBytes(response);
        } catch (Exception e) {
            final String errorMessage = MessageFormat.format(
                    "Cannot crawl url {0}: {1}",
                    crawlingUrl,
                    e.getMessage()
            );

            throw new CrawlingException(errorMessage);
        }
    }

    private static void throwCrawlingExceptionIfResponseNotSuccessful(
            String crawlingUrl,
            Response response
    ) {
        if (response.isSuccessful()) {
            return;
        }

        final String errorMessage = MessageFormat.format(
                "Cannot crawl url {0}: status code - {1}, status message - {2}",
                crawlingUrl,
                response.code(),
                response.message()
        );

        tryCloseResponse(response);

        throw new CrawlingException(errorMessage);
    }

    private static byte[] getResponseBodyBytes(Response response) throws IOException {
        if (isNull(response.body())) {
            return new byte[0];
        }

        final byte[] responseBodyBytes = response.body().bytes();

        tryCloseResponse(response);

        return responseBodyBytes;
    }

    private static void tryCloseResponse(Response response) {
        try {
            response.close();
        } catch (Exception e) {
            logger.error("Cannot close response", e);
        }
    }

}




