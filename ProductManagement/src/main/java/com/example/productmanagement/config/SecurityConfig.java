package com.example.productmanagement.config;

import com.example.productmanagement.security.JwtAuthenticationFilter;
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
	SecurityFilterChain security(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
		return http.csrf(csrf -> csrf.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(a -> a.anyRequest().authenticated())
				.exceptionHandling(e -> e.authenticationEntryPoint(
						(req, res, ex) -> res.sendError(HttpStatus.UNAUTHORIZED.value(), "JWT required or invalid"))
						.accessDeniedHandler((req, res, ex) -> res.sendError(HttpStatus.FORBIDDEN.value())))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
	}
}
