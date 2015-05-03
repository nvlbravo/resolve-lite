package org.resolvelite.semantics;

import org.resolvelite.typereasoning.TypeGraph;

import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

public class MTInvalid extends MTType {

    private static WeakHashMap<TypeGraph, MTInvalid> instances =
            new WeakHashMap<>();

    public static MTInvalid getInstance(TypeGraph g) {
        MTInvalid result = instances.get(g);
        if ( result == null ) {
            result = new MTInvalid(g);
            instances.put(g, result);
        }
        return result;
    }

    public String getName() {
        return "Invalid";
    }

    private MTInvalid(TypeGraph g) {
        super(g);
    }

    @Override public List<? extends MTType> getComponentTypes() {
        return Collections.emptyList();
    }

    @Override public void acceptOpen(TypeVisitor v) {
        v.beginMTType(this);
        v.beginMTInvalid(this);
    }

    @Override public void accept(TypeVisitor v) {
        acceptOpen(v);

        v.beginChildren(this);
        v.endChildren(this);

        acceptClose(v);
    }

    @Override public void acceptClose(TypeVisitor v) {
        v.endMTInvalid(this);
        v.endMTType(this);
    }
}