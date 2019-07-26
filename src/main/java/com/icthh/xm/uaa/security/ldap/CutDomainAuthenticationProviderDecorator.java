package com.icthh.xm.uaa.security.ldap;

import static com.icthh.xm.uaa.config.Constants.AUTH_USERNAME_DOMAIN_SEPARATOR;
import static java.util.Arrays.asList;

import com.icthh.xm.uaa.domain.properties.TenantProperties.Ldap;
import java.util.LinkedList;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@RequiredArgsConstructor
public class CutDomainAuthenticationProviderDecorator implements AuthenticationProvider {

    private final AuthenticationProvider authenticationProvider;
    private final Ldap conf;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String name = authentication.getName();
        Object credentials = authentication.getCredentials();

        if (conf.getUseNameWithoutDomain()) {
            LinkedList<String> parts = new LinkedList<>(asList(name.split(AUTH_USERNAME_DOMAIN_SEPARATOR)));
            name = parts.getFirst();
        }

        return authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(name, credentials));
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return authenticationProvider.supports(aClass);
    }
}

