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

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * A package-private utility class with helper methods for {@link AssertLogger}.
 * <p>
 * This class centralizes common assertion logic and checks performed on {@link MockHandler}
 * and its {@link MockLogRecord}s. It is not intended for public use.
 * </p>
 */
@UtilityClass
class AssertHelper {
    /**
     * Extracts the list of recorded {@link MockLogRecord}s from a handler.
     *
     * @param handler the handler to extract records from. Must be a {@link MockHandler}.
     * @return the list of log records.
     */
    List<MockLogRecord> handlerToRecords(final MockHandler handler) {
        return handler.getLogRecords();
    }

    /**
     * Retrieves a specific {@link MockLogRecord} from a handler by its index.
     *
     * @param handler     the handler to get the record from.
     * @param recordIndex the index of the record to retrieve.
     * @return the {@link MockLogRecord} at the specified index.
     * @throws AssertionError if the index is out of bounds.
     */
    MockLogRecord handlerIndexToRecord(final MockHandler handler, final int recordIndex) {
        final List<MockLogRecord> logRecords = handlerToRecords(handler);
        Assertions.assertTrue(recordIndex < logRecords.size(),
                String.format("should have enough log records; requested record: %d, available records: %d", recordIndex, logRecords.size()));
        return logRecords.get(recordIndex);
    }

    /**
     * Asserts that a log record's formatted message contains all the specified message parts.
     *
     * @param record       the log record to check.
     * @param messageParts the substrings expected to be in the log message.
     * @throws AssertionError if the message does not contain all parts.
     */
    void assertMessageParts(final MockLogRecord record, final String[] messageParts) {
        final boolean hasAllParts = hasAllMessageParts(record, messageParts);
        Assertions.assertTrue(hasAllParts,
                String.format("should contain all expected message parts; expected parts: %s; actual message: %s",
                        String.join(", ", messageParts), record.getFormattedMessage()));
    }

    /**
     * Asserts that a log record's formatted message does NOT contain all the specified message parts.
     *
     * @param record       the log record to check.
     * @param messageParts the substrings expected NOT to be in the log message.
     * @throws AssertionError if the message contains all parts.
     */
    void assertMessagePartsNot(final MockLogRecord record, final String[] messageParts) {
        final boolean hasAllParts = hasAllMessageParts(record, messageParts);
        Assertions.assertFalse(hasAllParts,
                String.format("should not contain all expected message parts; unexpected parts: %s; actual message: %s",
                        String.join(", ", messageParts), record.getFormattedMessage()));
    }

    /**
     * Asserts that a log record has the expected {@link Level}.
     *
     * @param record        the log record to check.
     * @param expectedLevel the expected log level.
     * @throws AssertionError if the record's level does not match the expected one.
     */
    void assertLevel(final MockLogRecord record, final Level expectedLevel) {
        Assertions.assertSame(expectedLevel, record.getLevel(),
                String.format("should have expected log level; expected: %s, actual: %s", expectedLevel, record.getLevel()));
    }

    /**
     * Asserts that a log record does NOT have the unexpected {@link Level}.
     *
     * @param record         the log record to check.
     * @param unexpectedLevel the log level expected NOT to be present.
     * @throws AssertionError if the record's level matches the unexpected one.
     */
    void assertLevelNot(final MockLogRecord record, final Level unexpectedLevel) {
        Assertions.assertNotSame(unexpectedLevel, record.getLevel(),
                String.format("should not have unexpected log level; unexpected: %s", unexpectedLevel));
    }

    /**
     * Asserts that a {@link Throwable} is not null and is an instance of the expected class.
     *
     * @param record                 the log record to check.
     * @param throwable              the throwable to check.
     * @param expectedThrowableClass the expected class of the throwable.
     * @throws AssertionError if the throwable is null or not of the expected type.
     */
    void assertThrowableOfInstance(final MockLogRecord record, final Throwable throwable, final Class<? extends Throwable> expectedThrowableClass) {
        Assertions.assertNotNull(throwable, "should have a throwable");
        Assertions.assertTrue(expectedThrowableClass.isInstance(throwable),
                String.format("should have expected throwable type; expected: %s, actual: %s",
                        expectedThrowableClass.getName(), throwable.getClass().getName()));
    }

