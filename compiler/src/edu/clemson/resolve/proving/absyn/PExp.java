
package edu.clemson.resolve.proving.absyn;

import edu.clemson.resolve.misc.Utils;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import edu.clemson.resolve.semantics.MathClssftn;
import edu.clemson.resolve.semantics.Quantification;
import edu.clemson.resolve.semantics.programtype.ProgType;
import org.stringtemplate.v4.ST;

import java.util.*;

/**
 * This class represents the root of the prover abstract syntax tree (AST) hierarchy.
 * <p>
 * Unlike previous expression hierarchies used by the compiler, {@code PExp}s are immutable and exist without the
 * complications introduced by control structures. And while {@code PExp}s technically exist to represent <em>only</em>
 * mathematical expressions, realize that many 'programmatic' ones such as calls are also converted into {@code PExp}s
 * for vc generation purposes.</p>
 */
public abstract class PExp {

    /**
     * These fields are primarily for the {@link edu.clemson.resolve.vcgen.VCGenerator}; display requires certain
     * information about where this {@link PExp} stands in a file (with relation to VC gen) as well as its
     * textual vcExplanation (for example, "ensures clause of operation: Foo")
     */
    @Nullable
    private final Token vcLocation;
    @Nullable
    private final String vcExplanation;

    public final int structureHash, valueHash;

    /** Backing field for {@link #getMathClssftn()} */
    private final MathClssftn type;

    /**
     * Since the removal of the Exp hierarchy, the role of {@code PExps} has expanded considerably.
     * <p>
     * So in other words, if this {@code PExp} was born out of a programmatic expression (for vcgen), program type info
     * should be present, if not, then these should/will be {@code null}.</p>
     */
    private final ProgType progType;

    private List<PExp> cachedFunctionApplications = null;
    private Set<PSymbol> cachedQuantifiedVariables = null;
    private Set<PSymbol> cachedFreeVariables = null;
    private Set<PSymbol> cachedIncomingVariables = null;

    public PExp(@NotNull PSymbol.HashDuple hashes, @NotNull MathClssftn type) {
        this(hashes.structureHash, hashes.valueHash, type, null);
    }

    public PExp(@NotNull PSymbol.HashDuple hashes, @NotNull MathClssftn type, @Nullable ProgType progType,
                @Nullable Token vcLocation, @Nullable String vcExplanation) {
        this(hashes.structureHash, hashes.valueHash, type, progType, vcLocation, vcExplanation);
    }

    public PExp(@NotNull PSymbol.HashDuple hashes, @NotNull MathClssftn type, @Nullable ProgType progType) {
        this(hashes.structureHash, hashes.valueHash, type, progType);
    }

    public PExp(int structureHash, int valueHash, @NotNull MathClssftn type) {
        this(structureHash, valueHash, type, null);
    }

    public PExp(int structureHash, int valueHash, @NotNull MathClssftn type, @Nullable ProgType progType) {
        this(structureHash, valueHash, type, progType, null, null);
    }

    public PExp(int structureHash, int valueHash, @NotNull MathClssftn type,
                @Nullable ProgType progType, @Nullable Token vcLocation, @Nullable String vcExplanation) {
        this.type = type;
        this.progType = progType;
        this.structureHash = structureHash;
        this.valueHash = valueHash;
        this.vcLocation = vcLocation;
        this.vcExplanation = vcExplanation;
    }

    @Override
    public int hashCode() {
        return valueHash;
    }

    @Nullable
    public final ProgType getProgType() {
        return progType;
    }

    @NotNull
    public final MathClssftn getMathClssftn() {
        return type;
    }

    @NotNull
    public PExp substitute(List<PExp> currents, PExp repl) {
        Map<PExp, PExp> substitutions = new HashMap<>();
        for (PExp current : currents) {
            substitutions.put(current, repl);
        }
        return substitute(substitutions);
    }

    /**
     * Returns a new {@code PExp} whose subexpressions appearing in {@code currents} are substituted by those in
     * {@code repls}. In order to call this, it must be the case that {@code currents.size() == repls.size()}.
     *
     * @param currents a list of sub-expressions to be substituted (replaced)
     * @param repls    a list of replacement {@code PExp}s.
     *
     * @return the {@code PExp} with substitutions made
     */
    @NotNull
    public PExp substitute(@NotNull List<PExp> currents, @NotNull List<PExp> repls) {
        if (currents.size() != repls.size()) {
            throw new IllegalArgumentException("substitution lists must be the same length");
        }
        return substitute(Utils.zip(currents, repls));
    }

