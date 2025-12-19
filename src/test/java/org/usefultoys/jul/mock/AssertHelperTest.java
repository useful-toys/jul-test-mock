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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AssertHelper}.
 */
@DisplayName("AssertHelper Test Suite")
class AssertHelperTest {

    private MockLogRecord createRecord(Level level, String message, Throwable throwable) {
        final LogRecord logRecord = new LogRecord(level, message);
        logRecord.setLoggerName("test-logger");
        logRecord.setThrown(throwable);
        return new MockLogRecord(logRecord);
    }

    @Nested
    @DisplayName("handlerToRecords() tests")
    class HandlerToRecordsTest {
        @Test
        @DisplayName("should return list of records")
        void shouldReturnListOfRecords() {
            final MockHandler handler = new MockHandler();
            final Logger logger = Logger.getLogger("test");
            logger.addHandler(handler);
            logger.info("message 1");
            logger.info("message 2");
            final List<MockLogRecord> records = AssertHelper.handlerToRecords(handler);
            assertEquals(2, records.size(), "should return the correct number of records");
        }
    }

    @Nested
    @DisplayName("handlerIndexToRecord() tests")
    class HandlerIndexToRecordTest {
        @Test
        @DisplayName("should return record at valid index")
        void shouldReturnRecordAtValidIndex() {
            final MockHandler handler = new MockHandler();
            final Logger logger = Logger.getLogger("test");
            logger.addHandler(handler);
            logger.info("message 1");
            final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, 0);
            assertNotNull(record, "should return a record");
            assertEquals("message 1", record.getFormattedMessage());
        }

