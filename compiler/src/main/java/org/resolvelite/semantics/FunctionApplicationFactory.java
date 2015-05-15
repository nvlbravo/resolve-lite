package org.resolvelite.semantics;

import java.util.List;

public interface FunctionApplicationFactory {

    public MTType buildFunctionApplication(TypeGraph g, MTFunction f,
            String calledAsName, List<MTType> arguments);
}