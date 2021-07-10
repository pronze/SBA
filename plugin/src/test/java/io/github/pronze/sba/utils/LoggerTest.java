package io.github.pronze.sba.utils;

import io.github.pronze.sba.utils.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggerTest {

    @BeforeAll
    static void enableMockMode() {
        Logger.mockMode();
    }

    @Test
    public void testLogger() {
        assertTrue(Logger.isInitialized());
        Logger.trace("Initialized logger!");
    }
}
