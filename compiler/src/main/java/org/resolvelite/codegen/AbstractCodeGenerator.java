package org.resolvelite.codegen;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.resolvelite.codegen.CodeGenerator;
import org.resolvelite.codegen.model.OutputModelObject;
import org.resolvelite.compiler.ErrorKind;
import org.resolvelite.compiler.ResolveCompiler;
import org.resolvelite.compiler.tree.AnnotatedTree;
import org.stringtemplate.v4.*;
import org.stringtemplate.v4.misc.STMessage;

import java.io.IOException;
import java.io.Writer;

public abstract class AbstractCodeGenerator {

    public static final String TEMPLATE_ROOT =
            "org/resolvelite/templates/codegen";

    protected final String language;
    protected final ResolveCompiler compiler;
    protected final AnnotatedTree module;
    protected final STGroup templates;

    public final int lineWidth = 72;

    public AbstractCodeGenerator(ResolveCompiler rc, AnnotatedTree rootTarget,
            String language) throws IllegalStateException {
        this.compiler = rc;
        this.module = rootTarget;
        this.language = language;
        this.templates = loadTemplates();
        if ( templates == null ) {
            throw new IllegalStateException();
        }
    }

    @NotNull public AnnotatedTree getModule() {
        return module;
    }

    @NotNull public ResolveCompiler getCompiler() {
        return compiler;
    }

    protected ST walk(OutputModelObject outputModel) {
        ModelConverter walker = new ModelConverter(compiler, templates);
        return walker.walk(outputModel);
    }

    public String getFileName() {
        ST extST = templates.getInstanceOf("fileExtension");
        String moduleName = module.getName();
        return moduleName + extST.render();
    }

    public void writeFile(ST outputFileST) {
        write(outputFileST, getFileName());
    }

    private void write(ST code, String fileName) {
        try {
            Writer w = compiler.getOutputFileWriter(module, fileName);
            STWriter wr = new AutoIndentWriter(w);
            wr.setLineWidth(lineWidth);
            code.write(wr);
            w.close();
        }
        catch (IOException ioe) {
            compiler.errorManager.toolError(ErrorKind.CANNOT_WRITE_FILE, ioe,
                    fileName);
        }
    }

    @Nullable public STGroup loadTemplates() {
        String groupFileName =
                CodeGenerator.TEMPLATE_ROOT + "/" + language + "/" + language
                        + STGroup.GROUP_FILE_EXTENSION;
        STGroup result = null;
        try {
            result = new STGroupFile(groupFileName);
        }
        catch (IllegalArgumentException iae) {
            compiler.errorManager.toolError(
                    ErrorKind.MISSING_CODE_GEN_TEMPLATES, iae, language);
        }
        if ( result == null ) {
            return null;
        }
        result.registerRenderer(Integer.class, new NumberRenderer());
        result.registerRenderer(String.class, new StringRenderer());
        result.setListener(new STErrorListener() {

            @Override public void compileTimeError(STMessage msg) {
                reportError(msg);
            }

            @Override public void runTimeError(STMessage msg) {
                reportError(msg);
            }

            @Override public void IOError(STMessage msg) {
                reportError(msg);
            }

            @Override public void internalError(STMessage msg) {
                reportError(msg);
            }

            private void reportError(STMessage msg) {
                compiler.errorManager.toolError(
                        ErrorKind.STRING_TEMPLATE_WARNING, msg.cause,
                        msg.toString());
            }
        });
        return result;
    }
}