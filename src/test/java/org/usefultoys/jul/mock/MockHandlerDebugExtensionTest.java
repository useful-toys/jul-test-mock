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
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Example tests demonstrating the MockHandlerDebugExtension.
 * <p>
 * When assertions fail, the extension automatically prints all logged records,
 * making it easier to debug test failures.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("MockHandlerDebugExtension Examples")
class MockHandlerDebugExtensionTest {

    /**
     * Example of using the extension with a single handler.
     * When the assertion fails, all logged records will be printed automatically.
     */
    @Nested
    @DisplayName("Single Handler Example")
    @ExtendWith(MockHandlerDebugExtension.class)
    class SingleHandlerExample {

        @Test
        @DisplayName("example: logs multiple records and assertion passes")
        void exampleMultipleRecordsWithSuccess() {
            final Logger logger = Logger.getLogger("example.single");
            logger.setUseParentHandlers(false);
            final MockHandler handler = new MockHandler();
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);

            // Log some records
            logger.fine("Debug message");
            logger.info("Info message");
            logger.warning("Warning message");

            // This assertion passes
            assertEquals(3, handler.getRecordCount(), "should have 3 records");
        }

        @Test
        @DisplayName("example: logs records with different levels")
        void exampleDifferentLogLevels() {
            final Logger logger = Logger.getLogger("example.levels");
            logger.setUseParentHandlers(false);
            final MockHandler handler = new MockHandler();
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);

            logger.finest("Finest level");
            logger.fine("Fine level");
            logger.info("Info level");
            logger.warning("Warning level");
            logger.severe("Severe level");

            // This will show all 5 records with their levels
            assertEquals(5, handler.getRecordCount(), "should have 5 records");
        }
    }

    /**
     * Example of using the extension with multiple handlers.
     * The extension will print records from all MockHandler instances found in parameters or fields.
     */
    @Nested
    @DisplayName("Multiple Handlers Example")
    @ExtendWith(MockHandlerDebugExtension.class)
    class MultipleHandlersExample {

        @Test
        @DisplayName("example: logs to multiple handlers")
        void exampleMultipleHandlers() {
            final Logger logger1 = Logger.getLogger("example.handler1");
            logger1.setUseParentHandlers(false);
            final MockHandler handler1 = new MockHandler();
            logger1.addHandler(handler1);
            logger1.setLevel(Level.ALL);

            final Logger logger2 = Logger.getLogger("example.handler2");
            logger2.setUseParentHandlers(false);
            final MockHandler handler2 = new MockHandler();
            logger2.addHandler(handler2);
            logger2.setLevel(Level.ALL);

            // Log to first handler
            logger1.info("Message to handler 1");
            logger1.warning("Warning to handler 1");

            // Log to second handler
            logger2.info("Message to handler 2");
            logger2.severe("Error to handler 2");

            // Verify counts
            assertEquals(2, handler1.getRecordCount(), "handler1 should have 2 records");
            assertEquals(2, handler2.getRecordCount(), "handler2 should have 2 records");
        }
    }

    /**
     * Example of using the extension with exceptions.
     */
    @Nested
    @DisplayName("Exceptions Example")
    @ExtendWith(MockHandlerDebugExtension.class)
    class ExceptionsExample {

        @Test
        @DisplayName("example: logs exceptions")
        void exampleWithException() {
            final Logger logger = Logger.getLogger("example.exception");
            logger.setUseParentHandlers(false);
            final MockHandler handler = new MockHandler();
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);

            final Exception testException = new RuntimeException("Test error message");

            logger.info("Normal message");
            logger.log(Level.SEVERE, "Error occurred", testException);

            assertEquals(2, handler.getRecordCount(), "should have 2 records");
            assertEquals(testException, handler.getRecord(1).getThrown(), "should have exception");
        }
    }

    /**
     * Example of passing tests (no debug output).
     */
    @Nested
    @DisplayName("Passing Test Example")
    @ExtendWith(MockHandlerDebugExtension.class)
    class PassingTestExample {

        @Test
        @DisplayName("example: test passes - no debug output")
        void examplePassingTest() {
            final Logger logger = Logger.getLogger("example.passing");
            logger.setUseParentHandlers(false);
            final MockHandler handler = new MockHandler();
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);

            logger.info("This is a passing test");

            // This assertion passes, so no debug output is printed
            assertEquals(1, handler.getRecordCount(), "should have 1 record");
        }
    }
}
