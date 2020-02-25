package com.icthh.xm.uaa.security.oauth2;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@Slf4j
@Component
public class CustomerUserAuthenticationFilter extends GenericFilterBean {

    @Autowired
    private CustomUserAuthenticationService customUserAuthenticationService;

    @Override
    @SneakyThrows
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        log.info("Run custom authentication user filter");

        customUserAuthenticationService.process(request, response, chain);
    }

}
