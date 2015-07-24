package edu.clemson.resolve.misc;

import edu.clemson.resolve.codegen.CodeGenPipeline;
import edu.clemson.resolve.compiler.ErrorKind;
import edu.clemson.resolve.compiler.RESOLVECompiler;
import org.antlr.v4.runtime.misc.NotNull;
import org.codehaus.plexus.util.FileUtils;

import javax.tools.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;

public class Archiver {

    private final List<CodeGenPipeline.JarUnit> rawJavaSrcs = new ArrayList<>();
    private final String entryPointName, tmpdir;
    private final RESOLVECompiler resolveCompiler;

    public Archiver(RESOLVECompiler rc, String entryPoint,
                    List<CodeGenPipeline.JarUnit> javaSrcsToPackage) {
        this.resolveCompiler = rc;
        this.rawJavaSrcs.addAll(javaSrcsToPackage);
        this.entryPointName = entryPoint;

        //create the temp dir that will house our .java and .class files.
        try {
            this.tmpdir = Files.createTempDirectory("RESOLVE_").toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void archive() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        GenCodeDiagnosticListener listener =
                new GenCodeDiagnosticListener(resolveCompiler);
        StandardJavaFileManager fileManager  =
                compiler.getStandardFileManager(listener, null, null);

        List<String> filesToCompile = new ArrayList<>();
        //write all .java files to tmpdir
        for (CodeGenPipeline.JarUnit u : rawJavaSrcs) {
            String javaClassName = u.javaClassName+".java";
            Utils.writeFile(tmpdir, javaClassName, u.javaClassSrc);
            filesToCompile.add(tmpdir + File.separator + javaClassName);
        }

        //copy over the external java files
        File source = new File(RESOLVECompiler.getCoreLibraryDirectory()
                + File.separator + "external");
        if (source == null) {
            throw new IllegalStateException("invalid installation -- missing"
                    + "external folder");
        }
        for (File externalFile : source.listFiles()) {
            try {
                File destFile = new File(tmpdir + File.separator
                                + externalFile.getName());
                Files.copy(externalFile.toPath(), destFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                filesToCompile.add(destFile.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Iterable<? extends JavaFileObject> fileObjects =
                fileManager.getJavaFileObjectsFromStrings(filesToCompile);
        JavaCompiler.CompilationTask task = compiler.getTask(null,
                fileManager, listener, null, null, fileObjects);
        Boolean result = task.call();
        if ( result ) {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, ".");
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, entryPointName);
            JarOutputStream target = null;
            try {
                target = new JarOutputStream(
                        new FileOutputStream(resolveCompiler.outputDirectory
                                + File.separator
                                + entryPointName+".jar"), manifest);
                add(new File(tmpdir), target);
                target.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        eraseTempDir();
    }

    private void add(File source, JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            for (File f : source.listFiles()) {
                JarEntry entry = new JarEntry(f.getName());
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);
                in = new BufferedInputStream(new FileInputStream(f));

                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) break;
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            }
        }
        finally {
            if ( in != null ) in.close();
        }
    }

    protected void eraseFiles() {
        File tmpdirF = new File(tmpdir);
        String[] files = tmpdirF.list();
        for(int i = 0; files!=null && i < files.length; i++) {
            new File(tmpdir+"/"+files[i]).delete();
        }
    }

    protected void eraseTempDir() {
        File tmpdirF = new File(tmpdir);
        if ( tmpdirF.exists() ) {
            eraseFiles();
            tmpdirF.delete();
        }
    }

    class GenCodeDiagnosticListener implements DiagnosticListener {
        private final RESOLVECompiler compiler;

        public GenCodeDiagnosticListener(RESOLVECompiler rc) {
            this.compiler = rc;
        }
        @Override public void report(Diagnostic diagnostic) {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                compiler.errMgr.toolError(ErrorKind.GENERATED_JAVA_ERROR,
                        diagnostic.getMessage(Locale.ENGLISH));
            }
        }
    }
}
