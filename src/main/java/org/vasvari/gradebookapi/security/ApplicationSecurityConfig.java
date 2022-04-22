package org.vasvari.gradebookapi.security;

import org.vasvari.gradebookapi.jwt.JwtAuthenticationEntryPoint;
import org.vasvari.gradebookapi.jwt.JwtRequestFilter;
import org.vasvari.gradebookapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationEntryPoint entryPoint;
    private final UserService userService;
    private final JwtRequestFilter requestFilter;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(
                        "/api/subjects/**",
                        "/api/assignments/**",
                        "/api/gradebook/**",
                        "/api/users/password-change",
                        "/api/student_gradebook/**",
                        "/api/subject_gradebook/**"
                ).hasAnyRole(ApplicationUserRole.ADMIN.name(), ApplicationUserRole.TEACHER.name(), ApplicationUserRole.STUDENT.name())
                .antMatchers(
                        "/api/students/**"
                ).hasAnyRole(ApplicationUserRole.ADMIN.name(), ApplicationUserRole.TEACHER.name())
                .antMatchers(
                        "/api/teachers/**",
                        "/api/users/**"
                ).hasRole(ApplicationUserRole.ADMIN.name())
                .antMatchers(
                        "/api/student-user/**"
                ).hasRole(ApplicationUserRole.STUDENT.name())
                .antMatchers(
                        "/api/teacher-user/**"
                ).hasRole(ApplicationUserRole.TEACHER.name())

                .antMatchers("/api/authenticate/**").permitAll()
                // Reject every unauthenticated request and send error code 401.
                .and().exceptionHandling().authenticationEntryPoint(entryPoint)
                // We don't need sessions to be created.
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Add a filter to validate the tokens with every request
        http.addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class);
    }


    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userService);

        return provider;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
}
