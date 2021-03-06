package edu.clemson.resolve.compiler;

import edu.clemson.resolve.compiler.DependencyCollectingListener.DependencyHolder;
import edu.clemson.resolve.proving.absyn.PExp;
import edu.clemson.resolve.vcgen.VCOutputFile;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import edu.clemson.resolve.semantics.DumbMathClssftnHandler;
import edu.clemson.resolve.semantics.MathClssftn;
import edu.clemson.resolve.semantics.ModuleIdentifier;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import edu.clemson.resolve.semantics.programtype.ProgType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Represents a collection of information to be associated with a top level
 * {@link edu.clemson.resolve.parser.ResolveParser.ModuleDeclContext}.
 * <p>
 * We use this approach over {@code returns} clauses in the grammar to help us keep our grammar as general
 * as possible.</p>
 *
 * @author dtwelch
 */
public class AnnotatedModule {

    /**
     * Any infix app that's defined (via a definition) to be chainable (i.e.: one where x OP y OP z is,
     * internally, interpreted as "x OP y and y OP z"). Note that any operator defined
     * to be chainable had better be a predicate as it will become an argument to binary "and".
     */
    public ParseTreeProperty<Boolean> chainableInfixApps = new ParseTreeProperty<>();

    public ParseTreeProperty<MathClssftn> mathClssftns = new ParseTreeProperty<>();
    public ParseTreeProperty<ProgType> progTypes = new ParseTreeProperty<>();
    /**
     * For each {@link edu.clemson.resolve.parser.ResolveParser.MathExpContext} and
     * {@link edu.clemson.resolve.parser.ResolveParser.ProgExpContext}, this map keeps a pointer to its corresponding
     * AST (represented by {@link PExp}).
     */
    public ParseTreeProperty<PExp> exprASTs = new ParseTreeProperty<>();

    /** Aliases to modules -- this gets passed off to various moduleScope's */
    public final Map<String, ModuleIdentifier> aliases = new HashMap<>();

    private final String fileName;
    private final Token name;
    private final ParseTree root;
    private VCOutputFile vcs = null;

    public boolean hasParseErrors;
    private final ModuleIdentifier identifier;
    private final DependencyHolder dependencies;

    public AnnotatedModule(@NotNull ParseTree root,
                           @NotNull Token name,
                           @NotNull String fileName,
                           boolean hasParseErrors,
                           @NotNull DependencyHolder dependencies) {
        this.hasParseErrors = hasParseErrors;
        this.root = root;
        this.name = name;
        this.fileName = fileName;
        this.dependencies = dependencies;
        this.identifier = new ModuleIdentifier(name, new File(fileName));
    }

    public AnnotatedModule(@NotNull ParseTree root,
                           @NotNull Token name,
                           @NotNull String fileName,
                           boolean hasParseErrors,
                           @NotNull Set<ModuleIdentifier> uses) {
        this(root, name, fileName, hasParseErrors, new DependencyCollectingListener.DependencyHolderBuilder(uses).build());
    }

    public AnnotatedModule(@NotNull ParseTree root, @NotNull Token name, @NotNull String fileName) {
        this(root, name, fileName, false, Collections.emptySet());
    }

    @NotNull
    public DependencyHolder getDependencies() {
        return dependencies;
    }

    @NotNull
    public ModuleIdentifier getModuleIdentifier() {
        return identifier;
    }

    public void setVCs(@Nullable VCOutputFile vco) {
        this.vcs = vco;
    }

    @NotNull
    public PExp getMathExpASTFor(@NotNull DumbMathClssftnHandler g, @Nullable ParserRuleContext ctx) {
        PExp result = exprASTs.get(ctx);
        return result != null ? result : g.getTrueExp();
    }

    public boolean chainableCtx(@NotNull ParserRuleContext ctx) {
        boolean result = chainableInfixApps.get(ctx) != null;
        if (result) {
            result = chainableInfixApps.get(ctx);
        }
        return result;
    }

    @NotNull
    public Token getNameToken() {
        return name;
    }

    @NotNull
    public String getFilePath() {
        return fileName;
    }

    @NotNull
    public ParseTree getRoot() {
        return root;
    }

    @Nullable
    public VCOutputFile getVCOutput() {
        return vcs;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = (o instanceof AnnotatedModule);
        if (result) {
            result = this.name.getText().equals(((AnnotatedModule) o).name.getText());
        }
        return result;
    }

    @Override
    public String toString() {
        return name.getText();
    }

}