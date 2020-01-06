# Basic Tutorial

**This section is under construction**

<!--
## Kickstarting an Application {#kickstart}

Add the following Maven dependency to your project:

```xml
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

## Implementing an Atomic Operation {#operation}

See [StringOperation.java](https://github.com/dice-group/faraday-cage/tree/master/example-application/src/main/java/org/aksw/faraday_cage/example/StringOperation.java)  
See [StringProviderOperation.java](https://github.com/dice-group/faraday-cage/tree/master/example-application/src/main/java/org/aksw/faraday_cage/example/StringProviderOperation.java)  
See [SubStringOperation.java](https://github.com/dice-group/faraday-cage/tree/master/example-application/src/main/java/org/aksw/faraday_cage/example/SubStringOperation.java)  
See [StringPrinterOperation.java](https://github.com/dice-group/faraday-cage/tree/master/example-application/src/main/java/org/aksw/faraday_cage/example/StringPrinterOperation.java)  

## Writing a Validation Graph {#validaton}

See [Validation Graphs](https://github.com/dice-group/faraday-cage/tree/master/example-application/src/main/resources/shacl)

## Bringing it All Together {#integration}

See [ExampleApplication.java](https://github.com/dice-group/faraday-cage/tree/master/example-application/src/main/java/org/aksw/faraday_cage/example/ExampleApplication.java)

## Writing a Configuration Graph {#configuration}

See [config.ttl](https://github.com/dice-group/faraday-cage/tree/master/example-application/config.ttl)  

```
@prefix : <urn:example:fcage-tutorial/> .
@prefix fcage: <http://w3id.org/fcage/> .

:e1 fcage:hasInput :e2 .

:e2 fcage:hasInput ( :e3 :e4 ) .

:e3 fcage:hasOutput ( [ :toNode :e4 ; :toPort 0 ]
                      [ :toNode :e5 ; :toPort 1 ]
                      [ :toNode :e5 ; :toPort 0 ] ) .

:e4 fcage:hasInput ( :e3 :e2 ) ;
    fcage:hasOutput ( [ :toNode :e5 ; :toPort 2 ] ) .

:e5 a :somePluginClassIdentifier
```

The above example generates the following graph:
```
e1 ----/> e2 ----/> e3 ----------------/> e5
            \          \           /
             \-----------/> e4 ----/
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

## Running the Application {#running}

```
mvn package shade:shade
java -jar target/example-application-1.0.0.jar config.ttl
```
-->