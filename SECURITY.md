# Security Policy

## Supported Versions

Security fixes are applied on `main` for the current `0.1` (`spring-boot-streaming`) application. See [pom.xml](pom.xml) for the Spring Boot version.

| Version | Supported |
| ------- | --------- |
| `main` (`0.1`) | Yes |
| Historical commits / removed prebuilt WARs | No |

Supported runtime and build environments are JDK 17+ and Maven 3.9+.

## Reporting a Vulnerability

Please do **not** report undisclosed security vulnerabilities through public GitHub issues, pull requests, or discussions.

Use GitHub's [private vulnerability reporting](https://github.com/tinywind/video-streaming-service/security/advisories/new) for this repository to submit a report confidentially. If you cannot use GitHub Security Advisories, contact the maintainer directly at `tinywind0@gmail.com`.

When reporting an issue, please include as much of the following as possible:

- Commit SHA and Java / Maven version used
- Affected endpoint, JSP view, or component (directory browsing, MP4 range streaming, annotation batch persistence, or CSV download)
- A minimal reproduction or proof of concept
- Description of the potential impact and any suggested mitigation

## Response Process

- We aim to acknowledge receipt of a vulnerability report within 7 days.
- Validated issues are tracked privately through a GitHub Security Advisory while a fix is prepared and verified.
- Once a patch is available on `main`, we will publish the advisory and credit reporters who wish to be acknowledged.

## Scope and Security Model Notes

`video-streaming-service` is a Spring Boot + JSP web application that serves MP4 videos from a configured `-Dvideo.root` directory and records timestamped keyboard annotations in H2 for CSV export.

### In scope

- Path traversal or arbitrary file disclosure outside the configured `video.root` directory during directory listing or MP4 streaming
- Cross-site scripting (XSS) in JSP views, CSV formula injection in exported annotation batches, or SQL injection in persistence queries
- Vulnerabilities in Spring Boot, H2, Jackson, Hibernate, or other Maven dependencies declared in `pom.xml`

### Out of scope

- Intentional local-host access when an operator exposes `http://localhost:8080/` on an untrusted network without an external reverse proxy or authentication layer
- Codec/playback issues in client browsers
