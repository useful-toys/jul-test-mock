# TDR-0002: Use JUnit Jupiter for Tests and Assertions

**Status**: Accepted

**Date**: 2025-12-18

## Context

To validate the correctness of `jul-test-mock`, a robust testing framework is necessary. Furthermore, one of the core features of this library is to provide assertion utilities (`AssertHandler`) that integrate with the test lifecycle of the consuming project. The library, therefore, needs a foundation for its own tests and for the features it exposes to its users. Common options in the Java ecosystem are JUnit 4, JUnit 5 (Jupiter), and TestNG.

## Decision

We decided to use **JUnit Jupiter (JUnit 5)** as the fundamental dependency for the library's internal tests and assertion features.

The dependency was added with `compile` scope in the `pom.xml`, which means that `junit-jupiter-api` is not only used for the internal tests of `jul-test-mock` but also becomes a transitive dependency for projects that use this library.

## Consequences

### Positive

*   **Modern and Extensible API**: JUnit 5 offers a more modern API with clear annotations (`@Test`, `@BeforeEach`, `@DisplayName`) and a powerful extension model.
*   **Market Adoption**: JUnit 5 is the de facto standard for new Java projects, ensuring greater compatibility and familiarity.
*   **Integration of Assertions**: `AssertHandler` can directly use JUnit's assertion exceptions (like `AssertionFailedError`), integrating natively with test reports.
*   **No Custom Exceptions**: Reuses standard JUnit exceptions, simplifying code and maintenance.

### Negative

*   **Transitive Dependency**: Any project using `jul-test-mock` will have a transitive dependency on `junit-jupiter-api`, which can rarely cause conflicts with incompatible JUnit 5 versions.
*   **Coupling with Test Framework**: The library becomes coupled to JUnit 5; assertion features may not work with other testing frameworks like TestNG without adaptation.

## Alternatives Considered

### 1. JUnit 4

*   **Description**: Use the traditional JUnit 4 framework for tests and assertions.
*   **Rejected because**: JUnit 4 has been superseded by JUnit 5, is less extensible, and lacks modern features like `@DisplayName` and parameterized tests. New projects should adopt JUnit 5 to leverage its advantages.

### 2. TestNG

*   **Description**: Use TestNG as the testing framework for assertions and internal tests.
*   **Rejected because**: While TestNG is capable, JUnit 5 is the industry standard for new Java projects and has better ecosystem integration. Choosing JUnit 5 aligns with the majority of modern Java projects.

### 3. No Test Framework (Custom Assertions)

*   **Description**: Create custom assertion utilities without depending on any testing framework.
*   **Rejected because**: This would force users to either use different assertion libraries or create their own, fragmenting the ecosystem. JUnit 5 provides a standard, well-understood exception hierarchy that test runners recognize and display appropriately.
