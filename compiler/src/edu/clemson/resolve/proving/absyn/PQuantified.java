package edu.clemson.resolve.proving.absyn;

import edu.clemson.resolve.misc.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rsrg.semantics.MTType;
import org.rsrg.semantics.Quantification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//This is really just a purely syntactic node to help us know where to print
//quantifiers (at which level), and which vars, etc.
public class PQuantified extends PExp {

    private final Quantification quantificationType;
    private final PExp assertion;
    private final List<PLambda.MathSymbolDeclaration> declaredSymbols =
            new ArrayList<>();

    public PQuantified(@NotNull PExp assertion,
                       @NotNull Quantification quantificationType,
                       @NotNull List<PLambda.MathSymbolDeclaration> symDecls) {
        super(assertion.structureHash, assertion.valueHash,
                assertion.getMathType(), assertion.getMathTypeValue());
        this.quantificationType = quantificationType;
        this.assertion = assertion;
        this.declaredSymbols.addAll(symDecls);
    }

    @NotNull public Quantification getQuantificationType() {
        return quantificationType;
    }

    @NotNull public PExp getAssertion() {
        return assertion;
    }

    @NotNull public List<PLambda.MathSymbolDeclaration> getDeclaredSymbols() {
        return declaredSymbols;
    }

    @Override public void accept(PExpListener v) {

    }

    @NotNull @Override public PExp substitute(
            @NotNull Map<PExp, PExp> substitutions) {
        return new PQuantified(assertion.substitute(substitutions),
                quantificationType, declaredSymbols);
    }

    @Override public boolean containsName(String name) {
        return assertion.containsName(name);
    }

    @NotNull @Override public List<? extends PExp> getSubExpressions() {
        List<PExp> result = new ArrayList<>();
        result.add(assertion);
        return result;
    }

    @NotNull @Override protected String getCanonicalName() {
        return "Quantified exp";
    }

    @Override protected void splitIntoConjuncts(@NotNull List<PExp> accumulator) {
        accumulator.add(this);
    }

    @NotNull @Override public PExp withIncomingSignsErased() {
        return new PQuantified(assertion.withIncomingSignsErased(),
                quantificationType, declaredSymbols);
    }

    @NotNull @Override public PExp withQuantifiersFlipped() {
        return new PQuantified(assertion.withQuantifiersFlipped(),
                quantificationType.flipped(), declaredSymbols);
    }

    @NotNull @Override public Set<PSymbol> getIncomingVariablesNoCache() {
        return assertion.getIncomingVariables();
    }

    @NotNull @Override public Set<PSymbol> getQuantifiedVariablesNoCache() {
        return assertion.getQuantifiedVariables();
    }

    @NotNull @Override public List<PExp> getFunctionApplicationsNoCache() {
        return assertion.getFunctionApplications();
    }

    @Override protected Set<String> getSymbolNamesNoCache(
            boolean excludeApplications, boolean excludeLiterals) {
        return assertion.getSymbolNames(excludeApplications, excludeLiterals);
    }

    @Override public String toString() {
        List<String> symNames = declaredSymbols.stream()
                .map(d -> d.name).collect(Collectors.toList());
        String qType = quantificationType == Quantification.UNIVERSAL ?
                "Forall" : "Exists";
        return qType + " " + Utils.join(symNames, ", ") + ":" +
                declaredSymbols.get(0).type + " " + assertion.toString();
    }

    @Override public boolean equals(Object o) {
        boolean result = (o instanceof PQuantified);
        if (result) {
            result = assertion.equals(o);
        }
        return result;
    }

}