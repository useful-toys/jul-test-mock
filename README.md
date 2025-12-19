# jul-test-mock

A lightweight in-memory mock implementation of Java Util Logging (JUL) designed specifically for unit testing. This library provides a test-focused Handler implementation that captures log records in memory, enabling clean and powerful assertions in your tests.

## Overview

`jul-test-mock` provides a `MockHandler` that captures log records during test execution, along with comprehensive assertion utilities for validating logged content. Unlike production logging frameworks, this mock is optimized for test scenarios with minimal dependencies and maximum control.

## Key Features

- **JUnit 5 Integration**: `@JulMock` extension for automatic handler setup and cleanup
- **In-Memory Handler**: Captures all log records in a simple ArrayList
- **Zero Configuration**: Works out of the box with standard JUL
- **Test-Focused Design**: Built for test environments, not production
- **Comprehensive Assertions**: Purpose-built assertion methods for all common test scenarios
- **Message Parts Philosophy**: Test log messages by their semantic parts, not exact strings
- **No External Dependencies**: Built on top of standard JUL and JUnit 5
- **Fluent API**: Natural and readable test code
- **Java 8+ Compatible**: Works with Java 8 and higher versions

## Maven Dependency

```xml
<dependency>
    <groupId>org.usefultoys</groupId>
    <artifactId>jul-test-mock</artifactId>
    <version>1.9.0</version>
    <scope>test</scope>
</dependency>
```

## Getting Started

### Recommended: Using JUnit 5 Extension

The easiest way to use jul-test-mock is with the JUnit 5 `@JulMock` extension. It handles handler setup and cleanup automatically:

```java
import org.usefultoys.jul.mock.MockHandler;
import org.usefultoys.jul.mock.MockHandlerExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.logging.Logger;
import java.util.logging.Level;
import static org.usefultoys.jul.mock.AssertLogger.*;

@ExtendWith(MockHandlerExtension.class)
class MyTest {
    
    @JulMock
    MockHandler handler;
    
    @Test
    void shouldLogInfoMessage() {
        Logger logger = Logger.getLogger("test");
        logger.info("Hello World");
        
        assertRecordCount(handler, 1);
        assertRecord(handler, 0, Level.INFO, "Hello World");
    }
}
```

**Key Benefits:**
- Automatic handler registration with the logger
- Automatic handler cleanup after each test
- Clean, minimal test code
- Configurable log level control via annotation

### Manual Handler Management (Alternative)

If you prefer manual control or cannot use JUnit extensions:

```java
import org.usefultoys.jul.mock.MockHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import static org.usefultoys.jul.mock.AssertLogger.*;

class MyTest {
    
    private MockHandler handler;
    private Logger logger;
    
    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("test");
        logger.setUseParentHandlers(false);
        handler = new MockHandler();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }
    
    @AfterEach
    void tearDown() {
        logger.removeHandler(handler);
    }
    
    @Test
    void shouldLogInfoMessage() {
        logger.info("Hello World");
        
        assertRecordCount(handler, 1);
        assertRecord(handler, 0, Level.INFO, "Hello World");
    }
}
```

## AssertLogger API

The `AssertLogger` class provides static assertion methods for validating logged records. All assertions include descriptive error messages to help diagnose test failures.

### Basic Assertions

#### assertRecord(handler, index, level, ...messageParts)

Validates a specific log record by index, level, and message content. Message parts are matched using `contains()` semantics - they don't need to be exact matches.

```java
logger.info("User alice logged in from 192.168.1.100");

// These all pass - testing semantic parts
assertRecord(handler, 0, Level.INFO, "alice", "logged in");
assertRecord(handler, 0, Level.INFO, "192.168.1.100");
assertRecord(handler, 0, Level.INFO, "User", "alice", "192.168");
```

**Why message parts?** Testing for exact string matches makes tests brittle. By checking for semantic parts, your tests remain robust even when log message formatting changes.

#### assertRecordCount(handler, expectedCount)

Validates the total number of log records captured:

```java
logger.info("Starting");
logger.info("Processing");
logger.info("Completed");

assertRecordCount(handler, 3);
```

#### assertNoRecords(handler)

Validates that no records were logged:

```java
// Code that shouldn't log anything
someQuietOperation();

assertNoRecords(handler);
```

