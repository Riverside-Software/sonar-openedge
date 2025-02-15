# CABL - Code Analyzer for ABL

CABL enables analysis of OpenEdge procedural and object-oriented code on the [SonarQube](https://www.sonarqube.org) platform, by reporting:
 * Metrics (lines of code, comment density, complexity, shared objects)
 * Issues found in the source code and DF files, such as performance killers, usage of deprecated keywords or incorrect usage of functions, potential runtime issues, and so on
 * Code coverage from unit tests
 * Code duplication
 * Compiler warnings
 * XREF information
 * Vulnerabilities and security hotspots

<img src="https://github.com/Riverside-Software/sonar-openedge/wiki/img/main-page-01.png" align="center" vspace="5">

## Documentation

Documentation is available [here](https://github.com/Riverside-Software/sonar-openedge/wiki)

## How do I get started?

While having a build process based on [PCT](https://github.com/Riverside-Software/pct) is recommended (as some metrics and rules can only be executed with the build output of PCT), you can still try the OpenEdge plugin for SonarQube directly on your codebase. Just follow the [instructions](https://github.com/Riverside-Software/sonar-openedge/wiki/Getting-started).

PCT is open-source and free, but if you want to rely on our consulting services, please contact us at contact@riverside-software.fr

## Build status

OpenEdge plugin - main branch: <br/>  [![Build Status](http://ci.rssw.eu/job/sonar-openedge/job/main/badge/icon)](http://ci.rssw.eu/job/sonar-openedge/job/main/)

## IntelliJ support

Opening this repository in IntelliJ requires the [ANTLR 4 plugin](https://plugins.jetbrains.com/plugin/7358-antlr-v4). Once installed, open "ANTLR v4 default project settings", and add `src/main/antlr4/imports` to "Location of imported grammars".
