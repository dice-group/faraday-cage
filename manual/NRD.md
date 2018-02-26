## NRD
Nested Range Definitions (NRD) define the allowed structure of RDF subgraphs.
They are designed for (1) communicating the expected structure of an RDF subgraph
for automatic user interface design and (2) easy automatic verification of RDF
subgraphs in applications.
They therefore have a much narrower scope than ontology languages and
allow for more lightweight implementations.

The namespace for the NRD vocabulary is

`@prefix nrd: <http://nrd.aksw.org/vocabulary/1.0/#> .`

A NRD has the `rdf:type` of `nrd:NRD` and needs to have a single predicate
`nrd:type` that designates its type.

The following `nrd:type`s exist:

| `nrd:Type` | Description |
|---|---|
|`nrd:Literal`| All literal values |
|`nrd:Resource`| Just IRI resources |
|`nrd:BNode`| Just blank nodes |
|`nrd:List`| All lists |
|`nrd:Predicate`| All predicates |

Depending on their `nrd:type` NRDs have a set of properties that they can or must define:

| Property | Scope | Required | Description |
| --- | --- | --- | --- |
| `nrd:iri` | `nrd:Resource` `nrd:Predicate` | yes | IRI of the specified RDF resource |
| `nrd:required` | `nrd:Predicate` | no | "true" or "false", indicating if this predicate must be present |
| `nrd:comment` | all types | no | a comment providing extra information to the end user (e.g. in an automatically generated GUI) |
| `nrd:range` | all but `nrd:Literal` | no | a `rdf:List` of `nrd:NRD` instances  |

A `nrd:NRD` describes the structure of a RDF subgraph by recursively defining ranges
of RDF resources using `nrd:range`.
The notion of a `nrd:range` is different than that of `rdfs:range`, as

```
nrd:range rdfs:domain rdfs:Resource
nrd:range rdfs:range rdf:List 
```

As already noted in the table above, ranges are defined using RDF lists of resources that are themselves NRDs,
which is why they are actually called **Nested** Range Definitions.

The `nrd:type`s of nested `nrd:NRD`s are restricted based on the `nrd:type` of the
parent `nrd:NRD` as follows:

| Parent `nrd:type` | Allowed Child `nrd:type` |
| ----------------- | --------------------------|
| `nrd:Literal`      |  *none*            |
| `nrd:Resource`      |  `nrd:Predicate`            |
| `nrd:BNode`      |  `nrd:Predicate`            |
| `nrd:List`      |  *all but* `nrd:Predicate`            |
| `nrd:Predicate`      |  *all but* `nrd:Predicate`            |

A root NRD, i.e. a NRD that is not nested in another NRD, must have a `nrd:type` of `nrd:Resource`.

An extension that specifies required alternatives, dependent requires, allowed values for literals
and arity constraints is planned.


## JSON/NRD

Example: 
```
{
  "plugin1": [{
    "~uri": "http://example.com/#parameter1",
    "~required": true|false,
    "~range": [{
      	"~type": "resource|bNode|list|literal",
        "~required": true|false,  // only for resource
        ...
    }],
    "~comment": "some comment about this parameter"
  }],
  "plugin2": [{ ... }]
}
```