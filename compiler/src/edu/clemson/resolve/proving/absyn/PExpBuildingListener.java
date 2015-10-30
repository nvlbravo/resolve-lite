package edu.clemson.resolve.proving.absyn;

import edu.clemson.resolve.compiler.AnnotatedTree;
import edu.clemson.resolve.misc.HardCodedProgOps;
import edu.clemson.resolve.misc.Utils;
import edu.clemson.resolve.parser.ResolveBaseListener;
import edu.clemson.resolve.parser.ResolveParser;
import edu.clemson.resolve.proving.absyn.PSymbol.PSymbolBuilder;
import edu.clemson.resolve.proving.absyn.PApply.PApplyBuilder;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.rsrg.semantics.MTInvalid;
import org.rsrg.semantics.MTType;
import org.rsrg.semantics.Quantification;
import org.rsrg.semantics.programtype.PTType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts parse tree math exprs to an equivalent abstract-syntax form,
 * represented by the {@link PExp} hierarchy.
 */
public class PExpBuildingListener<T extends PExp> extends ResolveBaseListener {

    private final ParseTreeProperty<MTType> types, typeValues;
    private final ParseTreeProperty<PTType> progTypes;
    private final ParseTreeProperty<PExp> repo;

    private final Map<String, Quantification> quantifiedVars = new HashMap<>();
    private final MTInvalid dummyType;

    public PExpBuildingListener(AnnotatedTree annotations) {
        this(annotations, null);
    }

    public PExpBuildingListener(AnnotatedTree annotations, MTInvalid dummyType) {
        this.types = annotations.mathTypes;
        this.typeValues = annotations.mathTypeValues;
        this.progTypes = annotations.progTypes;
        this.repo = annotations.mathPExps;
        this.dummyType = dummyType;
    }

    @SuppressWarnings("unchecked") public T getBuiltPExp(ParseTree t) {
        return (T) repo.get(t);
    }

    @Override public void exitMathTypeAssertionExp(
            ResolveParser.MathTypeAssertionExpContext ctx) {
        repo.put(ctx, repo.get(ctx.mathExp()));
    }

    @Override public void exitMathTypeExp(
            ResolveParser.MathTypeExpContext ctx) {
        repo.put(ctx, repo.get(ctx.mathExp()));
    }

    @Override public void exitMathAssertionExp(
            ResolveParser.MathAssertionExpContext ctx) {
        repo.put(ctx, repo.get(ctx.getChild(0)));
    }

    @Override public void exitMathNestedExp(
            ResolveParser.MathNestedExpContext ctx) {
        repo.put(ctx, repo.get(ctx.mathAssertionExp()));
    }

    @Override public void exitMathPrimeExp(
            ResolveParser.MathPrimeExpContext ctx) {
        repo.put(ctx, repo.get(ctx.mathPrimaryExp()));
    }

    @Override public void exitMathPrimaryExp(
            ResolveParser.MathPrimaryExpContext ctx) {
        repo.put(ctx, repo.get(ctx.getChild(0)));
    }

    @Override public void enterMathQuantifiedExp(
            ResolveParser.MathQuantifiedExpContext ctx) {
        for (TerminalNode term : ctx.mathVariableDeclGroup().ID()) {
            String quantifier = ctx.q.getText();
            quantifiedVars.put(term.getText(),
                    quantifier.equals("Forall") ? Quantification.UNIVERSAL
                            : Quantification.EXISTENTIAL);
        }
    }

    @Override public void exitMathQuantifiedExp(
            ResolveParser.MathQuantifiedExpContext ctx) {
        for (TerminalNode term : ctx.mathVariableDeclGroup().ID()) {
            quantifiedVars.remove(term.getText());
        }
        repo.put(ctx, repo.get(ctx.mathAssertionExp()));
    }

    @Override public void exitMathUnaryApplyExp(
            ResolveParser.MathUnaryApplyExpContext ctx) {

    }

    @Override public void exitMathInfixApplyExp(
            ResolveParser.MathInfixApplyExpContext ctx) {
        PSymbol function = new PSymbolBuilder(ctx.op.getText())
                .mathType(types.get(ctx)).mathTypeValue(typeValues.get(ctx))
                .build();

        PApplyBuilder result = new PApplyBuilder(function)
                .applicationType(types.get(ctx))
                .applicationTypeValue(typeValues.get(ctx))
                .arguments(Utils.collect(PExp.class, ctx.mathExp(), repo));

        repo.put(ctx, result.build());
        //OK, you're going to need a map from STRING -> MTType for the infix ops.
    }

