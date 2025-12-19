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

import java.util.List;

/**
 * Utility class for formatting logged records in a readable format.
 * Used for debugging failed assertions by displaying all captured log records.
 *
 * @author Daniel Felix Ferber
 */
public final class HandlerRecordFormatter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private HandlerRecordFormatter() {
        // Utility class
    }

    /**
     * Formats all logged records from a handler into a readable string.
     * Shows record index, level, logger name, and message for each record.
     *
     * @param handler the MockHandler instance
     * @return a formatted string containing all logged records
     */
    public static String formatLoggedRecords(final MockHandler handler) {
        if (handler == null) {
            return "  (handler is null)";
        }

        final List<MockLogRecord> records = handler.getLogRecords();

        if (records.isEmpty()) {
            return "  (no records logged)";
        }

        final StringBuilder sb = new StringBuilder(256);
        sb.append(String.format("  Total records: %d%n", records.size()));

        int index = 0;
        for (final MockLogRecord record : records) {
            sb.append(String.format("  [%d] %-7s", index++, record.getLevel().getName()));
            sb.append(String.format(" | logger=%-30s", record.getLoggerName()));
            sb.append(String.format(" | %s%n", record.getFormattedMessage()));

            if (record.getThrown() != null) {
                sb.append(String.format("        └─ throwable: %s: %s%n", 
                    record.getThrown().getClass().getSimpleName(),
                    record.getThrown().getMessage()));
            }
        }

        return sb.toString();
    }
}
