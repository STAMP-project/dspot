package fr.inria.diversify.dspot.support;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
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
        if(this.compilerOptions.complianceLevel >= 3276800L && this.compilerOptions.processAnnotations) {
            if(this.checkVMVersion(3276800L)) {
                this.initializeAnnotationProcessorManager();
                if(this.classNames != null) {
                    this.batchCompiler.setBinaryTypes(this.processClassNames(this.batchCompiler.lookupEnvironment));
                }
            } else {
                this.logger.logIncorrectVMVersionForAnnotationProcessing();
            }
        }

        this.compilerOptions.verbose = this.verbose;
        this.compilerOptions.produceReferenceInfo = this.produceRefInfo;

        try {
            this.logger.startLoggingSources();
            this.batchCompiler.compile(this.getCompilationUnits());
        } finally {
            this.logger.endLoggingSources();
        }

        if(this.extraProblems != null) {
            this.loggingExtraProblems();
            this.extraProblems = null;
        }

        if(this.compilerStats != null) {
            this.compilerStats[this.currentRepetition] = this.batchCompiler.stats;
        }

        this.logger.printStats();
    }

    private ReferenceBinding[] processClassNames(LookupEnvironment environment) {
        int length = this.classNames.length;
        ReferenceBinding[] referenceBindings = new ReferenceBinding[length];

        for(int i = 0; i < length; ++i) {
            String currentName = this.classNames[i];
            Object compoundName = null;
            char[][] var8;
            if(currentName.indexOf(46) != -1) {
                char[] type = currentName.toCharArray();
                var8 = CharOperation.splitOn('.', type);
            } else {
                var8 = new char[][]{currentName.toCharArray()};
            }

            ReferenceBinding var9 = environment.getType(var8);
            if(var9 == null || !var9.isValidBinding()) {
                throw new IllegalArgumentException(this.bind("configure.invalidClassName", currentName));
            }

            if(var9.isBinaryBinding()) {
                referenceBindings[i] = var9;
            }
        }

        return referenceBindings;
    }

    private boolean checkVMVersion(long minimalSupportedVersion) {
        String classFileVersion = System.getProperty("java.class.version");
        if(classFileVersion == null) {
            return false;
        } else {
            int index = classFileVersion.indexOf(46);
            if(index == -1) {
                return false;
            } else {
                int majorVersion;
                try {
                    majorVersion = Integer.parseInt(classFileVersion.substring(0, index));
                } catch (NumberFormatException var6) {
                    return false;
                }

                switch(majorVersion) {
                    case 45:
                        if(2949123L >= minimalSupportedVersion) {
                            return true;
                        }

                        return false;
                    case 46:
                        if(3014656L >= minimalSupportedVersion) {
                            return true;
                        }

                        return false;
                    case 47:
                        if(3080192L >= minimalSupportedVersion) {
                            return true;
                        }

                        return false;
                    case 48:
                        if(3145728L >= minimalSupportedVersion) {
                            return true;
                        }

                        return false;
                    case 49:
                        if(3211264L >= minimalSupportedVersion) {
                            return true;
                        }

                        return false;
                    case 50:
                        if(3276800L >= minimalSupportedVersion) {
                            return true;
                        }

                        return false;
                    case 51:
                        if(3342336L >= minimalSupportedVersion) {
                            return true;
                        }

                        return false;
                    case 52:
                        if(3407872L >= minimalSupportedVersion) {
                            return true;
                        }

                        return false;
                    default:
                        return false;
                }
            }
        }
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