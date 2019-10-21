package eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor;

import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.ObjectLog;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.Observation;
import eu.stamp_project.dspot.AmplificationException;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.TestWithLogGenerator;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
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

/**
 * Created by Andrew Bwogi
 * abwogi@kth.se
 * on 26/08/19
 */
public class Observer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observer.class);

    private CtType originalClass;

    private DSpotCompiler compiler;

    private Map<CtMethod<?>, List<CtLocalVariable<?>>> variableReadsAsserted;

    private TestCompiler testCompiler;

    public Observer(CtType originalClass,
                    DSpotCompiler compiler,
                    Map<CtMethod<?>, List<CtLocalVariable<?>>> variableReadsAsserted,
                    TestCompiler testCompiler) {
        this.originalClass = originalClass;
        this.compiler = compiler;
        this.variableReadsAsserted = variableReadsAsserted;
        this.testCompiler = testCompiler;
    }

    /**
     * Instruments logs to tests and then compiles and runs tests to gather observation point values.
     *
     * Details for test instrumentation with logs in {@link TestWithLogGenerator#createTestWithLog(CtMethod, String, List)}.
     *
     * @param testClass Test class
     * @param testCases Passing test methods
     * @return Observation point values
     * @throws AmplificationException when something wrong happens
     */
    public Map<String, Observation>  getObservations(CtType<?> testClass, List<CtMethod<?>> testCases) throws AmplificationException {
        CtType clone = testClass.clone();
        testClass.getPackage().addType(clone);
        LOGGER.info("Add observations points in passing tests.");
        LOGGER.info("Instrumentation...");
        final List<CtMethod<?>> testCasesWithLogs;
        testCasesWithLogs = addLogs(testCases);
        final List<CtMethod<?>> testsToRun = setupTests(testCasesWithLogs,clone);
        return compileRunTests(clone,testsToRun);
    }

    // add logs in tests to observe state of tested program
    private List<CtMethod<?>> addLogs(List<CtMethod<?>> testCases) throws AmplificationException{
        List<CtMethod<?>> testCasesWithLogs = testCases.stream()
                .map(ctMethod -> {
                            DSpotUtils.printProgress(testCases.indexOf(ctMethod), testCases.size());
                            return TestWithLogGenerator.createTestWithLog(
                                    ctMethod,
                                    this.originalClass.getPackage().getQualifiedName(),
                                    this.variableReadsAsserted.get(ctMethod)
                            );
                        }
                ).filter(ctMethod -> !ctMethod.getBody().getStatements().isEmpty())
                .collect(Collectors.toList());
        if (testCasesWithLogs.isEmpty()) {
            LOGGER.warn("Could not continue the assertion amplification since all the instrumented test have an empty body.");
            throw new AmplificationException("All instrumented tests have an empty body.");
        }
        return testCasesWithLogs;
    }

    // clone and set up tests with logs
    private List<CtMethod<?>> setupTests(List<CtMethod<?>> testCasesWithLogs, CtType clone){
        final List<CtMethod<?>> testsToRun = new ArrayList<>();
        IntStream.range(0, 3).forEach(i -> testsToRun.addAll(
                testCasesWithLogs.stream()

                        //Optimization: Tracking cloned test methods using AmplificationHelper as candidates
                        //for caching their associated Test Framework
                        .map(CloneHelper::cloneMethod)
                        .peek(ctMethod -> ctMethod.setSimpleName(ctMethod.getSimpleName() + i))
                        .peek(clone::addMethod)
                        .collect(Collectors.toList())
        ));
        ObjectLog.reset();
        return testsToRun;
    }

    // compile and run tests with logs
    private Map<String, Observation> compileRunTests(CtType clone, final List<CtMethod<?>> testsToRun) throws AmplificationException{
        LOGGER.info("Run instrumented tests. ({})", testsToRun.size());
        TestFramework.get().generateAfterClassToSaveObservations(clone, testsToRun);
        final TestResult result = this.testCompiler.compileAndRun(clone,
                this.compiler,
                testsToRun
        );
        if (!result.getFailingTests().isEmpty()) {
            LOGGER.warn("Some instrumented test failed!");
        }
        return ObjectLog.getObservations();
    }
}
