package com.intensescaper.proxy;

import java.util.Optional;

public interface ProxyRotator {

    /**
     * Kullanılabilir bir proxy döndürür. Yoksa boş döner.
     */
    Optional<ProxySpec> acquire();
}

