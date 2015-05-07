package org.resolvelite.semantics;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.resolvelite.compiler.ErrorKind;
import org.resolvelite.compiler.ResolveCompiler;
import org.resolvelite.compiler.tree.AnnotatedTree;
import org.resolvelite.compiler.tree.ImportCollection;
import org.resolvelite.compiler.tree.ImportCollection.ImportType;
import org.resolvelite.parsing.ResolveBaseListener;
import org.resolvelite.parsing.ResolveParser;
import org.resolvelite.proving.absyn.PExp;
import org.resolvelite.proving.absyn.PExpBuildingListener;
import org.resolvelite.semantics.programtype.*;
import org.resolvelite.semantics.query.NameQuery;
import org.resolvelite.semantics.symbol.*;
import org.resolvelite.typereasoning.TypeGraph;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefSymbolsAndScopes extends ResolveBaseListener {

    protected ResolveCompiler compiler;
    protected SymbolTable symtab;
    protected AnnotatedTree tree;
    protected TypeGraph g;

    boolean walkingModuleParameter = false;

    DefSymbolsAndScopes(@NotNull ResolveCompiler rc,
            @NotNull SymbolTable symtab, AnnotatedTree annotatedTree) {
        this.compiler = rc;
        this.symtab = symtab;
        this.tree = annotatedTree;
        this.g = symtab.getTypeGraph();
    }

    @Override public void enterConceptModule(
            @NotNull ResolveParser.ConceptModuleContext ctx) {
        symtab.startModuleScope(tree).addImports(
                tree.imports.getImportsOfType(ImportType.NAMED));
        for (ResolveParser.GenericTypeContext generic : ctx.genericType()) {
            try {
                symtab.getInnermostActiveScope().define(
                        new GenericSymbol(g, generic.getText(), generic,
                                getRootModuleID()));
            }
            catch (DuplicateSymbolException dse) {
                compiler.errorManager.semanticError(ErrorKind.DUP_SYMBOL,
                        ctx.name, ctx.name.getText());
            }
        }
    }

    @Override public void exitConceptModule(
            @NotNull ResolveParser.ConceptModuleContext ctx) {
        symtab.endScope();
    }

    @Override public void enterFacilityModule(
            @NotNull ResolveParser.FacilityModuleContext ctx) {
        symtab.startModuleScope(tree).addImports(
                tree.imports.getImportsOfType(ImportType.NAMED));
    }

    @Override public void exitFacilityModule(
            @NotNull ResolveParser.FacilityModuleContext ctx) {
        symtab.endScope();
    }

    @Override public void exitFacilityDecl(
            @NotNull ResolveParser.FacilityDeclContext ctx) {
        try {
            symtab.getInnermostActiveScope().define(
                    new FacilitySymbol(ctx, getRootModuleID(), symtab));
        }
        catch (DuplicateSymbolException dse) {
            compiler.errorManager.semanticError(ErrorKind.DUP_SYMBOL, ctx.name,
                    ctx.name.getText());
        }
    }

    @Override public void enterModuleParameterDecl(
            @NotNull ResolveParser.ModuleParameterDeclContext ctx) {
        walkingModuleParameter = true;
    }

    @Override public void exitModuleParameterDecl(
            @NotNull ResolveParser.ModuleParameterDeclContext ctx) {
        walkingModuleParameter = false;
    }

    @Override public void enterTypeModelDecl(
            @NotNull ResolveParser.TypeModelDeclContext ctx) {
        symtab.startScope(ctx);
    }

    @Override public void exitTypeModelDecl(
            @NotNull ResolveParser.TypeModelDeclContext ctx) {
        MathSymbol exemplar = null;
        MTType modelType = null;
        try {
            //NOTE: Can't walk the whole ctx here just yet. Say exemplar is 'b',
            //and initialization stipulates that b = true. Since we only just get
            //around to adding the (typed) binding for exemplar 'b' below, we
            //won't be able to properly type all of 'ctx's subexpressions right
            //here.
            annotateExps(ctx.mathTypeExp());
            modelType = tree.mathTypeValues.get(ctx.mathTypeExp());
            exemplar =
                    symtab.getInnermostActiveScope()
                            .define(new MathSymbol(symtab.getTypeGraph(),
                                    ctx.exemplar.getText(), modelType, null,
                                    ctx, getRootModuleID())).toMathSymbol();
        }
        catch (DuplicateSymbolException e) {
            throw new RuntimeException("duplicate exemplar!??");
        }
        //now annotate types for all subexpressions within the typeModelDecl
        //tree (e.g. contraints, init, final, etc) before leaving the scope.
        annotateExps(ctx);
        symtab.endScope();
        if ( ctx.mathTypeExp().getText().equals(ctx.name.getText()) ) {
            compiler.errorManager.semanticError(ErrorKind.INVALID_MATH_MODEL,
                    ctx.mathTypeExp().getStart(), ctx.mathTypeExp().getText());
        }
        ParserRuleContext constraint =
                ctx.constraintClause() != null ? ctx.constraintClause() : null;
        ParserRuleContext initRequires =
                ctx.typeModelInit() != null ? ctx.typeModelInit()
                        .requiresClause() : null;
        ParserRuleContext initEnsures =
                ctx.typeModelInit() != null ? ctx.typeModelInit()
                        .ensuresClause() : null;
        ParserRuleContext finalRequires =
                ctx.typeModelFinal() != null ? ctx.typeModelFinal()
                        .requiresClause() : null;
        ParserRuleContext finalEnsures =
                ctx.typeModelFinal() != null ? ctx.typeModelFinal()
                        .ensuresClause() : null;
        try {
            symtab.getInnermostActiveScope().define(
                    new ProgTypeModelSymbol(symtab.getTypeGraph(), ctx.name
                            .getText(), modelType, new PTFamily(modelType,
                            ctx.name.getText(), ctx.exemplar.getText(),
                            buildPExp(constraint), buildPExp(initRequires),
                            buildPExp(initEnsures), buildPExp(finalRequires),
                            buildPExp(finalEnsures), getRootModuleID()),
                            exemplar, ctx, getRootModuleID()));
        }
        catch (DuplicateSymbolException e) {
            compiler.errorManager.semanticError(ErrorKind.DUP_SYMBOL, ctx.name,
                    ctx.name.getText());
        }
    }

    @Override public void enterTypeRepresentationDecl(
            @NotNull ResolveParser.TypeRepresentationDeclContext ctx) {
        symtab.startScope(ctx);
    }

    @Override public void exitTypeRepresentationDecl(
            @NotNull ResolveParser.TypeRepresentationDeclContext ctx) {
        ProgTypeModelSymbol typeDefn = null;
        try {
            typeDefn =
                    symtab.getInnermostActiveScope()
                            .queryForOne(new NameQuery(null, ctx.name, false))
                            .toProgTypeModelSymbol();
        }
        catch (DuplicateSymbolException dse) {
            compiler.errorManager.semanticError(ErrorKind.DUP_SYMBOL, ctx.name,
                    ctx.name.getText());
        }
        catch (NoSuchSymbolException|UnexpectedSymbolException e) {
            //if we don't find the type model at all (or we find something
            //that ISN'T a type model) -- it doesn't matter, we'll proceed under
            //the assumption that the thing isn't actually
        }
        String exemplarName =
                typeDefn != null ? typeDefn.getExemplar().getName() : ctx
                        .getText().substring(0, 1).toUpperCase();
        PTType baseType =
                ctx.record() != null ? getProgramType(ctx.record())
                        : getProgramType(ctx.type());
        annotateExps(ctx);

        ParserRuleContext initRequires =
                ctx.typeImplInit() != null ? ctx.typeImplInit()
                        .requiresClause() : null;
        ParserRuleContext initEnsures =
                ctx.typeImplInit() != null ? ctx.typeImplInit().ensuresClause()
                        : null;
        ParserRuleContext finalRequires =
                ctx.typeImplFinal() != null ? ctx.typeImplFinal()
                        .requiresClause() : null;
        ParserRuleContext finalEnsures =
                ctx.typeImplFinal() != null ? ctx.typeImplFinal()
                        .ensuresClause() : null;

        PTRepresentation reprType =
                new PTRepresentation(g, baseType, ctx.name.getText(), typeDefn,
                        buildPExp(initRequires), buildPExp(initEnsures),
                        buildPExp(finalRequires), buildPExp(finalEnsures),
                        getRootModuleID());
        try {
            symtab.getInnermostActiveScope().define(
                    new ProgVariableSymbol(exemplarName, ctx, reprType,
                            getRootModuleID()));
        }
        catch (DuplicateSymbolException e) {}
        symtab.endScope();
        try {
            symtab.getInnermostActiveScope().define(
                    new ProgReprTypeSymbol(g, ctx.name.getText(), ctx,
                            getRootModuleID(), typeDefn, reprType, ctx
                                    .conventionClause(), null));
        }
        catch (DuplicateSymbolException dse) {
            compiler.errorManager.semanticError(ErrorKind.DUP_SYMBOL, ctx.name,
                    ctx.name.getText());
        }
    }

    @Override public void enterOperationProcedureDecl(
            @NotNull ResolveParser.OperationProcedureDeclContext ctx) {
        symtab.startScope(ctx);
        try {
            if ( ctx.type() != null ) {
                symtab.getInnermostActiveScope().define(
                        new ProgVariableSymbol(ctx.name.getText(), ctx,
                                getProgramType(ctx.type()), getRootModuleID()));
            }
        }
        catch (DuplicateSymbolException e) {
            compiler.errorManager.semanticError(ErrorKind.DUP_SYMBOL, ctx.name,
                    ctx.name.getText());
        }
    }

    @Override public void exitOperationProcedureDecl(
            @NotNull ResolveParser.OperationProcedureDeclContext ctx) {
        annotateExps(ctx); //annotate all exps before we leave
        symtab.endScope();
        insertFunction(ctx.name, ctx, ctx.type());
    }

    @Override public void enterOperationDecl(
            @NotNull ResolveParser.OperationDeclContext ctx) {
        symtab.startScope(ctx);
        try {
            if ( ctx.type() != null ) {
                symtab.getInnermostActiveScope().define(
                        new MathSymbol(g, ctx.name.getText(), getProgramType(
                                ctx.type()).toMath(), null, ctx,
                                getRootModuleID()));
            }
        }
        catch (DuplicateSymbolException e) {
            compiler.errorManager.semanticError(ErrorKind.DUP_SYMBOL, ctx.name,
                    ctx.name.getText());
        }
    }

    @Override public void exitOperationDecl(
            @NotNull ResolveParser.OperationDeclContext ctx) {
        annotateExps(ctx); //annotate all exps before we leave
        symtab.endScope();
        insertFunction(ctx.name, ctx, ctx.type());
    }

    @Override public void exitParameterDeclGroup(
            @NotNull ResolveParser.ParameterDeclGroupContext ctx) {
        PTType programType = getProgramType(ctx.type());
        for (TerminalNode t : ctx.Identifier()) {
            try {
                ProgParameterSymbol.ParameterMode mode =
                        ProgParameterSymbol.getModeMapping().get(
                                ctx.parameterMode().getText());
                symtab.getInnermostActiveScope().define(
                        new ProgParameterSymbol(symtab.getTypeGraph(), t
                                .getText(), mode, programType, ctx,
                                getRootModuleID()));
            }
            catch (DuplicateSymbolException dse) {
                compiler.errorManager.semanticError(ErrorKind.DUP_SYMBOL,
                        t.getSymbol(), t.getText());
            }
        }
    }

    @Override public void exitVariableDeclGroup(
            @NotNull ResolveParser.VariableDeclGroupContext ctx) {
        insertVariables(ctx.Identifier(), ctx.type());
    }

    @Override public void exitRecordVariableDeclGroup(
            @NotNull ResolveParser.RecordVariableDeclGroupContext ctx) {
        insertVariables(ctx.Identifier(), ctx.type());
    }

    private void insertVariables(List<TerminalNode> terminalGroup,
            ResolveParser.TypeContext type) {
        PTType programType = getProgramType(type);
        for (TerminalNode t : terminalGroup) {
            try {
                ProgVariableSymbol vs =
                        new ProgVariableSymbol(t.getText(), t, programType,
                                getRootModuleID());
                symtab.getInnermostActiveScope().define(vs);
            }
            catch (DuplicateSymbolException dse) {
                compiler.errorManager.semanticError(ErrorKind.DUP_SYMBOL,
                        t.getSymbol(), t.getText());
            }
        }
    }

    private void insertFunction(@NotNull Token name, ParserRuleContext ctx,
            @Nullable ResolveParser.TypeContext type) {
        try {
            List<ProgParameterSymbol> params =
                    symtab.scopes.get(ctx).getSymbolsOfType(
                            ProgParameterSymbol.class);
            symtab.getInnermostActiveScope().define(
                    new OperationSymbol(symtab.getTypeGraph(), name.getText(),
                            ctx, getProgramType(type), getRootModuleID(),
                            params, walkingModuleParameter));
        }
        catch (DuplicateSymbolException dse) {
            compiler.errorManager.semanticError(ErrorKind.DUP_SYMBOL, name,
                    name.getText());
        }
    }

    protected PTType getProgramType(@Nullable ResolveParser.TypeContext type) {
        return type == null ? PTVoid.getInstance(g) : getProgramType(type,
                type.qualifier, type.name);
    }

    protected PTType getProgramType(@NotNull ParserRuleContext ctx,
            @Nullable Token qualifier, @NotNull Token typeName) {
        return getProgramType(compiler, ctx,
                qualifier != null ? qualifier.getText() : null,
                typeName.getText());
    }

    /**
     * Returns a {@link PTType} based on context {@code ctx}; {@link PTInvalid}
     * if the symbol retrieved was not typed properly.
     */
    protected static PTType getProgramType(ResolveCompiler compiler,
            @NotNull ParserRuleContext ctx, @Nullable String qualifier,
            @NotNull String typeName) {
        ProgTypeSymbol result = null;
        try {
            return compiler.symbolTable.getInnermostActiveScope()
                    .queryForOne(new NameQuery(qualifier, typeName, true))
                    .toProgTypeSymbol().getProgramType();
        }
        catch (NoSuchSymbolException | DuplicateSymbolException e) {
            compiler.errorManager.semanticError(e.getErrorKind(),
                    ctx.getStart(), typeName);
        }
        return PTInvalid.getInstance(compiler.symbolTable.getTypeGraph());
    }

    protected PTType getProgramType(@NotNull ResolveParser.RecordContext ctx) {
        Map<String, PTType> fields = new LinkedHashMap<>();
        for (ResolveParser.RecordVariableDeclGroupContext fieldGrp : ctx
                .recordVariableDeclGroup()) {
            PTType grpType = getProgramType(fieldGrp.type());
            for (TerminalNode t : fieldGrp.Identifier()) {
                fields.put(t.getText(), grpType);
            }
        }
        return new PTRecord(g, fields);
    }

    protected PExp buildPExp(ParserRuleContext ctx) {
        if ( ctx == null ) {
            return g.getTrueExp();
        }
        PExpBuildingListener<PExp> builder =
                new PExpBuildingListener<>(tree.mathTypes, tree.mathTypeValues);
        ParseTreeWalker.DEFAULT.walk(builder, ctx);
        return builder.getBuiltPExp(ctx);
    }

    /**
     * Annotates all expressions (and subexpressions) in parsetree {@code ctx}
     * with type info.
     * 
     * @param ctx The subtree to annotate.
     */
    protected final void annotateExps(@NotNull ParseTree ctx) {
        ComputeTypes annotator = new ComputeTypes(compiler, symtab, tree);
        ParseTreeWalker.DEFAULT.walk(annotator, ctx);
    }

    protected final String getRootModuleID() {
        return symtab.getInnermostActiveScope().getModuleID();
    }
}
