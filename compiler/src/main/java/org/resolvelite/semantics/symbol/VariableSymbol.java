package org.resolvelite.semantics.symbol;

import org.resolvelite.semantics.Scope;

public class VariableSymbol extends BaseSymbol implements TypedSymbol {

    public VariableSymbol(String name, Scope enclosingScope) {
        super(enclosingScope, name);
    }

    @Override public String toString() {
        String s = "";
        s = scope.getScopeDescription() + ".";
        if ( type != null ) {
            return '<' + s + getName() + "." + type + '>';
        }
        return s + getName();
    }
}