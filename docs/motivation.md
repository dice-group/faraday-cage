# Our Motivation

FARADAY-CAGE is a computation modelling framework based on the idea of ETL systems that computation tasks can be regarded
as directed acyclic graphs (DAGs), where the graphs edges represent data flow and the nodes represent atomic computation nodes.
In most of these systems, computation nodes are configurable to some extent.
FARADAY-CAGE offers application developers an execution engine with automatic parallel execution of DAG-shaped computations.
The set of available atomic computation units, called execution nodes, does not need to be predetermined,
thanks to a plugin system, powered by .\
FARADAY-CAGEs configuration system is built on Linked Data standards. 
The interface of execution nodes is declared using the Shapes Constraint Language (SHACL).
SHACL is a part of the W3C RDF specifications suite and can be used to declare specifications of datastructures in RDF, SPARQL and JavaScript, which can
be automatically validated by a SHACL engine. These specifications can also be used to automatically generate web forms or other frontends for data entry and
modification.
Therefore, application developers can declare their application plugin types and constraints in a language understood by machines and humans alike.\
The configuration graph, which describes the arrangement of concrete execution nodes in a DAG, is declared using a very simple RDF vocabulary.
As a result, end users may share their configurations in the Linked Open Data (LOD) Cloud in order to promote reuse and reproducibility of results.
They can also annotate these configurations with meta and provenance data using any desired RDF vocabulary. 

In combination, FARADAY-CAGEs use of cutting-edge specifications in the Linked Data realm has the potential to 
1. boost ETL-like application developer productivity by providing automatic configuration validation and frontend generation.
2. allow end users of ETL-like applications to effortlessly annotate, share and reuse their configurations

In the following, we will describe the basic conceptual entities of FARADAY-CAGE in detail.