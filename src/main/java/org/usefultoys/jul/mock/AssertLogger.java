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

import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.logging.Level;

/**
 * Utility class providing static assertion methods for testing {@link MockHandler} instances.
 * <p>
 * This class contains methods to verify that logged records match expected criteria such as log level
 * and message content. All assertion methods are static and take a {@link MockHandler} instance as their
 * first parameter.
 * <p>
 * Example usage:
 * <pre>{@code
 * Logger logger = Logger.getLogger("test");
 * MockHandler handler = new MockHandler();
 * logger.addHandler(handler);
 * logger.info("Test message");
 * 
 * AssertLogger.assertRecord(handler, 0, Level.INFO, "Test message");
 * }</pre>
 *
 * @author Daniel Felix Ferber
 */
public final class AssertLogger {

    private AssertLogger() {
        // Utility class - prevent instantiation
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with the expected message parts.
     *
     * @param handler       the MockHandler instance to check
     * @param recordIndex   the index of the record to check
     * @param messageParts  an array of substrings that should be present in the record's message
     */
    public static void assertRecord(final MockHandler handler, final int recordIndex, final String... messageParts) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        AssertHelper.assertMessageParts(record, messageParts);
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with the expected level and message parts.
     *
     * @param handler       the MockHandler instance to check
     * @param recordIndex   the index of the record to check
     * @param expectedLevel the expected log level of the record
     * @param messageParts  an array of substrings that should be present in the record's message
     */
    public static void assertRecord(final MockHandler handler, final int recordIndex, final Level expectedLevel,
                                     final String... messageParts) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        AssertHelper.assertLevel(record, expectedLevel);
        AssertHelper.assertMessageParts(record, messageParts);
    }

