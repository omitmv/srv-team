---
name: spring-platform-modernization
description: Use when modernizing srv-team build/platform to the approved Java, Spring Boot, Maven, Flyway, Testcontainers and configuration baseline before domain implementation.
---

# Spring platform modernization

Read:

- `docs/Refinamentos Técinicos/MVP/refinamento-modernizacao-plataforma.md`
- `docs/Refinamentos Técinicos/MVP/refinamento-modernizacao-pom.md`
- the transversal implementation plan.

## Target baseline

- Java 21 LTS
- Spring Boot 4.1.1
- Maven 3.9.16 Wrapper
- MySQL authoritative
- Flyway schema management
- Hibernate `ddl-auto=validate`
- Testcontainers MySQL
- Maven Enforcer minimum Java/Maven versions

## Workflow

1. Inspect current POM, wrapper, plugins and application configuration.
2. Change only what is required by the approved modernization.
3. Prefer Spring Boot dependency management over redundant explicit versions.
4. Remove redundant explicit logging dependencies when covered by Boot.
5. Verify third-party library compatibility before version changes not fixed by the refinement.
6. Externalize secrets; never copy production credentials into examples/tests.
7. Add/repair automated verification.
8. Run Maven build/tests using the Wrapper.

Do not combine unrelated domain refactoring with the platform modernization gate.