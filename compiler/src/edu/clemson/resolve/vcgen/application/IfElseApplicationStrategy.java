package edu.clemson.resolve.vcgen.application;

import edu.clemson.resolve.misc.Utils;
import edu.clemson.resolve.parser.ResolveParser;
import edu.clemson.resolve.proving.absyn.PApply;
import edu.clemson.resolve.proving.absyn.PExp;
import edu.clemson.resolve.proving.absyn.PSymbol;
import edu.clemson.resolve.semantics.DumbMathClssftnHandler;
import edu.clemson.resolve.semantics.MathFunctionClssftn;
import edu.clemson.resolve.vcgen.AssertiveBlock;
import edu.clemson.resolve.vcgen.VCAssertiveBlock;
import edu.clemson.resolve.vcgen.stats.VCIfElse;
import edu.clemson.resolve.vcgen.VCAssertiveBlock.VCAssertiveBlockBuilder;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.List;

public class IfElseApplicationStrategy implements VCStatRuleApplicationStrategy<VCIfElse> {

    @NotNull
    @Override
    public AssertiveBlock applyRule(@NotNull Deque<VCAssertiveBlockBuilder> branches,
                                    @NotNull VCAssertiveBlockBuilder block,
                                    @NotNull VCIfElse stat) {
        VCAssertiveBlockBuilder neg = new VCAssertiveBlockBuilder(block);

        PExp mathCond = getMathCondition(block, stat);
        block.assume(mathCond);
        block.stats(Utils.apply(stat.getThenStmts(), e->e.copyWithEnclosingBlock(block)));

        PExp negatedCondition = negateMathCondition(block.g, mathCond);
        neg.assume(negatedCondition);
        neg.stats(Utils.apply(stat.getElseStmts(), e->e.copyWithEnclosingBlock(neg)));
        branches.push(neg);
        return block.snapshot();
    }

    @NotNull
    @Override
    public String getDescription() {
        return "If-else app";
    }

    @NotNull
    private static PExp negateMathCondition(DumbMathClssftnHandler g, PExp mathConditionToNegate) {
        PExp name = new PSymbol.PSymbolBuilder("⌐")
                .mathClssfctn(new MathFunctionClssftn(g, g.BOOLEAN, g.BOOLEAN))
                .build();
        return new PApply.PApplyBuilder(name)
                .applicationType(g.BOOLEAN)
                .arguments(mathConditionToNegate)
                .build();
    }

    @NotNull
    PExp getMathCondition(@NotNull VCAssertiveBlock.VCAssertiveBlockBuilder block,
                          @NotNull VCIfElse stat) {
        PExp progCondition = stat.getProgIfCondition();

        //This is either going to be a while or an if underlying (both have branch conditions)
        ResolveParser.ProgExpContext progConditionNode = null;
        ParserRuleContext o = stat.getDefiningContext();
        if (o instanceof ResolveParser.IfStmtContext) {
            progConditionNode = ((ResolveParser.IfStmtContext)o).progExp();
        }
        else {
            progConditionNode = ((ResolveParser.WhileStmtContext)o).progExp();
        }
        FunctionAssignApplicationStrategy.Invk_Cond invokeConditionListener =
                new FunctionAssignApplicationStrategy.Invk_Cond(progConditionNode, block);
        progCondition.accept(invokeConditionListener);
        PExp mathCond = invokeConditionListener.mathFor(progCondition);
        return mathCond;
    }
}