### Level-Based Assertions

#### assertRecordCountByLevel(handler, level, expectedCount)

Counts records at a specific level:

```java
logger.info("Application started");
logger.warning("Low disk space");
logger.info("Processing request");
logger.severe("Critical error");

assertRecordCountByLevel(handler, Level.INFO, 2);
assertRecordCountByLevel(handler, Level.WARNING, 1);
assertRecordCountByLevel(handler, Level.SEVERE, 1);
```

### Message-Based Assertions

#### assertHasRecord(handler, level, ...messageParts)

Checks if any record exists with the specified level and message parts, regardless of order:

```java
logger.info("Started processing");
logger.warning("Database connection slow");
logger.info("Completed successfully");

// Order doesn't matter for existence assertions
assertHasRecord(handler, Level.WARNING, "Database", "slow");
assertHasRecord(handler, Level.INFO, "Completed");
```

#### assertRecordCountByMessage(handler, messageSubstring, expectedCount)

Counts records containing a specific substring:

```java
logger.info("Processing user alice");
logger.warning("User bob authentication failed");
logger.info("User charlie logged out");

assertRecordCountByMessage(handler, "user", 2); // Case-sensitive
```

### Exception Assertions

#### assertRecordWithThrowable(handler, index, exceptionClass, ...messageParts)

Validates exception information in a log record:

```java
try {
    throw new SQLException("Connection timeout after 30 seconds");
} catch (SQLException e) {
    logger.log(Level.SEVERE, "Database operation failed", e);
}

assertRecord(handler, 0, Level.SEVERE, "Database", "failed");
assertRecordWithThrowable(handler, 0, SQLException.class, "timeout", "30 seconds");
```

#### assertRecordWithThrowableMessage(handler, index, exceptionClass, ...messageParts)

Validates just the exception's message:

```java
assertRecordWithThrowableMessage(handler, 0, SQLException.class, "Connection", "timeout");
```

### Sequence Assertions

#### assertRecordSequence(handler, ...levels)

Validates the exact sequence of log levels:

```java
logger.info("Process starting");
logger.fine("Step 1 completed");
logger.warning("Warning occurred");
logger.info("Process finished");

assertRecordSequence(handler, Level.INFO, Level.FINE, Level.WARNING, Level.INFO);
```

#### assertRecordSequence(handler, ...messageParts)

Validates the sequence of message content:

```java
assertRecordSequence(handler, "starting", "Step 1", "Warning", "finished");
```

## Common Testing Scenarios

### Testing Message Parts (Recommended Approach)

The message parts philosophy is central to jul-test-mock. Instead of testing exact log messages, test for the semantic components that matter:

```java
@ExtendWith(MockHandlerExtension.class)
class UserServiceTest {
    
    @JulMock
    MockHandler handler;
    
    @Test
    void shouldLogUserLogin() {
        Logger logger = Logger.getLogger("UserService");
        
        // Production code might change formatting
        logger.info(String.format("User %s logged in from IP %s at %s", 
            "alice", "192.168.1.100", "2024-01-15 10:30:00"));
        
        // Test semantic parts, not exact format
        assertRecord(handler, 0, Level.INFO, "alice", "192.168.1.100", "logged in");
        
        // All these would pass even if the format changes
        assertRecord(handler, 0, Level.INFO, "User", "alice");
        assertRecord(handler, 0, Level.INFO, "alice", "IP");
        assertRecord(handler, 0, Level.INFO, "logged in", "192.168");
    }
}
```

### Testing with Parameterized Messages

JUL supports parameterized messages using MessageFormat syntax:

```java
@ExtendWith(MockHandlerExtension.class)
class ParameterizedMessageTest {
    
    @JulMock
    MockHandler handler;
    
    @Test
    void shouldFormatParameterizedMessages() {
        Logger logger = Logger.getLogger("test");
        
        // JUL uses {0}, {1} placeholders
        logger.log(Level.INFO, "User {0} accessed {1} at {2}", 
            new Object[]{"alice", "/admin", "10:30 AM"});
        
        // Verify formatted message
        MockLogRecord record = handler.getRecord(0);
        assertEquals("User alice accessed /admin at 10:30 AM", 
            record.getFormattedMessage());
        
        // Or use message parts
        assertRecord(handler, 0, Level.INFO, "alice", "/admin", "10:30");
    }
}
```

