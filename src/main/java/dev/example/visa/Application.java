package dev.example.visa;

import io.micronaut.runtime.Micronaut;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Hooks;

@Slf4j
public class Application {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        Micronaut.run(Application.class, args);
    }

}