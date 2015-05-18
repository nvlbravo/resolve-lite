package org.resolvelite.semantics;

import org.resolvelite.typereasoning.TypeGraph;

import java.util.Collections;
import java.util.List;

/**
 * Represents a type that is simply a named reference to some bound variable.
 * For example, in {@code BigUnion t : MType}{t}}, the second {@code t} is a
 * named type.
 */
public class MTNamed extends MTType {

    private final static int BASE_HASH = "MTNamed".hashCode();

    public final String name;

    public MTNamed(TypeGraph g, String name) {
        super(g);
        this.name = name;
    }

    @Override public List<MTType> getComponentTypes() {
        return Collections.emptyList();
    }

    @Override public MTType withComponentReplaced(int index, MTType newType) {
        throw new IndexOutOfBoundsException();
    }

    @Override public String toString() {
        return "'" + name + "'";
    }

    @Override public int getHashCode() {
        return BASE_HASH;
    }

    @Override public void acceptOpen(TypeVisitor v) {
        v.beginMTType(this);
        v.beginMTNamed(this);
    }

    @Override public void accept(TypeVisitor v) {
        acceptOpen(v);

        v.beginChildren(this);
        v.endChildren(this);

        acceptClose(v);
    }

    @Override public void acceptClose(TypeVisitor v) {
        v.endMTNamed(this);
        v.endMTType(this);
    }

}