package com.empresa.sistema_facturacion.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitado si manejas API/Tokens
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/auth/**", "/login", "/css/**", "/js/**").permitAll()

                        // REGLA BODEGA
                        .requestMatchers("/api/productos/**", "/api/inventario/**").hasAnyRole("ADMIN", "BODEGA")

                        // REGLA CAJERO
                        .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "CAJERO")

                        // REGLA ADMIN
                        .requestMatchers("/api/reportes/cierre-caja").hasAnyRole("ADMIN", "CAJERO") // El cajero entra pero su lógica filtra
                        .requestMatchers("/api/reportes/**", "/api/configuracion-sri/**").hasRole("ADMIN")

                        // Cualquier otra ruta requiere autenticación base
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Le dice a Spring que use TU pantalla personalizada
                        .loginProcessingUrl("/login") // Endpoint que procesa el POST internamente
                        .defaultSuccessUrl("/dashboard", true) // ¡LA MAGIA! Al tener éxito va directo al dashboard
                        .failureUrl("/login?error=true") // Si falla, vuelve con el parámetro de error
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )
                // Conservamos tu filtro JWT como mecanismo secundario para Postman
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}