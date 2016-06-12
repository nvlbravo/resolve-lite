package edu.clemson.resolve.semantics;

import edu.clemson.resolve.misc.Utils;
import edu.clemson.resolve.parser.ResolveLexer;
import edu.clemson.resolve.parser.ResolveParser;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Identifies a particular module unambiguously.
 * <p>
 * <strong>Note:</strong> Currently, we only permit one level of namespace. But ultimately that will probably change
 * (because, for example, at this moment if there were two "Stack_Templates", we couldn't deal with that. A
 * java class-path-like solution seems inevitable.  For the moment however, this is just a wrapper around the token
 * based name of the module to facilitate changing how we deal with modules later.</p>
 * <p>
 * We use {@link Token}s internally here so we have position information for the module we're identifying is readily
 * available (mostly for error rendering purposes).</p>
 */
public class ModuleIdentifier implements Comparable<ModuleIdentifier> {

    @NotNull
    public static final ModuleIdentifier GLOBAL = new ModuleIdentifier();

    public final Set<String> tagAliases = new HashSet<>();
    @NotNull
    private Token name;
    private final boolean globalFlag;

    public List<Token> fromClausePath = new ArrayList<>();

    private ModuleIdentifier() {
        this.name = new CommonToken(ResolveLexer.ID, "GLOBAL");
        this.globalFlag = true;
    }

    public ModuleIdentifier(@NotNull ResolveParser.UsesSpecContext m) {
        this(m.ID().getSymbol(), m.alias() != null ?
                Utils.apply(m.fromClause().ID(), TerminalNode::getSymbol) : new ArrayList<>());
    }

    public ModuleIdentifier(Token t, @NotNull List<Token> fromPath) {
        this.name = t;
        this.fromClausePath.addAll(fromPath);
        this.globalFlag = false;
    }

    @NotNull
    public String getNameString() {
        return name.getText();
    }

    @NotNull public String getFullyQualifiedName() {
        return this.toString();
    }

    @NotNull
    public Token getNameToken() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        boolean result = (o instanceof ModuleIdentifier);
        if (result) {
            result = ((ModuleIdentifier) o).name.getText().equals(name.getText());
        }
        return result;
    }

    public int hashCode() {
        return name.getText().hashCode();
    }

    @Override
    public int compareTo(@NotNull ModuleIdentifier o) {
        return name.getText().compareTo(o.name.getText());
    }

    @NotNull
    public String toString() {
        List<String> pieces = Utils.apply(moduleIdentPieces, Token::getText);
        return Utils.join(pieces, ".");
    }
}
