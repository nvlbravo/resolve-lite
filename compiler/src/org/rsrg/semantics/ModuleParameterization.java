package org.rsrg.semantics;

import edu.clemson.resolve.parser.ResolveParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rsrg.semantics.programtype.PTType;
import org.rsrg.semantics.symbol.*;

import java.util.*;

public class ModuleParameterization {

    @NotNull private final MathSymbolTable scopeRepo;
    @NotNull private final ModuleIdentifier moduleIdentifier;

    @NotNull private final List<ProgTypeSymbol> actualGenerics = new ArrayList<>();
    @NotNull private final FacilitySymbol instantiatingFacility;

    public ModuleParameterization(@NotNull ModuleIdentifier moduleIdentifier,
                                  @NotNull List<ProgTypeSymbol> actualGenerics,
                                  @NotNull FacilitySymbol instantiatingFacility,
                                  @NotNull MathSymbolTable scopeRepo) {
        this.instantiatingFacility = instantiatingFacility;
        this.scopeRepo = scopeRepo;
        this.actualGenerics.addAll(actualGenerics);
        this.moduleIdentifier = moduleIdentifier;
    }

    @NotNull public Scope getScope(boolean instantiated)
            throws NoSuchModuleException {
        ModuleScopeBuilder originalScope =
                scopeRepo.getModuleScope(moduleIdentifier);
        Scope result = originalScope;
        result = scopeRepo.getModuleScope(moduleIdentifier);
        if ( instantiated ) {
            Map<String, PTType> genericInstantiations;
            genericInstantiations = getGenericInstantiations(originalScope, null);
            result = new InstantiatedScope(originalScope,
                        genericInstantiations, instantiatingFacility);
        }
        return result;
    }

    /*private List<ModuleParameterSymbol> getFormalParameters(
            boolean instantiateGenerics)
            throws NoSuchModuleException {
        ModuleScopeBuilder s = scopeRepo.getModuleScope(moduleIdentifier);
        List<ModuleParameterSymbol> moduleParams =
                s.getSymbolsOfType(ModuleParameterSymbol.class);
        if (instantiateGenerics) {
            for (ModuleParameterSymbol moduleParam : )
        }
        return
    }*/

    private Map<String, PTType> getGenericInstantiations(
            ModuleScopeBuilder moduleScope,
            List<ResolveParser.ProgExpContext> actualArguments) {
        Map<String, PTType> result = new HashMap<>();

        List<ModuleParameterSymbol> moduleParams =
                moduleScope.getSymbolsOfType(ModuleParameterSymbol.class);
        List<ProgParameterSymbol> formalGenerics = new ArrayList<>();

        for (ModuleParameterSymbol param : moduleParams) {
            try {
                ProgParameterSymbol p = param.toProgParameterSymbol();
                if (p.getMode() == ProgParameterSymbol.ParameterMode.TYPE) {
                    formalGenerics.add(p);
                }
            } catch (UnexpectedSymbolException e) {
                //no problem, we wont add it.
            }
        }
        if ( formalGenerics.size() != actualGenerics.size() ) {
            //we shouldn't have to do this in here I don't think. Can't really
            //give a nice error (no pointer to errMgr here), and we can't throw
            // an exception to be caught
            //in the populator -- unless of course adding yet another caught
            //exception to the signature of Scope.* methods sounds appealing...
            //which it certainly doesn't.
            throw new RuntimeException("generic list sizes do not match");
        }
        Iterator<ProgTypeSymbol> suppliedGenericIter =
                actualGenerics.iterator();
        for (ProgParameterSymbol formalGeneric : formalGenerics) {
            result.put(formalGeneric.getName(), suppliedGenericIter
                    .next().getProgramType());
        }
        return result;
    }

    @NotNull public ModuleIdentifier getModuleIdentifier() {
        return moduleIdentifier;
    }
}
