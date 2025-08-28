# Contribution Guidelines

## Table of Contents

- [Project Home](../README.md)
- Contribution Guidelines
  - [Jagex Account Integration](#jagex-account-integration)
  - [Getting Started](#getting-started)

In order to start developing features for the Project Xeric plugin, you will need the
[IntelliJ IDEA](https://www.jetbrains.com/idea/) IDE for coding, the [RuneLite](https://runelite.net/)
client for running the Test Client, a [Java 11 JDK](https://www.openlogic.com/openjdk-downloads?field_java_parent_version_target_id=406&field_operating_system_target_id=All&field_architecture_target_id=All&field_java_package_target_id=396)
installation available, and [Git](https://git-scm.com/) for version control and interacting with the
GitHub repository.

## Jagex Account Integration

If you have converted your account to a Jagex Account, you will need to follow the
[Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) RuneLite
guide to obtain login credentials when launching RuneLite from your IDE.

> NOTE: You should probably remove the `--insecure-write-credentials` Client argument after
> opening the client for the first time and verifying the `credentials.properties` file is generated

## Getting Started

- Install the following IntelliJ IDEA plugins:
  - [Lombok](https://plugins.jetbrains.com/plugin/6317-lombok) by JetBrains
- Download/Clone the repository to your local machine and open the project in IntelliJ.
- Verify the following:
  - Navigate to "File > Project Structure"
    - "SDK" is set to "11"
    - "Language level" is set to "SDK default"
  - Navigate to "Run > Edit Configurations"
    - "run-plugin" is available under "Application"
    - "run-plugin" run configuration has "java 11" set
    - "spotless" is available under "Gradle"
- Attempt to run the Test Client run configuration in debug mode
  - If you use a Jagex Account, follow the steps outlined in [Jagex Account Integration](#jagex-account-integration)
    or you may encounter problems
  - If you encounter any errors, verify the above steps and try again, or [submit an issue](https://github.com/Septem151/project-xeric/issues/new/choose)

