package org.rsrg.semantics.symbol;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rsrg.semantics.ModuleIdentifier;
import org.rsrg.semantics.UnexpectedSymbolException;
import org.rsrg.semantics.programtype.PTType;

import java.util.Map;

/** A wrapper for a 'parameter-like symbol' such as an {@link OperationSymbol},
 *  {@link ProgParameterSymbol}, or {@link MathSymbol} that happens to be
 *  functioning as a formal parameter for a module when declared.
 */
public class ModuleParameterSymbol extends Symbol {

    private final Symbol wrappedParamSymbol;

    public ModuleParameterSymbol(@NotNull Symbol symbol, @NotNull String name,
                                 @Nullable ParserRuleContext definingTree,
                                @NotNull ModuleIdentifier moduleIdentifier) {
        super(name, definingTree, moduleIdentifier);
        this.wrappedParamSymbol = symbol;
    }

    public ModuleParameterSymbol(ProgParameterSymbol p) {
        this(p, p.getName(), p.definingTree, p.getModuleIdentifier());
    }

    public ModuleParameterSymbol(MathSymbol p) {
        this(p, p.getName(), p.definingTree, p.getModuleIdentifier());
    }

    /** Returns the program type; will be {@code null} in the case where we
     *  wrap a {@code MathSymbol} (arising from a defn passed to facility).
     */
    @Nullable public PTType getProgramType() {
        PTType progType = null;
        if (wrappedParamSymbol instanceof OperationSymbol) {
            progType = ((OperationSymbol) wrappedParamSymbol).getReturnType();
        }
        else if (wrappedParamSymbol instanceof ProgParameterSymbol) {
            progType = ((ProgParameterSymbol) wrappedParamSymbol).getDeclaredType();
        }
        return progType;
    }

    @Override public boolean isModuleTypeParameter() {
        boolean result = (wrappedParamSymbol instanceof ProgParameterSymbol);
        if (result) {
            result = ((ProgParameterSymbol)wrappedParamSymbol).getMode() ==
                    ProgParameterSymbol.ParameterMode.TYPE;
        }
        return result;
    }

    @Override public boolean isModuleOperationParameter() {
        return wrappedParamSymbol instanceof OperationSymbol;
    }

    @NotNull public Symbol getWrappedParamSymbol() {
        return wrappedParamSymbol;
    }

    /** Handle these toXXXX methods strategically, meaning only those that a
     *  conceivably module parameterizable {@link Symbol} might need.
     */
    @NotNull @Override public MathSymbol toMathSymbol()
            throws UnexpectedSymbolException {
        return wrappedParamSymbol.toMathSymbol();
    }

    @NotNull @Override public ProgVariableSymbol toProgVariableSymbol()
            throws UnexpectedSymbolException {
        return wrappedParamSymbol.toProgVariableSymbol();
    }

    @NotNull @Override public ProgParameterSymbol toProgParameterSymbol()
            throws UnexpectedSymbolException {
        return wrappedParamSymbol.toProgParameterSymbol();
    }

    @NotNull @Override public ProgTypeSymbol toProgTypeSymbol()
            throws UnexpectedSymbolException {
        return wrappedParamSymbol.toProgTypeSymbol();
    }

    @NotNull @Override public String getSymbolDescription() {
        return "a module parameter symbol";
    }

    @NotNull @Override public Symbol instantiateGenerics(
            @NotNull Map<String, PTType> genericInstantiations,
            @Nullable FacilitySymbol instantiatingFacility) {
        return wrappedParamSymbol.instantiateGenerics(
                genericInstantiations, instantiatingFacility);
    }
}
