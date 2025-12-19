# TDR-0003: Focus on the JUL Handler Interface

**Status**: Accepted

**Date**: 2025-12-18

## Context

Java Util Logging (JUL) is the standard logging framework built into the Java Development Kit. When developing a testing tool for JUL logs, a decision had to be made whether the mock should simulate a specific logging backend or implement the JUL Handler interface directly.

The JUL Handler interface is the extension point for custom logging handlers in the standard Java logging framework, making it the most flexible and compatible approach for a testing tool.

## Decision

We decided to design and implement `jul-test-mock` as a **direct implementation of the JUL Handler interface**.

Instead of simulating the internal behavior of specific JUL handlers or other logging libraries, the project implements `java.util.logging.Handler`, positioning itself as one of the possible custom handlers that developers can attach to JUL loggers at runtime. This allows seamless integration with any code using JUL without requiring additional adapters or facades.

## Consequences

### Positive

*   **Maximum Compatibility**: By implementing the JUL Handler interface, `jul-test-mock` can be used in any project that uses JUL, regardless of the JUL configuration. To use the mock, one simply needs to attach the `MockHandler` to the desired logger using `logger.addHandler(mockHandler)`.
*   **Abstraction and Simplicity**: The library only deals with the JUL Handler contract (`LogRecord`, `Level`, `Handler`), which is a simple and stable API. This reduces the development and maintenance complexity of the mock.
*   **Focus on the Contract, Not the Implementation**: Testing what is logged via JUL means that the tests are verifying the application's logging behavior through the standard API, not implementation details. This makes tests robust against changes in JUL configuration or handler implementations.
*   **Zero Framework Integration Required**: No need to replace logging implementations or use service providers. Simply add the handler to a logger.

### Negative

*   **Limited to JUL API**: `jul-test-mock` cannot be used to test features specific to other logging libraries like Logback appender configurations or Log4j2 lookups. The tool's scope is deliberately limited to what is possible through the JUL API.
*   **Requires Manual Handler Setup**: Unlike some logging facades, developers must explicitly add and configure the `MockHandler` on each logger they want to monitor (though this is straightforward with `@BeforeEach` setup).

## Alternatives Considered

### 1. Simulate a Specific JUL Handler Implementation

*   **Description**: Create a mock that simulates the behavior of specific JUL handlers like `FileHandler` or `ConsoleHandler`.
*   **Rejected because**: This would limit the scope to particular implementations. The JUL Handler interface is the right abstraction level, allowing the mock to work with any JUL-based logging setup without coupling to specific implementations.

### 2. Bridge to a Different Logging Framework

*   **Description**: Implement a bridge that converts JUL logs to SLF4J or another logging facade for testing.
*   **Rejected because**: This adds complexity and extra dependencies. Applications using JUL should be tested with JUL itself, not through a different framework. Bridges can introduce subtle behavioral differences.

### 3. Wrapper Around JUL's Built-in MemoryHandler

*   **Description**: Extend or wrap JUL's `MemoryHandler` to provide testing capabilities.
*   **Rejected because**: `MemoryHandler` is designed as a buffering handler for production use, not testing. It has different semantics and less control over captured records. Building from the Handler interface gives more appropriate semantics for testing.

## References

- [Java Util Logging Documentation](https://docs.oracle.com/javase/8/docs/api/java/util/logging/Handler.html)
- JUL Handler Interface API
