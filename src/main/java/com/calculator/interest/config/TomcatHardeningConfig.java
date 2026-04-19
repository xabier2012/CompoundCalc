package com.calculator.interest.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customises the embedded Tomcat connector so the {@code Server} HTTP header
 * is not emitted, preventing version disclosure to clients and scanners.
 */
@Configuration
public class TomcatHardeningConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(
                connector -> connector.setProperty("server", " ")
        );
    }
}
