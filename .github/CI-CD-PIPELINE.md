# CI/CD Pipeline Configuration for Multi-Version SLF4J Support

## Overview

This document describes the CI/CD pipeline configuration to ensure compatibility with both SLF4J 1.7.x and 2.0.x versions.

## Workflows Modified

### 1. Maven Build, Test and Analyze (`maven-build-test.yml`)

**Purpose**: Validates code changes on PRs and feature branch pushes

**Key Changes**:
- ✅ Added matrix strategy to test with both `slf4j-1.7` and `slf4j-2.0` profiles
- ✅ Tests run in parallel for faster feedback
- ✅ Coverage reports uploaded only from SLF4J 1.7 build (to avoid duplication)
- ✅ `fail-fast: false` ensures both versions are tested even if one fails

**Configuration**:
```yaml
strategy:
  matrix:
    slf4j-profile: [slf4j-1.7, slf4j-2.0]
  fail-fast: false
```

**Benefits**:
- Early detection of compatibility issues
- Faster CI pipeline (parallel execution)
- Single source of truth for coverage metrics (SLF4J 1.7 only)

### 2. Release and Deploy Version (`release-deploy-version.yml`)

**Purpose**: Creates releases and deploys to Maven Central

**Key Changes**:
- ✅ Added `test-compatibility` job that runs before deployment
- ✅ Tests both SLF4J versions with `fail-fast: true` (abort on first failure)
- ✅ Deployment only proceeds if all tests pass
- ✅ Artifact built with SLF4J 1.7 profile (default, compatible with both versions)

**Configuration**:
```yaml
jobs:
  test-compatibility:
    strategy:
      matrix:
        slf4j-profile: [slf4j-1.7, slf4j-2.0]
      fail-fast: true

  build-release-deploy:
    needs: test-compatibility
    # ... deployment steps
```

**Benefits**:
- Guarantees that released artifacts work with both SLF4J versions
- Prevents broken releases
- Fast failure on compatibility issues

## Static Analysis (SARIF)

**Purpose**: Show code quality findings and test coverage in the pull request without gating the build.

The `static-analysis` job in `maven-build-test.yml` runs `mvn clean verify -P static-analysis`. The profile runs
Checkstyle (`checkstyle.xml`), PMD (`pmd-ruleset.xml`) and SpotBugs with Find Security Bugs. Each tool writes a SARIF
report to `target/`, and no tool fails the build. The job is not part of `Status Gateway`.

| Tool | Report | Code Scanning category |
|---|---|---|
| Checkstyle | `target/checkstyle-result.sarif` | `checkstyle` |
| PMD | `target/pmd.sarif.json` | `pmd` |
| SpotBugs + Find Security Bugs | `target/spotbugsSarif.json` | `spotbugs` |

`.github/scripts/sarif_report.py` prepares and summarizes the reports:
- `normalize` rewrites file URIs to repository-relative paths, which Code Scanning needs to place alerts.
- `markdown` prints finding counts per tool and level, plus the top rules.
- `coverage` prints JaCoCo coverage (total and per package) from `target/site/jacoco/jacoco.csv`.

**Where the results appear**:
- **Job summary**: findings and coverage tables on the workflow run page.
- **Pull request comment**: the same tables in a single comment, updated on each run (skipped for forks).
- **Code Scanning**: alerts in the Security tab and inline annotations on changed lines of the pull request.
- **Artifact** `sarif-reports`: the SARIF files, kept for 7 days.

Pull requests from forks have a read-only token, so the comment and the Code Scanning upload are skipped for them.

**Run locally**: `mvn clean verify -P static-analysis`. Suggestions for next steps are in
[STATIC-ANALYSIS-IMPROVEMENTS.md](../STATIC-ANALYSIS-IMPROVEMENTS.md).

### CodeQL

CodeQL runs through GitHub's default setup (Settings → Code security), not through a workflow file. The `Main Protection`
ruleset requires results from the tool named `CodeQL`, so a pull request waits for them. If a pull request shows
"Waiting for Code Scanning results", check that the default setup is still enabled.

## Pipeline Flow

### Standard Build (PR/Push)

```
Trigger: PR or Push to feature branch
   ↓
Matrix Build (parallel):
   ├─ Test with SLF4J 1.7 → Upload Coverage
   └─ Test with SLF4J 2.0
   ↓
Result: ✅ Pass / ❌ Fail
```

### Release Build (Tag)

```
Trigger: Push tag v*.*.*
   ↓
Test Compatibility (parallel):
   ├─ Test with SLF4J 1.7
   └─ Test with SLF4J 2.0
   ↓
Both Pass? ───No──> ❌ Abort Release
   │
  Yes
   ↓
Build & Deploy (SLF4J 1.7 profile)
   ↓
Create GitHub Release
   ↓
Upload Release Assets
```

## Test Coverage Strategy

**Why only upload coverage from SLF4J 1.7?**

1. **Avoid Duplication**: The codebase is identical; uploading coverage twice would skew metrics
2. **Consistency**: Single source of truth for coverage reports
3. **Efficiency**: Reduces CI time and external service calls
4. **Clarity**: Simpler to track coverage trends over time

**Coverage still validates both versions:**
- All 318 tests run against both SLF4J 1.7 and 2.0
- Any test failure in either version will fail the build
- Coverage report from 1.7 is representative of the entire codebase

## Local Testing

Developers can test both versions locally before pushing:

```bash
# Test with SLF4J 1.7
mvn clean verify -P slf4j-1.7

# Test with SLF4J 2.0
mvn clean verify -P slf4j-2.0
```

## Monitoring and Alerts

- **GitHub Actions**: Check status on PR and release pages
- **Coverage Reports**: Codecov, Coveralls, Codacy (SLF4J 1.7 baseline)
- **Release Notes**: Auto-generated with each version tag

## Troubleshooting

### Build fails only on SLF4J 2.0

**Symptoms**: SLF4J 1.7 tests pass, but 2.0 tests fail

**Causes**:
- Missing implementation for SLF4J 2.0 methods
- Incorrect service provider registration
- API differences between versions

**Solution**:
1. Check `MockServiceProvider.java` is compiled (not excluded)
2. Verify `META-INF/services/org.slf4j.spi.SLF4JServiceProvider` exists
3. Review SLF4J 2.0 API changes

### Coverage not uploading

**Symptoms**: Coverage badges show "unknown" or old data

**Causes**:
- SLF4J 1.7 build failed
- Secrets not configured
- Coverage tool API issues

**Solution**:
1. Verify SLF4J 1.7 build passes
2. Check secrets: `CODECOV_TOKEN`, `CODACY_PROJECT_TOKEN`
3. Review coverage tool logs in Actions

### Release blocked

**Symptoms**: Release workflow stuck at test-compatibility

**Causes**:
- One or both SLF4J versions failing tests
- Test environment issues

**Solution**:
1. Review test logs for both matrix jobs
2. Run tests locally with both profiles
3. Fix failing tests before re-releasing

## Future Enhancements

- **Multi-JDK Testing**: Test with Java 8, 11, 17, 21
- **Dependency Matrix**: Test with different JUnit 5 versions
- **Performance Benchmarks**: Compare performance across SLF4J versions
- **Nightly Builds**: Test with SLF4J snapshot versions

## References

- [TDR-0005: Multiple SLF4J Version Support](../TDR-0005-multiple-slf4j-version-support.md)
- [BUILD-PROFILES.md](../BUILD-PROFILES.md)
- [GitHub Actions Matrix Strategy](https://docs.github.com/en/actions/using-jobs/using-a-matrix-for-your-jobs)

