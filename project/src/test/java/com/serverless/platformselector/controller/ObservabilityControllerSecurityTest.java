package com.serverless.platformselector.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ObservabilityControllerSecurityTest {

    @Test
    void summaryEndpointShouldRequireAdminAuthority() throws NoSuchMethodException {
        Method summaryMethod = ObservabilityController.class.getMethod("summary");
        PreAuthorize preAuthorize = summaryMethod.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasAuthority('ADMIN')", preAuthorize.value());
    }
}
