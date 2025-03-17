package dev.example.visa.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.env.Environment;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Switches the logging configuration based on the current environment.
 * Uses JSON logging in production and standard console logging in development.
 */
@Slf4j
@Singleton
@Context
public class LoggingConfigSwitcher {

    private final Environment environment;

    @Inject
    public LoggingConfigSwitcher(Environment environment) {
        this.environment = environment;
    }

    @EventListener
    public void onStartup(ServerStartupEvent event) {
        boolean isProd = environment.getActiveNames().contains(Environment.KUBERNETES);

        if (isProd) {
            try {
                LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                context.reset();

                InputStream configStream = getClass().getClassLoader().getResourceAsStream("logback-json.xml");
                if (configStream != null) {
                    configurator.doConfigure(configStream);
                    log.info("Switched to JSON logging for production environment");
                } else {
                    log.warn("Could not find logback-json.xml, using default logging configuration");
                }
            } catch (Exception e) {
                log.error("Error switching to production logging configuration", e);
            }
        } else {
            log.info("Using standard console logging for non-production environment");
        }
    }
}