package org.resolvelite.semantics;

import org.antlr.v4.runtime.tree.ParseTree;
import org.resolvelite.semantics.symbol.MathSymbol;
import org.resolvelite.semantics.symbol.Symbol;
import org.resolvelite.typereasoning.TypeGraph;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@code ScopeBuilder} is a working, mutable realization of {@link Scope}.
 * 
 * Note that {@code ScopeBuilder} has no public constructor. Instances of this
 * class can be acquired through calls to some of the methods of
 * {@link SymbolTable}.
 */
public class ScopeBuilder extends BaseScope {

    private final TypeGraph typeGraph;

    ScopeBuilder(SymbolTable s, TypeGraph g, ParseTree definingTree,
            Scope parent, String moduleID) {
        super(s, definingTree, parent, moduleID, new HashMap<>());
        this.typeGraph = g;
    }

    void setParent(Scope parent) {
        this.parent = parent;
    }

    public MathSymbol addBinding(String name,
                Symbol.Quantification q, ParseTree definingTree, MTType type,
                MTType typeValue)
            throws DuplicateSymbolException {

        MathSymbol entry =
                new MathSymbol(typeGraph, name, q, definingTree,
                        type, typeValue, moduleID);
        symbols.put(name, entry);
        return entry;
    }

    public MathSymbol addBinding(String name,
                                      Symbol.Quantification q,
                                      ParseTree definingTree, MTType type)
            throws DuplicateSymbolException {
        return addBinding(name, q, definingTree, type, null);
    }

    public MathSymbol addBinding(String name,
                                      ParseTree definingTree, MTType type,
                                      MTType typeValue)
            throws DuplicateSymbolException {

        return addBinding(name, Symbol.Quantification.NONE,
                definingTree, type, typeValue);
    }

    public MathSymbol addBinding(String name, ParseTree definingTree,
                                 MTType type)
            throws DuplicateSymbolException {
        return addBinding(name, Symbol.Quantification.NONE, definingTree, type);
    }

    public MathSymbol addBinding(String name, MTType type, MTType typeValue)
            throws DuplicateSymbolException {
        return addBinding(name, null, type, typeValue);
    }
}
