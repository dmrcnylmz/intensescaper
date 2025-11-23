package com.intensescaper.proxy;

import org.springframework.util.StringUtils;

public record ProxySpec(String host,
                        int port,
                        String username,
                        String password,
                        String protocol) {

    public boolean hasAuth() {
        return StringUtils.hasText(username) && StringUtils.hasText(password);
    }
}