    /**
     * Returns {@code true} if the provided {@code substitutions} have no affect on {@code this} expression;
     * {@code false} otherwise.
     *
     * @param substitutions substitutions to make
     *
     * @return whether or not the substitutions given changes the expr
     */
    public boolean staysSameAfterSubstitution(Map<PExp, PExp> substitutions) {
        PExp thisSubstituted = substitute(substitutions);
        return this.equals(thisSubstituted);
    }

    public boolean staysSameAfterSubstitution(PExp current, PExp repl) {
        PExp thisSubstituted = substitute(current, repl);
        return this.equals(thisSubstituted);
    }

    @NotNull
    public PExp substitute(PExp current, PExp replacement) {
        Map<PExp, PExp> e = new LinkedHashMap<>();
        e.put(current, replacement);
        return substitute(e);
    }

    public abstract PExp withPrimeMarkAdded();

    /**
     * Returns true if the {@link MathClssftn} of this expression matches (or is a subtype) of {@code other};
     * {@code false} otherwise.
     *
     * @param other some {@code MathClassification}.
     *
     * @return whether or not the mathFor types of this or {@code other} matches
     */
    public boolean typeMatches(MathClssftn other) {
        //return other.isSubtypeOf(getClassification());
        return true;
    }

    /**
     * @see PExp#typeMatches(MathClssftn)
     */
    public boolean typeMatches(PExp other) {
        return typeMatches(other.getMathClssftn());
    }

    public abstract void accept(PExpListener v);

    /**
     * Substitutes all occurences of the subexpressions matching those defined in {@code substitutions.keyset()} with
     * the corresponding {@code PExp} defined by the map, returning a new (substituted) {@code PExp}.
     *
     * @param substitutions map like {@code existing PExp -> replacement PExp}
     *
     * @return a, new, substituted expression
     */
    @NotNull
    public abstract PExp substitute(@NotNull Map<PExp, PExp> substitutions);

    /**
     * Returns {@code true} iff {@code this} contains a subexpression whose 'name' field matches {@code name};
     * {@code false} otherwise.
     *
     * @param name some name
     *
     * @return whether or not the name appears anywhere in {@code this}'s subtree
     */
    public abstract boolean containsName(String name);

    /**
     * Returns the {@link Quantification} for {@code this} expression.
     *
     * @return forall, exists, or none
     */
    public Quantification getQuantification() {
        return Quantification.NONE;
    }

    /**
     * Returns a list containing all immediate children of {@code this}.
     *
     * @return a list of subexpressions
     */
    @NotNull
    public abstract List<? extends PExp> getSubExpressions();

    /**
     * A predicate that returns {@code true} in any of the following cases:
     * <ul>
     * <li>If we're an instance of {@code PSymbol} whose name is simply {@code true}.</li>
     * <li>If we're an expression with a top level app of of binary {@code =}s whose left and right arguments
     * are themselves equal (as determined via a call to {@link PExp#equals(Object)}).</li>
     * </ul>;
     *
     * @return whether or not we represent a trivially 'true' expression
     */
    public boolean isObviouslyTrue() {
        return false;
    }

    /**
     * Returns {@code true} if this {@code PExp} represents a primitive app of the {@code =} operator;
     * {@code false} otherwise.
     *
     * @return whether or not we have represent a top-level app of equals
     */
    public boolean isEquality() {
        return false;
    }

    public boolean isConjunct() {
        return false;
    }

    /**
     * Returns {@code true} if this {@code PExp} is prefixed by the {@code @} marker (incoming marker); {@code false}
     * otherwise.
     *
     * @return whether or not {@code this} is an incoming expression
     */
    public boolean isIncoming() {
        return false;
    }

    public boolean isLiteralFalse() {
        return false;
    }

    public boolean isLiteralTrue() {
        return false;
    }

    public boolean isVariable() {
        return false;
    }

    /**
     * If this {@code PExp} is one with a sensible (meaning: extant) name, then this method simply returns it,
     * independent of any parens or other syntactic characteristics.
     * <p>
     * If {@code this} expression is anonoymous, then we simply return a canned string such as <code>\:PLamda</code>
     * or <code>{ PSet }</code>.</p>
     * <p>
     * If your dealing with a curried style top-level app of the form {@code SS(k)(Cen(k))}, then the canonical
     * name returned should simply be <tt>SS</tt>.</p>
     *
     * @return the canonical name
     */
    @NotNull
    public abstract String getTopLevelOperationName();

    /**
     * Returns {@code true} iff this expression represents a primitive such as {@code 1..n} or some boolean value;
     * {@code false} otherwise.
     *
     * @return whether or not this
     */
    public boolean isLiteral() {
        return false;
    }

