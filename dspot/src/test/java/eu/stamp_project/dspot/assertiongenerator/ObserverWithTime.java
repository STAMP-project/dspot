package eu.stamp_project.dspot.assertiongenerator;

import eu.stamp_project.compare.ObjectLog;
import eu.stamp_project.compare.Observation;
import eu.stamp_project.dspot.AmplificationException;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.testmethodreconstructor_components.Observer;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.testmethodreconstructor_components.observer_components.TestWithLogGenerator;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
import eu.stamp_project.utils.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ObserverWithTime {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observer.class);

    private CtType originalClass;

    private InputConfiguration configuration;

    private DSpotCompiler compiler;

    private Map<CtMethod<?>, List<CtLocalVariable<?>>> variableReadsAsserted;

    public long timeInstrumentation;

    public long timeRunningInstrumentation;

    public ObserverWithTime(CtType originalClass,
                    InputConfiguration configuration,
                    DSpotCompiler compiler,
                    Map<CtMethod<?>, List<CtLocalVariable<?>>> variableReadsAsserted) {
        this.originalClass = originalClass;
        this.configuration = configuration;
        this.compiler = compiler;
        this.variableReadsAsserted = variableReadsAsserted;
    }

    /**
     * Instruments logs to tests and then compiles and runs tests to gather observation point values.
     * <p>
     * <p>Details for test instrumentation with logs in {@link TestWithLogGenerator#createTestWithLog(CtMethod, String, List)}.
     *
     * @param testClass Test class
     * @param testCases Passing test methods
     * @return Observation point values
     * @throws AmplificationException
     */
    public Map<String, Observation>  getObservations(CtType<?> testClass, List<CtMethod<?>> testCases) throws AmplificationException {
        CtType clone = testClass.clone();
        testClass.getPackage().addType(clone);
        LOGGER.info("Add observations points in passing tests.");
        LOGGER.info("Instrumentation...");
        long start = System.currentTimeMillis();
        final List<CtMethod<?>> testCasesWithLogs;
        testCasesWithLogs = addLogs(testCases);
        final List<CtMethod<?>> testsToRun = setupTests(testCasesWithLogs,clone);
        timeInstrumentation = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        Map<String, Observation> observations = compileRunTests(clone,testsToRun);
        timeRunningInstrumentation = System.currentTimeMillis() - start;
        return observations;
    }

    // add logs in tests to observe state of tested program
    private List<CtMethod<?>> addLogs(List<CtMethod<?>> testCases) {
        final List<CtMethod<?>> testCasesWithLogs = testCases.stream()
                .map(ctMethod -> {
                            DSpotUtils.printProgress(testCases.indexOf(ctMethod), testCases.size());
                            return TestWithLogGenerator.createTestWithLog(
                                    ctMethod,
                                    this.originalClass.getPackage().getQualifiedName(),
                                    this.variableReadsAsserted.get(ctMethod)
                            );
                        }
                ).collect(Collectors.toList());
        return testCasesWithLogs;
    }

    // clone and set up tests with logs
    private List<CtMethod<?>> setupTests(List<CtMethod<?>> testCasesWithLogs, CtType clone){
        final List<CtMethod<?>> testsToRun = new ArrayList<>();
        IntStream.range(0, 1).forEach(i -> testsToRun.addAll(
                testCasesWithLogs.stream()
                        .map(CtMethod::clone)
                        .map(ctMethod -> {
                            ctMethod.setSimpleName(ctMethod.getSimpleName() + i);
                            return ctMethod;
                        })
                        .map(ctMethod -> {
                            clone.addMethod(ctMethod);
                            return ctMethod;
                        })
                        .collect(Collectors.toList())
        ));
        ObjectLog.reset();
        return testsToRun;
    }

    // compile and run tests with logs
    private Map<String, Observation> compileRunTests(CtType clone, final List<CtMethod<?>> testsToRun) throws AmplificationException{
        LOGGER.info("Run instrumented tests. ({})", testsToRun.size());
        TestFramework.get().generateAfterClassToSaveObservations(clone, testsToRun);
        final TestResult result = TestCompiler.compileAndRun(clone,
                this.compiler,
                testsToRun,
                this.configuration
        );
        if (!result.getFailingTests().isEmpty()) {
            LOGGER.warn("Some instrumented test failed!");
        }
        return ObjectLog.getObservations();
    }

    public void reset() {
        this.timeInstrumentation = 0;
        this.timeRunningInstrumentation = 0;
    }
}
