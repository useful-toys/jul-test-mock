# TDR-0004: JUnit 5 Extension for Debug Logging on Test Failures

**Status**: Proposed (Future Implementation)

**Date**: 2025-12-18

## Context

When unit tests using `AssertHandler` fail, developers cannot see what records were actually logged, making debugging time-consuming. The current API only shows what was expected, not what was actually logged.

**Problem**: When assertions fail, developers must manually add `System.out.println()` statements, use the debugger, or inspect `MockHandler.getLogRecords()` to understand what went wrong.

**Example**:
```java
@Test
void testLogging() {
    logger.info("Starting process");
    logger.warning("Problem detected");
    logger.severe("Process failed");
    
    AssertHandler.assertRecord(handler, 0, "Expected message");
    // Fails with: "Expected record not found at index 0"
    // But which records WERE logged?
}
```

## Decision

We will implement a **JUnit 5 Extension** (`AssertHandlerDebugExtension`) that automatically prints all captured log records to `System.err` when any test assertion fails.

### How It Works

1. **Extension Class**: Implements `InvocationInterceptor` from JUnit 5
2. **Record Formatter**: Utility class for human-readable output of log records
3. **Usage**: Add `@ExtendWith(AssertHandlerDebugExtension.class)` to test classes

```java
@ExtendWith(AssertHandlerDebugExtension.class)
class MyTest {
    @Test
    void test() {
        Logger logger = Logger.getLogger("test");
        MockHandler handler = new MockHandler();
        logger.addHandler(handler);
        
        // Test code...
        // On assertion failure, all captured records are printed to stderr
    }
}
```

When a test fails, output shows:
```
Captured log records:
  Total records: 3
  [0] INFO   | message="Starting process"
  [1] WARN   | message="Problem detected"
  [2] SEVERE | message="Process failed"
```

## Consequences

### Positive

*   **Zero Code Changes to AssertHandler**: Existing API remains unchanged and fully backwards compatible.
*   **Automatic Behavior**: Works automatically once applied via `@ExtendWith`; impossible to forget to use.
*   **Clean Separation of Concerns**: Debug functionality is separate from assertion logic; can be enabled/disabled per test.
*   **JUnit 5 Standard Pattern**: Uses established JUnit 5 extension mechanism, well-documented and understood.
*   **Improved Developer Experience**: Faster debugging of test failures with immediate visibility into what was logged.
*   **Zero Overhead on Success**: No performance impact when tests pass; only formats records when assertion fails.

### Negative

*   **Additional Test Output**: Failed tests produce more output, which can be verbose in build logs.
*   **Learning Curve**: Developers need to understand JUnit 5 extensions and `@ExtendWith` annotation.
*   **Not Automatic for All Tests**: Requires explicit annotation; must be added to each test class or base class.
*   **Limited Auto-Discovery**: Current implementation would detect handlers passed as test method parameters; doesn't automatically find handler fields.

## Alternatives Considered

### 1. Modify AssertHandler Methods

*   **Description**: Wrap every assertion method with try-catch to print records on failure.
*   **Rejected because**: Would require modifying multiple methods with code repetition, making it intrusive and difficult to maintain.

### 2. Utility Wrapper Class

*   **Description**: Users explicitly call a utility around assertions: `AssertHandlerDebug.withDebug(handler, () -> {...})`
*   **Rejected because**: Requires manual wrapping in every test, verbose, easy to forget, defeats the purpose of being automatic.

### 3. Custom AssertionError Subclass

*   **Description**: Throw custom exception that includes record list.
*   **Rejected because**: Still requires modifying assertion methods and changes exception types, which could be a breaking change.

### 4. TestWatcher Extension

*   **Description**: Use `TestWatcher` to detect failures after the test completes.
*   **Rejected because**: `TestWatcher` runs after the test, cannot easily access handler instances, and would require test-level registration.

## Implementation

- **Extension Class**: `org.usefultoys.jul.mock.AssertHandlerDebugExtension` (implements `InvocationInterceptor`)
- **Formatter Class**: `org.usefultoys.jul.mock.LogRecordFormatter` (utility for formatting records)
- **Tests**: `AssertHandlerDebugExtensionTest.java` with examples for single/multiple handlers, parameters, and exceptions
- **No new dependencies**: Uses only existing JUnit 5 dependencies

## References

- [JUnit 5 Extensions Documentation](https://junit.org/junit5/docs/current/user-guide/#extensions)
- [InvocationInterceptor API](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/InvocationInterceptor.html)
- TDR-0002: Use JUnit Jupiter for Tests and Assertions
