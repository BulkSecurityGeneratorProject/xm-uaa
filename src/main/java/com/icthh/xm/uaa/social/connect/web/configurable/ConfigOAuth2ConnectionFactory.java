package com.icthh.xm.uaa.social.connect.web.configurable;

import com.icthh.xm.uaa.domain.properties.TenantProperties.Social;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;

public class ConfigOAuth2ConnectionFactory extends OAuth2ConnectionFactory<ConfigOAuth2Api> {

    private final Social social;

    public ConfigOAuth2ConnectionFactory(Social social) {
        super(social.getProviderId(), new ConfigServiceProvider(social), new ConfigAdapter());
        this.social = social;
    }

    @Override
    public String getScope() {
        return social.getScope();
    }

}
