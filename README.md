# FARADAY-CAGE 

[![Build Status](https://travis-ci.org/dice-group/faraday-cage.svg?branch=master)](https://travis-ci.org/dice-group/faraday-cage)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b2d1aa91a51f4beab8bd3ed18ba0c729)](https://www.codacy.com/app/kvndrsslr/faraday-cage?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dice-group/faraday-cage&amp;utm_campaign=Badge_Grade)
[![GNU Affero General Public License v3.0](https://img.shields.io/badge/license-GNU_Affero_General_Public_License_v3.0-blue.svg)](./LICENSE)
![Java 1.9+](https://img.shields.io/badge/java-1.9+-lightgray.svg)

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

```
<dependencies>
  <dependency>
    <groupId>org.aksw.faraday_cage</groupId>
    <artifactId>faraday-cage</artifactId>
    <version>{insert version here}</version>
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

