package org.rsrg.semantics.symbol;

import edu.clemson.resolve.proving.absyn.PExp;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rsrg.semantics.ModuleIdentifier;
import org.rsrg.semantics.programtype.ProgType;
import org.rsrg.semantics.programtype.ProgVoidType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OperationSymbol extends Symbol {

    private final ProgType returnType;
    private final List<ProgParameterSymbol> parameters = new ArrayList<>();

    @NotNull private final PExp requires, ensures;

    public OperationSymbol(@NotNull String name,
                           @Nullable ParserRuleContext definingTree,
                           @NotNull PExp requires, @NotNull PExp ensures,
                           @NotNull ProgType type,
                           @NotNull ModuleIdentifier moduleIdentifier,
                           @NotNull List<ProgParameterSymbol> params) {
        super(name, definingTree, moduleIdentifier);
        this.parameters.addAll(params);
        this.returnType = type;
        this.requires = requires;
        this.ensures = ensures;
    }

    @NotNull public PExp getRequires() {
        return requires;
    }

    @NotNull public PExp getEnsures() {
        return ensures;
    }

    @NotNull public List<ProgParameterSymbol> getParameters() {
        return parameters;
    }

    /** Get the return type of this operation. Note that in the cases where
     *  there isn't something returned, this should always return an instance
     *  of {@link ProgVoidType}.
     *
     *  @return the return {@link ProgType}.
     */
    @NotNull public ProgType getReturnType() {
        return returnType;
    }

    @NotNull @Override public OperationSymbol toOperationSymbol() {
        return this;
    }

    @NotNull @Override public ProgVariableSymbol toProgVariableSymbol() {
        return new ProgVariableSymbol(name, definingTree, returnType,
                moduleIdentifier);
    }

    @NotNull @Override public String getSymbolDescription() {
        return "an operation";
    }

    @NotNull @Override public String toString() {
        return getName() + ":" + parameters;
    }

    @NotNull @Override public OperationSymbol instantiateGenerics(
            @NotNull Map<String, ProgType> genericInstantiations,
            @Nullable FacilitySymbol instantiatingFacility) {

        InstantiationFunction f =
                new InstantiationFunction(genericInstantiations,
                        instantiatingFacility);
        List<ProgParameterSymbol> newParams = parameters.stream()
                .map(f::apply).collect(Collectors.toList());
        return new OperationSymbol(getName(), getDefiningTree(), requires,
                ensures, returnType.instantiateGenerics(genericInstantiations,
                        instantiatingFacility), getModuleIdentifier(), newParams);
    }

    private static class InstantiationFunction
            implements
                Function<ProgParameterSymbol, ProgParameterSymbol> {

        @NotNull private final Map<String, ProgType> genericInstantiations;
        @NotNull private final FacilitySymbol instantiatingFacility;

        public InstantiationFunction(
                @NotNull Map<String, ProgType> instantiations,
                @NotNull FacilitySymbol instantiatingFacility) {
            this.genericInstantiations =
                    new HashMap<String, ProgType>(instantiations);
            this.instantiatingFacility = instantiatingFacility;
        }

        @Override public ProgParameterSymbol apply(
                @NotNull ProgParameterSymbol input) {
            return (ProgParameterSymbol) input.instantiateGenerics(
                    genericInstantiations, instantiatingFacility);
        }
    }
}