### Testing Multiple Records

```java
@ExtendWith(MockHandlerExtension.class)
class MultipleRecordsTest {
    
    @JulMock
    MockHandler handler;
    
    @Test
    void shouldLogMultipleRecordsAndVerifyEachByIndex() {
        Logger logger = Logger.getLogger("test");
        
        // Act - log multiple records with different levels
        logger.info("Application started successfully");
        logger.fine("Database connection pool initialized with 10 connections");
        logger.warning("Cache miss ratio is high: 67%");
        logger.info("User alice logged in from 192.168.1.100");
        logger.severe("Critical system error detected in payment module");
        
        // Assert - verify total count first
        assertRecordCount(handler, 5);
        
        // Then verify each record individually by index
        assertRecord(handler, 0, Level.INFO, "Application started");
        assertRecord(handler, 1, Level.FINE, "Database", "10 connections");
        assertRecord(handler, 2, Level.WARNING, "Cache", "67%");
        assertRecord(handler, 3, Level.INFO, "alice", "192.168.1.100");
        assertRecord(handler, 4, Level.SEVERE, "Critical", "payment");
    }
}
```

### Controlling Log Levels

```java
@ExtendWith(MockHandlerExtension.class)
class LogLevelTest {
    
    @JulMock(fineEnabled = false)
    MockHandler handler;
    
    @Test
    void shouldNotCaptureFineMessages() {
        Logger logger = Logger.getLogger("test");
        
        logger.fine("This won't be captured");
        logger.info("This will be captured");
        
        assertRecordCount(handler, 1);
        assertRecord(handler, 0, Level.INFO, "This will be captured");
    }
}
```

### Verifying Record Sequences

```java
@ExtendWith(MockHandlerExtension.class)
class WorkflowTest {
    
    @JulMock
    MockHandler handler;
    
    @Test
    void shouldLogWorkflowSteps() {
        Logger logger = Logger.getLogger("test");
        
        logger.info("Process starting");
        logger.fine("Step 1 completed");
        logger.warning("Warning occurred");
        logger.info("Process finished");
        
        // Verify exact sequence of levels
        assertRecordSequence(handler, Level.INFO, Level.FINE, Level.WARNING, Level.INFO);
        
        // Verify sequence of message parts
        assertRecordSequence(handler, "starting", "Step 1", "Warning", "finished");
    }
}
```

### Counting Records

```java
@ExtendWith(MockHandlerExtension.class)
class RecordCountTest {
    
    @JulMock
    MockHandler handler;
    
    @Test
    void shouldCountRecords() {
        Logger logger = Logger.getLogger("test");
        
        logger.info("Application started");
        logger.warning("Authentication failed");
        logger.info("Processing request");
        logger.severe("Critical error occurred");
        
        // Count total records
        assertRecordCount(handler, 4);
        
        // Count by level
        assertRecordCountByLevel(handler, Level.INFO, 2);
        assertRecordCountByLevel(handler, Level.SEVERE, 1);
        
        // Count by message content
        assertRecordCountByMessage(handler, "error", 1);
    }
}
```

### Using Existence-Based Assertions

When record order doesn't matter, use existence-based assertions:

```java
@ExtendWith(MockHandlerExtension.class)
class ExistenceAssertionTest {
    
    @JulMock
    MockHandler handler;
    
    @Test
    void shouldContainExpectedRecords() {
        Logger logger = Logger.getLogger("test");
        
        logger.info("User alice logged in from 127.0.0.1");
        logger.warning("Invalid password attempt");
        logger.severe("Database connection failed");
        
        // Check if any record contains specific text parts (order doesn't matter)
        assertHasRecord(handler, Level.INFO, "alice", "127.0.0.1");
        assertHasRecord(handler, Level.SEVERE, "Database");
        assertHasRecord(handler, Level.WARNING, "password");
    }
}
```

### Testing Exception Logging

```java
@ExtendWith(MockHandlerExtension.class)
class ExceptionLoggingTest {
    
    @JulMock
    MockHandler handler;
    
    @Test
    void shouldLogExceptionDetails() {
        Logger logger = Logger.getLogger("test");
        
        try {
            throw new SQLException("Connection timeout after 30 seconds");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database operation failed", e);
        }
        
        // Test both the message and the exception
        assertRecord(handler, 0, Level.SEVERE, "Database", "failed");
        assertRecordWithThrowable(handler, 0, SQLException.class, "timeout", "30 seconds");
    }
}
```

