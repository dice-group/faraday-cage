package org.aksw.faraday_cage;

import org.aksw.faraday_cage.nodes.Node;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 *
 *
 */
public class ExecutionGraph {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionGraph.class);

  private Map<Resource, List<Edge>> edges = new HashMap<>();
  private Supplier<Analytics> fn = null;

  private static class Edge {
    private int fromPort;
    private int toPort;
    private Resource toNode;

    private Edge(int fromPort, int toPort, Resource toNode) {
      this.fromPort = fromPort;
      this.toPort = toPort;
      this.toNode = toNode;
    }

    int getFromPort() {
      return fromPort;
    }

    int getToPort() {
      return toPort;
    }

    Resource getToNode() {
      return toNode;
    }
  }

  public ExecutionGraph() {
    
  }

  public ExecutionGraph addEdge(Resource from, int fromPort, Resource to, int toPort) {
    if (!edges.containsKey(from)) {
      edges.put(from, new ArrayList<>());
    }
    edges.get(from).add(new Edge(fromPort, toPort, to));
    return this;
  }

  public Analytics execute() {
    if (fn == null) {
      throw new RuntimeException("Need to compile ExecutionGraph before executing it!");
    }
    return fn.get();
  }

  public <T> Supplier<Analytics> compile(ExecutionFactory<T> executionFactory) {
    return compile(executionFactory, CompletableFutureFactory.DEFAULT);
  }

  public <T> Supplier<Analytics> compile(ExecutionFactory<T> executionFactory, CompletableFutureFactory futureFactory) {
    validateEdges();
    return fn = new ExecutionGraphCompiler<>(executionFactory, futureFactory).compile();
  }

  private void validateEdges() {
    BiConsumer<HashMap<Resource, SortedSet<Integer>>, String> checkPortNumbers = (ports, dir) -> ports.forEach((k, v) -> {
      int j = -1;
      for (Integer i : v) {
        if (i != ++j) {
          throw new RuntimeException("Error in " + k + ": missing " + dir + "port #" + j + "! There were " + v.size() + " ports declared.");
        }
      }
    });
    HashMap<Resource, SortedSet<Integer>> outPorts = new HashMap<>();
    edges.forEach((key, value) -> value.forEach(c -> {
      SortedSet<Integer> set = (outPorts.containsKey(key)) ? outPorts.get(key) : new TreeSet<>();
      set.add(c.getFromPort());
      outPorts.put(key, set);
    }));
    checkPortNumbers.accept(outPorts, "out");
    HashMap<Resource, SortedSet<Integer>> inPorts = new HashMap<>();
    edges.values().stream()
      .flatMap(Collection::stream)
      .forEach(c -> {
        SortedSet<Integer> set = (inPorts.containsKey(c.getToNode())) ? inPorts.get(c.getToNode()) : new TreeSet<>();
        set.add(c.getToPort());
        inPorts.put(c.getToNode(), set);
      });
    checkPortNumbers.accept(inPorts, "in");
  }

  private class ExecutionGraphCompiler<T> {

    private final Map<Resource, int[]> degrees = getDegrees();
    private final HashMap<Resource, Execution<T>> visitedHubs = new HashMap<>();
    private final List<Pipeline> startPipelines = new ArrayList<>();
    private final List<Hub> startHubs = new ArrayList<>();
    private final Map<Resource, Hub> hubs = new HashMap<>();
    private final CompletableFutureFactory completableFutureFactory;
    private final ExecutionFactory<T> factory;
    private Pipeline currentPipe;
    private CompletableFuture<T> joiner;
    private Analytics analytics;

    private class Pipeline implements Function<T, CompletableFuture<T>> {
      private CompletableFuture<T> trigger = completableFutureFactory.getInstance();
      private CompletableFuture<T> result = this.trigger;
      private Function<T, CompletableFuture<T>> callBack = null;
      boolean finished = false;

      void setCallback(Function<T, CompletableFuture<T>> fn) {
        this.callBack = fn;
      }

      void chain(Execution<T> fn) {
        this.result = result.thenApply(t -> {
          logger.info("{} executes", fn.getId().toString());
          T result = fn.apply(t);
          analytics.gatherFrom(fn);
          return result;
        });
      }

      /**
       * Wrapper method around callback. Allows chaining of callback at building although the
       * actual callback function is only supplied later on.
       *
       * @param data
       *         resulting data from this {@code Pipeline}
       */
      private CompletableFuture<T> callBack(T data) {
        if (this.callBack != null) {
          return this.callBack.apply(data);
        } else {
          logger.trace("No callback provided: leaf encountered!");
        }
        return completableFutureFactory.getCompletedInstance(data);
      }

      @Override
      public CompletableFuture<T> apply(T data) {
        trigger.complete(data);
        return finish();
      }

      CompletableFuture<T> finish() {
        if (!finished) {
          result = result.thenCompose(this::callBack);
          finished = true;
        }
        return result;
      }

    }

    private class Hub {

      private List<T> inDates = new ArrayList<>();
      private List<T> outDates = new ArrayList<>();
      private CompletableFuture<Void> trigger = completableFutureFactory.getInstance();
      private CompletableFuture<T> completion = completableFutureFactory.getCompletedInstance(null);
      private int outCount = 0;
      private int inCount = 0;
      private boolean firstOut = true;
      private Execution<T> hubExecution;

      private Hub(Execution<T> hubExecution) {
        this.hubExecution = hubExecution;
      }

      void addIn(Pipeline in, int inIndex) {
        inCount++;
        inDates.add(null);
        in.setCallback(data -> this.consume(data, inIndex));
      }

      void addOut(Pipeline out, int outIndex) {
        outCount++;
        CompletableFuture<T> x;
        if (firstOut) {
          firstOut = false;
          x = trigger.thenApply($ -> outDates.get(outIndex)).thenCompose(out);
          completion = completion.thenCombine(x, (a, b) -> b);
        } else {
          x = trigger.thenApplyAsync($ -> outDates.get(outIndex)).thenCompose(out);
          completion = completion.thenCombine(x, (a, b) -> b);
        }
      }

      /**
       * Consume a model and place it at index {@code i} in the {@code inModels}.
       *
       * @param i
       *         index the consumed model should be assigned
       * @param data
       *         model to be consumed
       */
      private synchronized CompletableFuture<T> consume(T data, int i) {
        inDates.set(i, data);
        logger.trace("Pipe gives model to hub!");
        if (--inCount == 0) {
          logger.trace("Hub executes!");
          return execute();
        }
        return completableFutureFactory.getCompletedInstance(null);
      }

      /**
       * Execute this {@code ExecutionHub}, passing all the input models to the
       * encapsulated operator and in turn passing that operators output models as input to the
       * outgoing {@code ExecutionPipeline}s.
       */
      CompletableFuture<T> execute() {
        logger.info("{} executes", hubExecution.getId().toString());
        this.outDates = hubExecution.apply(inDates);
        analytics.gatherFrom(hubExecution);
        if (outDates.size() != outCount) {
          throw new RuntimeException(
            "Unexpected number of generated output data from Plugin " + hubExecution.getId()
              + "(Expected: " + outCount + ", Actual: " + outDates.size() + ")");
        }
        trigger.complete(null);
        return completion;
      }


    }

    ExecutionGraphCompiler(ExecutionFactory<T> factory, CompletableFutureFactory completableFutureFactory) {
      this.factory = factory;
      this.completableFutureFactory = completableFutureFactory;
      this.joiner = completableFutureFactory.getCompletedInstance(null);
      this.currentPipe = new Pipeline();
    }

    Map<Resource, int[]> getDegrees() {
      Map<Resource, int[]> degrees = new HashMap<>();
      edges.forEach((key, value) -> degrees.put(key, new int[]{0, value.size()}));
      new ArrayList<>(edges.entrySet()).stream()
        .flatMap(e -> e.getValue().stream())
        .map(c -> c.toNode)
        .forEach(p -> {
          if (!degrees.containsKey(p)) {
            degrees.put(p, new int[]{1, 0});
          } else {
            degrees.get(p)[0] = degrees.get(p)[0]+1;
          }
        });
      return degrees;
    }

    void dfs(Edge edge, Resource parent, final Deque<Resource> recStack) {
      final Resource node = edge.getToNode();
      recStack.push(node);
      getEdges(node).forEach(next -> {
        if (recStack.contains(next.toNode)) {
          throw new RuntimeException("Cyclic Graph detected! Cycle in [" +
            StreamSupport.stream(((Iterable<Resource>) recStack::descendingIterator).spliterator(), false)
              .map(Resource::getLocalName)
              .reduce(":", (a, b) -> a + b + ", :") + next.toNode.getLocalName() + "]");
        }
      });
      boolean recur = !isHub(node);
      if (isHub(node) && !visitedHubs.containsKey(node)) {
        visitedHubs.put(node, createAndInitExecution(node));
        recur = true;
      }
      if (parent == null) {
        if (isHub(node)) {
          addStartHub(visitedHubs.get(node));
        } else {
          addStart(createAndInitExecution(node));
        }
      } else {
        final int fromPort = edge.getFromPort();
        final int toPort = edge.getToPort();
        final boolean parentIsHub = isHub(parent);
        final boolean nodeIsHub = isHub(node);
        if (parentIsHub && nodeIsHub) {
          Execution<T> uParent = visitedHubs.get(parent);
          Execution<T> uNode = visitedHubs.get(node);
          chainFromHubToHub(uParent, fromPort, uNode, toPort);
        } else if (parentIsHub) {
          Execution<T> uParent = visitedHubs.get(parent);
          Execution<T> uNode = createAndInitExecution(node);
          chainFromHub(uParent, fromPort, uNode);
        } else if (nodeIsHub) {
          Execution<T> uNode = visitedHubs.get(node);
          chainIntoHub(uNode, toPort);
        } else {
          Execution<T> uNode = createAndInitExecution(node);
          chain(uNode);
        }
      }
      if (recur) {
        getEdges(node).forEach(r -> dfs(r, node, recStack));
      }
      recStack.pop();
    }

    private Set<Resource> getStartNodes() {
      return degrees.entrySet().stream()
        .filter(e -> e.getValue()[0] == 0)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
    }

    private List<Edge> getEdges(Resource execution) {
      return edges.getOrDefault(execution, Collections.emptyList());
    }


    private boolean isHub(Resource execution) {
      int[] d = degrees.get(execution);
      return d[0] > 1 || d[1] > 1;
    }

    Execution<T> createAndInitExecution(Resource executionId) {
      Execution<T> execution = factory.create(executionId);
      if (execution instanceof Node) {
        int[] d = degrees.get(executionId);
        ((Node) execution).init(executionId, d[0], d[1]);
      } else if (execution instanceof Plugin) {
        ((Plugin) execution).init(executionId);
      }
      if (execution instanceof Parametrized) {
        ParameterMap parameterMap = ((Parametrized) execution).createParameterMap();
        parameterMap.init(executionId);
        ((Parametrized) execution).init(parameterMap);
      }
      return execution;
    }

    @NotNull
    ExecutionGraph.ExecutionGraphCompiler addStart(@NotNull Execution<T> execution) {
      currentPipe = new Pipeline();
      currentPipe.chain(execution);
      startPipelines.add(currentPipe);
      return this;
    }

    @NotNull
    ExecutionGraphCompiler addStartHub(@NotNull Execution<T> hubExecution) {
      Hub hub = new Hub(hubExecution);
      hubs.put(hubExecution.getId(), hub);
      startHubs.add(hub);
      return this;
    }

    @NotNull
    ExecutionGraphCompiler chain(@NotNull Execution<T> execution) {
      currentPipe.chain(execution);
      return this;
    }

    ExecutionGraphCompiler chainIntoHub(@NotNull Execution<T> to, int toPort) {
      if (!hubs.containsKey(to.getId())) {
        hubs.put(to.getId(), new Hub(to));
      }
      hubs.get(to.getId()).addIn(currentPipe, toPort);
      currentPipe = new Pipeline();
      return this;
    }

    ExecutionGraphCompiler chainFromHub(@NotNull Execution<T> from, int fromPort, @NotNull Execution<T> execution) {
      if (!hubs.containsKey(from.getId())) {
        throw new IllegalStateException("Hub needs to be declared before outgoing edges can be made");
      }
      currentPipe = new Pipeline();
      currentPipe.chain(execution);
      hubs.get(from.getId()).addOut(currentPipe, fromPort);
      return this;
    }

    @NotNull ExecutionGraphCompiler chainFromHubToHub(@NotNull Execution<T> from, int fromPort, @NotNull Execution<T> to, int toPort) {
      if (!hubs.containsKey(from.getId())) {
        throw new IllegalStateException("Hub needs to be declared before outgoing edges can be made");
      }
      if (!hubs.containsKey(to.getId())) {
        hubs.put(to.getId(), new Hub(to));
      }
      currentPipe = new Pipeline();
      hubs.get(to.getId()).addIn(currentPipe, toPort);
      hubs.get(from.getId()).addOut(currentPipe, fromPort);
      return this;
    }

    Supplier<Analytics> compile() {
      analytics = new Analytics();
      final CompletableFuture<T> trigger = completableFutureFactory.getInstance();
      final Set<Resource> startNodes = getStartNodes();
      startNodes.forEach(node -> dfs(new Edge(0, 0, node), null, new ArrayDeque<>()));
      final Consumer<CompletableFuture<T>> addToJoiner =
        future -> joiner = joiner.thenCombine(future, (a, b) -> b);
      startPipelines.stream()
        .map(trigger::thenComposeAsync)
        .forEach(addToJoiner);
      startHubs.stream()
        .map(hub -> trigger.thenComposeAsync(($ -> hub.execute())))
        .forEach(addToJoiner);
      return () -> {
        trigger.complete(null);
        joiner.join();
        if (joiner.isCompletedExceptionally()) {
          try {
            joiner.get();
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e.getCause());
          }
        }
        return analytics;
      };
    }
  }

}
