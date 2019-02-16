package com.personal.crawling.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
@EnableWebFlux
public class ProxyServer {

    public static void main(String[] args) {
        SpringApplication.run(ProxyServer.class);
    }

    @Bean
    RouterFunction<ServerResponse> homeUrl() {
        return route(
                GET("/"),
                request -> ServerResponse.ok().body(BodyInserters.fromObject("Hello world"))
        );
    }

}




