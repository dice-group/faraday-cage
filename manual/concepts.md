# Conceptual Introduction
FARADAY-CAGE features a plugin system, the notion of configuration graphs, execution graphs,
execution nodes and wrappers. All these will be explained in this section.

## Plugins {#plugins}
[PF4J](https://pf4j.org) powers our plugin system.
This means that appropriate implementations of atomic operations can be supplied, discovered and instantiated
at runtime. Use the `@Extension` annotation on your atomic operations classes to enable automatic
discovery.
In order to be discovered by our framework, classes need to implement our `Plugin` interface, which is
the PF4J extension point.
However, you typically do not need to implement this interface directly, since currently there are
only two kinds of plugins to distinguish in FARADAY-CAGE: (1) execution nodes and (2) execution
node wrappers, which will be explained in the following subsections.
Their interfaces extend on the `Plugin` interface.

## Execution Nodes {#nodes}
Atomic operations are represented by so called execution nodes, which are classes that implement the
`ExecutionNode<T>` interface, which extend the `Plugin` interface.
The type parameter `T` stands for the class which represents your data.
Execution nodes can be parameterized, which is marked by implementing the `Parameterized` interface.
The easiest and recommended way to write your own execution nodes is by extending our abstract classes
`AbstractExecutionNode<T>` and `AbstractParameterizedExecutionNode<T>` which implement validity
checks and sane defaults for some of these interfaces methods.
Execution nodes can have multiple inputs and multiple outputs. Each type of execution node will
specify the range of in- and outputs that they accept using their *degree bounds*.

## Wrappers & Decorators {#wrappers}
Execution node wrappers are the second type of plugins in FARADAY-CAGE and implement the
`ExecutionNodeWrapper<V extends ExecutionNode<T>,T>` interface. They are basically factories for 
`ExecutionNodeDecorator<T>` instances. These can decorate execution nodes for various use cases such
as capturing analytics data, data annotation and caching.
Execution node wrappers can also be parameterized.
In order to implement your own, you can extend the abstract convenience classes
`AbstractExecutionNodeWrapper<V,T>` and `AbstractParameterizedExecutionNodeWrapper<V,T>`.
Idiomatic usage of these classes is demonstrated in the [tutorial](./tutorial.html).   

## Execution Graph {#graph}
The execution graph is a directed acyclic labeled multigraph. Its vertices are (potentially wrapped)
execution nodes and its directed edges represent the flow of data.
Each execution node in an execution graph can have multiple inputs and multiple outputs.
As the order of the inputs and outputs does matter, the edges are *labeled*, i.e.
they specify from which so called **source port** a data flow originates and to which **destination port**
it points. An execution graph is considered to be valid if it satisfies the acyclic property and if
for each execution node the number of incoming and outgoing edges is within its degree bounds.     

## Configuration Graph {#config}
The configuration graph is an RDF description of the execution graph.
It specifies not only the shape of the execution graph but also sets parameter values of the parameterized
execution nodes and execution node wrappers as well as declares which execution node wrappers decorate
which execution nodes.