        @Test
        @DisplayName("should throw for out of bounds index")
        void shouldThrowForOutOfBoundsIndex() {
            final MockHandler handler = new MockHandler();
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertHelper.handlerIndexToRecord(handler, 0));
            assertTrue(error.getMessage().contains("should have enough log records"), "should throw for out of bounds");
        }
    }

    @Nested
    @DisplayName("assertMessageParts() tests")
    class AssertMessagePartsTest {
        @Test
        @DisplayName("should pass if message contains all parts")
        void shouldPassWhenAllPartsPresent() {
            final MockLogRecord record = createRecord(Level.INFO, "This is a test message.", null);
            assertDoesNotThrow(() -> AssertHelper.assertMessageParts(record, new String[]{"This is", "test message"}), "should not throw when parts are present");
        }

        @Test
        @DisplayName("should fail if message does not contain all parts")
        void shouldFailWhenNotAllPartsPresent() {
            final MockLogRecord record = createRecord(Level.INFO, "This is a test message.", null);
            assertThrows(AssertionError.class, () -> AssertHelper.assertMessageParts(record, new String[]{"This is", "another message"}), "should throw when parts are missing");
        }
    }

    @Nested
    @DisplayName("assertLevel() tests")
    class AssertLevelTest {
        @Test
        @DisplayName("should pass if levels are the same")
        void shouldPassForSameLevel() {
            final MockLogRecord record = createRecord(Level.INFO, "message", null);
            assertDoesNotThrow(() -> AssertHelper.assertLevel(record, Level.INFO), "should not throw for same level");
        }

        @Test
        @DisplayName("should fail if levels are different")
        void shouldFailForDifferentLevel() {
            final MockLogRecord record = createRecord(Level.INFO, "message", null);
            assertThrows(AssertionError.class, () -> AssertHelper.assertLevel(record, Level.WARNING), "should throw for different level");
        }
    }

    @Nested
    @DisplayName("assertThrowableOfInstance() tests")
    class AssertThrowableOfInstanceTest {
        @Test
        @DisplayName("should pass for same class or superclass")
        void shouldPassForSameOrSuperclass() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", new IllegalArgumentException("error"));
            final Throwable throwable = record.getThrown();
            assertAll("should pass for valid class hierarchies",
                    () -> assertDoesNotThrow(() -> AssertHelper.assertThrowableOfInstance(record, throwable, IllegalArgumentException.class), "should pass for same class"),
                    () -> assertDoesNotThrow(() -> AssertHelper.assertThrowableOfInstance(record, throwable, RuntimeException.class), "should pass for superclass")
            );
        }

        @Test
        @DisplayName("should fail for a different class")
        void shouldFailForWrongType() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", new IllegalArgumentException("error"));
            final Throwable throwable = record.getThrown();
            assertThrows(AssertionError.class, () -> AssertHelper.assertThrowableOfInstance(record, throwable, IllegalStateException.class), "should throw for different class");
        }
    }

    @Nested
    @DisplayName("assertThrowableHasMessageParts() tests")
    class AssertThrowableHasMessagePartsTest {
        @Test
        @DisplayName("should pass if throwable message contains all parts")
        void shouldPassWhenAllPartsPresent() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", new RuntimeException("This is an error."));
            final Throwable throwable = record.getThrown();
            assertDoesNotThrow(() -> AssertHelper.assertThrowableHasMessageParts(record, throwable, new String[]{"is an", "error"}), "should not throw when parts are present");
        }

        @Test
        @DisplayName("should fail if throwable message does not contain all parts")
        void shouldFailWhenNotAllPartsPresent() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", new RuntimeException("This is an error."));
            final Throwable throwable = record.getThrown();
            assertThrows(AssertionError.class, () -> AssertHelper.assertThrowableHasMessageParts(record, throwable, new String[]{"is an", "mistake"}), "should throw when parts are missing");
        }
    }

    @Nested
    @DisplayName("hasAllMessageParts() for record tests")
    class HasAllMessagePartsForRecordTest {
        @Test
        @DisplayName("should return true if message contains all parts")
        void shouldReturnTrueWhenAllPartsPresent() {
            final MockLogRecord record = createRecord(Level.INFO, "This is a test message.", null);
            assertTrue(AssertHelper.hasAllMessageParts(record, new String[]{"is a", "message"}), "should be true when all parts are found");
        }

        @Test
        @DisplayName("should return false if message does not contain all parts")
        void shouldReturnFalseWhenPartsMissing() {
            final MockLogRecord record = createRecord(Level.INFO, "This is a test message.", null);
            assertFalse(AssertHelper.hasAllMessageParts(record, new String[]{"is a", "payload"}), "should be false when parts are missing");
        }
    }

    @Nested
    @DisplayName("isLevel() tests")
    class IsLevelTest {
        @Test
        @DisplayName("should return true for same level, false otherwise")
        void shouldReturnCorrectBoolean() {
            final MockLogRecord record = createRecord(Level.FINE, "message", null);
            assertTrue(AssertHelper.isLevel(record, Level.FINE), "should be true for same level");
            assertFalse(AssertHelper.isLevel(record, Level.INFO), "should be false for different level");
        }
    }

    @Nested
    @DisplayName("isThrowableOfInstance() tests")
    class IsThrowableOfInstanceTest {
        @Test
        @DisplayName("should return true for same class or superclass")
        void shouldReturnTrueForSameOrSuperclass() {
            final Throwable throwable = new IllegalArgumentException("error");
            assertTrue(AssertHelper.isThrowableOfInstance(throwable, IllegalArgumentException.class), "should be true for same class");
            assertTrue(AssertHelper.isThrowableOfInstance(throwable, RuntimeException.class), "should be true for superclass");
        }

        @Test
        @DisplayName("should return false for different class or null")
        void shouldReturnFalseForDifferentClassOrNull() {
            final Throwable throwable = new IllegalArgumentException("error");
            assertFalse(AssertHelper.isThrowableOfInstance(throwable, IllegalStateException.class), "should be false for different class");
            assertFalse(AssertHelper.isThrowableOfInstance(throwable, null), "should be false for null class");
        }
    }

    @Nested
    @DisplayName("hasAllMessageParts() for throwable tests")
    class HasAllMessagePartsForThrowableTest {
        @Test
        @DisplayName("should return true if message contains all parts")
        void shouldReturnTrueWhenAllPartsPresent() {
            final Throwable throwable = new RuntimeException("This is an error message.");
            assertTrue(AssertHelper.hasAllMessageParts(throwable, new String[]{"error", "message"}), "should be true when all parts are found");
        }

        @Test
        @DisplayName("should return false if message does not contain all parts")
        void shouldReturnFalseWhenPartsMissing() {
            final Throwable throwable = new RuntimeException("This is an error message.");
            assertFalse(AssertHelper.hasAllMessageParts(throwable, new String[]{"error", "payload"}), "should be false when parts are missing");
        }

        @Test
        @DisplayName("should return false if throwable message is null")
        void shouldReturnFalseForNullMessage() {
            final Throwable throwableWithoutMessage = new RuntimeException();
            assertFalse(AssertHelper.hasAllMessageParts(throwableWithoutMessage, new String[]{"error"}), "should be false for null message");
        }
    }

    @Nested
    @DisplayName("assertMessagePartsNot() tests")
    class AssertMessagePartsNotTest {
        @Test
        @DisplayName("should pass if message does not contain all parts")
        void shouldPassWhenPartsNotPresent() {
            final MockLogRecord record = createRecord(Level.INFO, "This is a test message.", null);
            assertDoesNotThrow(() -> AssertHelper.assertMessagePartsNot(record, new String[]{"This is", "another message"}), "should not throw when parts are not present");
        }

        @Test
        @DisplayName("should fail if message contains all parts")
        void shouldFailWhenAllPartsPresent() {
            final MockLogRecord record = createRecord(Level.INFO, "This is a test message.", null);
            assertThrows(AssertionError.class, () -> AssertHelper.assertMessagePartsNot(record, new String[]{"This is", "test message"}), "should throw when all parts are present");
        }
    }

    @Nested
    @DisplayName("assertLevelNot() tests")
    class AssertLevelNotTest {
        @Test
        @DisplayName("should pass if levels are different")
        void shouldPassForDifferentLevel() {
            final MockLogRecord record = createRecord(Level.INFO, "message", null);
            assertDoesNotThrow(() -> AssertHelper.assertLevelNot(record, Level.WARNING), "should not throw for different level");
        }

        @Test
        @DisplayName("should fail if levels are the same")
        void shouldFailForSameLevel() {
            final MockLogRecord record = createRecord(Level.INFO, "message", null);
            assertThrows(AssertionError.class, () -> AssertHelper.assertLevelNot(record, Level.INFO), "should throw for same level");
        }
    }

    @Nested
    @DisplayName("assertThrowableNotOfInstance() tests")
    class AssertThrowableNotOfInstanceTest {
        @Test
        @DisplayName("should pass for a different class")
        void shouldPassForDifferentClass() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", new IllegalArgumentException("error"));
            final Throwable throwable = record.getThrown();
            assertDoesNotThrow(() -> AssertHelper.assertThrowableNotOfInstance(record, throwable, IllegalStateException.class), "should not throw for different class");
        }

        @Test
        @DisplayName("should pass if throwable is null")
        void shouldPassForNullThrowable() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", null);
            final Throwable throwable = record.getThrown();
            assertDoesNotThrow(() -> AssertHelper.assertThrowableNotOfInstance(record, throwable, IllegalArgumentException.class), "should not throw for null throwable");
        }

        @Test
        @DisplayName("should fail for the same class")
        void shouldFailForSameClass() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", new IllegalArgumentException("error"));
            final Throwable throwable = record.getThrown();
            assertThrows(AssertionError.class, () -> AssertHelper.assertThrowableNotOfInstance(record, throwable, IllegalArgumentException.class), "should throw for same class");
        }

        @Test
        @DisplayName("should fail for a superclass")
        void shouldFailForSuperclass() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", new IllegalArgumentException("error"));
            final Throwable throwable = record.getThrown();
            assertThrows(AssertionError.class, () -> AssertHelper.assertThrowableNotOfInstance(record, throwable, RuntimeException.class), "should throw for superclass");
        }
    }

    @Nested
    @DisplayName("assertThrowableHasMessagePartsNot() tests")
    class AssertThrowableHasMessagePartsNotTest {
        @Test
        @DisplayName("should pass if throwable message does not contain all parts")
        void shouldPassWhenPartsNotPresent() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", new RuntimeException("This is an error."));
            final Throwable throwable = record.getThrown();
            assertDoesNotThrow(() -> AssertHelper.assertThrowableHasMessagePartsNot(record, throwable, new String[]{"is an", "mistake"}), "should not throw when parts are not present");
        }

        @Test
        @DisplayName("should pass if throwable is null")
        void shouldPassForNullThrowable() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", null);
            final Throwable throwable = record.getThrown();
            assertDoesNotThrow(() -> AssertHelper.assertThrowableHasMessagePartsNot(record, throwable, new String[]{"error"}), "should not throw for null throwable");
        }

        @Test
        @DisplayName("should fail if throwable message contains all parts")
        void shouldFailWhenAllPartsPresent() {
            final MockLogRecord record = createRecord(Level.SEVERE, "message", new RuntimeException("This is an error."));
            final Throwable throwable = record.getThrown();
            assertThrows(AssertionError.class, () -> AssertHelper.assertThrowableHasMessagePartsNot(record, throwable, new String[]{"is an", "error"}), "should throw when all parts are present");
        }
    }
}
