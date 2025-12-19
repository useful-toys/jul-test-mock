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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Level;

/**
 * Annotation to inject a {@link MockHandler} into a test field or parameter.
 * <p>
 * When applied to a field, the field must be of type {@link MockHandler}.
 * The {@link MockHandler} instance will be automatically created and
 * configured based on the annotation's attributes.
 * <p>
 * Example usage:
 * <pre>{@code
 * @ExtendWith(MockHandlerExtension.class)
 * public class MyTest {
 *     @JulMock
 *     MockHandler handler;
 *
 *     @JulMock(level = Level.WARNING)
 *     MockHandler warnHandler;
 *
 *     @Test
 *     void testSomething(@JulMock MockHandler methodHandler) {
 *         // Use methodHandler in test
 *     }
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface JulMock {

    /**
     * The logging level to set on the handler.
     * Defaults to {@link Level#ALL}, which captures all log levels.
     *
     * @return The logging level for the handler.
     */
    String level() default "ALL";

    /**
     * Whether the handler is enabled.
     * Defaults to true.
     *
     * @return true if the handler should be enabled, false otherwise.
     */
    boolean enabled() default true;
}
