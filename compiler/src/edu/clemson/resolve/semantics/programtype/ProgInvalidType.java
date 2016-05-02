package edu.clemson.resolve.semantics.programtype;

import org.jetbrains.annotations.NotNull;
import edu.clemson.resolve.semantics.DumbTypeGraph;
import edu.clemson.resolve.semantics.MathClassification;
import edu.clemson.resolve.semantics.symbol.FacilitySymbol;

import java.util.Map;
import java.util.WeakHashMap;

public class ProgInvalidType extends ProgType {

    @NotNull
    private static WeakHashMap<DumbTypeGraph, ProgInvalidType> instances =
            new WeakHashMap<>();

    @NotNull
    public static ProgInvalidType getInstance(@NotNull DumbTypeGraph g) {
        ProgInvalidType result = instances.get(g);
        if (result == null) {
            result = new ProgInvalidType(g);
            instances.put(g, result);
        }
        return result;
    }

    private ProgInvalidType(@NotNull DumbTypeGraph g) {
        super(g);
    }

    @NotNull
    @Override
    public MathClassification toMath() {
        return getTypeGraph().INVALID;
    }

    @Override
    public String toString() {
        return "Invalid";
    }

    @NotNull
    @Override
    public ProgType instantiateGenerics(
            @NotNull Map<String, ProgType> genericInstantiations,
            @NotNull FacilitySymbol instantiatingFacility) {
        return this;
    }

}
