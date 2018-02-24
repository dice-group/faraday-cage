package org.aksw.faraday_cage.execution.graph;

import org.aksw.faraday_cage.concurrent.CompletableFutureFactory;
import org.aksw.faraday_cage.execution.Execution;
import org.aksw.faraday_cage.execution.HubExecution;
import org.aksw.faraday_cage.plugin.Identifiable;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 *
 *
 */
public class DefaultExecutionGraphBuilder<U extends Execution<T> & Identifiable, V extends HubExecution<T> & Identifiable, T> implements ExecutionGraphBuilder<U, V> {

  private static final Logger logger = LoggerFactory.getLogger(DefaultExecutionGraphBuilder.class);

  private List<Pipeline> startPipelines = new ArrayList<>();
  private List<Hub> startHubs = new ArrayList<>();
  private Map<Resource, Hub> hubs = new HashMap<>();
  private Pipeline currentPipe = new Pipeline();
  private CompletableFutureFactory completableFutureFactory;
  private CompletableFuture<T> trigger;
  private CompletableFuture<T> joiner;

  private class Pipeline implements Function<T, CompletableFuture<T>> {
    private CompletableFuture<T> trigger = completableFutureFactory.getInstance();
    private CompletableFuture<T> result = this.trigger;
    private Function<T, CompletableFuture<T>> callBack = null;
    boolean finished = false;

    void setCallback(Function<T, CompletableFuture<T>> fn) {
      this.callBack = fn;
    }

    void chain(U fn) {
      this.result = result.thenApply(fn::apply);
    }

    /**
     * Wrapper method around callback. Allows chaining of callback at building although the
     * actual callback function is only supplied later on.
     *
     * @param data resulting data from this {@code Pipeline}
     */
    private CompletableFuture<T> callBack(T data) {
      if (this.callBack != null) {
        return this.callBack.apply(data);
      } else {
        logger.info("No callback provided: leaf encountered!");
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
    private V hubExecution;

    private Hub(V hubExecution) {
      this.hubExecution = hubExecution;
    }

    void addIn(Pipeline in, int inIndex) {
      inCount++;
      inDates.add(null);
      in.setCallback(data -> this.consume(data, inIndex));
    }

    void addOut(Pipeline out, int outIndex) {
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
     * @param i  index the consumed model should be assigned
     * @param data  model to be consumed
     */
    private synchronized CompletableFuture<T> consume(T data, int i) {
      inDates.set(i, data);
      logger.info("Pipe gives model to hub!");
      if (--inCount == 0) {
        logger.info("Hub executes!");
        return execute();
      }
      return CompletableFutureFactory.getCompleted(null);
    }

    /**
     * Execute this {@code ExecutionHub}, passing all the input models to the
     * encapsulated operator and in turn passing that operators output models as input to the
     * outgoing {@code ExecutionPipeline}s.
     */
    public CompletableFuture<T> execute() {
      this.outDates = hubExecution.apply(inDates);
      if (outDates.size() != outCount) {
        throw new RuntimeException(
          "Unexpected number of generated output data from Plugin " + hubExecution.getId()
            + "(Expected: " + outCount + ", Actual: " + outDates.size() + ")");
      }
      trigger.complete(null);
      return completion;
    }


  }

  public DefaultExecutionGraphBuilder(CompletableFutureFactory completableFutureFactory) {
    this.completableFutureFactory = completableFutureFactory;
    this.trigger = completableFutureFactory.getInstance();
    this.joiner = completableFutureFactory.getCompletedInstance(null);
  }

  @NotNull
  @Override
  public ExecutionGraphBuilder addStart(@NotNull U execution) {
    currentPipe = new Pipeline();
    currentPipe.chain(execution);
    startPipelines.add(currentPipe);
    return this;
  }

  @NotNull
  @Override
  public ExecutionGraphBuilder addStartHub(@NotNull V hubExecution) {
    Hub hub = new Hub(hubExecution);
    hubs.put(hubExecution.getId(), hub);
    startHubs.add(hub);
    return this;
  }

  @NotNull
  @Override
  public ExecutionGraphBuilder chain(@NotNull U execution) {
    currentPipe.chain(execution);
    return this;
  }

  @Override
  public ExecutionGraphBuilder chainIntoHub(@NotNull V to, int toPort) {
    if (!hubs.containsKey(to.getId())){
      hubs.put(to.getId(), new Hub(to));
    }
    hubs.get(to.getId()).addIn(currentPipe, toPort);
    currentPipe = new Pipeline();
    return this;
  }

  @Override
  public ExecutionGraphBuilder chainFromHub(@NotNull V from, int fromPort, @NotNull U execution) {
    if (!hubs.containsKey(from.getId())){
      throw new IllegalStateException("Hub needs to be declared before outgoing connections can be made");
    }
    currentPipe = new Pipeline();
    currentPipe.chain(execution);
    hubs.get(from.getId()).addOut(currentPipe, fromPort);
    return this;
  }

  @NotNull
  @Override
  public ExecutionGraphBuilder chainFromHubToHub(@NotNull V from, int fromPort, @NotNull V to, int toPort) {
    if (!hubs.containsKey(from.getId())){
      throw new IllegalStateException("Hub needs to be declared before outgoing connections can be made");
    }
    if (!hubs.containsKey(to.getId())){
      hubs.put(to.getId(), new Hub(to));
    }
    currentPipe = new Pipeline();
    hubs.get(to.getId()).addIn(currentPipe, toPort);
    hubs.get(from.getId()).addOut(currentPipe, fromPort);
    return this;
  }

  @NotNull
  @Override
  public ExecutionGraph build() {

    final Consumer<CompletableFuture<T>> addToJoiner =
      future -> joiner = joiner.thenCombine(future, (a,b) -> b);

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
        } catch (Exception e) {
          throw new RuntimeException(e.getCause());
        }
      }
    };
  }


}
