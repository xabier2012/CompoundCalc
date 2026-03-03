package com.calculator.interest.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Temporary diagnostic endpoint for debugging i18n issues in production.
 * Remove once the issue is resolved.
 */
@RestController
public class DebugController {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    public DebugController(MessageSource messageSource, LocaleResolver localeResolver) {
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
    }

    @GetMapping("/api/debug/locale")
    public Map<String, Object> debugLocale(HttpServletRequest request) {
        Map<String, Object> info = new LinkedHashMap<>();

        Locale resolved = localeResolver.resolveLocale(request);
        info.put("resolvedLocale", resolved.toString());
        info.put("resolvedLanguage", resolved.getLanguage());
        info.put("localeResolverClass", localeResolver.getClass().getName());

        info.put("requestLocale", request.getLocale().toString());
        info.put("acceptLanguageHeader", request.getHeader("Accept-Language"));
        info.put("xForwardedProto", request.getHeader("X-Forwarded-Proto"));
        info.put("xForwardedHost", request.getHeader("X-Forwarded-Host"));

        // Check cookies
        Map<String, String> cookies = new LinkedHashMap<>();
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                cookies.put(c.getName(), c.getValue());
            }
        }
        info.put("cookies", cookies);

        // Try resolving a message key in both locales
        info.put("footer.copyright_resolved", messageSource.getMessage("footer.copyright", null, resolved));
        info.put("footer.copyright_es", messageSource.getMessage("footer.copyright", null, Locale.forLanguageTag("es")));
        info.put("footer.copyright_en", messageSource.getMessage("footer.copyright", null, Locale.forLanguageTag("en")));

        // Check which resource bundles exist
        try {
            messageSource.getMessage("nav.brand", null, Locale.forLanguageTag("es"));
            info.put("messages_es_found", true);
        } catch (Exception e) {
            info.put("messages_es_found", false);
        }

        return info;
    }
}
