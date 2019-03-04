package org.aksw.faraday_cage.engine;

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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 */
class ExecutionGraphCompiler<T> {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionGraphCompiler.class);

  private final Set<ExecutionGraphNode<T>> visitedHubs = new HashSet<>();
  private final List<ExecutionPipeline> startPipelines = new ArrayList<>();
  private final List<ExecutionHub> startHubs = new ArrayList<>();
  private final Map<ExecutionGraphNode<T>, ExecutionHub> hubs = new HashMap<>();
  private final Map<ExecutionGraphNode<T>, List<ExecutionGraph.Edge<T>>> edges;
  private final Map<ExecutionGraphNode<T>, int[]> degrees;
  private ExecutionPipeline currentPipe;
  private CompletableFuture<T> joiner;

  private static String optionalShortFormOf(Resource resource) {
    return Objects.nonNull(resource.getModel()) ?
      resource.getModel().shortForm(resource.getURI()) :
      resource.getURI();
  }

  private class ExecutionPipeline implements Function<T, CompletableFuture<T>> {
    private CompletableFuture<T> trigger = new ThreadlocalInheritingCompletableFuture<>();
    private CompletableFuture<T> result = this.trigger;
    private Function<T, CompletableFuture<T>> callBack = null;

    void setCallback(Function<T, CompletableFuture<T>> fn) {
      this.callBack = fn;
    }

    void chain(ExecutionGraphNode<T> fn) {
      this.result = result.thenApply(t -> {
        logger.info("{} executes", fn.getId().toString());
        List<T> result = fn.apply(t == null ? List.of() : List.of(t));
        if (!result.isEmpty()) {
          return result.get(0);
        } else if (fn.getOutDegree() == 0) {
          return null;
        } else {
          throw new RuntimeException("Got null reference in non-leaf node.");
        }
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
      return ThreadlocalInheritingCompletableFuture.completedFuture(data);
    }

    @Override
    public CompletableFuture<T> apply(T data) {
      trigger.complete(data);
      try {
        T t = result.get();
        return callBack(t);
      } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

  }

  private class ExecutionHub {

    private List<T> inDates = new ArrayList<>();
    private List<T> outDates = new ArrayList<>();
    private CompletableFuture<Void> trigger = new ThreadlocalInheritingCompletableFuture<>();
    private CompletableFuture<T> completion = ThreadlocalInheritingCompletableFuture.completedFuture(null);
    private int outCount = 0;
    private int inCount = 0;
    private boolean firstOut = true;
    private ExecutionGraphNode<T> hubExecutionGraphNode;

    private ExecutionHub(ExecutionGraphNode<T> hubExecutionGraphNode) {
      this.hubExecutionGraphNode = hubExecutionGraphNode;
    }

    void addIn(ExecutionPipeline in, int inIndex) {
      inCount++;
      inDates.add(null);
      in.setCallback(data -> this.consume(data, inIndex));
    }

    void addOut(ExecutionPipeline out, int outIndex) {
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
      return ThreadlocalInheritingCompletableFuture.completedFuture(null);
    }

    /**
     * Execute this {@code ExecutionHub}, passing all the input models to the
     * encapsulated operator and in turn passing that operators output models as input to the
     * outgoing {@code ExecutionPipeline}s.
     */
    CompletableFuture<T> execute() {
      logger.info("{} executes", hubExecutionGraphNode.getId().toString());
      this.outDates = hubExecutionGraphNode.apply(inDates);
      if (outDates.size() != outCount) {
        throw new RuntimeException(
          "Unexpected number of generated output data from ExecutionNode " + hubExecutionGraphNode.getId()
            + "(Expected: " + outCount + ", Actual: " + outDates.size() + ")");
      }
      trigger.complete(null);
      return completion;
    }


  }

  private void validateEdges() {
    BiConsumer<HashMap<ExecutionGraphNode<T>, SortedSet<Integer>>, String> checkPortNumbers = (ports, dir) -> ports.forEach((k, v) -> {
      int j = -1;
      for (Integer i : v) {
        if (i != ++j) {
          throw new InvalidExecutionGraphException("Error in " + k + ": missing " + dir + "port #" + j + "! There were " + v.size() + " ports declared.");
        }
      }
    });
    HashMap<ExecutionGraphNode<T>, SortedSet<Integer>> outPorts = new HashMap<>();
    edges.forEach((key, value) -> value.forEach(c -> {
      SortedSet<Integer> set = (outPorts.containsKey(key)) ? outPorts.get(key) : new TreeSet<>();
      set.add(c.getFromPort());
      outPorts.put(key, set);
    }));
    checkPortNumbers.accept(outPorts, "out");
    HashMap<ExecutionGraphNode<T>, SortedSet<Integer>> inPorts = new HashMap<>();
    edges.values().stream()
      .flatMap(Collection::stream)
      .forEach(c -> {
        SortedSet<Integer> set = (inPorts.containsKey(c.getToNode())) ? inPorts.get(c.getToNode()) : new TreeSet<>();
        set.add(c.getToPort());
        inPorts.put(c.getToNode(), set);
      });
    checkPortNumbers.accept(inPorts, "in");
  }

  ExecutionGraphCompiler(Map<ExecutionGraphNode<T>, List<ExecutionGraph.Edge<T>>> edges) {
    this.edges = edges;
    validateEdges();
    this.degrees = getDegrees();
    this.joiner = ThreadlocalInheritingCompletableFuture.completedFuture(null);
    this.currentPipe = new ExecutionPipeline();
  }

  private Map<ExecutionGraphNode<T>, int[]> getDegrees() {
    Map<ExecutionGraphNode<T>, int[]> degrees = new HashMap<>();
    // counting out degree is trivial
    edges.forEach((key, value) -> degrees.put(key, new int[]{0, value.size()}));
    // counting in degree is harder
    new ArrayList<>(edges.entrySet()).stream()
      .flatMap(e -> e.getValue().stream())
      .map(ExecutionGraph.Edge::getToNode)
      .forEach(p -> {
        if (!degrees.containsKey(p)) {
          degrees.put(p, new int[]{1, 0});
        } else {
          degrees.get(p)[0] = degrees.get(p)[0]+1;
        }
      });
    return degrees;
  }

  private void dfs(ExecutionGraph.Edge<T> edge, ExecutionGraphNode<T> parent, final Deque<ExecutionGraphNode<T>> recStack) {
    final ExecutionGraphNode<T> node = edge.getToNode();
    recStack.push(node);
    getEdges(node).forEach(next -> {
      if (recStack.contains(next.getToNode())) {
        throw new InvalidExecutionGraphException(
          "Cyclic Graph detected! Cycle in [" +
          StreamSupport.stream(((Iterable<ExecutionGraphNode<T>>) recStack::descendingIterator).spliterator(), false)
            .dropWhile(r -> !next.getToNode().equals(r))
            .map(Plugin::getId)
            .map(ExecutionGraphCompiler::optionalShortFormOf)
            .reduce("", (a, b) -> a + b + ", ") + ExecutionGraphCompiler.optionalShortFormOf(next.getToNode().getId()) +
          "]"
        );
      }
    });
    boolean recur = !isHub(node);
    if (isHub(node) && !visitedHubs.contains(node)) {
      visitedHubs.add(initDegrees(node));
      recur = true;
    }
    if (parent == null) {
      if (isHub(node)) {
        addStartHub(node);
      } else {
        addStart(initDegrees(node));
      }
    } else {
      final int fromPort = edge.getFromPort();
      final int toPort = edge.getToPort();
      final boolean parentIsHub = isHub(parent);
      final boolean nodeIsHub = isHub(node);
      if (parentIsHub && nodeIsHub) {
        chainFromHubToHub(parent, fromPort, node, toPort);
      } else if (parentIsHub) {
        chainFromHub(parent, fromPort, initDegrees(node));
      } else if (nodeIsHub) {
        chainIntoHub(node, toPort);
      } else {
        chain(initDegrees(node));
      }
    }
    if (recur) {
      getEdges(node).forEach(r -> dfs(r, node, recStack));
    }
    recStack.pop();
  }

  private Set<ExecutionGraphNode<T>> getStartNodes() {
    return degrees.entrySet().stream()
      .filter(e -> e.getValue()[0] == 0)
      .map(Map.Entry::getKey)
      .collect(Collectors.toSet());
  }

  private List<ExecutionGraph.Edge<T>> getEdges(ExecutionGraphNode<T> execution) {
    return edges.getOrDefault(execution, Collections.emptyList());
  }


  private boolean isHub(ExecutionGraphNode<T> execution) {
    int[] d = degrees.get(execution);
    return d[0] > 1 || d[1] > 1;
  }

  private ExecutionGraphNode<T> initDegrees(ExecutionGraphNode<T> execution) {
    int[] d = degrees.get(execution);
    execution.initDegrees(d[0], d[1]);
    return execution;
  }

  private void addStart(@NotNull ExecutionGraphNode<T> executionGraphNode) {
    currentPipe = new ExecutionPipeline();
    currentPipe.chain(executionGraphNode);
    startPipelines.add(currentPipe);
  }

  private void addStartHub(@NotNull ExecutionGraphNode<T> hubExecutionGraphNode) {
    ExecutionHub hub = new ExecutionHub(hubExecutionGraphNode);
    hubs.put(hubExecutionGraphNode, hub);
    startHubs.add(hub);
  }

  private void chain(@NotNull ExecutionGraphNode<T> executionGraphNode) {
    currentPipe.chain(executionGraphNode);
  }

  private void chainIntoHub(@NotNull ExecutionGraphNode<T> to, int toPort) {
    if (!hubs.containsKey(to)) {
      hubs.put(to, new ExecutionHub(to));
    }
    hubs.get(to).addIn(currentPipe, toPort);
    currentPipe = new ExecutionPipeline();
  }

  private void chainFromHub(@NotNull ExecutionGraphNode<T> from, int fromPort, @NotNull ExecutionGraphNode<T> executionGraphNode) {
    if (!hubs.containsKey(from)) {
      throw new IllegalStateException("Hub needs to be declared before outgoing edges can be created");
    }
    currentPipe = new ExecutionPipeline();
    currentPipe.chain(executionGraphNode);
    hubs.get(from).addOut(currentPipe, fromPort);
  }

  private void chainFromHubToHub(@NotNull ExecutionGraphNode<T> from, int fromPort, @NotNull ExecutionGraphNode<T> to, int toPort) {
    if (!hubs.containsKey(from)) {
      throw new IllegalStateException("Hub needs to be declared before outgoing edges can be created");
    }
    if (!hubs.containsKey(to)) {
      hubs.put(to, new ExecutionHub(to));
    }
    currentPipe = new ExecutionPipeline();
    hubs.get(to).addIn(currentPipe, toPort);
    hubs.get(from).addOut(currentPipe, fromPort);
  }

  CompiledExecutionGraph compile(String runId) {
    final CompletableFuture<T> trigger = new ThreadlocalInheritingCompletableFuture<>();
    final Set<ExecutionGraphNode<T>> startNodes = getStartNodes();
    if (startNodes.isEmpty()) {
      throw new InvalidExecutionGraphException("No root nodes have been detected. Please supply a non-empty acyclic configuration graph!");
    }
    startNodes.forEach(node -> dfs(new ExecutionGraph.Edge<>(0, 0, node), null, new ArrayDeque<>()));
    final Consumer<CompletableFuture<T>> addToJoiner =
      future -> joiner = joiner.thenCombine(future, (a, b) -> b);
    startPipelines.stream()
      .map(trigger::thenComposeAsync)
      .forEach(addToJoiner);
    startHubs.stream()
      .map(hub -> trigger.thenComposeAsync(($ -> hub.execute())))
      .forEach(addToJoiner);
    return new CompiledExecutionGraph(runId, trigger, joiner);
  }
}
