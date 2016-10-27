package edu.clemson.resolve.vcgen.model;

import edu.clemson.resolve.proving.absyn.PApply;
import edu.clemson.resolve.proving.absyn.PExp;
import edu.clemson.resolve.vcgen.application.VCStatRuleApplicationStrategy;
import edu.clemson.resolve.vcgen.model.VCAssertiveBlock.VCAssertiveBlockBuilder;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

public class VCAssign extends VCRuleBackedStat {

    private final PExp left, right;

    public VCAssign(ParserRuleContext ctx,
                    VCAssertiveBlock.VCAssertiveBlockBuilder block,
                    VCStatRuleApplicationStrategy apply,
                    PExp left,
                    PExp right) {
        super(ctx, block, apply);
        this.left = left;
        this.right = right;
    }

    @NotNull
    public PExp getLeft() {
        return left;
    }

    @NotNull
    public PExp getRight() {
        return right;
    }

    public boolean isFunctionAssignment() {
        return right instanceof PApply;
    }

    @NotNull
    @Override
    public VCRuleBackedStat copyWithBlock(@NotNull VCAssertiveBlockBuilder b) {
        return new VCAssign(definingCtx, b, applicationStrategy, left, right);
    }
}