## Best Practices

### 1. Use JUnit 5 Extension for Convenience

```java
// Recommended - automatic handler management
@ExtendWith(MockHandlerExtension.class)
class MyTest {
    @JulMock MockHandler handler;
    // No setup or cleanup needed!
}
```

### 2. Choose the Right Assertion Type

**Use index-based assertions** when order matters:
```java
assertRecord(handler, 0, Level.INFO, "Starting");
assertRecord(handler, 1, Level.INFO, "Completed");
```

**Use existence-based assertions** when order doesn't matter:
```java
assertHasRecord(handler, Level.SEVERE, "Database", "failed");
```

**Use counting assertions** for volume verification:
```java
assertRecordCount(handler, 5);
assertRecordCountByLevel(handler, Level.SEVERE, 0); // No errors expected
```

**Use sequence assertions** for workflow validation:
```java
assertRecordSequence(handler, Level.INFO, Level.FINE, Level.WARNING, Level.INFO);
```

### 3. Test Message Parts, Not Exact Strings

```java
// Good - tests semantic content
assertRecord(handler, 0, Level.INFO, "User", "alice", "logged in");

// Brittle - breaks when format changes
assertRecord(handler, 0, Level.INFO, "User alice logged in at 2024-01-15 10:30:00 from 192.168.1.100");
```

### 4. Test Both Messages and Exceptions

```java
logger.log(Level.SEVERE, "Operation failed", new SQLException("Connection timeout"));

assertRecord(handler, 0, Level.SEVERE, "Operation failed");
assertRecordWithThrowable(handler, 0, SQLException.class, "Connection", "timeout");
```

### 5. Disable Parent Handlers in Tests

```java
@BeforeEach
void setUp() {
    logger = Logger.getLogger("test");
    logger.setUseParentHandlers(false); // Prevent console output during tests
    logger.addHandler(handler);
}
```

### 6. Validate Record Counts for Performance

```java
@Test
void shouldLogMinimally() {
    // Ensure production code doesn't log excessively
    performOperation();
    
    assertRecordCountByLevel(handler, Level.FINE, 0); // No debug in production
    assertRecordCountByLevel(handler, Level.INFO, 1);  // Single info message expected
}
```

## Differences from SLF4J Test Mock

While `jul-test-mock` is inspired by `slf4j-test-mock`, there are some key differences due to the nature of JUL:

1. **Handler-based instead of Logger-based**: JUL uses Handlers to process log records, so you attach a MockHandler to your logger
2. **No Marker support**: JUL doesn't have a Marker concept like SLF4J
3. **No MDC support**: JUL doesn't have built-in MDC (though you can use ThreadContext or similar patterns)
4. **Different log levels**: JUL uses FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE instead of TRACE, DEBUG, INFO, WARN, ERROR
5. **Parameterized messages use MessageFormat**: JUL uses `{0}`, `{1}` style instead of `{}` placeholders

## Thread Safety

This mock implementation is designed for single-threaded test environments. For parallel test execution, use unique logger names per test class or method.

## Requirements

- Java 8 or higher
- JUnit 5 (for JUnit extension and assertion utilities)
- Lombok (build-time dependency for code generation)

## Documentation

For more detailed information about the implementation and advanced topics:

- **[TDR-0001: In-Memory Event Storage](doc/TDR-0001-in-memory-event-storage.md)** - Why ArrayList for record storage
- **[TDR-0002: Use of JUnit5 Assertions](doc/TDR-0002-use-of-junit5-assertions.md)** - Why JUnit Jupiter assertions
- **[TDR-0003: Focus on JUL Handler Interface](doc/TDR-0003-focus-on-jul-handler-interface.md)** - Design philosophy
- **[TDR-0004: JUnit Extension for Debug Logging](doc/TDR-0004-junit-extension-for-debug-logging.md)** - Debug output on test failures

## License

Licensed under the Apache License, Version 2.0. See the [LICENSE](../LICENSE) file for details.

## Contributing

This library follows the project's [GitHub Copilot Instructions](../.github/copilot-instructions.md) for code style and development practices.
