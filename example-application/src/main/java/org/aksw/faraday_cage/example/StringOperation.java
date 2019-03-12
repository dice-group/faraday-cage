package org.aksw.faraday_cage.example;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.Parameterized;

/**
 * Utility interface to reference ExecutionNode<String> in an easy static fashion
 */
public interface StringOperation extends ExecutionNode<String>, Parameterized {}