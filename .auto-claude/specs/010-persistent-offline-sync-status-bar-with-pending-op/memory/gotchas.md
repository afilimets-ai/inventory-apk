# Gotchas & Pitfalls

Things to watch out for in this codebase.

## [2026-03-24 15:18]
Java/JDK not installed in development environment - blocks gradle compilation verification

_Context: When running ./gradlew compileDebugKotlin, got error 'JAVA_HOME is not set and no java command could be found'. This blocks verification but doesn't prevent code completion. Code should compile successfully once Java 17+ is installed and JAVA_HOME is set._
