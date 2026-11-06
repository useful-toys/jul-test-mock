# Static Analysis: Improvement Suggestions

Working notes for the `static-analysis` Maven profile and its SARIF reports. This is a proposal
list, not a commitment: each item can be adopted, changed or dropped independently.

## Current state

- Profile: `static-analysis` in `pom.xml` (`mvn clean verify -P static-analysis`).
- Analyzers: Checkstyle (rules in `checkstyle.xml`, mirroring the IntelliJ code style:
  120 columns, 4-space indent, 8-space continuation, no star imports), PMD (`pmd-ruleset.xml`) and
  SpotBugs with Find Security Bugs, plus `dependency:analyze-only`. Error Prone runs in every build,
  outside the profile.
- Output: `target/checkstyle-result.sarif`, `target/pmd.sarif.json`, `target/spotbugsSarif.json`.
- CI: job `static-analysis` in `.github/workflows/maven-build-test.yml` runs after the build job on
  its compiled classes, normalizes the SARIF paths (`.github/scripts/sarif_report.py`), writes a job
  summary, uploads to Code Scanning, comments on the pull request and stores the SARIF as an
  artifact. It is informative and does not gate the pipeline. See `.github/CI-CD-PIPELINE.md`.
- First run of the custom Checkstyle rules: 144 findings (111 `maxLineLen`, 23 trailing spaces,
  6 `import.avoidStar`, 4 `indentation.error`). The stock `sun_checks.xml` produced 700.

## Adoption order

1. Validate the report channels (summary, PR comment, inline annotations) with Checkstyle.
2. Add PMD, then SpotBugs with Find Security Bugs, one analyzer per change.
3. Show test coverage (JaCoCo) in the same job summary and PR comment.

## Checkstyle

- Decide whether Javadoc on the public API is required (`MissingJavadocMethod`,
  `MissingJavadocType`, public scope only). The stock rules reported 29 missing Javadocs. This is
  a library for tests used by others, so public API documentation is probably worth enforcing.
- Review the 111 long lines: wrap them or raise the limit. Comments wrap automatically in the IDE
  (`WRAP_COMMENTS`), so most should be code or Javadoc.
- Consider adding, one at a time: `EmptyBlock`, `EmptyStatement`, `MissingSwitchDefault`,
  `SimplifyBooleanExpression`, `FinalClass`, `HideUtilityClassConstructor`.
- Decide whether test sources need a relaxed rule set. Today they are analyzed with the same rules
  (`includeTestSourceDirectory`).
- Optional: export the project's IntelliJ style as `.editorconfig` so the IDE, CI and other editors
  agree on indent and line length.

## PMD

- Start from a small ruleset (`bestpractices`, `errorprone`, `security`) instead of the default
  quickstart, to keep noise low.
- Exclude or relax rules that clash with test code (for example `JUnitTestContainsTooManyAsserts`,
  `JUnitAssertionsShouldIncludeMessage`) and with Lombok-generated code.
- Confirm that the plugin version in use writes SARIF and where it places the file.

## SpotBugs and Find Security Bugs

- Add `lombok.config` with `lombok.addLombokGeneratedAnnotation = true` so generated code is
  skipped instead of producing false positives.
- Start with `threshold=Medium`, then lower it once the baseline is clean.
- Find Security Bugs may report little on this project (a logging mock with no I/O or web code);
  keep it anyway, since the cost is one dependency.
- Use an exclude filter file for justified false positives, with a comment on each entry.

## Coverage

- JaCoCo already produces `target/site/jacoco/jacoco.xml`; it does not write SARIF. Show line, branch
  and method coverage (total and per package) in the job summary and in the same PR comment.
- Consider `jacoco:check` with a minimum threshold, so local builds also fail on a coverage drop.
  Today the 90% target exists only in `codecov.yml`.
- Surefire test results could also go in the job summary (counts, failures), which reduces the
  need to open external services.
- Coverage is measured only on the JDK 21 build; the other JDK jobs only run tests.

## CI and reporting

- Keep the static analysis job informative until the baseline is clean, then decide on a gate
  (for example, fail on new alerts only, using Code Scanning's PR check).
- Run one SARIF upload per tool with a distinct `category` so alerts do not overwrite each other.
- Pull requests from forks have a read-only token: upload and comment are skipped, the job summary
  still works.
- Document the profile and the reports in `.github/CI-CD-PIPELINE.md` once the tools are stable.
- Pin third-party actions by version (already done for the new ones) and review them periodically.

## Overlap with existing tools

The repository also uses Qodana, Codecov, Coveralls and Codacy, and probably CodeQL through GitHub's
default setup. Nothing is being removed. After the new reports prove useful, compare findings and
decide whether any tool is redundant:

- Qodana (Community, starter profile) overlaps with PMD and Checkstyle.
- Coveralls and Codacy receive the same `jacoco.xml` as Codecov.
- Codecov provides history and patch coverage that the in-job report does not.
- CodeQL complements SpotBugs; keep unless proven redundant.

## Other candidates

Adopted:

- Error Prone as a compiler plugin: runs on every build and reports bug patterns as compiler warnings
  (no SARIF; GitHub shows them as annotations). Its first run found a reference comparison of `Level`
  in `AssertHelper.isLevel` and an implicit default charset in `MockHandler`, both fixed. NullAway
  remains an option if nullness annotations are wanted.
- `maven-dependency-plugin:analyze-only` in the static analysis job, reported in the job summary.
  No problems found: `junit-jupiter-api` with `compile` scope is right, since the main code uses it.

Dropped:

- OWASP Dependency-Check: Dependabot alerts already cover dependency CVEs without build cost. With
  rare builds, the NVD database cache expires between work periods and each period would start with
  a full download (about 11 minutes, NVD API key required).

Still open:

- PIT mutation testing, to measure test quality beyond coverage (slow; on demand).
- `sarif-multitool` to merge reports from several tools.
