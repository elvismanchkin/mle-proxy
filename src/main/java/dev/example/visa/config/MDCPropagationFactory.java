package dev.example.visa.config;

import io.micrometer.context.ContextRegistry;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Factory
public class MDCPropagationFactory implements ApplicationEventListener<StartupEvent> {

    @Override
    public void onApplicationEvent(StartupEvent event) {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new MDCThreadLocalAccessor());

        Hooks.enableAutomaticContextPropagation();

        log.info("MDC context propagation set up successfully.");
    }

    @Singleton
    public reactor.core.scheduler.Scheduler contextAwareScheduler() {
        return Schedulers.boundedElastic();
    }
}