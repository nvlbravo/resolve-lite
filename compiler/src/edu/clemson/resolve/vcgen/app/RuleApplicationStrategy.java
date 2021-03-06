package edu.clemson.resolve.vcgen.app;

import edu.clemson.resolve.vcgen.VCAssertiveBlock;
import edu.clemson.resolve.vcgen.VCAssertiveBlock.VCAssertiveBlockBuilder;
import org.jetbrains.annotations.NotNull;
import edu.clemson.resolve.vcgen.stats.VCRuleBackedStat;

import java.util.Deque;

public interface RuleApplicationStrategy<T extends VCRuleBackedStat> {

    @NotNull
    public VCAssertiveBlock applyRule(@NotNull Deque<VCAssertiveBlockBuilder> branches,
                                      @NotNull VCAssertiveBlockBuilder block,
                                      @NotNull T stat);
    @NotNull
    public String getDescription();
}
