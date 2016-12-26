package fr.inria.diversify.dspot.support;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.Log;
import org.apache.commons.io.output.NullWriter;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import spoon.compiler.Environment;
import spoon.compiler.ModelBuildingException;
import spoon.compiler.builder.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.DefaultCoreFactory;
import spoon.support.JavaOutputProcessor;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.FileSystemFolder;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/22/16
 */
public class DSpotCompiler extends JDTBasedSpoonCompiler {

    private DiversifyClassLoader customClassLoader;
    private FileSystem environment;

    public static DSpotCompiler buildCompiler(InputProgram program, boolean withTest) {
        StandardEnvironment env = new StandardEnvironment();
        env.setComplianceLevel(program.getJavaVersion());
        env.setVerbose(true);
        env.setDebug(true);
        env.setNoClasspath(false);

        DefaultCoreFactory f = new DefaultCoreFactory();
        Factory factory = new FactoryImpl(f, env);
        DSpotCompiler compiler = new DSpotCompiler(factory);
        for (String s : buildSourceDirectoriesList(program, withTest)) {
            for (String dir : s.split(System.getProperty("path.separator"))) {
                try {
                    if (!dir.isEmpty()) {
                        Log.debug("add {} to classpath", dir);
                        File dirFile = new File(dir);
                        if (dirFile.isDirectory()) {
                            compiler.addInputSource(dirFile);
                        }
                    }
                } catch (Exception e) {
                    Log.error("error in initSpoon", e);
                    throw new RuntimeException(e);
                }
            }
        }
        String[] sourceClasspath = Arrays.stream(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
                .map(url -> {
                    try {
                        return url.toURI();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(uri -> new File(uri).exists())
                .map(URI::getPath)
                .toArray(String[]::new);
        compiler.setSourceClasspath(sourceClasspath);

        try {
            compiler.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        program.setFactory(compiler.getFactory());
        compiler.init();
        return compiler;
    }

    private void init() {
        if (this.getBinaryOutputDirectory() == null) {
            File classOutputDir = new File("tmpDir/tmpClasses_" + System.currentTimeMillis());
            if (!classOutputDir.exists()) {
                classOutputDir.mkdirs();
            }
            this.setBinaryOutputDirectory(classOutputDir);
        }
        if (this.getSourceOutputDirectory().toString().equals("spooned")) {
            File sourceOutputDir = new File("tmpDir/tmpSrc_" + System.currentTimeMillis());
            if (!sourceOutputDir.exists()) {
                sourceOutputDir.mkdirs();
            }
            this.setSourceOutputDirectory(sourceOutputDir);
        }

        Environment env = this.getFactory().getEnvironment();
        env.setDefaultFileGenerator(new JavaOutputProcessor(this.getSourceOutputDirectory(),
                new DefaultJavaPrettyPrinter(env)));
    }

    private static Collection<String> buildSourceDirectoriesList(InputProgram program, boolean withTest) {
        ArrayList<String> sourceDirectoryList = new ArrayList<String>();
        sourceDirectoryList.add(program.getAbsoluteSourceCodeDir());
        if (withTest) {
            sourceDirectoryList.add(program.getAbsoluteTestSourceCodeDir());
        }
        sourceDirectoryList.add(program.getExternalSourceCodeDir());
        return sourceDirectoryList;
    }

    public DSpotCompiler(Factory factory) {
        super(factory);
    }

    public DSpotCompiler(Factory factory, DiversifyClassLoader classLoader) {
        super(factory);
        this.customClassLoader = classLoader;
    }

    public void setCustomClassLoader(DiversifyClassLoader customClassLoader) {
        this.customClassLoader = customClassLoader;
    }

    public boolean compileFileIn(File directory, boolean withLog) {
//        initInputClassLoader();
        javaCompliance = factory.getEnvironment().getComplianceLevel();
        javaCompliance = 8;

//        MainCompiler compiler = new MainCompiler(this, true, environment);
        DSpotJDTBatchCompiler compiler = new DSpotJDTBatchCompiler(this, true, environment);

        final SourceOptions sourcesOptions = new SourceOptions();
        sourcesOptions.sources((new FileSystemFolder(directory).getAllJavaFiles()));


        String[] finalClassPath = getFinalClassPathAsStrings();

        final ClasspathOptions classpathOptions = new ClasspathOptions()
                .encoding(this.encoding)
                .classpath(finalClassPath)
                .binaries(getBinaryOutputDirectory());

        final String[] args = new JDTBuilderImpl() //
                .classpathOptions(classpathOptions) //
                .complianceOptions(new ComplianceOptions().compliance(javaCompliance)) //
                .advancedOptions(new AdvancedOptions().preserveUnusedVars().continueExecution().enableJavadoc()) //
                .annotationProcessingOptions(new AnnotationProcessingOptions().compileProcessors()) //
                .sources(sourcesOptions) //
                .build();

        final String[] finalArgs = new String[args.length + 1];
        finalArgs[0] = "-proceedOnError";
        for (int i = 0; i < args.length; i++) {
            finalArgs[i + 1] = args[i];
        }

        if (!withLog) {
            compiler.logger = new Main.Logger(compiler, new PrintWriter(new NullWriter()), new PrintWriter(new NullWriter()));
        }

        compiler.compile(finalArgs);
        environment = compiler.getEnvironment();

        return compiler.globalErrorsCount == 0;
    }


    private String[] getFinalClassPathAsStrings() {
        URL[] urls;
        if (customClassLoader != null) {
            urls = customClassLoader.getURLs();
        } else {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            urls = ((URLClassLoader) classLoader).getURLs();
        }
        String[] finalClassPath = new String[urls.length];

        for (int i = 0; i < urls.length; i++) {
            finalClassPath[i] = urls[i].getFile();
        }
        return finalClassPath;
    }

    protected void report(Environment environment, CategorizedProblem problem) {
        if (problem == null) {
            throw new IllegalArgumentException("problem cannot be null");
        }

        File file = new File(new String(problem.getOriginatingFileName()));
        String filename = file.getAbsolutePath();

        String message = problem.getMessage() + " at " + filename + ":"
                + problem.getSourceLineNumber();

        if (problem.isError()) {
            if (!environment.getNoClasspath()) {
                // by default, compilation errors are notified as exception
                throw new ModelBuildingException(message);
            }
        }

    }

}
