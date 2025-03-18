// package dev.example.visa;
//
// import io.micronaut.context.ApplicationContext;
// import io.micronaut.context.annotation.Property;
// import io.micronaut.web.router.Router;
// import io.micronaut.web.router.UriRoute;
// import jakarta.inject.Inject;
// import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
// import org.junit.jupiter.api.Test;
//
// import java.util.List;
// import java.util.stream.Collectors;
//
// @MicronautTest(environments = "test")
// @Property(name = "micronaut.otel.enabled", value = "false")
// @Property(name = "tracing.opentelemetry.enabled", value = "false")
// @Property(name = "rabbitmq.uri", value = "amqp://guest:guest@localhost:5672")
// @Property(name = "visa.security.vault.enabled", value = "false")
// public class DebugRoutesTest {
//
//    @Inject
//    ApplicationContext context;
//
//    @Inject
//    Router router;  // Inject Micronaut's Router
//
//    @Test
//    void listRoutes() {
//        // Get all mapped routes
//        List<String> routes = router.uriRoutes()
////                .map(UriRoute::toString)
//                .map(uriRouteInfo -> uriRouteInfo.toString())
//                .collect(Collectors.toList());
//
//        // Print them
//        System.out.println("Registered Routes:");
//        routes.forEach(System.out::println);
//    }
// }
