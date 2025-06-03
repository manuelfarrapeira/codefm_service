package org.web.codefm.infrastructure.consul;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.web.codefm.domain.consul.EjemploConsul;

@RefreshScope
@Component
public class EjemploConsulImpl implements EjemploConsul {

    @Value("${consul.parametro}")
    private String miPropiedad;


    @Override
    public String getparametro() {
        return miPropiedad;
    }
}
