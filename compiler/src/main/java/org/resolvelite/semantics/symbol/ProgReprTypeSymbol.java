package org.resolvelite.semantics.symbol;

import org.antlr.v4.runtime.tree.ParseTree;
import org.resolvelite.semantics.programtype.PTRepresentation;
import org.resolvelite.semantics.programtype.PTType;
import org.resolvelite.typereasoning.TypeGraph;

import java.util.Map;

public class ProgReprTypeSymbol extends Symbol {

    //Note: This is null in the case where we represent a standalone
    //representation (e.g.: a facility module bound record)
    protected final ProgTypeModelSymbol definition;
    protected final ParseTree convention, correspondence;
    protected final TypeGraph typeGraph;
    protected final PTRepresentation representation;

    public ProgReprTypeSymbol(TypeGraph g, String name,
            ParseTree definingElement, String moduleID,
            ProgTypeModelSymbol definition, PTRepresentation representation,
            ParseTree convention, ParseTree correspondence) {
        super(name, definingElement, moduleID);
        this.definition = definition;
        this.representation = representation;
        this.convention = convention;
        this.correspondence = correspondence;
        this.typeGraph = g;
    }

    public PTRepresentation getRepresentationType() {
        return representation;
    }

    public ProgTypeModelSymbol getDefinition() {
        return definition;
    }

    @Override public ProgTypeSymbol toProgTypeSymbol() {
        return new ProgTypeSymbol(typeGraph, getName(), representation,
                (definition == null) ? null : definition.modelType,
                getDefiningTree(), getModuleID());
    }

    @Override public ProgReprTypeSymbol toProgReprTypeSymbol() {
        return this;
    }

    @Override public String getSymbolDescription() {
        return "a program type representation definition";
    }

    @Override public String toString() {
        return getName();
    }

    @Override public Symbol instantiateGenerics(
            Map<String, PTType> genericInstantiations,
            FacilitySymbol instantiatingFacility) {

        //Representation is an internal implementation detail of a realization
        //and cannot be accessed through a facility instantiation
        throw new UnsupportedOperationException("Cannot instantiate "
                + this.getClass());
    }

}
