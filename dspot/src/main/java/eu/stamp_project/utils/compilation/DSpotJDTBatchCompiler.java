package eu.stamp_project.utils.compilation;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.Util;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;
import spoon.support.compiler.jdt.JDTBatchCompiler;

import java.io.File;
import java.io.IOException;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/23/16
 */
public class DSpotJDTBatchCompiler extends JDTBatchCompiler {

    private FileSystem environment;

    public DSpotJDTBatchCompiler(JDTBasedSpoonCompiler jdtCompiler, FileSystem environment ) {
        super(jdtCompiler);
        this.environment = environment;
    }

    @Override
    public void performCompilation() {
        if(environment == null) {
            environment = this.getLibraryAccess();
        }

        this.startTime = System.currentTimeMillis();
        this.compilerOptions = new CompilerOptions(this.options);
        this.compilerOptions.performMethodsFullRecovery = false;
        this.compilerOptions.performStatementsRecovery = false;
        this.batchCompiler = new Compiler(environment, this.getHandlingPolicy(), this.compilerOptions, this.getBatchRequestor(), this.getProblemFactory(), this.out, this.progress);
        this.batchCompiler.remainingIterations = this.maxRepetition - this.currentRepetition;
        String setting = System.getProperty("jdt.compiler.useSingleThread");
        this.batchCompiler.useSingleThread = setting != null && setting.equals("true");

        this.compilerOptions.verbose = this.verbose;
        this.compilerOptions.produceReferenceInfo = this.produceRefInfo;

        try {
            this.logger.startLoggingSources();
            this.batchCompiler.compile(this.getCompilationUnits());
        } finally {
            this.logger.endLoggingSources();
        }
        this.logger.printStats();
    }


    @Override
    public CompilationUnit[] getCompilationUnits() {
        int fileCount = this.filenames.length;
        CompilationUnit[] units = new CompilationUnit[fileCount];
        HashtableOfObject knownFileNames = new HashtableOfObject(fileCount);
        String defaultEncoding = (String)this.options.get("org.eclipse.jdt.core.encoding");
        if(Util.EMPTY_STRING.equals(defaultEncoding)) {
            defaultEncoding = null;
        }

        for(int i = 0; i < fileCount; ++i) {
            char[] charName = this.filenames[i].toCharArray();
            if(knownFileNames.get(charName) != null) {
                throw new IllegalArgumentException(this.bind("unit.more", this.filenames[i]));
            }

            knownFileNames.put(charName, charName);
            File file = new File(this.filenames[i]);
            if(!file.exists()) {
                throw new IllegalArgumentException(this.bind("unit.missing", this.filenames[i]));
            }

            String encoding = this.encodings[i];
            if(encoding == null) {
                encoding = defaultEncoding;
            }

            String fileName;
            try {
                fileName = file.getCanonicalPath();
            } catch (IOException var10) {
                fileName = this.filenames[i];
            }
            units[i] = new CompilationUnit((char[])null, fileName, encoding, this.destinationPaths[i], false, null);
        }
        return units;
    }

    public FileSystem getEnvironment() {
        return environment;
    }


}