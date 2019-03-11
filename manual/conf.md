# Configuration Vocabulary

Configuration graphs are written using the **fcage** vocabulary with the designated namespace `http://w3id.org/fcage/`.  
In this section we will document this vocabulary.
All examples are in *TURTLE* serialization and assume the following prefix definitions:
```
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix : <urn:example:fcage-manual/> .
@prefix fcage: <http://w3id.org/fcage/> .
```

### `fcage:ExecutionNode` {#node}
Base class for all execution nodes.
All execution node implementation types discoverable from the plugin system are automatically
marked as subclass of `fcage:ExecutionNode`.  

### `fcage:ExecutionNodeWrapper` {#wrapper}
Base class for all execution node wrappers.
All execution node wrapper implementation types discoverable from the plugin system are automatically
marked as subclass of `fcage:ExecutionNodeWrapper`.

### `fcage:hasOutput` {#out}
**Domain**: `fcage:ExecutionNode`  
**Range**: --- (*see description*)   
**Description**:  
There are three ways to define outgoing connections to other execution nodes:
1. **port-explicit list**  
  The most verbose way to declare outputs. Here the object is an `rdf:List` of resources with the
  `fcage:toNode` and `fcage:toPort` properties. The object of `fcage:toNode` needs to be an instance
  of `fcage:ExecutionNode` while the range of `fcage:toPort` is `xsd:integer`.
2. **port-implicit list**  
  In case the destinations of an execution node have no other input or use the `fcage:hasInput`
  property, this abbreviated form can be used. Here the object is an `rdf:List` of instanced of `fcage:ExecutionNode`. 
3. **port-implicit single**  
  Shortcut for single output - single input connection. Here the object is just an instance of `fcage:ExecutionNode`.

### `fcage:hasInput` {#in}
**Domain**: `fcage:ExecutionNode`  
**Range**: `rdf:List` of `fcage:ExecutionNode`  
**Description**:  
This property is only needed when an execution node has *multiple inputs* whose ports are *not* stated
explicitly.

### `fcage:decoratedBy` {#deco}
**Domain**: --- (*see description*)  
**Range**: --- (*see description*)  
**Description**:  
Declare execution node decorators for (1) individual execution node instances or (2) all execution nodes of a given type.
The object of `fcage:decoratedBy` is either an `fcage:ExecutionNodeWrapper` or an `rdf:List` of `fcage:ExecutionNodeWrapper`.  
Decorators are applied left to right and from broadest scope to narrowest scope.  
For illustration purposes consider the following declarations:
```
:MyWrapperType rdfs:subClassOf fcage:ExecutionNodeWrapper .
:myWrapper1 a :MyWrapperType .
:myWrapper2 a :MyWrapperType .
:myWrapper3 a :MyWrapperType .
:myWrapper4 a :MyWrapperType .
:myWrapper5 a :MyWrapperType .

:MyExecutionNodeType rdfs:subClassOf fcage:ExecutionNode .
:myNode a :MyExecutionNodeType .

:myNode fcage:decoratedBy ( :myWrapper1 :myWrapper2 ) .
fcage:ExecutionNode fcage:decoratedBy ( :myWrapper3 :myWrapper4 ) .
:MyExecutionNodeType fcage:decoratedBy :myWrapper5 .
```
The resulting nesting is `myWrapper2(myWrapper1(myWrapper5(myWrapper4(myWrapper3(myNode)))))`.   