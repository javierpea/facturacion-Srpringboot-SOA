package com.empresa.sistema_facturacion.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Activa las anotaciones @PreAuthorize dentro de los controladores
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitado para facilitar pruebas de desarrollo
                .authorizeHttpRequests(auth -> auth
                        // 1. RECURSOS PÚBLICOS
                        .requestMatchers("/api/auth/**", "/login", "/css/**", "/js/**").permitAll()

                        // 2. PERMISOS GRANULARES DE LECTURA (Para el POS del Cajero y Bodega)
                        // El Cajero y el Administrador necesitan buscar clientes
                        .requestMatchers("/clientes/api/buscar/**").hasAnyAuthority("ADMIN", "CAJERO")

                        // El Cajero, Bodeguero y Administrador necesitan buscar productos y consultar stock global
                        .requestMatchers("/productos/api/buscar/**", "/api/inventario/**").hasAnyAuthority("ADMIN", "BODEGA", "CAJERO")

                        // 3. PERMISOS DE MANTENIMIENTO COMPLETOS (Vistas e Inserciones)
                        .requestMatchers("/productos/**", "/categorias/**").hasAnyAuthority("ADMIN", "BODEGA")
                        .requestMatchers("/clientes/**", "/api/ventas/**", "/ventas/**").hasAnyAuthority("ADMIN", "CAJERO")
                        .requestMatchers("/sucursales/**", "/api/reportes/**", "/api/configuracion-sri/**", "/configuracion-sri/**").hasAnyAuthority("ADMIN")

                        // 4. ACCESO AL PANEL DE CONTROL GENERAL
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