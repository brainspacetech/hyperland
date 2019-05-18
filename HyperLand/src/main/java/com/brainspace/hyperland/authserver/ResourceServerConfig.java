package com.brainspace.hyperland.authserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@EnableResourceServer
@Configuration
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "resource_id";

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID).stateless(false);
    }




   // @Override
    public void configure(HttpSecurity http) throws Exception {
        http.

                authorizeRequests()
                .antMatchers("/index.html","/*.js","/*.png", "/*.woff*","/*.ttf","/*.ico","/home", "/about").anonymous()
                .antMatchers("/master/getAll/**").hasAnyRole("ADMIN","OFFICE_USER","AGENT")
                .antMatchers("/search/**").hasAnyRole("CHECKER","ADMIN","OFFICE_USER","CUSTOMER","AGENT")
                .antMatchers("/transaction/**").hasAnyRole("CHECKER","ADMIN","OFFICE_USER")
                .antMatchers("/transaction/approve/**").hasAnyRole("CHECKER")
                .antMatchers("/master/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and().exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
  }

}
