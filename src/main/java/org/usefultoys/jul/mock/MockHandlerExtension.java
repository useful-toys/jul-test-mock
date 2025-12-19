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

import org.junit.jupiter.api.extension.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.logging.Level;

/**
 * JUnit Jupiter extension to integrate {@link MockHandler} instances into tests.
 * <p>
 * This extension provides automatic injection and configuration of {@link MockHandler}
 * instances into test class fields and method parameters annotated with {@link JulMock}.
 * It ensures that {@link MockHandler} instances are properly initialized and reset
 * before each test.
 * </p>
 */
public class MockHandlerExtension implements
        TestInstancePostProcessor,
        BeforeEachCallback,
        ParameterResolver {

    /**
     * Default constructor for JUnit 5 extension instantiation.
     */
    public MockHandlerExtension() {
        // Default constructor
    }

    /**
     * Initializes {@link MockHandler} fields in the test instance.
     * <p>
     * This method is called once per test instance. It scans for fields of type
     * {@link MockHandler} and, if annotated with {@link JulMock},
     * creates and configures a {@link MockHandler} instance, then injects it into the field.
     * </p>
     *
     * @param testInstance The test instance.
     * @param context      The extension context.
     */
    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) {
        final Class<?> testClass = testInstance.getClass();
        final Field[] fields = testClass.getDeclaredFields();
        for (final Field field : fields) {
            if (isHandlerField(field)) {
                final MockHandler handler = createAndConfigureHandler(field);
                setField(field, testInstance, handler);
            }
        }
    }

    /**
     * Resets and reconfigures injected {@link MockHandler} instances before each test method.
     * <p>
     * This ensures that each test starts with a clean {@link MockHandler} state, preventing
     * test interference.
     * </p>
     *
     * @param context The extension context.
     * @throws IllegalAccessException If the field is inaccessible.
     */
    @Override
    public void beforeEach(final ExtensionContext context) throws IllegalAccessException {
        final Object testInstance = context.getRequiredTestInstance();
        final Class<?> testClass = testInstance.getClass();
        final Field[] fields = testClass.getDeclaredFields();
        for (final Field field : fields) {
            if (isHandlerField(field)) {
                field.setAccessible(true);
                final Object value = field.get(testInstance);
                if (value instanceof MockHandler) {
                    final MockHandler handler = (MockHandler) value;
                    reconfigureHandler(field, handler);
                }
            }
        }
    }

    /**
     * Determines if a parameter can be resolved by this extension.
     * <p>
     * This extension supports parameters of type {@link MockHandler}.
     * </p>
     *
     * @param parameterContext The parameter context.
     * @param extensionContext The extension context.
     * @return {@code true} if the parameter is of type {@link MockHandler}, {@code false} otherwise.
     */
    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final Class<?> type = parameterContext.getParameter().getType();
        return MockHandler.class.equals(type);
    }

    /**
     * Resolves a parameter of type {@link MockHandler}.
     * <p>
     * A new {@link MockHandler} instance is created and configured based on the
     * {@link JulMock} annotation on the parameter, then returned.
     * </p>
     *
     * @param parameterContext The parameter context.
     * @param extensionContext The extension context.
     * @return A configured {@link MockHandler} instance.
     * @throws ParameterResolutionException If the parameter cannot be resolved.
     */
    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final Parameter parameter = parameterContext.getParameter();
        return createAndConfigureHandler(parameter);
    }

    /**
     * Checks if a given field is of type {@link MockHandler}.
     *
     * @param field The field to check.
     * @return {@code true} if the field is a MockHandler type, {@code false} otherwise.
     */
    private static boolean isHandlerField(final Field field) {
        return MockHandler.class.equals(field.getType());
    }

    /**
     * Creates and configures a {@link MockHandler} based on the provided annotated element.
     *
     * @param element The annotated element (field or parameter) that may have {@link JulMock} annotation.
     * @return A configured {@link MockHandler} instance.
     * @throws ExtensionConfigurationException If the handler cannot be created.
     */
    private MockHandler createAndConfigureHandler(final AnnotatedElement element) {
        final MockHandler handler = new MockHandler();
        applyConfig(handler, element);
        return handler;
    }

    /**
     * Reconfigures an existing {@link MockHandler} with configuration from the annotated element.
     *
     * @param element The annotated element (field or parameter) that may have {@link JulMock} annotation.
     * @param handler The existing {@link MockHandler} instance to reconfigure.
     * @throws ExtensionConfigurationException If the handler cannot be configured.
     */
    private void reconfigureHandler(final AnnotatedElement element, final MockHandler handler) {
        applyConfig(handler, element);
    }

    /**
     * Applies the configuration from the {@link JulMock} annotation to the {@link MockHandler}.
     * <p>
     * This includes clearing records and setting the level.
     * </p>
     *
     * @param handler The {@link MockHandler} instance to configure.
     * @param element The annotated element providing the configuration.
     */
    private void applyConfig(final MockHandler handler, final AnnotatedElement element) {
        // Always clear records before each test
        handler.clearRecords();

        final JulMock cfg = element.getAnnotation(JulMock.class);

        if (cfg == null) {
            handler.setLevel(Level.ALL);
            return;
        }

        try {
            final Level level = Level.parse(cfg.level());
            handler.setLevel(level);
        } catch (final IllegalArgumentException e) {
            throw new ExtensionConfigurationException("Invalid log level: " + cfg.level(), e);
        }
    }

    /**
     * Sets the value of a {@link MockHandler} field in the test instance.
     *
     * @param field    The field to set.
     * @param instance The test instance.
     * @param handler  The {@link MockHandler} instance to set.
     * @throws ExtensionConfigurationException If the field cannot be set.
     */
    private static void setField(final Field field, final Object instance, final MockHandler handler) {
        try {
            field.setAccessible(true);
            field.set(instance, handler);
        } catch (final IllegalAccessException e) {
            throw new ExtensionConfigurationException("Could not set handler field: " + field, e);
        }
    }
}
