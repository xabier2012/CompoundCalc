package com.calculator.interest.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds defensive HTTP security headers to every response.
 * <p>
 * This filter does not require Spring Security; it covers the baseline
 * headers recommended by OWASP for a stateless public web application:
 * <ul>
 *   <li>{@code X-Content-Type-Options: nosniff} — prevents MIME sniffing.</li>
 *   <li>{@code X-Frame-Options: DENY} — blocks clickjacking via iframes.</li>
 *   <li>{@code Referrer-Policy: strict-origin-when-cross-origin} — limits
 *       information leakage via the {@code Referer} header.</li>
 *   <li>{@code Permissions-Policy} — disables sensor/device APIs the app
 *       does not use.</li>
 *   <li>{@code Strict-Transport-Security} — forces HTTPS for one year
 *       (only useful behind an HTTPS reverse proxy, harmless otherwise).</li>
 *   <li>{@code Content-Security-Policy} — whitelists the CDNs the Thymeleaf
 *       layout loads (Bootstrap, Chart.js, Font Awesome) and forbids inline
 *       event handlers. {@code 'unsafe-inline'} is kept for the inline
 *       i18n/theme scripts emitted by Thymeleaf.</li>
 * </ul>
 * Also strips the {@code Server} header so the Tomcat version is not leaked.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final String CSP =
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
            "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
            "font-src 'self' https://cdnjs.cloudflare.com data:; " +
            "img-src 'self' data:; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self'";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy",
                "accelerometer=(), camera=(), geolocation=(), gyroscope=(), " +
                "magnetometer=(), microphone=(), payment=(), usb=()");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.setHeader("Content-Security-Policy", CSP);
        response.setHeader("Server", "");
        chain.doFilter(request, response);
    }
}
