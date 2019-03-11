package org.aksw.faraday_cage.example;

import org.aksw.faraday_cage.decorator.ExecutionNodeWrapper;

/**
 * Utility interface to reference ExecutionNodeWrapper<StringOperation, String> in an easy static fashion
 */
public interface StringOperationWrapper extends ExecutionNodeWrapper<StringOperation, String> {}