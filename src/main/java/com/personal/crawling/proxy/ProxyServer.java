package com.personal.crawling.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

public class ProxyServer {

    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    public static void main(String[] args) {
        Spark.get("/", (req, res) -> "Hello World");
    }

}




