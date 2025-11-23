package com.intensescaper.proxy;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class NoOpProxyRotator implements ProxyRotator {

    @Override
    public Optional<ProxySpec> acquire() {
        return Optional.empty();
    }
}

