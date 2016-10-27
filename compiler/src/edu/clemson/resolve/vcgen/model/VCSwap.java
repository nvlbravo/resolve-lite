package edu.clemson.resolve.vcgen.model;

import edu.clemson.resolve.proving.absyn.PExp;
import edu.clemson.resolve.vcgen.application.VCStatRuleApplicationStrategy;
import edu.clemson.resolve.vcgen.model.VCAssertiveBlock.VCAssertiveBlockBuilder;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

public class VCSwap extends VCRuleBackedStat {

    private final PExp left, right;

    public VCSwap(ParserRuleContext ctx,
                  VCAssertiveBlockBuilder block,
                  VCStatRuleApplicationStrategy apply,
                  PExp left,
                  PExp right) {
        super(ctx, block, apply);
        this.left = left;
        this.right = right;
    }

    @NotNull
    @Override
    public VCSwap copyWithBlock(@NotNull VCAssertiveBlockBuilder b) {
        return new VCSwap(definingCtx, b, applicationStrategy, left, right);
    }
}
