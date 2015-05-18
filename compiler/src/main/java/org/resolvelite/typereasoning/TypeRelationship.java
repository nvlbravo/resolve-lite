package org.resolvelite.typereasoning;

import org.resolvelite.proving.absyn.PExp;
import org.resolvelite.semantics.BindingException;
import org.resolvelite.semantics.MTType;
import org.resolvelite.semantics.NoSolutionException;
import org.resolvelite.semantics.TypeMismatchException;

import java.util.*;

public class TypeRelationship {

    private final TypeGraph typeGraph;
    private final MTType destinationType;
    private final BindingExpression bindingExpression;

    public TypeRelationship(TypeGraph typeGraph, MTType destinationType,
            PExp bindingExpression) {

        this.typeGraph = typeGraph;
        this.destinationType = destinationType;
        this.bindingExpression =
                new BindingExpression(typeGraph, bindingExpression);
    }

    public MTType getSourceType() {
        return bindingExpression.getType();
    }

    public MTType getDestinationType() {
        return destinationType;
    }

    public String toString() {
        return "destination: " + destinationType + "\nbindingexpression: "
                + bindingExpression;
    }

    public String getBindingExpressionString() {
        return bindingExpression.toString();
    }

    public boolean hasTrivialBindingCondition() {
        return true;
    }

    public PExp getValidTypeConditionsTo(MTType value,
            Map<String, MTType> typeBindings) throws NoSolutionException {
        //TODO: stub for now.
        return typeGraph.getTrueExp();
    }

    public PExp getValidTypeConditionsTo(PExp value,
            Map<String, MTType> typeBindings) throws NoSolutionException {
        //TODO: stub for now.
        return typeGraph.getTrueExp();
    }
}
