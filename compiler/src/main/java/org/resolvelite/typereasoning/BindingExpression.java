package org.resolvelite.typereasoning;

import org.resolvelite.proving.absyn.PExp;
import org.resolvelite.proving.absyn.PSymbol;
import org.resolvelite.semantics.BindingException;
import org.resolvelite.semantics.MTType;
import org.resolvelite.semantics.TypeMismatchException;

import java.util.HashMap;
import java.util.Map;

public class BindingExpression {

    private final TypeGraph typeGraph;
    private PExp expression;

    public BindingExpression(TypeGraph g, PExp expression) {
        this.expression = expression;
        this.typeGraph = g;
    }

    public MTType getType() {
        return expression.getMathType();
    }

    public MTType getTypeValue() {
        return expression.getMathTypeValue();
    }

    @Override public String toString() {
        return expression.toString();
    }

    private MTType getTypeUnderBinding(MTType original,
            Map<String, MTType> typeBindings) {
        return original.getCopyWithVariablesSubstituted(typeBindings);
    }
}