    @Override public void exitMathOutfixApplyExp(
            ResolveParser.MathOutfixApplyExpContext ctx) {

    }

    @Override public void exitMathSymbolExp(
            ResolveParser.MathSymbolExpContext ctx) {
        PSymbolBuilder result = new PSymbolBuilder(ctx.name.getText())
                .qualifier(ctx.qualifier)
                .incoming(ctx.incoming != null)
                .quantification(quantifiedVars.get(ctx.name.getText()))
                .mathTypeValue(getMathTypeValue(ctx))
                .mathType(getMathType(ctx));
        repo.put(ctx, result.build());
    }

    @Override public void exitMathLambdaExp(
            ResolveParser.MathLambdaExpContext ctx) {
        List<PLambda.Parameter> parameters = new ArrayList<>();
        for (ResolveParser.MathVariableDeclGroupContext grp : ctx
                .mathVariableDeclGroup()) {
            for (TerminalNode term : grp.ID()) {
                parameters.add(new PLambda.Parameter(term.getText(),
                        getMathTypeValue(grp.mathTypeExp())));
            }
        }
        repo.put(ctx, new PLambda(parameters, repo.get(ctx.mathExp())));
    }

    @Override public void exitMathAlternativeExp(
            ResolveParser.MathAlternativeExpContext ctx) {
        List<PExp> conditions = new ArrayList<>();
        List<PExp> results = new ArrayList<>();
        PExp otherwiseResult = null;

        for (ResolveParser.MathAlternativeItemExpContext alt : ctx
                .mathAlternativeItemExp()) {
            if ( alt.condition != null ) {
                conditions.add(repo.get(alt.condition));
                results.add(repo.get(alt.result));
            }
            else {
                otherwiseResult = repo.get(alt.result);
            }
        }
        PAlternatives result =
                new PAlternatives(conditions, results, otherwiseResult,
                        getMathType(ctx), getMathTypeValue(ctx));
        repo.put(ctx, result);
    }

    /*@Override public void exitMathSegmentsExp(
            Resolve.MathSegmentsExpContext ctx) {
        //Todo: Type the individual segs of a seg exp.
        List<String> nameComponents = ctx.mathFunctionApplicationExp().stream()
                .map(app -> ((PSymbol) repo.get(app)).getName())
                .collect(Collectors.toList());
        PSymbol last = (PSymbol)repo.get(ctx.mathFunctionApplicationExp()
                .get(ctx.mathFunctionApplicationExp().size() - 1));

        String name = Utils.join(nameComponents, ".");
        PSymbolBuilder result = new PSymbolBuilder(name).arguments(
                last.getArguments()).incoming(ctx.AT() != null)
                .mathType(last.getMathType());

        repo.put(ctx, result.build());
    }*/

    @Override public void exitMathPrefixApplyExp(
            ResolveParser.MathPrefixApplyExpContext ctx) {
    }

    @Override public void exitMathBooleanLiteralExp(
            ResolveParser.MathBooleanLiteralExpContext ctx) {
        PSymbolBuilder result = new PSymbol.PSymbolBuilder(ctx.getText()) //
                .mathType(getMathType(ctx)).literal(true);
        repo.put(ctx, result.build());
    }

    @Override public void exitMathIntegerLiteralExp(
            ResolveParser.MathIntegerLiteralExpContext ctx) {
        PSymbolBuilder result = new PSymbol.PSymbolBuilder(ctx.getText()) //
                .mathType(getMathType(ctx)).literal(true);
        repo.put(ctx, result.build());
    }

    private PExp buildLiteral(String literalText, MTType type, MTType typeValue,
                              PTType progType) {
        PSymbol.PSymbolBuilder result =
                new PSymbolBuilder(literalText).mathType(type)
                        .progType(progType).mathTypeValue(typeValue)
                        .literal(true);
        return result.build();
    }

    private MTType getMathType(ParseTree t) {
        return types.get(t) == null ? dummyType : types.get(t);
    }

    private MTType getMathTypeValue(ParseTree t) {
        return typeValues.get(t) == null ? dummyType : typeValues.get(t);
    }
}
