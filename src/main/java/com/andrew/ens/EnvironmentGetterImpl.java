package com.andrew.ens;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;

@RequiredArgsConstructor
public class EnvironmentGetterImpl implements EnvironmentGetter {

    private final Environment environment;

    @Override
    public String getProperty(String key) {
        return environment.getProperty(key);
    }
}
