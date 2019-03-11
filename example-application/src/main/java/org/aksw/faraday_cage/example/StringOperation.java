package org.aksw.faraday_cage.example;

import org.aksw.faraday_cage.engine.ExecutionGraphNode;
import org.aksw.faraday_cage.engine.Parameterized;

/**
 * Utility interface to reference ExecutionGraphNode<String> in an easy static fashion
 */
public interface StringOperation extends ExecutionGraphNode<String>, Parameterized {}