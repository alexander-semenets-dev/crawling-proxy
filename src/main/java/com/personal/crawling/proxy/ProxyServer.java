package com.personal.crawling.proxy;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Spark;

import java.io.IOException;
import java.text.MessageFormat;

import static java.util.Objects.isNull;

public class ProxyServer {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    public static void main(String[] args) {
        Spark.get("/crawl-url", (request, response) -> {
            final CrawledContent crawledContent = crawlUrl(request);

            response.header(CONTENT_TYPE_HEADER, crawledContent.getContentType());

            return crawledContent.getBytes();
        });
        Spark.get("/ping", (request, response) -> "Alive");
        Spark.exception(CrawlingException.class, (exception, request, response) -> {
            response.status(500);
            response.body(exception.getMessage());
        });
    }

    private static CrawledContent crawlUrl(Request request) {
        final String crawlingUrl = request.queryParams("url");

        return getCrawledContentBytes(crawlingUrl);
    }

    private static CrawledContent getCrawledContentBytes(String crawlingUrl) {
        final HttpUrl httpUrl = HttpUrl.parse(crawlingUrl);

        if (isNull(httpUrl)) {
            throw new CrawlingException(MessageFormat.format(
                    "Cannot parse url {0}",
                    crawlingUrl
            ));
        }

        final okhttp3.Request request = new okhttp3.Request.Builder()
                .url(httpUrl)
                .header("User-Agent", USER_AGENT)
                .get()
                .build();

        final Response response;

        try {
            response = httpClient.newCall(request)
                    .execute();
        } catch (IOException e) {
            throw new CrawlingException(MessageFormat.format(
                    "Cannot execute request to url {0}: {1}",
                    crawlingUrl,
                    e.getMessage()
            ));
        }

        throwCrawlingExceptionIfResponseNotSuccessful(crawlingUrl, response);

        try {
            return new CrawledContent(
                    getResponseBodyBytes(response),
                    response.header(CONTENT_TYPE_HEADER)
            );
        } catch (IOException e) {
            throw new CrawlingException(MessageFormat.format(
                    "Cannot crawl url {0}: {1}",
                    crawlingUrl,
                    e.getMessage()
            ));
        }
    }

    private static void throwCrawlingExceptionIfResponseNotSuccessful(
            String crawlingUrl,
            Response response
    ) {
        if (response.isSuccessful()) {
            return;
        }

        tryCloseResponse(response);

        throw new CrawlingException(MessageFormat.format(
                "Cannot crawl url {0}: status code - {1}, status message - {2}",
                crawlingUrl,
                response.code(),
                response.message()
        ));
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




