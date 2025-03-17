package dev.example.visa.config;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ThreadLocalAccessor;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Slf4j
@Factory
public class MDCPropagationFactory implements ApplicationEventListener<StartupEvent> {

    @Override
    public void onApplicationEvent(StartupEvent event) {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new MDCThreadLocalAccessor());

        Hooks.enableAutomaticContextPropagation();

        log.info("MDC context propagation set up successfully.");
    }

    /**
     * Basic implementation of ThreadLocalAccessor for MDC state propagation.
     */
    static class MDCThreadLocalAccessor implements ThreadLocalAccessor<Map<String, String>> {

        @Override
        public Object key() {
            return "MDC";
        }

        @Override
        public Map<String, String> getValue() {
            return MDC.getCopyOfContextMap();
        }

        @Override
        public void setValue(Map<String, String> value) {
            if (value != null) {
                MDC.setContextMap(value);
            } else {
                MDC.clear();
            }
        }

        @Override
        public void restore(Map<String, String> value) {
            setValue(value);
        }
    }

    @Singleton
    public reactor.core.scheduler.Scheduler contextAwareScheduler() {
        return Schedulers.boundedElastic();
    }
}