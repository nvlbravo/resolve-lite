package resolvelite.semantics.symbol;

import org.antlr.v4.runtime.ParserRuleContext;
import resolvelite.semantics.Type;

/**
 * Encapsulates operation declarations, procedures, and facilityoperations.
 */
public class FunctionSymbol extends SymbolWithScope implements TypedSymbol {
    protected ParserRuleContext tree;
    protected Type retType;

    public FunctionSymbol(String name, ParserRuleContext tree) {
        super(name);
        this.tree = tree;
    }

    @Override public Type getType() {
        return retType;
    }

    @Override public void setType(Type type) {
        retType = type;
    }

}