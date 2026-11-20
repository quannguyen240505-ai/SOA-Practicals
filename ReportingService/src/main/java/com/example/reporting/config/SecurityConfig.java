package com.example.reporting.config;

import com.example.reporting.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	@Bean
	JwtAuthenticationFilter jwtFilter(@Value("${app.jwt.secret}") String secret) {
		return new JwtAuthenticationFilter(secret);
	}

	@Bean
	SecurityFilterChain security(HttpSecurity http, JwtAuthenticationFilter filter) throws Exception {
		return http.csrf(c -> c.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(a -> a.anyRequest().authenticated())
				.exceptionHandling(e -> e.authenticationEntryPoint(
						(req, res, ex) -> res.sendError(HttpStatus.UNAUTHORIZED.value(), "JWT required or invalid")))
				.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
	}
}
