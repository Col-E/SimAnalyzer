# SimAnalyzer [![Build Status](https://cloud.drone.io/api/badges/Col-E/SimAnalyzer/status.svg)](https://cloud.drone.io/Col-E/SimAnalyzer) [![](https://jitpack.io/v/Col-E/SimAnalyzer.svg)](https://jitpack.io/#Col-E/SimAnalyzer)

SimAnalyzer is an analyzer that computes the values of primitives and basic objects where possible.
Additional features like dead code detection are also available. 
The analyzer is highly configurable so that it can be customized to your personal use case with relative ease.

## Usage

### Add dependency

Add Jitpack to your repositories
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Add SimAnalyzer dependency _(where `VERSION` is the latest version)_
```xml
<dependency>
    <groupId>com.github.Col-E</groupId>
    <artifactId>SimAnalyzer</artifactId>
    <version>VERSION</version>
</dependency>
```
```groovy
implementation 'com.github.Col-E:SimAnalyzer:VERSION'
```

### Configure SimAnalyzer

```java
// Override SimAnalyzer's provider methods to add additional functionality or
// to enhance existing function with outside information provided by you
SimAnalzer analyzer = new SimAnalyzer(new SimInterpreter()) {
    @Override
    protected ResolvableExceptionFactory createExceptionFactory() {
        // Allow overriding error-resolving logic
        return super.createExceptionFactory();
    }

    @Override
    protected StaticInvokeFactory createStaticInvokeFactory() {
        // Allow managing the values of static invoke calls
        return super.createStaticInvokeFactory();
    }

    @Override
    protected StaticGetFactory createStaticGetFactory() {
        // Allow managing the values of static invoke calls
        return super.createStaticGetFactory();
    }

    @Override
    protected TypeChecker createTypeChecker() {
        // Allow better type checking, default uses system classpath
        return super.createTypeChecker();
    }
};
// Determine if we want to skip dead-code blocks
analyzer.setSkipDeadCodeBlocks(true / false);
// Determine if we want to throw unresolved errors, or keep them silent
analyzer.setThrowUnresolvedAnalyzerErrors(true / false);
```

### Exceptions

There are two primary exception types. There is the default ASM `AnalyzerException` and SimAnalyzer's `ResolableAnalyzerException`.

**AnalyzerException**: Thrown when problems occurred in analysis that could not be resolved

**ResolableAnalyzerException**: Logged interally when problems occurred in analysis and checked after analysis finishes to determine if the problem has been resolved. 
A problem can be resolved when ASM's analyzer revisits some frames and their values due to the nature of its control flow handling. 
 
 * If a problem is unresolved, frames will still have been generated.
 * Unresolved errors will be thrown unless `analyzer.setThrowUnresolvedAnalyzerErrors(false);` is set.
 
If any other exception type is thrown, please open a bug report with the full stacktrace.

## Recommended Reading

* **[ASM-Analysis JavaDoc](https://www.javadoc.io/doc/org.ow2.asm/asm-analysis/latest/index.html)** - ASM analysis javadocs.
* **[Using ASM-Analysis to remove dead code](https://coley.software/using-asm-analysis-to-remove-dead-code/)** - An example use case for SimAnalyzer to remove dead code.