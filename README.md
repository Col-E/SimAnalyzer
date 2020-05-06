# SimAnalyzer [![Build Status](https://cloud.drone.io/api/badges/Col-E/SimAnalyzer/status.svg)](https://cloud.drone.io/Col-E/SimAnalyzer)

SimAnalyzer is an analyzer that computes the values of primitives and basic objects where possible. 

## Usage 

### Setup 

Supply `VirtualValue` with a `BiPredicate<Type, Type>` that determines if the first argument is a parent of the second argument. 
```java
VirtualValue.setParentCheck((parent, child) -> /* code here */ );
```
Then use the `SimAnalyzer` as you would a typical ASM analyzer. 

### Exceptions

The interpreter that backs the analysis process throws `AnalyzerException` when a problem occurred that could not be handled. 
When an analysis error occurs in the interpreter it is saved and revisited later.
In most cases errors can be resolved after ASM finishes the analysis process. 
However, should this not be the case, then the first error logged will be thrown by the analyzer.

If any other exception type is thrown, please open a bug report with the full stacktrace.

## Recommended Reading

* [ASM-Analysis JavaDoc](https://www.javadoc.io/doc/org.ow2.asm/asm-analysis/latest/index.html)