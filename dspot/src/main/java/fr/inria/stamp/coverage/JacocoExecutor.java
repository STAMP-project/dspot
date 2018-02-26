package fr.inria.stamp.coverage;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.test.launcher.TestLauncher;
import fr.inria.stamp.test.listener.TestListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.ResourceBundle.clearCache;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/17
 */
@Deprecated // TODO should be replaced by EntryPoint from eu.stamp.testrunner project
public class JacocoExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JacocoExecutor.class);

    private MemoryClassLoader internalClassLoader;

    private IRuntime runtime;

    private Instrumenter instrumenter;

    private InputProgram program;

    private InputConfiguration configuration;

    public JacocoExecutor(InputProgram program, InputConfiguration configuration, CtType<?> testClass) {
        this.configuration = configuration;
        this.program = program;
        this.runtime = new LoggerRuntime();
        this.instrumenter = new Instrumenter(this.runtime);
        this.instrumentAll(configuration, testClass);
    }

    private void instrumentAll(InputConfiguration configuration, CtType<?> testClass) {
        final String classesDirectory = this.program.getProgramDir() + "/" + this.program.getClassesDir();
        this.internalClassLoader = MemoryClassLoaderFactory.createMemoryClassLoader(testClass, configuration);
        /* instrument all of them */
        final Iterator<File> iterator = FileUtils.iterateFiles(new File(classesDirectory), new String[]{"class"}, true);
        while (iterator.hasNext()) {
            final File next = iterator.next();
            final String fileName = next.getPath().substring(classesDirectory.length());
            final String fullQualifiedName = fileName.replaceAll("/", ".").substring(0, fileName.length() - ".class".length());
            try {
                this.internalClassLoader.addDefinition(fullQualifiedName,
                        this.instrumenter.instrument(this.internalClassLoader.getResourceAsStream(fileName), fullQualifiedName));
            } catch (IOException e) {
                LOGGER.error("Encountered a problem while instrumenting " + fullQualifiedName);
                throw new RuntimeException(e);
            }
        }
        clearCache(this.internalClassLoader);
    }

    public CoverageResults executeJacoco(CtType<?> testClass) {
        final String testClassesDirectory = this.program.getProgramDir() + "/" + this.program.getTestClassesDir();
        final RuntimeData data = new RuntimeData();
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        URLClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]
                    {new File(testClassesDirectory).toURI().toURL()}, this.internalClassLoader);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        final String resource = testClass.getQualifiedName().replace('.', '/') + ".class";
        try {
            this.internalClassLoader.addDefinition(
                    testClass.getQualifiedName(),
                    IOUtils.toByteArray(classLoader.getResourceAsStream(resource))
            );
            runtime.startup(data);
            final TestListener listener = TestLauncher.run(this.configuration, this.internalClassLoader, testClass);
            if (!listener.getFailingTests().isEmpty()) {
                LOGGER.warn("Some test(s) failed during computation of coverage:" + AmplificationHelper.LINE_SEPARATOR +
                        listener.getFailingTests()
                                .stream()
                                .map(Failure::toString)
                                .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
                );
            }
            data.collect(executionData, sessionInfos, false);
            runtime.shutdown();
            clearCache(this.internalClassLoader);
            return coverageResults(executionData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public Map<String, CoverageResults> executeJacoco(CtType<?> testClass, Collection<String> methodNames) {
        final String testClassesDirectory = this.program.getProgramDir() + "/" + this.program.getTestClassesDir();
        final String classesDirectory = this.program.getProgramDir() + "/" + this.program.getClassesDir();
        URLClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]
                    {new File(testClassesDirectory).toURI().toURL()}, this.internalClassLoader);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        final String resource = testClass.getQualifiedName().replace('.', '/') + ".class";
        try {
            this.internalClassLoader.addDefinition(
                    testClass.getQualifiedName(),
                    IOUtils.toByteArray(classLoader.getResourceAsStream(resource))
            );
            final RuntimeData data = new RuntimeData();
            this.runtime.startup(data);
            final JacocoListener jacocoListener = new JacocoListener(data, classesDirectory);
            final TestListener listener = TestLauncher.run(this.configuration, this.internalClassLoader, testClass, methodNames, jacocoListener);
            if (!listener.getFailingTests().isEmpty()) {
                LOGGER.warn("Some test(s) failed during computation of coverage:" + AmplificationHelper.LINE_SEPARATOR +
                        listener.getFailingTests()
                                .stream()
                                .map(Failure::toString)
                                .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
                );
            }
            this.runtime.shutdown();
            clearCache(this.internalClassLoader);
            return jacocoListener.getCoverageResultsMap();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CoverageResults coverageResults(ExecutionDataStore executionData) {
        final String classesDirectory = this.program.getProgramDir() + "/" + this.program.getClassesDir();
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

        try {
            analyzer.analyzeAll(new File(classesDirectory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new CoverageResults(coverageBuilder);
    }

}
