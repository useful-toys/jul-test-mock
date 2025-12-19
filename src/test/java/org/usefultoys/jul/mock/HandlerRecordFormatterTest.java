/*
 * Copyright 2025 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.jul.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HandlerRecordFormatter Tests")
class HandlerRecordFormatterTest {

    private Logger logger;
    private MockHandler handler;

    @BeforeEach
    @DisplayName("should setup logger and handler before each test")
    void setUp() {
        logger = Logger.getLogger("test.formatter");
        logger.setUseParentHandlers(false);
        handler = new MockHandler();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        handler.clearRecords();
    }

    @Test
    @DisplayName("should format empty handler")
    void shouldFormatEmptyHandler() {
        final String formatted = HandlerRecordFormatter.formatLoggedRecords(handler);

        assertTrue(formatted.contains("no records logged"), "should indicate no records");
    }

    @Test
    @DisplayName("should format null handler")
    void shouldFormatNullHandler() {
        final String formatted = HandlerRecordFormatter.formatLoggedRecords(null);

        assertTrue(formatted.contains("handler is null"), "should indicate null handler");
    }

    @Test
    @DisplayName("should format single record")
    void shouldFormatSingleRecord() {
        logger.info("Test message");

        final String formatted = HandlerRecordFormatter.formatLoggedRecords(handler);

        assertTrue(formatted.contains("Total records: 1"), "should show total count");
        assertTrue(formatted.contains("[0]"), "should show record index");
        assertTrue(formatted.contains("INFO"), "should show level");
        assertTrue(formatted.contains("test.formatter"), "should show logger name");
        assertTrue(formatted.contains("Test message"), "should show message");
    }

    @Test
    @DisplayName("should format multiple records")
    void shouldFormatMultipleRecords() {
        logger.info("First message");
        logger.warning("Second message");
        logger.severe("Third message");

        final String formatted = HandlerRecordFormatter.formatLoggedRecords(handler);

        assertTrue(formatted.contains("Total records: 3"), "should show total count");
        assertTrue(formatted.contains("[0]"), "should show first index");
        assertTrue(formatted.contains("[1]"), "should show second index");
        assertTrue(formatted.contains("[2]"), "should show third index");
        assertTrue(formatted.contains("First message"), "should show first message");
        assertTrue(formatted.contains("Second message"), "should show second message");
        assertTrue(formatted.contains("Third message"), "should show third message");
    }

    @Test
    @DisplayName("should format record with throwable")
    void shouldFormatRecordWithThrowable() {
        final RuntimeException ex = new RuntimeException("Error occurred");
        logger.log(Level.SEVERE, "Failed operation", ex);

        final String formatted = HandlerRecordFormatter.formatLoggedRecords(handler);

        assertTrue(formatted.contains("Total records: 1"), "should show total count");
        assertTrue(formatted.contains("SEVERE"), "should show level");
        assertTrue(formatted.contains("Failed operation"), "should show message");
        assertTrue(formatted.contains("throwable:"), "should show throwable marker");
        assertTrue(formatted.contains("RuntimeException"), "should show exception type");
        assertTrue(formatted.contains("Error occurred"), "should show exception message");
    }

    @Test
    @DisplayName("should format all log levels")
    void shouldFormatAllLogLevels() {
        logger.finest("Finest message");
        logger.finer("Finer message");
        logger.fine("Fine message");
        logger.config("Config message");
        logger.info("Info message");
        logger.warning("Warning message");
        logger.severe("Severe message");

        final String formatted = HandlerRecordFormatter.formatLoggedRecords(handler);

        assertTrue(formatted.contains("Total records: 7"), "should show total count");
        assertTrue(formatted.contains("FINEST"), "should show FINEST level");
        assertTrue(formatted.contains("FINER"), "should show FINER level");
        assertTrue(formatted.contains("FINE"), "should show FINE level");
        assertTrue(formatted.contains("CONFIG"), "should show CONFIG level");
        assertTrue(formatted.contains("INFO"), "should show INFO level");
        assertTrue(formatted.contains("WARNING"), "should show WARNING level");
        assertTrue(formatted.contains("SEVERE"), "should show SEVERE level");
    }

    @Test
    @DisplayName("should format records from different loggers")
    void shouldFormatRecordsFromDifferentLoggers() {
        final Logger logger2 = Logger.getLogger("another.logger");
        logger2.setUseParentHandlers(false);
        logger2.addHandler(handler);
        logger2.setLevel(Level.ALL);

        logger.info("From test.formatter");
        logger2.info("From another.logger");

        final String formatted = HandlerRecordFormatter.formatLoggedRecords(handler);

        assertTrue(formatted.contains("Total records: 2"), "should show total count");
        assertTrue(formatted.contains("test.formatter"), "should show first logger name");
        assertTrue(formatted.contains("another.logger"), "should show second logger name");
    }

    @Test
    @DisplayName("should format records with parameters")
    void shouldFormatRecordsWithParameters() {
        logger.log(Level.INFO, "User {0} logged in from {1}", new Object[]{"john", "192.168.1.1"});

        final String formatted = HandlerRecordFormatter.formatLoggedRecords(handler);

        assertTrue(formatted.contains("Total records: 1"), "should show total count");
        assertTrue(formatted.contains("User john logged in from 192.168.1.1"), 
                "should show formatted message with parameters");
    }
}
