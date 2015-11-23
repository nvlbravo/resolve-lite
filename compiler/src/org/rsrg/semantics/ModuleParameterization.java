package org.rsrg.semantics;

import org.antlr.v4.runtime.ParserRuleContext;
import org.rsrg.semantics.programtype.PTType;
import org.rsrg.semantics.symbol.FacilitySymbol;
import org.rsrg.semantics.symbol.ProgTypeSymbol;

import java.util.*;

public class ModuleParameterization {

    private final MathSymbolTable scopeRepo;
    private final String moduleID;

    //private final List<Resolve.ModuleArgumentContext> arguments =
    //        new ArrayList<>();
    private final List<ProgTypeSymbol> actualGenerics = new ArrayList<>();
    private final FacilitySymbol instantiatingFacility;

    public ModuleParameterization(String moduleID,
                                  List<ProgTypeSymbol> actualGenerics,
                                  ParserRuleContext TEMP,//Resolve.ModuleArgumentListContext actualArgs,
                                  FacilitySymbol instantiatingFacility, MathSymbolTable scopeRepo) {
        this.instantiatingFacility = instantiatingFacility;
        this.scopeRepo = scopeRepo;
        this.actualGenerics.addAll(actualGenerics);

        if ( TEMP != null ) {
            //arguments.addAll(actualArgs.moduleArgument());
        }
        this.moduleID = moduleID;
    }

    public Scope getScope(boolean instantiated) {
        Scope result;
        ModuleScopeBuilder originalScope =
                scopeRepo.getModuleScope(moduleID);
        result = originalScope;
        if ( instantiated ) {
            Map<String, PTType> genericInstantiations;

           // genericInstantiations =
           //         getGenericInstantiations(originalScope, null);
            //result =
           //         new InstantiatedScope(originalScope,
            //                genericInstantiations, instantiatingFacility);
        }
        return result;
    }

    /*private Map<String, PTType> getGenericInstantiations(
            ModuleScopeBuilder moduleScope,
            List<Resolve.ModuleArgumentContext> actualArguments) {
        Map<String, PTType> result = new HashMap<>();

        List<GenericSymbol> formalGenerics =
                moduleScope.getSymbolsOfType(GenericSymbol.class);

        if ( formalGenerics.size() != actualGenerics.size() ) {
            throw new RuntimeException("generic list sizes do not match");
        }
        Iterator<ProgTypeSymbol> suppliedGenericIter =
                actualGenerics.iterator();
        Iterator<GenericSymbol> formalGenericIter = formalGenerics.iterator();
        while (formalGenericIter.hasNext()) {
            result.put(formalGenericIter.next().getName(), suppliedGenericIter
                    .next().getProgramType());
        }
        return result;
    }*/

    public String getName() {
        return moduleID;
    }

    public String getModuleID() {
        return moduleID;
    }

   // public List<Resolve.ModuleArgumentContext> getArguments() {
   //     return arguments;
   // }
}
