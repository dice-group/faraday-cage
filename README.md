# FARADAY-CAGE 

[![Build Status](https://travis-ci.org/dice-group/faraday-cage.svg?branch=master)](https://travis-ci.org/dice-group/faraday-cage)
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

## Experimental Warning

FARADAY-CAGE is in an early development stage (v0.x.y), where API can and will change frequently.

## Documentation

Will be made available with the first stable release.

## Maven

```
<dependencies>
  <dependency>
    <groupId>com.github.dice-group</groupId>
    <artifactId>faraday-cage</artifactId>
    <version>{insert version here}</version>
  </dependency>
</dependencies>

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

