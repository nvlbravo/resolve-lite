package edu.clemson.resolve.semantics.programtype;

import edu.clemson.resolve.proving.absyn.PExp;
import edu.clemson.resolve.proving.absyn.PSymbol;
import org.jetbrains.annotations.NotNull;
import edu.clemson.resolve.semantics.DumbMathClssftnHandler;
import edu.clemson.resolve.semantics.ModuleIdentifier;

/**
 * Named types for which we know initialization and exemplar info. Note then
 * that this does not include generics.
 */
public abstract class ProgNamedType extends ProgType {

    @NotNull
    private final String name;
    @NotNull
    protected final PExp initEnsures;

    /**
     * Which module does this {@code ProgType}s reference appear in?
     */
    @NotNull
    private final ModuleIdentifier moduleIdentifier;

    public ProgNamedType(@NotNull DumbMathClssftnHandler g,
                         @NotNull String name,
                         @NotNull PExp initEnsures,
                         @NotNull ModuleIdentifier moduleIdentifier) {
        super(g);
        this.name = name;
        this.initEnsures = initEnsures;
        this.moduleIdentifier = moduleIdentifier;
    }

    @NotNull
    public ModuleIdentifier getModuleIdentifier() {
        return moduleIdentifier;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public abstract String getExemplarName();

    @NotNull
    public PSymbol getExemplarAsPSymbol() {
        return getExemplarAsPSymbol(false);
    }

    @NotNull
    public PSymbol getExemplarAsPSymbol(boolean incoming) {
        return new PSymbol.PSymbolBuilder(getExemplarName()).mathClssfctn(toMath()).build();
    }

    @NotNull
    public PExp getInitializationEnsures() {
        return initEnsures;
    }

    @Override
    public String toString() {
        return name;
    }

}
