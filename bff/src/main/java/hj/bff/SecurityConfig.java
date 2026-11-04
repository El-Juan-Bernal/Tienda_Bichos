package hj.bff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

  private static final String SCOPE_USER = "SCOPE_access_as_user";
  private static final String ROL_ADMIN = "Admin";

  @Bean
  @SuppressWarnings("java:S4502")
  SecurityFilterChain security(HttpSecurity http, JwtAuthenticationConverter jwtConverter) {
    return http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(
            SessionCreationPolicy.STATELESS
        ))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/data").hasAuthority(SCOPE_USER)
            // Destacados de la página de inicio: público, sin token
            .requestMatchers(HttpMethod.GET, "/api/productos/destacados").permitAll()
            // Ver el catálogo: cualquier usuario autenticado
            .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/*").hasAuthority(SCOPE_USER)
            // Crear, editar, habilitar y cargar productos: solo el rol Admin
            .requestMatchers("/api/productos", "/api/productos/**").hasRole(ROL_ADMIN)
            .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAuthority(SCOPE_USER)
            .requestMatchers(HttpMethod.GET, "/api/pedidos", "/api/pedidos/*/historial").hasAuthority(SCOPE_USER)
            .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/estado", "/api/pedidos/*/cancelar").hasAuthority(SCOPE_USER)
            .anyRequest().denyAll()
        )
        .oauth2ResourceServer(oauth ->
            oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
        )
        .build();
  }

  // Además de los scopes (SCOPE_...), convierte el claim 'roles' del token en ROLE_<rol>
  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      Collection<GrantedAuthority> autoridades = new ArrayList<>();
      Collection<GrantedAuthority> deScopes = scopes.convert(jwt);
      if (deScopes != null) {
        autoridades.addAll(deScopes);
      }
      List<String> roles = jwt.getClaimAsStringList("roles");
      if (roles != null) {
        roles.forEach(rol -> autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol)));
      }
      return autoridades;
    });
    return converter;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${cors.allowed-origins}") List<String> allowedOrigins) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}

