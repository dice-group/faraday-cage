package org.aksw.faraday_cage.engine;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.jena.rdf.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;


/**
 * A {@code ValidatableParameterMap} serves as registry of parameters that can be used
 * to configure a {@code ValidatableParameterMap}.
 * <p>
 * A {@code ParameterMap} holds a list of {@link Property} instances upon its
 * initialization, therefore acting as a registry of allowed {@code Property}s.
 * <br>
 * Furthermore, a {@code ValidatableParameterMap} supports setting values of its parameters
 * either {@link #add(Property, RDFNode) directly} or {@link #populate(Resource) automatically}.
 * <br>
 * Calling {@link #init()}} on a given {@code ValidatableParameterMap} will change its state to
 * initialized and prevent further modification of its values, effectively making it immutable.
 * It is mandatory to call {@link #init()} before obtaining values from a {@code ValidatableParameterMap}.
 */
public class ValidatableParameterMap {

  @NotNull
  public static ValidatableParameterMap emptyInstance() {
    return new ValidatableParameterMap(List.of(), ModelFactory.createDefaultModel());
  }

  /**
   * Obtain a builder instance
   * @return a fresh builder instance
   */
  @NotNull
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for {@code ValidatableParameterMap}
   */
  public static class Builder {

    @NotNull
    private List<Property> propertyList = new ArrayList<>();

    @NotNull
    private Model validationModel = ModelFactory.createDefaultModel();

    private Builder() {}

    /**
     * Declare a property to be allowed
     *
     * @param property   property declared
     * @return this instance for method chaining
     */
    @NotNull
    public Builder declareProperty(Property property) {
      propertyList.add(property);
      return this;
    }
    /**
     * Declare a validationShape
     *
     * @param validationShape  a Supplier of a SHACL graph
     * @return this instance for method chaining
     */
    @NotNull
    public Builder declareValidationShape(Model validationShape) {
      validationModel.add(validationShape);
      return this;
    }

    /**
     * Build an {@code ValidatableParameterMap} instance from the current state of the Builder
     * @return a fresh {@code ValidatableParameterMap} instance
     */
    @NotNull
    public ValidatableParameterMap build() {
      return new ValidatableParameterMap(propertyList, validationModel);
    }

  }

  private final Model validationModel;

  @NotNull
  private final List<Property> declaredProperties;

  @NotNull
  private final Multimap<Property, RDFNode> rootMap;

  private final Model backingModel = ModelFactory.createDefaultModel();

  private boolean initialized = false;

  /**
   * Private Constructor
   */
  private ValidatableParameterMap(@NotNull List<Property> declaredProperties, Model validationModel) {
    this.validationModel = validationModel;
    this.declaredProperties = declaredProperties;
    rootMap = ArrayListMultimap.create(declaredProperties.size(), 1);
  }

  /**
   * Populate the ValidatableParameterMap with data taken from the {@code Model} attached to the
   * given {@code Resource}.
   *
   * @param root  this {@code Resource} needs to be backed by a {@code Model}
   */
  @NotNull
  public ValidatableParameterMap populate(@NotNull Resource root) {
    checkWritable();
    declaredProperties.stream()
      .filter(root::hasProperty)
      .map(root::listProperties)
      .forEach(this::add);
    return this;
  }

  /**
   * Add all remaining {@link Statement}s from a given {@link StmtIterator} to this {@code ValidatableParameterMap}
   */
  @NotNull
  public ValidatableParameterMap add(@NotNull StmtIterator it) {
    checkWritable();
    it.forEachRemaining(stmt -> add(stmt.getPredicate(), stmt.getObject()));
    return this;
  }

  /**
   * Add a Statement to this {@code ValidatableParameterMap} by specifying its predicate and object.
   * If the object is not a literal, its subgraph is also added to the {@link Model} backing
   * this {@code ValidatableParameterMap} by depth-first search.
   * Infinite loops from cyclical subgraphs are prevented by maintaining a set of visited resources.
   */
  @NotNull
  public ValidatableParameterMap add(@NotNull Property p, @NotNull RDFNode node) {
    checkWritable();
    Deque<RDFNode> stack = new ArrayDeque<>();
    Set<Resource> visited = new HashSet<>();
    rootMap.put(p.inModel(backingModel), node.inModel(backingModel));
    stack.push(node);
    while (!stack.isEmpty()) {
      RDFNode n = stack.pop();
      if (n.isResource() && !visited.contains(n.asResource())) {
        Resource r = n.asResource();
        r.listProperties().forEachRemaining(stmt -> {
          backingModel.add(stmt);
          stack.push(stmt.getObject());
        });
        visited.add(r);
      }
    }
    return this;
  }

  /**
   * Initializes this {@code ValidatableParameterMap}.
   *
   * Initializing effectively disables all writing operations (attempting to write will result in exceptions)
   * and enables all reading operations.
   */
  @NotNull
  public ValidatableParameterMap init() {
    checkWritable();
    this.initialized = true;
    return this;
  }

  /**
   * Get a stream of objects for the given property.
   */
  public Stream<RDFNode> listPropertyObjects(Property p) {
    checkReadable();
    return rootMap.get(p).stream();
  }

  /**
   * Get the first object for a given property as an {@code Optional}.
   */
  @NotNull
  public Optional<RDFNode> getOptional(Property p) {
    checkReadable();
    return listPropertyObjects(p).findFirst();
  }

  /**
   * Get the first object for a given property as an {@code Optional}.
   */
  @NotNull
  public RDFNode get(Property p) {
    checkReadable();
    //noinspection OptionalGetWithoutIsPresent
    return listPropertyObjects(p).findFirst().get();
  }

  /**
   * Apply the parameters stored in this {@code ValidatableParameterMap} to the given
   * {@code Resource} representing a {@link Parameterized}.
   *
   * @return the parameter graph for the given {@code Resource}
   */
  public Model parametrize(@NotNull Resource exNode) {
    checkReadable();
    Model parameterModel = ModelFactory.createDefaultModel().add(backingModel);
    final Resource r = exNode.inModel(parameterModel);
    rootMap.forEach((p, o) -> parameterModel.add(r, p, o));
    return parameterModel;
  }

  public Model getValidationModel() {
    return validationModel;
  }

  private void checkWritable() {
    if (initialized) {
      throw new IllegalStateException("This ParameterMap has already been initialized, writing " +
        "operations are therefore prohibited!");
    }
  }

  private void checkReadable() {
    if (!initialized) {
      throw new IllegalStateException("This ParameterMap has not yet been initialized, reading " +
        "operations are therefore prohibited!");
    }
  }

}
