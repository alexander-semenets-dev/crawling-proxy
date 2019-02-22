package com.personal.crawling.proxy;

public class CrawledContent {

    private final byte[] bytes;
    private final String contentType;

    public CrawledContent(byte[] bytes, String contentType) {
        this.bytes = bytes;
        this.contentType = contentType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getContentType() {
        return contentType;
    }
}
