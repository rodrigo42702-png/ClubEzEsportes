package br.edu.eseg.clubesesportivos.ClubEzEsportes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {http.authorizeHttpRequests(auth -> auth
                    //Permite acessar a página inicial e os arquivos visuais.
                    .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                    //Somente ADMIN gerencia usuários e perfis.
                    .requestMatchers("/api/usuarios/**", "/api/perfis/**").hasRole("ADMIN")

                    // DMIN e FUNCIONARIO podem consultar dados.
                    .requestMatchers(HttpMethod.GET, "/api/**")
                    .hasAnyRole("ADMIN", "FUNCIONARIO")

                    //Outras operações da API exigem ADMIN.
                    .requestMatchers("/api/**")
                    .hasRole("ADMIN")

                    // Somente ADMIN pode realizar alterações pela interface Web.
                    .requestMatchers(HttpMethod.POST, "/clubes", "/socios", "/socios/*/editar", "/socios/*/situacao", "/pagamentos", "/pagamentos/*/status", "/pagamentos/*/editar")
                    .hasRole("ADMIN")

                    // Outras alterações pela interface Web exigem ADMIN.
                    .requestMatchers(HttpMethod.POST, "/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")
                    //Demais páginas exigem autenticação.
                    .anyRequest().authenticated()

            )

            //Habilita login por formulário.
            .formLogin(form -> form
                    //Usa o formulário HTML que acabamos de criar.
                    .loginPage("/login")
                    //Após autenticar, abre o painel.
                    .defaultSuccessUrl("/painel", true)
                    //Permite acessar a tela sem estar autenticado.
                    .permitAll()
            )

            //Permite autenticação HTTP Basic para testes REST.
            .httpBasic(Customizer.withDefaults())

            //Habilita logout e invalida a sessão.
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").invalidateHttpSession(true));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        //Armazena senhas usando hash BCrypt.
        return new BCryptPasswordEncoder();
    }
}