    /**
     * Asserts that the handler has recorded at least one record containing the expected message parts.
     *
     * @param handler       the MockHandler instance to check
     * @param messageParts  an array of substrings that should be present in at least one record's message
     */
    public static void assertHasRecord(final MockHandler handler, final String... messageParts) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> AssertHelper.hasAllMessageParts(record, messageParts));
        Assertions.assertTrue(hasRecord,
                String.format("should have at least one record containing expected message parts; expected: %s",
                        String.join(", ", messageParts)));
    }

    /**
     * Asserts that the handler has recorded at least one record with the expected level and all message parts.
     *
     * @param handler       the MockHandler instance to check
     * @param expectedLevel the expected log level
     * @param messageParts  an array of substrings that should all be present in the record's message
     */
    public static void assertHasRecord(final MockHandler handler, final Level expectedLevel,
                                        final String... messageParts) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> AssertHelper.isLevel(record, expectedLevel) && AssertHelper.hasAllMessageParts(record, messageParts));
        Assertions.assertTrue(hasRecord,
                String.format("should have at least one record with expected level and all message parts; expected level: %s, expected messages: %s",
                        expectedLevel, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with a throwable of the expected type.
     *
     * @param handler        the MockHandler instance to check
     * @param recordIndex    the index of the record to check
     * @param throwableClass the expected throwable class
     */
    public static void assertRecordWithThrowable(final MockHandler handler, final int recordIndex,
                                                  final Class<? extends Throwable> throwableClass) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        final Throwable throwable = record.getThrown();
        AssertHelper.assertThrowableOfInstance(record, throwable, throwableClass);
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with a throwable of the expected type and message.
     *
     * @param handler          the MockHandler instance to check
     * @param recordIndex      the index of the record to check
     * @param throwableClass   the expected throwable class
     * @param throwableMessage a substring that should be present in the throwable's message
     */
    public static void assertRecordWithThrowable(final MockHandler handler, final int recordIndex,
                                                  final Class<? extends Throwable> throwableClass,
                                                  final String throwableMessage) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        final Throwable throwable = record.getThrown();
        AssertHelper.assertThrowableOfInstance(record, throwable, throwableClass);
        AssertHelper.assertThrowableHasMessageParts(record, throwable, new String[]{throwableMessage});
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with a throwable of the expected type and message parts.
     *
     * @param handler               the MockHandler instance to check
     * @param recordIndex           the index of the record to check
     * @param throwableClass        the expected throwable class
     * @param throwableMessageParts an array of substrings that should be present in the throwable's message
     */
    public static void assertRecordWithThrowable(final MockHandler handler, final int recordIndex,
                                                  final Class<? extends Throwable> throwableClass,
                                                  final String... throwableMessageParts) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        final Throwable throwable = record.getThrown();
        AssertHelper.assertThrowableOfInstance(record, throwable, throwableClass);
        AssertHelper.assertThrowableHasMessageParts(record, throwable, throwableMessageParts);
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with any throwable.
     *
     * @param handler     the MockHandler instance to check
     * @param recordIndex the index of the record to check
     */
    public static void assertRecordHasThrowable(final MockHandler handler, final int recordIndex) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        Assertions.assertNotNull(record.getThrown(), "should have a throwable");
    }

    /**
     * Asserts that the handler has recorded at least one record with a throwable of the expected type.
     *
     * @param handler        the MockHandler instance to check
     * @param throwableClass the expected throwable class
     */
    public static void assertHasRecordWithThrowable(final MockHandler handler,
                                                     final Class<? extends Throwable> throwableClass) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> {
                    final Throwable throwable = record.getThrown();
                    return throwable != null && AssertHelper.isThrowableOfInstance(throwable, throwableClass);
                });
        Assertions.assertTrue(hasRecord,
                String.format("should have at least one record with expected throwable type; expected: %s",
                        throwableClass.getName()));
    }

    /**
     * Asserts that the handler has recorded at least one record with a throwable of the expected type and message.
     *
     * @param handler          the MockHandler instance to check
     * @param throwableClass   the expected throwable class
     * @param throwableMessage a substring that should be present in the throwable's message
     */
    public static void assertHasRecordWithThrowable(final MockHandler handler,
                                                     final Class<? extends Throwable> throwableClass,
                                                     final String throwableMessage) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> {
                    final Throwable throwable = record.getThrown();
                    if (throwable == null || !AssertHelper.isThrowableOfInstance(throwable, throwableClass)) {
                        return false;
                    }
                    return AssertHelper.hasAllMessageParts(throwable, new String[]{throwableMessage});
                });
        Assertions.assertTrue(hasRecord,
                String.format("should have at least one record with expected throwable type and message; expected type: %s, expected message: %s",
                        throwableClass.getName(), throwableMessage));
    }

    /**
     * Asserts that the handler has recorded at least one record with a throwable of the expected type and message parts.
     *
     * @param handler                the MockHandler instance to check
     * @param throwableClass         the expected throwable class
     * @param throwableMessageParts  an array of substrings that should be present in the throwable's message
     */
    public static void assertHasRecordWithThrowable(final MockHandler handler,
                                                     final Class<? extends Throwable> throwableClass,
                                                     final String... throwableMessageParts) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> {
                    final Throwable throwable = record.getThrown();
                    if (throwable == null || !AssertHelper.isThrowableOfInstance(throwable, throwableClass)) {
                        return false;
                    }
                    return AssertHelper.hasAllMessageParts(throwable, throwableMessageParts);
                });
        Assertions.assertTrue(hasRecord,
                String.format("should have at least one record with expected throwable type and message parts; expected type: %s, expected messages: %s",
                        throwableClass.getName(), String.join(", ", throwableMessageParts)));
    }

    /**
     * Asserts that the handler has recorded at least one record with any throwable.
     *
     * @param handler the MockHandler instance to check
     */
    public static void assertHasRecordWithThrowable(final MockHandler handler) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> record.getThrown() != null);
        Assertions.assertTrue(hasRecord, "should have at least one record with a throwable");
    }

    /**
     * Asserts that the handler has recorded the expected number of records.
     *
     * @param handler       the MockHandler instance to check
     * @param expectedCount the expected number of records
     */
    public static void assertRecordCount(final MockHandler handler, final int expectedCount) {
        final int actualCount = handler.getRecordCount();
        Assertions.assertEquals(expectedCount, actualCount,
                String.format("should have expected number of records; expected: %d, actual: %d",
                        expectedCount, actualCount));
    }

    /**
     * Asserts that the handler has recorded no records.
     *
     * @param handler the MockHandler instance to check
     */
    public static void assertNoRecords(final MockHandler handler) {
        assertRecordCount(handler, 0);
    }

    /**
     * Asserts that the handler has recorded the expected number of records with the specified level.
     *
     * @param handler       the MockHandler instance to check
     * @param level         the log level to count
     * @param expectedCount the expected number of records with the specified level
     */
    public static void assertRecordCountByLevel(final MockHandler handler, final Level level,
                                                 final int expectedCount) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final long actualCount = logRecords.stream()
                .filter(record -> AssertHelper.isLevel(record, level))
                .count();
        Assertions.assertEquals(expectedCount, actualCount,
                String.format("should have expected number of records with level %s; expected: %d, actual: %d",
                        level, expectedCount, actualCount));
    }

    /**
     * Asserts that the handler has recorded the expected number of records containing the specified message part.
     *
     * @param handler       the MockHandler instance to check
     * @param messagePart   a substring that should be present in the record's message
     * @param expectedCount the expected number of records containing the message part
     */
    public static void assertRecordCountByMessage(final MockHandler handler, final String messagePart,
                                                   final int expectedCount) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final long actualCount = logRecords.stream()
                .filter(record -> AssertHelper.hasAllMessageParts(record, new String[]{messagePart}))
                .count();
        Assertions.assertEquals(expectedCount, actualCount,
                String.format("should have expected number of records containing message part '%s'; expected: %d, actual: %d",
                        messagePart, expectedCount, actualCount));
    }

    /**
     * Asserts that the handler has recorded records in the exact sequence of log levels specified.
     *
     * @param handler        the MockHandler instance to check
     * @param expectedLevels the expected sequence of log levels
     */
    public static void assertRecordSequence(final MockHandler handler, final Level... expectedLevels) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);

        Assertions.assertEquals(expectedLevels.length, logRecords.size(),
                String.format("should have expected number of records for sequence; expected: %d, actual: %d",
                        expectedLevels.length, logRecords.size()));

        for (int i = 0; i < expectedLevels.length; i++) {
            final Level actualLevel = logRecords.get(i).getLevel();
            Assertions.assertSame(expectedLevels[i], actualLevel,
                    String.format("should have expected level at position %d; expected: %s, actual: %s",
                            i, expectedLevels[i], actualLevel));
        }
    }

    /**
     * Asserts that the handler has recorded records containing message parts in the exact sequence specified.
     *
     * @param handler              the MockHandler instance to check
     * @param expectedMessageParts the expected sequence of message parts
     */
    public static void assertRecordSequence(final MockHandler handler, final String... expectedMessageParts) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);

        Assertions.assertEquals(expectedMessageParts.length, logRecords.size(),
                String.format("should have expected number of records for sequence; expected: %d, actual: %d",
                        expectedMessageParts.length, logRecords.size()));

        for (int i = 0; i < expectedMessageParts.length; i++) {
            final String actualMessage = logRecords.get(i).getFormattedMessage();
            Assertions.assertTrue(actualMessage.contains(expectedMessageParts[i]),
                    String.format("should contain expected message part at position %d; expected: %s, actual message: %s",
                            i, expectedMessageParts[i], actualMessage));
        }
    }

    // Negative assertions - record at specific index does NOT match criteria

    /**
     * Asserts that the handler has recorded a record at the specified index that does NOT contain the given message parts.
     *
     * @param handler       the MockHandler instance to check
     * @param recordIndex   the index of the record to check
     * @param messageParts  an array of substrings that should NOT be present in the record's message
     */
    public static void assertRecordNot(final MockHandler handler, final int recordIndex, final String... messageParts) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        final boolean match = AssertHelper.hasAllMessageParts(record, messageParts);
        Assertions.assertFalse(match, String.format("should not have record at index %d with message parts; unexpected message parts: %s", recordIndex, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the handler has recorded a record at the specified index that does NOT have the given level AND message parts.
     *
     * @param handler       the MockHandler instance to check
     * @param recordIndex   the index of the record to check
     * @param unexpectedLevel  the log level that should NOT be present in the record
     * @param messageParts  an array of substrings that should NOT be present in the record's message
     */
    public static void assertRecordNot(final MockHandler handler, final int recordIndex, final Level unexpectedLevel, final String... messageParts) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        final boolean match = AssertHelper.isLevel(record, unexpectedLevel) && AssertHelper.hasAllMessageParts(record, messageParts);
        Assertions.assertFalse(match, String.format("should not have record at index %d with level and message parts; unexpected level: %s, unexpected message parts: %s", recordIndex, unexpectedLevel, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the handler has recorded a record at the specified index that does NOT have a throwable.
     *
     * @param handler       the MockHandler instance to check
     * @param recordIndex   the index of the record to check
     */
    public static void assertRecordNotWithThrowable(final MockHandler handler, final int recordIndex) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        Assertions.assertNull(record.getThrown(), String.format("record at index %d should NOT have a throwable; actual: %s", recordIndex, record.getThrown()));
    }

    /**
     * Asserts that the handler has recorded a record at the specified index that does NOT have a throwable of the given type.
     *
     * @param handler                   the MockHandler instance to check
     * @param recordIndex               the index of the record to check
     * @param unexpectedThrowableClass  the throwable class that should NOT be present in the record
     */
    public static void assertRecordNotWithThrowable(final MockHandler handler, final int recordIndex, final Class<? extends Throwable> unexpectedThrowableClass) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        final boolean match = AssertHelper.isThrowableOfInstance(record.getThrown(), unexpectedThrowableClass);
        Assertions.assertFalse(match, String.format("should not have record at index %d with throwable of type; unexpected type: %s", recordIndex, unexpectedThrowableClass.getName()));
    }

    /**
     * Asserts that the handler has recorded a record at the specified index that does NOT have a throwable of the given type AND message parts.
     *
     * @param handler                   the MockHandler instance to check
     * @param recordIndex               the index of the record to check
     * @param unexpectedThrowableClass  the throwable class that should NOT be present in the record
     * @param throwableMessageParts     an array of substrings that should NOT be present in the throwable's message
     */
    public static void assertRecordNotWithThrowable(final MockHandler handler, final int recordIndex, final Class<? extends Throwable> unexpectedThrowableClass, final String... throwableMessageParts) {
        final MockLogRecord record = AssertHelper.handlerIndexToRecord(handler, recordIndex);
        final boolean match = AssertHelper.isThrowableOfInstance(record.getThrown(), unexpectedThrowableClass) && AssertHelper.hasAllMessageParts(record.getThrown(), throwableMessageParts);
        Assertions.assertFalse(match, String.format("should not have record at index %d with throwable of type and message parts; unexpected type: %s, unexpected message parts: %s", recordIndex, unexpectedThrowableClass.getName(), String.join(", ", throwableMessageParts)));
    }

    // Negative assertions - no records matching criteria exist anywhere

    /**
     * Asserts that the handler has not recorded any record containing the specified message parts.
     *
     * @param handler       the MockHandler instance to check
     * @param messageParts  an array of substrings that should not be present together in any record's message
     */
    public static void assertNoRecord(final MockHandler handler, final String... messageParts) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> AssertHelper.hasAllMessageParts(record, messageParts));
        Assertions.assertFalse(hasRecord,
                String.format("should have no records containing message parts; unexpected message parts: %s", String.join(", ", messageParts)));
    }

    /**
     * Asserts that the handler has not recorded any record with the specified level and message parts.
     *
     * @param handler       the MockHandler instance to check
     * @param level         the log level that should not be present in any record
     * @param messageParts  an array of substrings that should not be present in any record's message
     */
    public static void assertNoRecord(final MockHandler handler, final Level level, final String... messageParts) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> AssertHelper.isLevel(record, level) && AssertHelper.hasAllMessageParts(record, messageParts));
        Assertions.assertFalse(hasRecord,
                String.format("should have no records with level and message parts; unexpected level: %s, unexpected messages: %s", level, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the handler has not recorded any record with a throwable.
     *
     * @param handler the MockHandler instance to check
     */
    public static void assertNoRecordWithThrowable(final MockHandler handler) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> record.getThrown() != null);
        Assertions.assertFalse(hasRecord, "should have no records with a throwable");
    }

    /**
     * Asserts that the handler has not recorded any record with a throwable of the specified type.
     *
     * @param handler                  the MockHandler instance to check
     * @param unexpectedThrowableClass the throwable class that should not be present in any record
     */
    public static void assertNoRecordWithThrowable(final MockHandler handler, final Class<? extends Throwable> unexpectedThrowableClass) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> AssertHelper.isThrowableOfInstance(record.getThrown(), unexpectedThrowableClass));
        Assertions.assertFalse(hasRecord, String.format("should have no records with throwable type; unexpected type: %s", unexpectedThrowableClass.getName()));
    }

    /**
     * Asserts that the handler has not recorded any record with a throwable of the specified type and message parts.
     *
     * @param handler                  the MockHandler instance to check
     * @param unexpectedThrowableClass the throwable class that should not be present in any record
     * @param throwableMessageParts    an array of substrings that should not be present in the throwable's message
     */
    public static void assertNoRecordWithThrowable(final MockHandler handler, final Class<? extends Throwable> unexpectedThrowableClass, final String... throwableMessageParts) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> AssertHelper.isThrowableOfInstance(record.getThrown(), unexpectedThrowableClass) && AssertHelper.hasAllMessageParts(record.getThrown(), throwableMessageParts));
        Assertions.assertFalse(hasRecord, String.format("should have no records with throwable type and message parts; unexpected type: %s, unexpected messages: %s", unexpectedThrowableClass.getName(), String.join(", ", throwableMessageParts)));
    }

    /**
     * Asserts that the handler has not recorded any record with a throwable containing the specified message parts.
     *
     * @param handler                 the MockHandler instance to check
     * @param throwableMessageParts   an array of substrings that should not be present in the throwable's message
     */
    public static void assertNoRecordWithThrowable(final MockHandler handler, final String... throwableMessageParts) {
        final List<MockLogRecord> logRecords = AssertHelper.handlerToRecords(handler);
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> AssertHelper.hasAllMessageParts(record.getThrown(), throwableMessageParts));
        Assertions.assertFalse(hasRecord, String.format("should have no records with throwable message parts; unexpected messages: %s", String.join(", ", throwableMessageParts)));
    }
}
