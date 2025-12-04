package dev.tomislavmiksik.phoenixbe.security;

import dev.tomislavmiksik.phoenixbe.entity.ApiKey;
import dev.tomislavmiksik.phoenixbe.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return pathMatcher.match("/api/admin/**", path) || pathMatcher.match("/api/auth/**", path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = request.getHeader(AUTH_TOKEN_HEADER_NAME);

        if(key == null || key.isEmpty()){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Missing API Key");
            return;
        }

        Optional<ApiKey> result = apiKeyRepository.findByKeyHashAndActiveTrue(DigestUtils.sha256Hex(key));

        if(result.isEmpty()){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid API key");
            return;
        }

        ApiKey apiKey = result.get();


        Instant expirationDate = apiKey.getExpiresAt();

        if(expirationDate != null && expirationDate.isBefore(Instant.now())){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Expired API key");
            return;
        }

        apiKey.setLastUsedAt(Instant.now());
        apiKeyRepository.save(apiKey);

        // Set authentication in SecurityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "api-key-user",
                        null,
                        Collections.emptyList()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
