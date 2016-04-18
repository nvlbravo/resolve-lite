package org.rsrg.semantics.symbol;

import edu.clemson.resolve.parser.ResolveParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rsrg.semantics.DumbTypeGraph;
import org.rsrg.semantics.MathClassification;
import org.rsrg.semantics.ModuleIdentifier;
import org.antlr.v4.runtime.ParserRuleContext;
import org.rsrg.semantics.programtype.ProgFamilyType;

/** Describes a "Type family .." introduction as would be found in an
 *  {@link ResolveParser.ConceptModuleDeclContext} or
 *  {@link ResolveParser.ConceptExtensionModuleDeclContext}
 */
public class TypeModelSymbol extends ProgTypeSymbol {

    @NotNull private final MathClssftnWrappingSymbol exemplar;

    public TypeModelSymbol(@NotNull DumbTypeGraph g, @NotNull String name,
                           @NotNull MathClassification modelType,
                           @NotNull ProgFamilyType programType,
                           @NotNull MathClssftnWrappingSymbol exemplar,
                           @Nullable ParserRuleContext definingTree,
                           @NotNull ModuleIdentifier moduleIdentifier) {
        super(g, name, programType, modelType, definingTree, moduleIdentifier);
        this.exemplar = exemplar;
    }

    @NotNull public MathClssftnWrappingSymbol getExemplar() {
        return exemplar;
    }

    @NotNull @Override public ProgFamilyType getProgramType() {
        return (ProgFamilyType) super.getProgramType();
    }

    @NotNull @Override public TypeModelSymbol toTypeModelSymbol() {
        return this;
    }
}
