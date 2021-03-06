package edu.clemson.resolve.vcgen.app;

import edu.clemson.resolve.proving.absyn.PExp;
import edu.clemson.resolve.proving.absyn.PSymbol;
import edu.clemson.resolve.semantics.DumbMathClssftnHandler;
import edu.clemson.resolve.vcgen.BasicLambdaBetaReducingListener;
import edu.clemson.resolve.vcgen.ListBackedSequent;
import edu.clemson.resolve.vcgen.Sequent;
import edu.clemson.resolve.vcgen.VCAssertiveBlock;
import edu.clemson.resolve.vcgen.VCAssertiveBlock.VCAssertiveBlockBuilder;
import edu.clemson.resolve.vcgen.stats.VCAssume;
import edu.clemson.resolve.vcgen.stats.VCConfirm;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ParsimoniousAssumeApplicationStrategy implements RuleApplicationStrategy<VCAssume> {

    @NotNull
    @Override
    public VCAssertiveBlock applyRule(@NotNull Deque<VCAssertiveBlockBuilder> branches,
                                      @NotNull VCAssertiveBlockBuilder block,
                                      @NotNull VCAssume stat) {
        List<PExp> allAssumptions = stat.getAssumeExp().splitIntoConjuncts();
        Collection<Sequent> existingSequents = block.finalConfirm.getSequents();
        PExp assumeExp = stat.getAssumeExp();

        Map<PExp, PExp> equalitySubstitutions = new HashMap<>();
        List<PExp> remainingAssumptions = new ArrayList<>();
        List<PExp> nonEffectualEqualities = new ArrayList<>();

        for (PExp assume : allAssumptions) {
            if (stat.isStipulatedAssumption()) {
                remainingAssumptions.add(assume);
                continue;
            }
            if (assume.isEquality()) {
                PExp lhs = assume.getSubExpressions().get(1);
                PExp rhs = assume.getSubExpressions().get(2);

                //if both lhs and rhs are replaceable vars, then the left had better
                //be a special verification-system conjured variable
                if (lhs.isVariable() && rhs.isVariable()) {
                    if (hasVerificationVariable(lhs)) {
                        equalitySubstitutions.put(lhs, rhs);
                    }
                    else {
                        remainingAssumptions.add(assume);
                    }
                }
                //left replaceablility
                else if (lhs.isVariable()) {
                    if (substitutesAny(existingSequents, lhs, rhs) || hasVerificationVariable(lhs)) {
                        equalitySubstitutions.put(lhs, rhs);
                    }
                    else {
                        nonEffectualEqualities.add(assume);
                    }
                }
                //right replaceability
                else if (rhs.isVariable()) {
                    if (substitutesAny(existingSequents, rhs, lhs) || hasVerificationVariable(rhs)) {
                        equalitySubstitutions.put(rhs, lhs);
                    }
                    else {
                        nonEffectualEqualities.add(assume);
                    }
                }
                //not replaceable...
                else {
                    remainingAssumptions.add(assume);
                }
            }
            else {
                //if we haven't done a replacement, then add it as well..
                remainingAssumptions.add(assume);
            }
        }
        List<PExp> remainingAssumptionsWithEqualSubt = new ArrayList<>();
        //TODO: ok, we know everything in "nonEffectualEqualities" doesn't affect any formulas in the sequents of the
        //final confirm... I wonder though if they would affect any of the "remAssumptions"? look into this.
        remainingAssumptionsWithEqualSubt.addAll(nonEffectualEqualities);
        for (PExp assumption : remainingAssumptions) {
            remainingAssumptionsWithEqualSubt.add(assumption.substitute(equalitySubstitutions));
        }
        VCConfirm substitutedConfirm = block.finalConfirm.withSequentFormulaSubstitution(equalitySubstitutions);
        List<Sequent> newFinalConfirmSequents =
                performParsimoniousStep(block.g, remainingAssumptionsWithEqualSubt,
                        substitutedConfirm.getSequents(), stat.isStipulatedAssumption());

        //newFinalConfirmSequents = betaReduceSequentFormulas(newFinalConfirmSequents);
        //newFinalConfirmSequents = simplifyLambdas(newFinalConfirmSequents);

        block.finalConfirm(newFinalConfirmSequents);
        return block.snapshot();
    }

    //TODO: Feels hacky... :)
    private boolean hasVerificationVariable(PExp s) {
        //return s.containsName("P_Val") || s.containsName("conc");
        return s.getTopLevelOperationName().contains("P_Val") || s.getTopLevelOperationName().contains("conc");
    }

    private List<Sequent> performParsimoniousStep(DumbMathClssftnHandler g,
                                                  List<PExp> assumptions,
                                                  Collection<Sequent> sequents,
                                                  boolean stipulated) {
        Map<PExp, PExp> confirmsToModifiedConfirms = new LinkedHashMap<>();
        List<Sequent> result = new LinkedList<>();

        for (Sequent sequent : sequents) {
            for (PExp assume : assumptions) {
                Set<String> intersection = assume.getSymbolNames(true, true);
                intersection.retainAll(getSymbolNamesFromSequent(sequent));
                if ((!intersection.isEmpty() && !assume.isObviouslyTrue()) || stipulated) {
                    sequent = sequent.addLeft(assume);
                }
            }
            result.add(sequent);
        }
        return result;
    }

    /**
     * Returns {@code true} if the substitution wff[s <~ t] affects a wff of any sequent
     * in {@code sequents}.
     *
     * @param sequents the collection of sequents to test.
     * @param s the expression to replace.
     * @param t the (substitute) replacement expression.
     */
    private boolean substitutesAny(Collection<Sequent> sequents, PExp s, PExp t) {
        for (Sequent sequent : sequents) {
            for (PExp wff : sequent.getLeftFormulas()) {
                PExp substituted = wff.substitute(s, t);
                if (!wff.equals(substituted)) return true;
            }
            for (PExp wff : sequent.getRightFormulas()) {
                PExp substituted = wff.substitute(s, t);
                if (!wff.equals(substituted)) return true;
            }
        }
        return false;
    }

    @NotNull
    private List<Sequent> betaReduceSequentFormulas(List<Sequent> existingSequents) {
        List<Sequent> newFinalConfirmSequentsBetaReduced = new LinkedList<>();
        for (Sequent sequent : existingSequents) {
            List<PExp> newLeft = new LinkedList<>();
            List<PExp> newRight = new LinkedList<>();
            for (PExp x : sequent.getLeftFormulas()) {
                BasicLambdaBetaReducingListener b = new BasicLambdaBetaReducingListener(x);
                x.accept(b);
                newLeft.add(b.getReducedExp());
            }
            for (PExp x : sequent.getRightFormulas()) {
                BasicLambdaBetaReducingListener b = new BasicLambdaBetaReducingListener(x);
                x.accept(b);
                newRight.add(b.getReducedExp());
            }
            newFinalConfirmSequentsBetaReduced.add(new ListBackedSequent(newLeft, newRight));
        }
        return newFinalConfirmSequentsBetaReduced;
    }

    /**
     * If there are nested lambdas where the otherwise conditions match up this collapses them into
     * the same level for ease of reading.
     */
    /*private List<PExp> simplifyLambdas(List<Sequent> sequents) {
        List<Sequent> newFinalConfirmWithSimplifiedSequents = new LinkedList<>();
        for (Sequent sequent : sequents) {
            List<PExp> newLeft = new LinkedList<>();
            List<PExp> newRight = new LinkedList<>();
            for (PExp x : sequent.getLeftFormulas()) {
                BasicLambdaBetaReducingListener b = new BasicLambdaBetaReducingListener(x);
                x.accept(b);
                newLeft.add(b.getReducedExp());
            }
            for (PExp x : sequent.getRightFormulas()) {
                BasicLambdaBetaReducingListener b = new BasicLambdaBetaReducingListener(x);
                x.accept(b);
                newRight.add(b.getReducedExp());
            }
            newFinalConfirmSequentsBetaReduced.add(new ListBackedSequent(newLeft, newRight));
        }
    }*/

    private Set<String> getSymbolNamesFromSequent(Sequent s) {
        Set<String> result = new HashSet<>();
        for (PExp e : s.getLeftFormulas()) {
            result.addAll(e.getSymbolNames(true, true));
        }
        for (PExp e : s.getRightFormulas()) {
            result.addAll(e.getSymbolNames(true, true));
        }
        return result;
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Parsimonious assume app";
    }
}