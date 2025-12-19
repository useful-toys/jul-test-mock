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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usefultoys.jul.mock.AssertLogger.*;

/**
 * Unit tests for {@link AssertLogger}.
 */
@DisplayName("AssertLogger")
class AssertLoggerTest {

    private Logger logger;
    private MockHandler handler;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("test.logger");
        logger.setUseParentHandlers(false);
        handler = new MockHandler();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        handler.clearRecords();
    }

    @Nested
    @DisplayName("assertRecord with message parts")
    class AssertRecordWithMessageParts {

        @Test
        @DisplayName("should pass when message contains expected part")
        void shouldPassWhenMessageContainsExpectedPart() {
            logger.info("Hello World");
            assertRecord(handler, 0, "World");
        }

        @Test
        @DisplayName("should pass with multiple message parts")
        void shouldPassWithMultipleMessageParts() {
            logger.info("Hello beautiful World");
            assertRecord(handler, 0, "Hello", "World");
        }

        @Test
        @DisplayName("should throw when message does not contain expected part")
        void shouldThrowWhenMessageDoesNotContainExpectedPart() {
            logger.info("Hello World");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecord(handler, 0, "Universe"));
            assertTrue(error.getMessage().contains("should contain all expected message parts"));
        }

        @Test
        @DisplayName("should throw when one of multiple message parts is missing")
        void shouldThrowWhenOneOfMultipleMessagePartsIsMissing() {
            logger.info("Hello beautiful World");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecord(handler, 0, "Hello", "Universe"));
            assertTrue(error.getMessage().contains("should contain all expected message parts"));
        }

        @Test
        @DisplayName("should throw when record index is out of bounds")
        void shouldThrowWhenRecordIndexIsOutOfBounds() {
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecord(handler, 0, "test"));
            assertTrue(error.getMessage().contains("should have enough log records"));
        }
    }

    @Nested
    @DisplayName("assertRecord with level and message")
    class AssertRecordWithLevelAndMessage {

        @Test
        @DisplayName("should pass when level and message match")
        void shouldPassWhenLevelAndMessageMatch() {
            logger.warning("Warning message");
            assertRecord(handler, 0, Level.WARNING, "Warning");
        }

        @Test
        @DisplayName("should throw when level does not match")
        void shouldThrowWhenLevelDoesNotMatch() {
            logger.info("Info message");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecord(handler, 0, Level.SEVERE, "Info"));
            assertTrue(error.getMessage().contains("should have expected log level"));
        }

        @Test
        @DisplayName("should throw when record index is out of bounds")
        void shouldThrowWhenRecordIndexIsOutOfBounds() {
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecord(handler, 0, Level.INFO, "test"));
            assertTrue(error.getMessage().contains("should have enough log records"));
        }
    }

    @Nested
    @DisplayName("assertHasRecord with message parts")
    class AssertHasRecordWithMessageParts {

        @Test
        @DisplayName("should pass when any record contains expected message part")
        void shouldPassWhenAnyRecordContainsExpectedMessagePart() {
            logger.info("First message");
            logger.warning("Second message");
            logger.severe("Third message");
            assertHasRecord(handler, "Second");
        }

        @Test
        @DisplayName("should pass with multiple message parts")
        void shouldPassWithMultipleMessageParts() {
            logger.info("First message");
            logger.warning("Second beautiful message");
            logger.severe("Third message");
            assertHasRecord(handler, "Second", "beautiful");
        }

        @Test
        @DisplayName("should throw when no record contains expected message part")
        void shouldThrowWhenNoRecordContainsExpectedMessagePart() {
            logger.info("First message");
            logger.warning("Second message");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecord(handler, "Missing"));
            assertTrue(error.getMessage().contains("should have at least one record containing expected message parts"));
        }

        @Test
        @DisplayName("should throw when no record contains all message parts")
        void shouldThrowWhenNoRecordContainsAllMessageParts() {
            logger.info("First message");
            logger.warning("Second beautiful message");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecord(handler, "Second", "Missing"));
            assertTrue(error.getMessage().contains("should have at least one record containing expected message parts"));
        }

        @Test
        @DisplayName("should throw when no records exist")
        void shouldThrowWhenNoRecordsExist() {
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecord(handler, "Any"));
            assertTrue(error.getMessage().contains("should have at least one record containing expected message parts"));
        }
    }

    @Nested
    @DisplayName("assertHasRecord with level and message")
    class AssertHasRecordWithLevelAndMessage {

        @Test
        @DisplayName("should pass when any record has expected level and message")
        void shouldPassWhenAnyRecordHasExpectedLevelAndMessage() {
            logger.fine("Debug message");
            logger.info("Info message");
            logger.severe("Error message");
            assertHasRecord(handler, Level.INFO, "Info");
        }

        @Test
        @DisplayName("should throw when no record has expected level and message combination")
        void shouldThrowWhenNoRecordHasExpectedLevelAndMessageCombination() {
            logger.fine("Debug message");
            logger.info("Info message");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecord(handler, Level.SEVERE, "Info"));
            assertTrue(error.getMessage().contains("should have at least one record with expected level and all message parts"));
        }
    }

    @Nested
    @DisplayName("assertRecordHasThrowable")
    class AssertRecordHasThrowable {

        @Test
        @DisplayName("should pass when record has any throwable")
        void shouldPassWhenRecordHasAnyThrowable() {
            logger.log(Level.SEVERE, "Error", new RuntimeException("any"));
            assertRecordHasThrowable(handler, 0);
        }

        @Test
        @DisplayName("should throw when record has no throwable")
        void shouldThrowWhenRecordHasNoThrowable() {
            logger.severe("Error");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordHasThrowable(handler, 0));
            assertTrue(error.getMessage().contains("should have a throwable"));
        }

        @Test
        @DisplayName("should throw when record index is out of bounds")
        void shouldThrowWhenRecordIndexIsOutOfBounds() {
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordHasThrowable(handler, 0));
            assertTrue(error.getMessage().contains("should have enough log records"));
        }
    }

    @Nested
    @DisplayName("assertRecordWithThrowable with class")
    class AssertRecordWithThrowableByClass {

        @Test
        @DisplayName("should pass when record has matching throwable class")
        void shouldPassWhenRecordHasMatchingThrowableClass() {
            logger.log(Level.SEVERE, "Error", new IOException("any"));
            assertRecordWithThrowable(handler, 0, IOException.class);
        }

        @Test
        @DisplayName("should pass when throwable is subclass of expected type")
        void shouldPassWhenThrowableIsSubclassOfExpectedType() {
            final IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
            logger.log(Level.SEVERE, "Error occurred", exception);
            assertRecordWithThrowable(handler, 0, RuntimeException.class);
        }

        @Test
        @DisplayName("should throw when throwable class does not match")
        void shouldThrowWhenThrowableClassDoesNotMatch() {
            logger.log(Level.SEVERE, "Error", new RuntimeException("any"));
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordWithThrowable(handler, 0, IOException.class));
            assertTrue(error.getMessage().contains("should have expected throwable type"));
        }

        @Test
        @DisplayName("should throw when record has no throwable")
        void shouldThrowWhenRecordHasNoThrowable() {
            logger.severe("Error message");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordWithThrowable(handler, 0, RuntimeException.class));
            assertTrue(error.getMessage().contains("should have a throwable"));
        }

        @Test
        @DisplayName("should throw when record index is out of bounds")
        void shouldThrowWhenRecordIndexIsOutOfBounds() {
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordWithThrowable(handler, 0, Throwable.class));
            assertTrue(error.getMessage().contains("should have enough log records"));
        }
    }

    @Nested
    @DisplayName("assertRecordWithThrowable with message parts")
    class AssertRecordWithThrowableWithMessageParts {

        @Test
        @DisplayName("should pass when throwable message contains expected parts")
        void shouldPassWhenThrowableMessageContainsExpectedParts() {
            logger.log(Level.SEVERE, "Database error", new RuntimeException("Connection failed"));
            assertRecordWithThrowable(handler, 0, RuntimeException.class, "Connection", "failed");
        }

        @Test
        @DisplayName("should throw when throwable message does not contain expected parts")
        void shouldThrowWhenThrowableMessageDoesNotContainExpectedParts() {
            logger.log(Level.SEVERE, "Database error", new RuntimeException("Connection failed"));
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordWithThrowable(handler, 0, RuntimeException.class, "Connection", "succeeded"));
            assertTrue(error.getMessage().contains("should contain all expected message parts in throwable"));
        }

        @Test
        @DisplayName("should throw when record has no throwable")
        void shouldThrowWhenRecordHasNoThrowable() {
            logger.severe("Error message");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordWithThrowable(handler, 0, RuntimeException.class, "any"));
            assertTrue(error.getMessage().contains("should have a throwable"));
        }

        @Test
        @DisplayName("should throw when record index is out of bounds")
        void shouldThrowWhenRecordIndexIsOutOfBounds() {
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordWithThrowable(handler, 0, RuntimeException.class, "any"));
            assertTrue(error.getMessage().contains("should have enough log records"));
        }
    }

    @Nested
    @DisplayName("assertRecordWithThrowable with class and message parts")
    class AssertRecordWithThrowableClassAndMessage {

        @Test
        @DisplayName("should pass when throwable type and message match")
        void shouldPassWhenThrowableTypeAndMessageMatch() {
            logger.log(Level.SEVERE, "Validation failed", new IllegalArgumentException("Invalid parameter: userId"));
            assertRecordWithThrowable(handler, 0, IllegalArgumentException.class, "Invalid parameter");
        }

        @Test
        @DisplayName("should throw when throwable message does not contain expected text")
        void shouldThrowWhenThrowableMessageDoesNotContainExpectedText() {
            logger.log(Level.SEVERE, "Error occurred", new RuntimeException("Different message"));
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordWithThrowable(handler, 0, RuntimeException.class, "Expected text"));
            assertTrue(error.getMessage().contains("should contain all expected message parts in throwable"));
        }

        @Test
        @DisplayName("should throw when throwable has null message")
        void shouldThrowWhenThrowableHasNullMessage() {
            logger.log(Level.SEVERE, "Error occurred", new RuntimeException((String) null));
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordWithThrowable(handler, 0, RuntimeException.class, "Any text"));
            assertTrue(error.getMessage().contains("should contain all expected message parts in throwable"));
        }

        @Test
        @DisplayName("should throw when record index is out of bounds")
        void shouldThrowWhenRecordIndexIsOutOfBounds() {
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordWithThrowable(handler, 0, Throwable.class, "test"));
            assertTrue(error.getMessage().contains("should have enough log records"));
        }
    }

    @Nested
    @DisplayName("assertHasRecordWithThrowable any type")
    class AssertHasRecordWithThrowableAnyType {

        @Test
        @DisplayName("should pass when any record has any throwable")
        void shouldPassWhenAnyRecordHasAnyThrowable() {
            logger.info("Regular message");
            logger.log(Level.SEVERE, "Error with exception", new Exception("Any exception"));
            assertHasRecordWithThrowable(handler);
        }

        @Test
        @DisplayName("should throw when no record has any throwable")
        void shouldThrowWhenNoRecordHasAnyThrowable() {
            logger.info("Regular message");
            logger.severe("Error without exception");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecordWithThrowable(handler));
            assertTrue(error.getMessage().contains("should have at least one record with a throwable"));
        }

        @Test
        @DisplayName("should throw when no records exist")
        void shouldThrowWhenNoRecordsExist() {
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecordWithThrowable(handler));
            assertTrue(error.getMessage().contains("should have at least one record with a throwable"));
        }
    }

    @Nested
    @DisplayName("assertHasRecordWithThrowable with class")
    class AssertHasRecordWithThrowableByClass {

        @Test
        @DisplayName("should pass when any record has expected throwable type")
        void shouldPassWhenAnyRecordHasExpectedThrowableType() {
            logger.info("Regular message");
            logger.log(Level.SEVERE, "Error with exception", new IllegalStateException("State error"));
            logger.warning("Warning message");
            assertHasRecordWithThrowable(handler, IllegalStateException.class);
        }

        @Test
        @DisplayName("should pass when throwable is subclass of expected type")
        void shouldPassWhenThrowableIsSubclassOfExpectedType() {
            logger.log(Level.SEVERE, "Error occurred", new IllegalArgumentException("Invalid argument"));
            assertHasRecordWithThrowable(handler, RuntimeException.class);
        }

        @Test
        @DisplayName("should throw when no record has expected throwable type")
        void shouldThrowWhenNoRecordHasExpectedThrowableType() {
            logger.log(Level.SEVERE, "Error with different exception", new RuntimeException("Runtime error"));
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecordWithThrowable(handler, IllegalStateException.class));
            assertTrue(error.getMessage().contains("should have at least one record with expected throwable type"));
        }

        @Test
        @DisplayName("should throw when no records have throwables")
        void shouldThrowWhenNoRecordsHaveThrowables() {
            logger.info("Message without exception");
            logger.severe("Error without exception");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecordWithThrowable(handler, RuntimeException.class));
            assertTrue(error.getMessage().contains("should have at least one record with expected throwable type"));
        }

        @Test
        @DisplayName("should throw when no records exist")
        void shouldThrowWhenNoRecordsExist() {
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecordWithThrowable(handler, RuntimeException.class));
            assertTrue(error.getMessage().contains("should have at least one record with expected throwable type"));
        }
    }

    @Nested
    @DisplayName("assertHasRecordWithThrowable with class and message parts")
    class AssertHasRecordWithThrowableClassAndMessage {

        @Test
        @DisplayName("should pass when any record has expected throwable type and message")
        void shouldPassWhenAnyRecordHasExpectedThrowableTypeAndMessage() {
            logger.info("Regular message");
            logger.log(Level.SEVERE, "Database error", new RuntimeException("Connection failed"));
            logger.log(Level.WARNING, "Network error", new IllegalStateException("Different error"));
            assertHasRecordWithThrowable(handler, RuntimeException.class, "Connection");
        }

        @Test
        @DisplayName("should throw when no record has matching throwable type and message")
        void shouldThrowWhenNoRecordHasMatchingThrowableTypeAndMessage() {
            logger.log(Level.SEVERE, "Error 1", new RuntimeException("Different message"));
            logger.log(Level.SEVERE, "Error 2", new IOException("Connection failed"));
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecordWithThrowable(handler, RuntimeException.class, "Connection"));
            assertTrue(error.getMessage().contains("should have at least one record"));
        }

        @Test
        @DisplayName("should handle throwables with null messages")
        void shouldHandleThrowablesWithNullMessages() {
            logger.log(Level.SEVERE, "Error", new RuntimeException((String) null));
            final AssertionError error = assertThrows(AssertionError.class, () -> assertHasRecordWithThrowable(handler, RuntimeException.class, "Any message"));
            assertTrue(error.getMessage().contains("should have at least one record"));
        }
    }

    @Nested
    @DisplayName("assertNoRecord")
    class AssertNoRecord {

        @Test
        @DisplayName("should pass when no record contains message parts")
        void shouldPassWhenNoRecordContainsMessageParts() {
            logger.info("message 1");
            assertNoRecord(handler, "message 2");
        }

        @Test
        @DisplayName("should throw when record contains message parts")
        void shouldThrowWhenRecordContainsMessageParts() {
            logger.info("message 1");
            assertThrows(AssertionError.class, () -> assertNoRecord(handler, "message 1"));
        }

        @Test
        @DisplayName("should pass when no record has level and message")
        void shouldPassWhenNoRecordHasLevelAndMessage() {
            logger.info("message 1");
            assertNoRecord(handler, Level.INFO, "message 2");
            assertNoRecord(handler, Level.SEVERE, "message 1");
        }

        @Test
        @DisplayName("should throw when record has level and message")
        void shouldThrowWhenRecordHasLevelAndMessage() {
            logger.info("message 1");
            assertThrows(AssertionError.class, () -> assertNoRecord(handler, Level.INFO, "message 1"));
        }
    }

    @Nested
    @DisplayName("assertNoRecordWithThrowable")
    class AssertNoRecordWithThrowable {

        @Test
        @DisplayName("should pass when no record has throwable")
        void shouldPassWhenNoRecordHasThrowable() {
            logger.info("message 1");
            assertNoRecordWithThrowable(handler);
        }

        @Test
        @DisplayName("should throw when record has throwable")
        void shouldThrowWhenRecordHasThrowable() {
            logger.log(Level.SEVERE, "message 1", new Exception());
            assertThrows(AssertionError.class, () -> assertNoRecordWithThrowable(handler));
        }

        @Test
        @DisplayName("should pass when no record has throwable of class")
        void shouldPassWhenNoRecordHasThrowableOfClass() {
            logger.log(Level.SEVERE, "message 1", new IOException());
            assertNoRecordWithThrowable(handler, RuntimeException.class);
        }

        @Test
        @DisplayName("should throw when record has throwable of class")
        void shouldThrowWhenRecordHasThrowableOfClass() {
            logger.log(Level.SEVERE, "message 1", new IOException());
            assertThrows(AssertionError.class, () -> assertNoRecordWithThrowable(handler, IOException.class));
        }

        @Test
        @DisplayName("should pass when no record has throwable with message parts")
        void shouldPassWhenNoRecordHasThrowableWithMessageParts() {
            logger.log(Level.SEVERE, "message 1", new IOException("message 1"));
            assertNoRecordWithThrowable(handler, "message 2");
        }

        @Test
        @DisplayName("should throw when record has throwable with message parts")
        void shouldThrowWhenRecordHasThrowableWithMessageParts() {
            logger.log(Level.SEVERE, "message 1", new IOException("message 1"));
            assertThrows(AssertionError.class, () -> assertNoRecordWithThrowable(handler, "message 1"));
        }

        @Test
        @DisplayName("should pass when no record has throwable of class and message")
        void shouldPassWhenNoRecordHasThrowableOfClassAndMessage() {
            logger.log(Level.SEVERE, "message 1", new IOException("message 1"));
            assertNoRecordWithThrowable(handler, IOException.class, "message 2");
            assertNoRecordWithThrowable(handler, RuntimeException.class, "message 1");
        }

        @Test
        @DisplayName("should throw when record has throwable of class and message")
        void shouldThrowWhenRecordHasThrowableOfClassAndMessage() {
            logger.log(Level.SEVERE, "message 1", new IOException("message 1"));
            assertThrows(AssertionError.class, () -> assertNoRecordWithThrowable(handler, IOException.class, "message 1"));
        }
    }

    @Nested
    @DisplayName("assertRecordNot")
    class AssertRecordNot {

        @Test
        @DisplayName("should pass when record does not contain message parts")
        void shouldPassWhenRecordDoesNotContainMessageParts() {
            logger.info("message 1");
            assertRecordNot(handler, 0, "message 2");
        }

        @Test
        @DisplayName("should throw when record contains message parts")
        void shouldThrowWhenRecordContainsMessageParts() {
            logger.info("message 1");
            assertThrows(AssertionError.class, () -> assertRecordNot(handler, 0, "message 1"));
        }

        @Test
        @DisplayName("should pass when record does not have level and message")
        void shouldPassWhenRecordDoesNotHaveLevelAndMessage() {
            logger.info("message 1");
            assertRecordNot(handler, 0, Level.INFO, "message 2");
            assertRecordNot(handler, 0, Level.SEVERE, "message 1");
        }

        @Test
        @DisplayName("should throw when record has level and message")
        void shouldThrowWhenRecordHasLevelAndMessage() {
            logger.info("message 1");
            assertThrows(AssertionError.class, () -> assertRecordNot(handler, 0, Level.INFO, "message 1"));
        }
    }

    @Nested
    @DisplayName("assertRecordNotWithThrowable")
    class AssertRecordNotWithThrowable {

        @Test
        @DisplayName("should pass when record does not have throwable")
        void shouldPassWhenRecordDoesNotHaveThrowable() {
            logger.info("message 1");
            assertRecordNotWithThrowable(handler, 0);
        }

        @Test
        @DisplayName("should throw when record has throwable")
        void shouldThrowWhenRecordHasThrowable() {
            logger.log(Level.SEVERE, "message 1", new Exception());
            assertThrows(AssertionError.class, () -> assertRecordNotWithThrowable(handler, 0));
        }

        @Test
        @DisplayName("should pass when record does not have throwable of class")
        void shouldPassWhenRecordDoesNotHaveThrowableOfClass() {
            logger.log(Level.SEVERE, "message 1", new IOException());
            assertRecordNotWithThrowable(handler, 0, RuntimeException.class);
        }

        @Test
        @DisplayName("should throw when record has throwable of class")
        void shouldThrowWhenRecordHasThrowableOfClass() {
            logger.log(Level.SEVERE, "message 1", new IOException());
            assertThrows(AssertionError.class, () -> assertRecordNotWithThrowable(handler, 0, IOException.class));
        }

        @Test
        @DisplayName("should pass when record does not have throwable of class and message")
        void shouldPassWhenRecordDoesNotHaveThrowableOfClassAndMessage() {
            logger.log(Level.SEVERE, "message 1", new IOException("message 1"));
            assertRecordNotWithThrowable(handler, 0, IOException.class, "message 2");
            assertRecordNotWithThrowable(handler, 0, RuntimeException.class, "message 1");
        }

        @Test
        @DisplayName("should throw when record has throwable of class and message")
        void shouldThrowWhenRecordHasThrowableOfClassAndMessage() {
            logger.log(Level.SEVERE, "message 1", new IOException("message 1"));
            assertThrows(AssertionError.class, () -> assertRecordNotWithThrowable(handler, 0, IOException.class, "message 1"));
        }
    }

    @Nested
    @DisplayName("assertRecordCount")
    class AssertRecordCount {

        @Test
        @DisplayName("should pass when record count matches expected")
        void shouldPassWhenRecordCountMatchesExpected() {
            logger.info("Message 1");
            logger.warning("Message 2");
            assertRecordCount(handler, 2);
        }

        @Test
        @DisplayName("should throw when record count does not match")
        void shouldThrowWhenRecordCountDoesNotMatch() {
            logger.info("Single message");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordCount(handler, 3));
            assertTrue(error.getMessage().contains("should have expected number of records"));
        }
    }

    @Nested
    @DisplayName("assertNoRecords")
    class AssertNoRecords {

        @Test
        @DisplayName("should pass when no records exist")
        void shouldPassWhenNoRecordsExist() {
            assertNoRecords(handler);
        }

        @Test
        @DisplayName("should throw when records exist")
        void shouldThrowWhenRecordsExist() {
            logger.fine("Debug message");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertNoRecords(handler));
            assertTrue(error.getMessage().contains("should have expected number of records"));
        }
    }

    @Nested
    @DisplayName("assertRecordCountByLevel")
    class AssertRecordCountByLevel {

        @Test
        @DisplayName("should pass when level count matches expected")
        void shouldPassWhenLevelCountMatchesExpected() {
            logger.info("Info 1");
            logger.warning("Warning");
            logger.info("Info 2");
            assertRecordCountByLevel(handler, Level.INFO, 2);
        }

        @Test
        @DisplayName("should throw when level count does not match")
        void shouldThrowWhenLevelCountDoesNotMatch() {
            logger.info("Info 1");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordCountByLevel(handler, Level.INFO, 2));
            assertTrue(error.getMessage().contains("should have expected number of records with level"));
        }
    }

    @Nested
    @DisplayName("assertRecordCountByMessage")
    class AssertRecordCountByMessage {

        @Test
        @DisplayName("should pass when message count matches expected")
        void shouldPassWhenMessageCountMatchesExpected() {
            logger.info("Hello World");
            logger.warning("Hello Universe");
            assertRecordCountByMessage(handler, "Hello", 2);
        }

        @Test
        @DisplayName("should throw when message count does not match")
        void shouldThrowWhenMessageCountDoesNotMatch() {
            logger.info("Hello World");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordCountByMessage(handler, "Hello", 2));
            assertTrue(error.getMessage().contains("should have expected number of records containing message part"));
        }
    }

    @Nested
    @DisplayName("assertRecordSequence with levels")
    class AssertRecordSequenceWithLevels {

        @Test
        @DisplayName("should pass when level sequence matches")
        void shouldPassWhenLevelSequenceMatches() {
            logger.info("Info");
            logger.warning("Warn");
            assertRecordSequence(handler, Level.INFO, Level.WARNING);
        }

        @Test
        @DisplayName("should throw when level sequence does not match")
        void shouldThrowWhenLevelSequenceDoesNotMatch() {
            logger.info("Info");
            logger.warning("Warn");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordSequence(handler, Level.WARNING, Level.INFO));
            assertTrue(error.getMessage().contains("should have expected level at position 0"));
        }
    }

    @Nested
    @DisplayName("assertRecordSequence with message parts")
    class AssertRecordSequenceWithMessageParts {

        @Test
        @DisplayName("should pass when message sequence matches")
        void shouldPassWhenMessageSequenceMatches() {
            logger.info("First step");
            logger.warning("Second step");
            assertRecordSequence(handler, "First", "Second");
        }

        @Test
        @DisplayName("should throw when message sequence does not match")
        void shouldThrowWhenMessageSequenceDoesNotMatch() {
            logger.info("First step");
            logger.warning("Second step");
            final AssertionError error = assertThrows(AssertionError.class, () -> assertRecordSequence(handler, "Second", "First"));
            assertTrue(error.getMessage().contains("should contain expected message part at position 0"));
        }
    }
}
