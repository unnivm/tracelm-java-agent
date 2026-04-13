package org.usbtechno.collector;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class ApiKeyProtectedTraceResourceProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("collector.security.api-key", "secret-key");
    }
}
