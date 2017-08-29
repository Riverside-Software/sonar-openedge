# OpenEdge plugin for SonarQube

[[img/main-page-01.png]]

The OpenEdge plugin for SonarQube enables analysis of OpenEdge procedural and object-oriented code, and reports:
 * Metrics (lines of code, comment density, complexity, shared objects, ...)
 * Issues found in the source code and DF files, such as performance killers, usage of deprecated keywords, ...
 * Code coverage from unit tests
 * Code duplication
 * Compiler warnings
 * XREF information

Documentation available in the [wiki](https://github.com/Riverside-Software/sonar-openedge/wiki)

## How do I get started?

While having a build process based on [PCT](https://github.com/Riverside-Software/pct) is recommended (as some metrics and rules can only be executed with the build output of PCT), you can still try the OpenEdge plugin for SonarQube directly on your codebase, just follow the [instructions](https://github.com/Riverside-Software/sonar-openedge/wiki/Getting-started).

PCT is open-source and free, but if you want to rely on our consulting services, please contact us at contact@riverside-software.fr

## Build status

* OpenEdge plugin - master branch : [![Build Status](http://ci.rssw.eu/job/sonar-openedge/job/master/badge/icon)](http://ci.rssw.eu/job/sonar-openedge/job/master/)
