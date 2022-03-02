package com.codecool.gradebookapi.security;

import com.codecool.gradebookapi.auth.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.concurrent.TimeUnit;

import static com.codecool.gradebookapi.security.ApplicationUserRole.ADMIN;
import static com.codecool.gradebookapi.security.ApplicationUserRole.TEACHER;

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final ApplicationUserService applicationUserService;

    @Autowired
    public ApplicationSecurityConfig(PasswordEncoder passwordEncoder,
                                     ApplicationUserService applicationUserService) {
        this.passwordEncoder = passwordEncoder;
        this.applicationUserService = applicationUserService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
//                .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED))  // handle unauthorized attempts
//                .and()
//                .headers().frameOptions().disable()     // to enable h2-console
//                .and()
                .authorizeRequests()
                    .antMatchers(
                        "/",
                        "index",
                        "/css/*",
                        "/js/*",
                        "/h2-console/*"
                    ).permitAll()
                    .antMatchers(
                        "/csrf",
                        "/v2/api-docs",
                        "/swagger-resources/**",
                        "/swagger-ui.html",
                        "/webjars/**"
                    ).permitAll()       // for swagger UI
                    .antMatchers(
                        "/api/users/**",
                        "/api/students/**",
                        "/api/teachers/**",
                        "/api/classes/**"
                    ).hasRole(ADMIN.name())
                    .antMatchers(
                        "/api/students/**",
                        "/api/assignments/**",
                        "/api/gradebook/**",
                        "/api/student_gradebook/**",
                        "/api/class_gradebook/**"
                    ).hasRole(TEACHER.name())
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage("/login")
                    .permitAll()
                    .failureUrl("/courses")
                    .defaultSuccessUrl("/swagger-ui.html", true)
                    .usernameParameter("username")  // these have to match the name parameters
                    .passwordParameter("password")  // in the login form
                    .and()
                .rememberMe()
                    .userDetailsService(applicationUserService)     // need this to work!!!
                    .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(20)) // overriding default 2 weeks expiration
                    .key("something-very-secure")   // hash key
                //.tokenRepository()    ----> use database instead of in-memory storage
                    .rememberMeParameter("remember-me")     // if not set, default password, username, remember-me are used
                    .and()
                .logout()
                    .logoutUrl("/logout")
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", HttpMethod.GET.name()))  // should use POST instead of GET if csrf is NOT disabled
                    .clearAuthentication(true)
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID", "remember-me")
                    .logoutSuccessUrl("/login");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
        auth.inMemoryAuthentication()
                .withUser("admin").password(passwordEncoder.encode("admin")).roles("ADMIN")
                .and()
                .withUser("teacher").password(passwordEncoder.encode("teacher")).roles("TEACHER")
                .and()
                .withUser("student").password(passwordEncoder.encode("student")).roles("STUDENT");
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(applicationUserService);

        return provider;
    }
}
