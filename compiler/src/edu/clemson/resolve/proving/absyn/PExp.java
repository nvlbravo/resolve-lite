package edu.clemson.resolve.proving.absyn;

import org.rsrg.semantics.MTType;

import java.util.*;

public abstract class PExp {

    public final int structureHash;
    public final int valueHash;
    private final MTType type, typeValue;

    private Set<String> cachedSymbolNames = null;
    private List<PExp> cachedFunctionApplications = null;
    private Set<PSymbol> cachedQuantifiedVariables = null;
    private Set<PSymbol> cachedIncomingVariables = null;

    public PExp(PSymbol.HashDuple hashes, MTType type, MTType typeValue) {
        this(hashes.structureHash, hashes.valueHash, type, typeValue);
    }

    public PExp(int structureHash, int valueHash, MTType type,
                MTType typeValue) {
        this.type = type;
        this.typeValue = typeValue;
        this.structureHash = structureHash;
        this.valueHash = valueHash;
    }

    @Override public int hashCode() {
        return valueHash;
    }

    public final MTType getMathType() {
        return type;
    }

    public final MTType getMathTypeValue() {
        return typeValue;
    }

    public PExp substitute(List<? extends PExp> currents, PExp repl) {
        Map<PExp, PExp> substitutions = new HashMap<>();
        for (PExp current : currents) {
            substitutions.put(current, repl);
        }
        return substitute(substitutions);
    }

    public PExp substitute(List<? extends PExp> currents, PExp... repls) {
        return substitute(currents, Arrays.asList(repls));
    }

    public PExp substitute(List<? extends PExp> currents,
            List<? extends PExp> replacements) {
        if ( currents.size() != replacements.size() ) {
            throw new IllegalArgumentException("substitution lists must be"
                    + "the same length");
        }
        Iterator<? extends PExp> replIter = replacements.iterator();
        Iterator<? extends PExp> currIter = currents.iterator();
        Map<PExp, PExp> result = new LinkedHashMap<>();
        while (replIter.hasNext()) {
            result.put(currIter.next(), replIter.next());
        }
        return substitute(result);
    }

    public PExp substitute(PExp current, PExp replacement) {
        Map<PExp, PExp> e = new LinkedHashMap<>();
        e.put(current, replacement);
        return substitute(e);
    }

    public void processStringRepresentation(PExpListener visitor, Appendable a) {
        /* PExpTextRenderingVisitor renderer = new PExpTextRenderingVisitor(a);
         PExpVisitor finalVisitor = new NestedPExpVisitors(visitor, renderer);

         this.accept(finalVisitor);*/
    }

    public boolean typeMatches(MTType other) {
        return other.isSubtypeOf(getMathType());
    }

    public boolean typeMatches(PExp other) {
        return typeMatches(other.getMathType());
    }

    public abstract void accept(PExpListener v);

    public abstract PExp substitute(Map<PExp, PExp> substitutions);

    public abstract boolean containsName(String name);

    public abstract List<? extends PExp> getSubExpressions();

    public abstract boolean isObviouslyTrue();

    public boolean isEquality() {
        return false;
    }

    public abstract boolean isLiteralFalse();

    public abstract boolean isVariable();

    public abstract boolean isLiteral();

    public abstract boolean isFunction();

    public final List<PExp> splitIntoConjuncts() {
        List<PExp> conjuncts = new ArrayList<>();
        splitIntoConjuncts(conjuncts);
        return conjuncts;
    }

    protected abstract void splitIntoConjuncts(List<PExp> accumulator);

    /**
     * Returns a copy of this {@code PExp} where all variables prefixed with
     * an '@' are replaced by just the variable. This is essentially applying
     * the 'remember' rule useful in {@link org.resolvelite.vcgen.VCGenerator}.
     * 
     * @return A '@-clean' version of this {@code PExp}.
     */
    public abstract PExp withIncomingSignsErased();

    public abstract PExp withQuantifiersFlipped();

    public final Set<PSymbol> getIncomingVariables() {
        if ( cachedIncomingVariables == null ) {
            cachedIncomingVariables =
                    Collections.unmodifiableSet(getIncomingVariablesNoCache());
        }
        return cachedIncomingVariables;
    }

    public abstract Set<PSymbol> getIncomingVariablesNoCache();

    public final Set<PSymbol> getQuantifiedVariables() {
        if ( cachedQuantifiedVariables == null ) {
            //We're immutable, so only do this once
            cachedQuantifiedVariables =
                    Collections
                            .unmodifiableSet(getQuantifiedVariablesNoCache());
        }
        return cachedQuantifiedVariables;
    }

    public abstract Set<PSymbol> getQuantifiedVariablesNoCache();

    public final List<PExp> getFunctionApplications() {
        if ( cachedFunctionApplications == null ) {
            //We're immutable, so only do this once
            cachedFunctionApplications = getFunctionApplicationsNoCache();
        }
        return cachedFunctionApplications;
    }

    public abstract List<PExp> getFunctionApplicationsNoCache();

    public final Set<String> getSymbolNames() {
        if ( cachedSymbolNames == null ) {
            //We're immutable, so only do this once
            cachedSymbolNames =
                    Collections.unmodifiableSet(getSymbolNamesNoCache());
        }
        return cachedSymbolNames;
    }

    protected abstract Set<String> getSymbolNamesNoCache();

    public static class HashDuple {
        public int structureHash;
        public int valueHash;

        public HashDuple(int structureHash, int valueHash) {
            this.structureHash = structureHash;
            this.valueHash = valueHash;
        }
    }
}