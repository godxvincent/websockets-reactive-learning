package com.godxvincent.websocketsreactivelearning.services;

import com.godxvincent.websocketsreactivelearning.models.GreetingRequest;
import com.godxvincent.websocketsreactivelearning.models.GreetingResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@Service
public class GreetingService {

    private GreetingResponse generateResponse(String name) {
        return new GreetingResponse("Hey " + name + " @ " + Instant.now());
    }

    public Flux<GreetingResponse> greeting(GreetingRequest greetingRequest) {
        return Flux.fromStream(
          Stream.generate(
                  () -> this.generateResponse(greetingRequest.getName())
          )
        ).delayElements(Duration.ofSeconds(1));
    }
}
