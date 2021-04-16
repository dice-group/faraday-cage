# FARADAY-CAGE 

[![Build Status](https://github.com/dice-group/faraday-cage/actions/workflows/run-tests.yml/badge.svg?branch=master&event=push)](https://github.com/dice-group/faraday-cage/actions/workflows/run-tests.yml)
[![GNU Affero General Public License v3.0](https://img.shields.io/badge/license-GNU_Affero_General_Public_License_v3.0-blue.svg)](./LICENSE)
![Java 11+](https://img.shields.io/badge/java-11+-lightgray.svg)

[comment]: <> ([![Codacy Badge]&#40;https://api.codacy.com/project/badge/Grade/b2d1aa91a51f4beab8bd3ed18ba0c729&#41;]&#40;https://www.codacy.com/app/kvndrsslr/faraday-cage?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dice-group/faraday-cage&amp;utm_campaign=Badge_Grade&#41;)

<img align="right" width="160" src="https://raw.githubusercontent.com/dice-group/faraday-cage/master/docs/_media/fcage.svg">

**FARADAY-CAGE** is the **F**r**a**mework fo**r** **A**cyclic **D**irected Graphs **Y**ielding
Parallel **C**omput**a**tions of **G**reat **E**fficiency. It originated from the execution engine
of the redesigned [DEER](https://github.com/dice-group/deer) and has now been outsourced into a
project of its own to evaluate the possibility of also using it for other projects.

FARADAY-CAGE enable your projects to use a RDF configuration driven execution engine that can handle
any data flow that is shaped as a [DAG](https://en.wikipedia.org/wiki/Directed_acyclic_graph).
You just need to extend a couple of abstract classes to tailor-fit FARADAY-CAGE to your application
domain.

## Documentation

Will be made available with the first stable release.

## Maven

```xml
<dependencies>
  <dependency>
    <groupId>org.aksw.faraday_cage</groupId>
    <artifactId>faraday-cage</artifactId>
    <version>1.1.0</version>
  </dependency>
</dependencies>

<repositories>
 <repository>
      <id>maven.aksw.internal</id>
      <name>University Leipzig, AKSW Maven2 Internal Repository</name>
      <url>http://maven.aksw.org/repository/internal/</url>
    </repository>

    <repository>
      <id>maven.aksw.snapshots</id>
      <name>University Leipzig, AKSW Maven2 Snapshot Repository</name>
      <url>http://maven.aksw.org/repository/snapshots/</url>
    </repository>
</repositories>
```

