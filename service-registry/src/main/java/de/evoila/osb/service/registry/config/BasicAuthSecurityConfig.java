package de.evoila.osb.service.registry.config;

import de.evoila.osb.service.registry.properties.BaseAuthenticationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@Order(1)
public class BasicAuthSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String SERVICES_AUTHORITY = "SERVICES";
    public static final String MANAGING_AUTHORITY = "MANAGING";

    private BaseAuthenticationBean baseAuthenticationBean;

    public BasicAuthSecurityConfig(BaseAuthenticationBean baseAuthenticationBean) {
        this.baseAuthenticationBean = baseAuthenticationBean;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(baseAuthenticationBean.getAdminUsername())
                .password(passwordEncoder().encode(baseAuthenticationBean.getAdminPassword()))
                .roles("ADMIN")
                .authorities("SERVICES", "MANAGING")
                .and()
                .withUser("test")
                .password(passwordEncoder().encode("test"))
                .roles("USER)")
                .authorities("SERVICES");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                //.and()
                //.authorizeRequests().anyRequest().denyAll()
                .and()
                .authorizeRequests().antMatchers("/status").permitAll()
                .and()
                .authorizeRequests().antMatchers("/service_instance/**", "/v2/**").hasAnyAuthority(SERVICES_AUTHORITY)
                .and()
                .authorizeRequests().antMatchers("/",
                                                            "/registryServiceInstances/**",
                                                            "/registryBindings/**",
                                                            "/companies/**",
                                                            "/sharedContexts/**",
                                                            "/cloudSites/**",
                                                            "/brokers/**",
                                                            "/cloudContexts/**",
                                                            "/profile/**"
                ).hasAuthority(MANAGING_AUTHORITY)
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .cors()
                .and()
                .csrf().disable();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint =
                new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("defaultEndpointRealm");
        return entryPoint;
    }
}
