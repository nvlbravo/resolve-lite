package org.rsrg.semantics.symbol;

import edu.clemson.resolve.parser.ResolveParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rsrg.semantics.MathSymbolTable;
import org.rsrg.semantics.ModuleIdentifier;
import org.rsrg.semantics.ModuleParameterization;
import org.rsrg.semantics.programtype.PTType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacilitySymbol extends Symbol {

    @NotNull private SpecImplementationPairing type;
    @NotNull private MathSymbolTable scopeRepo;

    /** A mapping from the rule contexts representing an module arg list
     *  to all the various {@link Symbol}s that represent the actual arguments
     *  supplied.
     *  <p>Right now I'm really only especially concerned about
     *  generics in this list ({@link ProgTypeSymbol} or
     *  {@link ProgParameterSymbol}s with a 'mode' of {@link ProgParameterSymbol.ParameterMode#TYPE}.
     *  </p>
     */
    @NotNull private final Map<ResolveParser.ModuleArgumentListContext,
            List<Symbol>> actualArgSymbols;

    private final Map<ModuleParameterization, ModuleParameterization>
            enhancementImplementations = new HashMap<>();
    private final List<ModuleParameterization> enhancements =
            new ArrayList<>();

    public FacilitySymbol(
            @NotNull ResolveParser.FacilityDeclContext facility,
            @NotNull ModuleIdentifier moduleIdentifier,
            @NotNull Map<ResolveParser.ModuleArgumentListContext,
                         List<Symbol>> actualArgSymbols,
            @NotNull MathSymbolTable scopeRepo) {
        super(facility.name.getText(), facility, moduleIdentifier);
        this.scopeRepo = scopeRepo;
        this.actualArgSymbols = actualArgSymbols;
        List<Symbol> specArgSymbols = actualArgSymbols.get(facility.specArgs);
        List<Symbol> implArgSymbols = actualArgSymbols.get(facility.implArgs);

        ModuleParameterization spec =
                new ModuleParameterization(new ModuleIdentifier(facility.spec),
                        specArgSymbols == null ?
                                new ArrayList<>() : specArgSymbols, this, scopeRepo);

        ModuleParameterization impl = null;

        List<ResolveParser.ProgExpContext> actualArgs =
                facility.implArgs != null ? facility.implArgs
                        .progExp() : new ArrayList<>();
        impl = new ModuleParameterization(new ModuleIdentifier(facility.impl),
                implArgSymbols == null ?
                        new ArrayList<>() : implArgSymbols, this, scopeRepo);

        this.type = new SpecImplementationPairing(spec, impl);

        //These are realized by the concept realization
        /*for (EnhancementItem realizationEnhancement : facility
                .getEnhancements()) {

            spec =
                    new ModuleParameterization(new ModuleIdentifier(
                            realizationEnhancement.getNameToken().getNameToken()),
                            realizationEnhancement.getParams(), this,
                            mySourceRepository);

            myEnhancements.add(spec);
            myEnhancementRealizations.put(spec, realization);
        }*/

        //These are realized by individual enhancement realizations
        /*for (ResolveParser.EnhancementPairDeclContext enhancement :
                facility.enhancementPairDecl()) {

            spec = new ModuleParameterization(enhancement.spec.getText(),
                            genericsPerFacility.get(enhancement),
                            enhancement.specArgs, this, scopeRepo);

            impl = new ModuleParameterization(enhancement.impl.getText(),
                        new ArrayList<>(), enhancement.implArgs,
                            this, scopeRepo);
            enhancements.add(spec);
            enhancementImplementations.put(spec, impl);
        }*/
    }

    @NotNull public List<ModuleParameterization> getEnhancements() {
        return enhancements;
    }

    @NotNull public SpecImplementationPairing getFacility() {
        return type;
    }

    @NotNull @Override public String getSymbolDescription() {
        return "a facility";
    }

    @NotNull @Override public String toString() {
        return getName();
    }

    @NotNull @Override public FacilitySymbol toFacilitySymbol() {
        return this;
    }

    @NotNull @Override public FacilitySymbol instantiateGenerics(
            @NotNull Map<String, PTType> genericInstantiations,
            @Nullable FacilitySymbol instantiatingFacility) {
        //TODO : This is probably wrong.  One of the parameters to a module
        //       used in the facility could be a generic, in which case it
        //       should be replaced with the corresponding concrete type--but
        //       how?
        return this;
    }

    public static class SpecImplementationPairing {

        @NotNull private final ModuleParameterization specification;
        @Nullable private final ModuleParameterization implementation;

        public SpecImplementationPairing(@NotNull ModuleParameterization spec) {
            this(spec, null);
        }

        public SpecImplementationPairing(@NotNull ModuleParameterization spec,
                                         @Nullable ModuleParameterization impl) {
            this.specification = spec;
            this.implementation = impl;
        }

        @NotNull public ModuleParameterization getSpecification() {
            return specification;
        }

        @Nullable public ModuleParameterization getImplementation() {
            return implementation;
        }
    }

}