    /**
     * Asserts that a {@link Throwable}} is NOT an instance of the unexpected class.
     * If the throwable is null, this assertion passes.
     *
     * @param record                   the log record to check.
     * @param throwable                the throwable to check.
     * @param unexpectedThrowableClass the class of the throwable expected NOT to be.
     * @throws AssertionError if the throwable is not null and is an instance of the unexpected type.
     */
    void assertThrowableNotOfInstance(final MockLogRecord record, final Throwable throwable, final Class<? extends Throwable> unexpectedThrowableClass) {
        if (throwable != null) {
            Assertions.assertFalse(unexpectedThrowableClass.isInstance(throwable),
                    String.format("should not have unexpected throwable type; unexpected: %s, actual: %s",
                            unexpectedThrowableClass.getName(), throwable.getClass().getName()));
        }
    }

    /**
     * Asserts that a {@link Throwable}'s message contains all the specified message parts.
     *
     * @param record                the log record to check.
     * @param throwable             the throwable to check.
     * @param throwableMessageParts the substrings expected to be in the throwable's message.
     * @throws AssertionError if the throwable's message does not contain all parts.
     */
    void assertThrowableHasMessageParts(final MockLogRecord record, final Throwable throwable, final String[] throwableMessageParts) {
        final boolean hasAllParts = hasAllMessageParts(throwable, throwableMessageParts);
        Assertions.assertTrue(hasAllParts,
                String.format("should contain all expected message parts in throwable; expected parts: %s; actual message: %s", String.join(", ", throwableMessageParts), throwable));
    }

    /**
     * Asserts that a {@link Throwable}'s message does NOT contain all the specified message parts.
     * If the throwable is null, this assertion passes.
     *
     * @param record                the log record to check.
     * @param throwable             the throwable to check.
     * @param throwableMessageParts the substrings expected NOT to be in the throwable's message.
     * @throws AssertionError if the throwable's message contains all parts.
     */
    void assertThrowableHasMessagePartsNot(final MockLogRecord record, final Throwable throwable, final String[] throwableMessageParts) {
        if (throwable != null) {
            final boolean hasAllParts = hasAllMessageParts(throwable, throwableMessageParts);
            Assertions.assertFalse(hasAllParts,
                    String.format("should not contain all expected message parts in throwable; unexpected parts: %s; actual message: %s", String.join(", ", throwableMessageParts), throwable));
        }
    }

    /**
     * Checks if a log record's formatted message contains all specified substrings.
     *
     * @param record       the log record.
     * @param messageParts the array of substrings to check for.
     * @return {@code true} if the message contains all parts, {@code false} otherwise.
     */
    boolean hasAllMessageParts(final MockLogRecord record, final String[] messageParts) {
        final String formattedMessage = record.getFormattedMessage();
        return Arrays.stream(messageParts).allMatch(formattedMessage::contains);
    }

    /**
     * Checks if a log record's level is the same as the expected level.
     *
     * @param record        the log record.
     * @param expectedLevel the expected level.
     * @return {@code true} if the levels are the same, {@code false} otherwise.
     */
    boolean isLevel(final MockLogRecord record, final Level expectedLevel) {
        return expectedLevel == record.getLevel();
    }

    /**
     * Checks if a throwable is an instance of a given class.
     *
     * @param throwable              the throwable to check.
     * @param expectedThrowableClass the class to check against.
     * @return {@code true} if the throwable is an instance of the class, {@code false} otherwise.
     */
    boolean isThrowableOfInstance(final Throwable throwable, final Class<? extends Throwable> expectedThrowableClass) {
        return expectedThrowableClass != null && expectedThrowableClass.isInstance(throwable);
    }

    /**
     * Checks if a throwable's message contains all specified substrings.
     *
     * @param throwable    the throwable to check.
     * @param messageParts the array of substrings to check for.
     * @return {@code true} if the message is not null and contains all parts, {@code false} otherwise.
     */
    boolean hasAllMessageParts(final Throwable throwable, final String[] messageParts) {
        final String message = throwable.getMessage();
        return message != null && Arrays.stream(messageParts).allMatch(message::contains);
    }
}
