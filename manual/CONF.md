# Configuration of FARADAY-CAGE

In order to get your DAG-shaped computations to work, you first need to specify the execution graph using RDF.
This can be done either programmatically or by declaring it in a configuration file.
Here, we will focus on the latter case. The first case will be discussed [later on](./TODO.md). 

## Core Configuration Vocabulary {#core}

The RDF graph consists of execution nodes which can optionally be plugins and / or parametrized.
An execution node can have multiple incoming edges (each carrying different data) and produce multiple outgoing edges (each carrying different data).
As a result, it is important to assign indices to edges for each execution node. We call these indices **ports**. 

There are two ways to connect execution nodes with edges:
  1. explicitly by giving only `:hasOutput` declarations on the nodes having outgoing edges.
  2. implicitly by matching `:hasOutput` and `:hasInput` declarations of endpoints on the nodes.
  
Execution nodes will be discovered automatically over the `:hasOutput` predicate.

**FARADAY-CAGE** specifies the following vocabulary for any given execution node, here labeled`:e1`:
 
* `:hasOutput` *(required)*
  declares the outgoing edges from this execution node.
  Allowed values are:
    1. another execution node `:e2` (if `:e1`s only edge goes to `:e2`)
    2. a list of other execution nodes (if `:e1` has edges to each execution node in the list)
    3. a list of blank nodes (or resources) representing outgoing edges with the following properties 
      * `:toNode`
        the execution node this edge points to
      * `:toPort`
        the port of the execution node this edge points to 
* `:hasInput` *(deprecated)*
  declares the incoming edges to this execution node.
  Allowed values are:
    1. another execution node `:e2` (if `:e1`s only edge comes from `:e2`)
    2. a list of other execution nodes (if `:e1` has edges leading to it from each execution node in the list)
  
  `:hasInput` needs to be declared only if there is more than 1 input and if the incoming edges have not
  been declared explicitly at their origin nodes.   
  It is deprecated as explicitly declaring nodes will become the single standard in a future version.
   
* `:implementedIn` (for plugin nodes)
  declares the resource identifying the implementation of the plugin. 
  
## Example (Turtle) {#example}

```
:e1 :hasOutput :e2 .

:e2 :hasOutput ( :e3 :e4 ) .

:e3 :hasOutput ( [ :toNode :e4 ; :toPort 0 ]
                 [ :toNode :e5 ; :toPort 1 ]
                 [ :toNode :e5 ; :toPort 0 ] ) .

:e4 :hasInput ( :e3 :e2 ) ;
    :hasOutput ( [ :toNode :e5 ; :toPort 2 ] ) .

:e5 :implementedIn :somePluginClassIdentifier
```

The above example generates the following graph:
```
e1 ----> e2 ----> e3 ----------------> e5
            \          \           /
             \-----------> e4 ----/
```
Note that the edges declared between `:e3` and `:e5` require the explicit syntax,
because with the implicit syntax double edges between nodes will be assigned to ports in order, i.e.

```
:e3 :hasOutput ( :e5 :e5 ) .
:e5 :hasInput ( :e3 :e3 ) .
```

is equivalent to

```
:e3 :hasOutput  ( [ :toNode :e5 ; :toPort 0 ]
                  [ :toNode :e5 ; :toPort 1 ] ) .
```

so if edges need to be assigned to ports in a different order than the origin ports, explicit syntax needs to be used.

## Parameters {#params}

An execution node can be parametrized, i.e. it can be assigned an arbitrary number of additional parameters
using vocabulary that will be defined by the execution node implementation using the **FARADAY-CAGE Parameters API**.
This vocabulary is not part of **FARADAY-CAGE** and will be included by the software that extends **FARADAY-CAGE**.
In a future version we will use [SHACL](https://www.w3.org/TR/shacl/#dfn-shacl-superclass) to automatically validate parameters.