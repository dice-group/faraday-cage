<img src="./logo.svg" style="float:right; margin:0em 0em 3em 1em"/>

# Getting started

## About this Documentation {#about}

This documentation aims to be a comprehensive guide to **FARADAY-CAGE**.
If you believe a part of it to be inaccurate or outdated, please
[file an issue](https://github.com/dice-group/faraday-cage/issues/new).

## Overview {#overview}

**FARADAY-CAGE** is the **F**r**a**mework fo**r** **A**cyclic **D**irected Graphs **Y**ielding
Parallel **C**omput**a**tions of **G**reat **E**fficiency. 🤖  
It enables your projects to use a RDF configuration driven execution engine that can handle
any homogenous [DAG](https://en.wikipedia.org/wiki/Directed_acyclic_graph)-shaped data flow.

**Your requirements:**
* Data processing application on the JVM with atomic operations which can be arbitrarily arranged as a DAG  
* Homogenous data flow, i.e. all data passed between atomic operations has the same type

**Our offering:**
* Automated validation of application configurations using [W3C Recommendation Shapes Constraint Language (SHACL)](https://www.w3.org/TR/shacl/#DatatypeConstraintComponent)
  * Skip the validation boilerplate in your implementations, instead spend time optimizing your business logic.
  * Use generic web frontends for user friendly configuration building and management with no additional work required  
* Parsing your RDF configuration file, generating a fully parallelized execution graph 
* Additional API for execution graph manipulation
* Supporting the development of plugins for your application utilizing [PF4J](https://pf4j.org)
 
**All you have to do:**
* Specify validation graphs for your atomic operations configuration using the SHACL vocabulary
* Extend our abstract classes and implement *highly configurable* atomic operations for your application domain, skipping validation boilerplate
* Call a few methods on our convenience facade class [`FaradayCageContext`](https://dice-group.github.io/faraday-cage/javadoc/org/aksw/faraday_cage/engine/FaradayCageContext.html)
* Write some documentation for your users
* *Lean back*