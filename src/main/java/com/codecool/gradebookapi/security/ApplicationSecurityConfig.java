package com.codecool.gradebookapi.security;

import com.codecool.gradebookapi.jwt.JwtAuthenticationEntryPoint;
import com.codecool.gradebookapi.jwt.JwtRequestFilter;
import com.codecool.gradebookapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

import static com.codecool.gradebookapi.security.ApplicationUserRole.ADMIN;
import static com.codecool.gradebookapi.security.ApplicationUserRole.TEACHER;

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
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
//        auth.inMemoryAuthentication()
//                .withUser("admin").password(passwordEncoder.encode("admin")).roles("ADMIN")
//                .and()
//                .withUser("teacher").password(passwordEncoder.encode("teacher")).roles("TEACHER")
//                .and()
//                .withUser("student").password(passwordEncoder.encode("student")).roles("STUDENT");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
//                .antMatchers(
//                        "/api/students/**", "/api/classes/**"
//                ).hasAnyRole(ADMIN.name(), TEACHER.name())
                .antMatchers(
                        "/api/assignments/**",
                        "/api/gradebook/**",
                        "/api/student_gradebook/**",
                        "/api/class_gradebook/**"
                ).hasAnyRole(ADMIN.name(), TEACHER.name())
                .antMatchers(
                        "/api/teachers/**",
                        //"/api/classes/**",
                        "/api/users/**"
                ).hasAnyRole(ADMIN.name())
//                .antMatchers(
//                        HttpMethod.GET,
//                        "/",
//                        "/v2/api-docs",
//                        "/v3/api-docs",
//                        "/webjars/**",
//                        "/swagger-resources/**",
//                        "/configuration/**",
//                        "/*.html",
//                        "/favicon.ico",
//                        "/**/*.html",
//                        "/**/*.css",
//                        "/**/*.js"
//                ).permitAll()
                .antMatchers("/api/authenticate/**").permitAll()
                // Permit all other request without authentication
                //.and().authorizeRequests().anyRequest().permitAll()
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
