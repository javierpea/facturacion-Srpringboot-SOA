package com.empresa.sistema_facturacion.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/login", "/css/**", "/js/**").permitAll()

                        // MÓDULOS DE INVENTARIO Y CATÁLOGOS (ADMIN y BODEGA)
                        .requestMatchers("/api/inventario/**", "/inventario/**", "/productos/**", "/categorias/**")
                        .hasAnyAuthority("ADMIN", "BODEGA", "ROLE_ADMIN", "ROLE_BODEGA")

                        // MÓDULO DE VENTAS Y CLIENTES (ADMIN y CAJERO)
                        .requestMatchers("/api/ventas/**", "/ventas/**", "/clientes/**")
                        .hasAnyAuthority("ADMIN", "CAJERO", "ROLE_ADMIN", "ROLE_CAJERO")

                        // MÓDULO ADMINISTRATIVO PURO (Solo ADMIN)
                        .requestMatchers("/sucursales/**", "/api/reportes/**", "/api/configuracion-sri/**")
                        .hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                        // Dashboard y Cierres de caja
                        .requestMatchers("/api/reportes/cierre-caja")
                        .hasAnyAuthority("ADMIN", "CAJERO", "ROLE_ADMIN", "ROLE_CAJERO")
                        .requestMatchers("/dashboard").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )
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