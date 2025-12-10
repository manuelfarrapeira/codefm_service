package org.web.codefm.infrastructure.consul;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.web.codefm.domain.consul.ConsulExample;

@RefreshScope
@Component
public class ConsulExampleImpl implements ConsulExample {

    @Value("${consul.parametro}")
    private String miPropiedad;


    @Override
    public String getparameter() {
        return miPropiedad;
    }
}
