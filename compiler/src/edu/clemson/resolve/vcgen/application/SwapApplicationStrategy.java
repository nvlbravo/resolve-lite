package edu.clemson.resolve.vcgen.application;

import edu.clemson.resolve.vcgen.AssertiveBlock;
import edu.clemson.resolve.vcgen.VCAssertiveBlock.VCAssertiveBlockBuilder;
import edu.clemson.resolve.vcgen.stats.VCSwap;
import org.jetbrains.annotations.NotNull;

public class SwapApplicationStrategy implements VCStatRuleApplicationStrategy<VCSwap> {

    //TODO: Todo, maybe make vcswapStat, vcwh
    @NotNull
    @Override
    public AssertiveBlock applyRule(@NotNull VCAssertiveBlockBuilder block, @NotNull VCSwap stat) {
       /* PExp workingConfirm = block.finalConfirm.getConfirmExp();
        PExp swapLeft = stat.getStatComponents().get(0);
        PExp swapRight = stat.getStatComponents().get(1);

        PExp temp = new PSymbolBuilder((PSymbol)swapLeft).name("_t;").build();

        workingConfirm = workingConfirm.substitute(swapRight, temp);
        workingConfirm = workingConfirm.substitute(swapLeft, swapRight);
        workingConfirm = workingConfirm.substitute(temp, swapLeft);
        block.finalConfirm(workingConfirm);*/
        return block.snapshot();
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Swap rule application";
    }
}