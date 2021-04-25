package com.godxvincent.websocketsreactivelearning.config;

import com.godxvincent.websocketsreactivelearning.models.GreetingRequest;
import com.godxvincent.websocketsreactivelearning.models.GreetingResponse;
import com.godxvincent.websocketsreactivelearning.services.GreetingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Configuration
@Log4j2
public class GreetingWebSocketConfiguration {

    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping(WebSocketHandler webSocketHandler) {
        // Por alguna razón toco establecer un orden en el mapeq
        return new SimpleUrlHandlerMapping(Map.of("/ws/greetings", webSocketHandler), -1);
    }

    /*@Bean
    public WebSocketHandler webSocketHandler(GreetingService greetingService) {
        return new WebSocketHandler() {
            @Override
            public Mono<Void> handle(WebSocketSession session) {
                // Obtengo el mensaje enviado a través de la sesión establecida.
                Flux<WebSocketMessage> webSocketMessageFlux = session.receive();
                // Transformo los mensajes a texto plano
                Flux<String> names = webSocketMessageFlux.map(WebSocketMessage::getPayloadAsText);
                // Inicializa un flujo a partir del Flux names de tipo string y lo transforma pasandole como funcion de cambio el constructor de GreetingRequest
                Flux<GreetingRequest> greetingRequestFlux = names.map(GreetingRequest::new);
                Flux<GreetingResponse> greetingResponseFlux = greetingRequestFlux.flatMap(greetingService::greeting);
                Flux<String> map = greetingResponseFlux.map(GreetingResponse::getMessage);
                Flux<WebSocketMessage> webSocketMessageFlux1 = map.map(session::textMessage);
                return session.send(webSocketMessageFlux1);
            }
        };
    }*/

    @Bean
    public WebSocketHandler webSocketHandler(GreetingService greetingService) {
        return session -> {
            // Obtengo el mensaje enviado a través de la sesión establecida.
            Flux<WebSocketMessage> webSocketMessageFlux =
                    session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .map(GreetingRequest::new)
                            .flatMap(greetingService::greeting)
                            .map(GreetingResponse::getMessage)
                            .map(session::textMessage)
                            .doOnEach(webSocketMessageSignal -> log.info(webSocketMessageSignal.getType()))
                            .doFinally(signalType -> log.info("Finally: "+signalType.toString()))
                            .doOnError(throwable -> log.info(throwable.getMessage()));
            return session.send(webSocketMessageFlux);
        };
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
