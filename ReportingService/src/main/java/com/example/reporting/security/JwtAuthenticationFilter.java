package com.example.reporting.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javax.crypto.SecretKey;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final SecretKey key;

	public JwtAuthenticationFilter(String secret) {
		key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		String header = req.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer "))
			try {
				String sub = Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload()
						.getSubject();
				if (sub != null && !sub.isBlank())
					SecurityContextHolder.getContext().setAuthentication(
							new UsernamePasswordAuthenticationToken(sub, null, Collections.emptyList()));
			} catch (JwtException | IllegalArgumentException ex) {
				SecurityContextHolder.clearContext();
			}
		chain.doFilter(req, res);
	}
}
