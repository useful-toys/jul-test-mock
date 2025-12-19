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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit 5 Extension that automatically prints logged records when a test fails.
 * <p>
 * This extension intercepts test execution and catches assertion errors.
 * When an assertion fails, it prints all logged records from MockHandler instances
 * found in the test method parameters or fields, making it easier to debug test failures.
 * <p>
 * Usage:
 * <pre>{@code
 * @ExtendWith(MockHandlerDebugExtension.class)
 * class MyTest {
 *     @Test
 *     void testSomething() {
 *         Logger logger = Logger.getLogger("test");
 *         MockHandler handler = new MockHandler();
 *         logger.addHandler(handler);
 *         logger.info("test message");
 *         AssertLogger.assertRecord(handler, 0, "expected"); // If fails, shows all records
 *     }
 * }
 * }</pre>
 *
 * @author Daniel Felix Ferber
 * @see HandlerRecordFormatter
 */
@AIGenerated("copilot")
public class MockHandlerDebugExtension implements InvocationInterceptor {

    /**
     * Default constructor for JUnit 5 extension instantiation.
     */
    public MockHandlerDebugExtension() {
        // Default constructor
    }

    /**
     * Intercepts test method execution to catch assertion errors and print logged records.
     *
     * @param invocation        the invocation to proceed with
     * @param invocationContext the context of the invocation
     * @param extensionContext  the extension context
     * @throws Throwable if the test method throws an exception
     */
    @Override
    public void interceptTestMethod(
            final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) throws Throwable {

        try {
            invocation.proceed();
        } catch (final AssertionError e) {
            // Find and print all MockHandler instances in test parameters and fields
            final List<MockHandler> mockHandlers = findMockHandlers(invocationContext, extensionContext);

            if (!mockHandlers.isEmpty()) {
                printLoggedRecords(mockHandlers, extensionContext);
            }

            throw e;
        }
    }

    /**
     * Finds all MockHandler instances in the test method's parameters and test class fields.
     *
     * @param invocationContext the invocation context containing test parameters
     * @param extensionContext  the extension context containing test instance
     * @return a list of MockHandler instances found
     */
    private static List<MockHandler> findMockHandlers(
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) {
        
        final List<MockHandler> mockHandlers = new ArrayList<>(5);

        // First, check method parameters
        final List<Object> arguments = invocationContext.getArguments();
        final Method method = invocationContext.getExecutable();
        final Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length && i < arguments.size(); i++) {
            final Object arg = arguments.get(i);
            if (arg instanceof MockHandler) {
                mockHandlers.add((MockHandler) arg);
            }
        }

        // Then, check test instance fields
        extensionContext.getTestInstance().ifPresent(testInstance -> {
            final Field[] fields = testInstance.getClass().getDeclaredFields();
            for (final Field field : fields) {
                if (MockHandler.class.equals(field.getType())) {
                    try {
                        field.setAccessible(true);
                        final Object value = field.get(testInstance);
                        if (value instanceof MockHandler) {
                            mockHandlers.add((MockHandler) value);
                        }
                    } catch (final IllegalAccessException e) {
                        // Ignore inaccessible fields
                    }
                }
            }
        });

        return mockHandlers;
    }

    /**
     * Prints all logged records from the MockHandler instances to standard error.
     *
     * @param mockHandlers     the list of MockHandler instances to print records from
     * @param extensionContext the extension context for getting test information
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static void printLoggedRecords(final List<MockHandler> mockHandlers, final ExtensionContext extensionContext) {
        final String testName = extensionContext.getDisplayName();

        System.err.println();
        System.err.println("╔════════════════════════════════════════════════════════════╗");
        System.err.println("║           LOGGED RECORDS (Assertion Failed)               ║");
        System.err.println("╠════════════════════════════════════════════════════════════╣");
        System.err.printf("║ Test: %-54s ║%n", testName);
        System.err.println("╚════════════════════════════════════════════════════════════╝");

        for (int i = 0; i < mockHandlers.size(); i++) {
            final MockHandler handler = mockHandlers.get(i);

            if (mockHandlers.size() > 1) {
                System.err.println();
                System.err.printf("Handler #%d:%n", i + 1);
                System.err.println("────────────────────────────────────────────────────────");
            }

            System.err.println(HandlerRecordFormatter.formatLoggedRecords(handler));
        }

        System.err.println();
    }
}
