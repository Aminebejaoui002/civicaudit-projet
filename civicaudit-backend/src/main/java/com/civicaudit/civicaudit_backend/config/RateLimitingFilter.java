package com.civicaudit.civicaudit_backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    // Map pour stocker les buckets par adresse IP
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Limite : 100 requêtes par minute par IP
    private static final long CAPACITY = 100;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIP = getClientIP(httpRequest);
        Bucket bucket = resolveBucket(clientIP);

        if (bucket.tryConsume(1)) {
            // Requête autorisée
            chain.doFilter(request, response);
        } else {
            // Limite dépassée
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\": \"Trop de requêtes. Veuillez réessayer dans quelques instants.\", " +
                "\"retryAfter\": 60}"
            );
        }
    }

    private Bucket resolveBucket(String clientIP) {
        return buckets.computeIfAbsent(clientIP, k -> createNewBucket());
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(CAPACITY, Refill.intervally(CAPACITY, REFILL_DURATION));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        // Gestion des proxies (X-Forwarded-For)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
