# SimAnalyzer [![Build Status](https://cloud.drone.io/api/badges/Col-E/SimAnalyzer/status.svg)](https://cloud.drone.io/Col-E/SimAnalyzer) [![](https://jitpack.io/v/Col-E/SimAnalyzer.svg)](https://jitpack.io/#Col-E/SimAnalyzer)

SimAnalyzer is an analyzer that computes the values of primitives and basic objects where possible.
Additional features like dead code detection are also available. 
The analyzer is highly configurable so that it can be customized to your personal use case with relative ease.

## Features

* Highly configurable
    * Provide custom type comparator for inheritance, required for accurate frame generation if custom types are defined in the analyzed code.
    * Provide custom exception factory, allowing for custom handling of resolvable errors.
    * Provide custom static-invoke factory, allowing custom defined return values of static method calls.
    * Provide custom static-get factory, allowing custom defined values of static fields.
* Detect dead code
    * By default dead code is skipped entirely, resulting in `null` values for frames at the indices of instructions within dead code blocks.
* Track instructions that contribute to values
    * Values track the instructions that directly contribute to their value
* Track basic control flow blocks

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
    
    @Override
    protected TypeResolver createTypeResolver() {
        // Allow common type resolution, defaults to only merging exactly equal types
        return super.createTypeChecker();
    }

     @Override
    protected ParameterFactory createParameterFactory() {
        // Allow the interpreter to be fed literal values for the parameters of the analyzed method
        return super.createParameterFactory();
    }
};
// Determine if we want to skip dead-code blocks
analyzer.setSkipDeadCodeBlocks(true / false);
// Determine if we want to throw unresolved errors, or keep them silent
analyzer.setThrowUnresolvedAnalyzerErrors(true / false);
```

To easily create a `TypeChecker` implementation you can use the built-in hierarchy graph tool `InheritanceGraph`
```java
// Setup the graph
InheritanceGraph graph = new InheritanceGraph();
graph.addClasspath(); // add all files loaded in the classpath
graph.addModulePath(); // add all files on the module path (Java 9+)
graph.addClass(new File("example.class")); // add single class
graph.addClass(Files.readAllBytes(Paths.get("example.class"))); // add bytecode
graph.addArchive(new File("example.jar")); // add jar or jmod (java module)
graph.addDirectory(new File("directory/with/classes-or-jars")); // add directory (recursive)
graph.add("child", Arrays.asList("parent1", "parent2")); // manually specify child/parent relations
// Use the graph
@Override
protected TypeChecker createTypeChecker() {
	return (parent, child) -> graph.getAllParents(child.getInternalName())
			.contains(parent.getInternalName());
}
```

The same can be done for a `TypeResolver`:
```java
// Using the same "graph" object from above
@Override
protected TypeResolver createTypeResolver() {
    return new TypeResolver() {
        @Override
        public Type common(Type type1, Type type2) {
            String common = graph.getCommon(type1.getInternalName(), type2.getInternalName());
            if (common != null)
                return Type.getObjectType(common);
            return TypeUtil.OBJECT_TYPE;
        }

        @Override
        public Type commonException(Type type1, Type type2) {
            String common = graph.getCommon(type1.getInternalName(), type2.getInternalName());
            if (common != null)
                return Type.getObjectType(common);
            return TypeUtil.EXCEPTION_TYPE;
        }
    };
}
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
* **[Using ASM-Analysis to remove dead code](http://archive.is/Ciz85)** - An example use case for SimAnalyzer to remove dead code.