    public boolean isFunctionApplication() {
        return false;
    }

    public boolean hasSymbolNamesInCommonWith(final PExp other, boolean excludeApplication, boolean excludeLiterals) {
        Set<String> myNames = this.getSymbolNames(excludeApplication, excludeLiterals);
        Set<String> othersNames = other.getSymbolNames(excludeApplication, excludeLiterals);
        myNames.retainAll(othersNames);
        return !myNames.isEmpty();
    }

    @NotNull
    public final List<PExp> splitIntoConjuncts() {
        List<PExp> conjuncts = new ArrayList<>();
        splitIntoConjuncts(conjuncts);
        return conjuncts;
    }

    protected abstract void splitIntoConjuncts(@NotNull List<PExp> accumulator);

    @Nullable public Token getVCLocation() {
        return vcLocation;
    }

    @Nullable public String getVCExplanation() {
        return vcExplanation;
    }

    public abstract PExp withVCInfo(@Nullable Token location, @Nullable String explanation);

    /**
     * Returns a new version of this {@code PExp} where all occurences of the
     * '@' marker are erased; useful in applying the 'remember' vcgen rule.
     *
     * @return A '@-clean' version of this {@code PExp}.
     */
    @NotNull
    public abstract PExp withIncomingSignsErased();

    @NotNull
    public abstract PExp withQuantifiersFlipped();

    /**
     * Returns a set of '@'-prefixed symbols appearing in the subexpressions of this {@code PExp}. Note that when we
     * say 'symbols' we mean both function applications and argument-less variables.
     *
     * @return the set of all incoming symbols
     */
    @NotNull
    public final Set<PSymbol> getIncomingVariables() {
        if (cachedIncomingVariables == null) {
            cachedIncomingVariables = Collections.unmodifiableSet(getIncomingVariablesNoCache());
        }
        return cachedIncomingVariables;
    }

    @NotNull
    public abstract Set<PSymbol> getIncomingVariablesNoCache();

    @NotNull
    public final Set<PSymbol> getQuantifiedVariables() {
        if (cachedQuantifiedVariables == null) {
            //We're immutable, so only do this once
            cachedQuantifiedVariables = Collections.unmodifiableSet(getQuantifiedVariablesNoCache());
        }
        return cachedQuantifiedVariables;
    }

    @NotNull
    public abstract Set<PSymbol> getQuantifiedVariablesNoCache();

    //TODO: Consider making this List<PApply>.. but what about lambdas, isn't
    //that a function app? Just a nameless function app?
    @NotNull
    public final List<PExp> getFunctionApplications() {
        if (cachedFunctionApplications == null) {
            //We're immutable, so only do this once
            cachedFunctionApplications = getFunctionApplicationsNoCache();
        }
        return cachedFunctionApplications;
    }

    @NotNull
    public abstract List<PExp> getFunctionApplicationsNoCache();

    @NotNull
    public final Set<PSymbol> getFreeVariables() {
        if (cachedFreeVariables == null) {
            //We're immutable, so only do this once
            cachedFreeVariables = Collections.unmodifiableSet(getFreeVariablesNoCache());
        }
        return cachedFreeVariables;
    }

    @NotNull
    public abstract Set<PSymbol> getFreeVariablesNoCache();

    @NotNull
    public final Set<String> getSymbolNames() {
        return getSymbolNames(false, false);
    }

    @NotNull
    public final Set<String> getSymbolNames(boolean excludeApplications, boolean excludeLiterals) {
        return getSymbolNamesNoCache(excludeApplications, excludeLiterals);
    }

    protected abstract Set<String> getSymbolNamesNoCache(boolean excludeApplications, boolean excludeLiterals);

    /**
     * Returns {@code true} iff this {@code PExp} and {@code o}, are equivalent with respect to structure and all
     * function and variable names; {@code false} otherwise.
     *
     * @param o the expression to compare with {@code this}
     *
     * @return whether {@code this} matches {@code o} with respect to structure and variable naming
     */
    @Override
    public abstract boolean equals(Object o);

    //default
    public String toString(boolean parenthesizeApplications) {
        return toString();
    }

    public String render(int lineWidth) {
        PExpTextRenderingListener l = new PExpTextRenderingListener(lineWidth);
        accept(l);
        ST result = l.getSTFor(this);
        String s = result.render(lineWidth);
        return s;
    }

    public String render() {
        return render(35);
    }

    /** A util container for storing node structural and value hashcodes. */
    public static class HashDuple {
        public int structureHash;
        public int valueHash;

        public HashDuple(int structureHash, int valueHash) {
            this.structureHash = structureHash;
            this.valueHash = valueHash;
        }
    }
}