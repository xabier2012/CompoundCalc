package com.calculator.interest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

/**
 * Web MVC configuration including internationalisation support.
 * <p>
 * Default locale is Spanish ({@code es}). Users can switch to English
 * (or back to Spanish) at any time by appending {@code ?lang=en|es} to any URL.
 * The preference is persisted in a cookie so it survives page navigation.
 * The {@code LocaleChangeInterceptor} is registered globally and accepts both
 * GET and POST, so language switching is always possible regardless of page state.
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * When true, the {@code lang} cookie is marked {@code Secure} so browsers
     * only send it over HTTPS. Defaults to true (production). Set
     * {@code APP_COOKIE_SECURE=false} for local HTTP development.
     */
    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * Cookie-based locale resolver defaulting to Spanish.
     * HttpOnly is enabled so JavaScript cannot read the cookie; SameSite=Lax
     * prevents CSRF leakage while still allowing top-level navigation.
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("lang");
        resolver.setDefaultLocale(Locale.forLanguageTag("es"));
        resolver.setCookiePath("/");
        resolver.setCookieMaxAge(Duration.ofDays(365));
        resolver.setCookieSameSite("Lax");
        resolver.setCookieHttpOnly(true);
        resolver.setCookieSecure(cookieSecure);
        return resolver;
    }

    /**
     * Intercepts {@code ?lang=} parameter on any HTTP method to switch the
     * active locale. Setting {@code setIgnoreInvalidLocale(true)} ensures that
     * malformed values (e.g. {@code ?lang=<script>}) are silently ignored
     * instead of throwing.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        interceptor.setIgnoreInvalidLocale(true);
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
