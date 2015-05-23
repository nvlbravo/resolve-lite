package org.resolvelite.semantics.programtype;

import org.resolvelite.semantics.MTNamed;
import org.resolvelite.semantics.MTType;
import org.resolvelite.semantics.symbol.FacilitySymbol;
import org.resolvelite.typereasoning.TypeGraph;

import java.util.Map;

public class PTGeneric extends PTType {

    private final String name;

    public PTGeneric(TypeGraph g, String name) {
        super(g);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override public MTType toMath() {
        return new MTNamed(getTypeGraph(), name);
    }

    @Override public PTType instantiateGenerics(
            Map<String, PTType> genericInstantiations,
            FacilitySymbol instantiatingFacility) {
        PTType result = this;
        if ( genericInstantiations.containsKey(name) ) {
            result = genericInstantiations.get(name);
        }
        return result;
    }

    @Override public boolean equals(Object o) {
        boolean result = (o instanceof PTGeneric);

        if ( result ) {
            PTGeneric oAsPTGeneric = (PTGeneric) o;
            result = name.equals(oAsPTGeneric.getName());
        }
        return result;
    }

    @Override public String toString() {
        return name;
    }

}