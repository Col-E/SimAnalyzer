# Contributing to SimAnalyzer

The following is a series of guidelines for contributing to SimAnalyzer. 
They're not _"rules"_ per say, rather they're more like goals to strive towards. 
Regardless of how closely you adhere to the following guidelines I really appreciate you taking the time to contribute, it means a lot :+1:

**Table of Contents**

 * [Reporting Bugs](#reporting-bugs)
 * [Suggesting Features](#suggesting-features)
 * [Coding Guidelines](#coding-guidelines)
 * [Pull Requests](#pull-requests)
 * [Commit messages](#commit-messages)
 
**TLDR?**

Follow the style of the rest of the code. 
Comment your code where it makes sense. 
Make sure the unit tests pass before submitting a pull request. Follow the [commit message rules](#commit-messages).

## Reporting Bugs

When creating an issue select the `Bug report` button. 
This will provide a template that you can fill in the details for your bug. 
Please include as much information as possible. 
This can include:

 * Clear and descriptive title
 * Log files
 * Steps to reproduce the bug 
 * An explanation of what you _\*expected\*_ to happen
 * The file being analyzed _(Do not share anything you do not own the rights to)_ 

## Suggesting Features

When creating an issue select the `Feature` button. 
This will provide a template that you can fill in the details for your feature idea. 
Be as descriptive as possible with your idea. 

## Coding Guidelines

SimAnalyzer uses Checkstyle to enforce a modified varient of the [Google Java guidelines](https://google.github.io/styleguide/javaguide.html). 
The default formatting of IntelliJ or Eclipse should work just fine. 
Don't auto-format entire classes at a time though. 
Only format code that you are modifying. 
This keeps the commits small and localized to the changes you're creating, making it easier for others to understand the intent behind each commit.

## Pull Requests

Before making a pull request make sure that your changes successfully compile and pass the unit tests. 
You can do so by running the following maven command: `mvn clean test`

When creating a pull request please consider the following when filling in the template:

 * Clear and descriptive title
 * A clear description of what changes are included in the pull

## Commit messages

This project follows the [Semantic Versioning](https://semver.org/) specification, which is completely automated through the Continuous Integration and [semantic-release](https://github.com/semantic-release/semantic-release). 
To make this possible, it is crucial to use the [Angular Commit Message Conventions](https://github.com/angular/angular.js/blob/master/DEVELOPERS.md#-git-commit-guidelines) in all of your commits, to allow the system to categorize your changes and take appropriate actions.

### Examples

 * **feat**: A new feature
 * **fix**: A bug fix
 * **docs**: Documentation only changes
 * **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
 * **refactor**: A code change that neither fixes a bug nor adds a feature
 * **perf**: A code change that improves performance
 * **test**: Adding missing or correcting existing tests
 * **chore**: Changes to the build process or auxiliary tools and libraries such as documentation generation
