package com.example.productmanagement.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final SecretKey key;

	public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String secret) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			try {
				Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7))
						.getPayload();
				String username = claims.getSubject();
				if (username != null && !username.isBlank()) {
					SecurityContextHolder.getContext().setAuthentication(
							new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList()));
				}
			} catch (JwtException | IllegalArgumentException ex) {
				SecurityContextHolder.clearContext();
			}
		}
		chain.doFilter(request, response);
	}
}
