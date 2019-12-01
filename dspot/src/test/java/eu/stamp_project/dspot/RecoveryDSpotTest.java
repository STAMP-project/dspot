package eu.stamp_project.dspot;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.amplifier.amplifiers.Amplifier;
import eu.stamp_project.dspot.assertiongenerator.AssertionGenerator;
import eu.stamp_project.dspot.amplifier.TextualDistanceInputAmplDistributor;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.selector.TakeAllSelector;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.compilation.TestCompiler;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.options.AutomaticBuilderEnum;
import eu.stamp_project.dspot.common.report.error.ErrorEnum;
import eu.stamp_project.dspot.common.configuration.test_finder.TestFinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/10/18
 */
public class RecoveryDSpotTest extends AbstractTestOnSample {

    private AutomaticBuilder builder;

    private UserInput configuration;

    private TestCompiler testCompiler;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        DSpotState.GLOBAL_REPORT.reset();
        configuration = new UserInput();
        configuration.setAbsolutePathToProjectRoot(this.getPathToProjectRoot());
        this.builder = AutomaticBuilderEnum.Maven.getAutomaticBuilder(configuration);
        this.testCompiler = new TestCompiler(
                0,
                false,
                configuration.getAbsolutePathToProjectRoot(),
                configuration.getClasspathClassesProject(),
                10000,
                "",
                false
        );
    }

    @After
    public void tearDown() throws Exception {
        DSpotState.GLOBAL_REPORT.reset();
    }

    public class SelectorThatThrowsError extends TakeAllSelector {

        private boolean throwsToAmplify;

        private boolean throwsToKeep;

        public SelectorThatThrowsError(AutomaticBuilder automaticBuilder, UserInput configuration) {
            super(automaticBuilder, configuration);
        }

        public void setThrowsToAmplify(boolean throwsToAmplify) {
            this.throwsToAmplify = throwsToAmplify;
        }

        public void setThrowsToKeep(boolean throwsToKeep) {
            this.throwsToKeep = throwsToKeep;
        }

        @Override
        public List<CtMethod<?>> selectToAmplify(CtType<?> classTest, List<CtMethod<?>> testsToBeAmplified) {
            if (throwsToAmplify) {
                throw new RuntimeException();
            }
            return super.selectToAmplify(classTest, testsToBeAmplified);
        }

        @Override
        public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
            if (throwsToKeep) {
                throw new RuntimeException();
            }
            return super.selectToKeep(amplifiedTestToBeKept);
        }
    }

    public class AmplifierThatThrowsError implements Amplifier {
        @Override
        public Stream<CtMethod<?>> amplify(CtMethod<?> testMethod, int iteration) {
            throw new RuntimeException();
        }

        @Override
        public void reset(CtType<?> testClass) {

        }
    }

    public class AssertionGeneratorThatThrowsError extends AssertionGenerator {
        public AssertionGeneratorThatThrowsError(DSpotCompiler compiler) {
            super(0.1F, compiler, testCompiler);
        }

        @Override
        public List<CtMethod<?>> assertionAmplification(CtType<?> testClass, List<CtMethod<?>> tests) {
            throw new RuntimeException();
        }
    }

    @Test
    public void testThatDSpotCanRecoverFromError() throws Exception {

        /*
            We test here, with different mock, that DSpot can recover for errors, continue and terminate the amplification process.
         */

        final SelectorThatThrowsError selector = new SelectorThatThrowsError(builder, configuration);
        selector.setThrowsToAmplify(true);
        DSpotCompiler compiler = DSpotCompiler.createDSpotCompiler(configuration, "");
        final TestFinder testFinder = new TestFinder(Collections.emptyList(), Collections.emptyList());
        final CtClass<?> testClassToBeAmplified = findClass("fr.inria.amp.OneLiteralTest");
        final List<CtMethod<?>> testListToBeAmplified = testFinder.findTestMethods(testClassToBeAmplified, Collections.emptyList());
        DSpotState dspotState = new DSpotState();
        dspotState.setDelta(0.1f);
        dspotState.setCompiler(compiler);
        dspotState.setTestSelector(selector);
        dspotState.setInputAmplDistributor(new TextualDistanceInputAmplDistributor(200, Collections.emptyList()));
        dspotState.setNbIteration(1);
        dspotState.setTestCompiler(testCompiler);
        dspotState.setAssertionGenerator(new AssertionGenerator(0.1f, compiler, testCompiler));
        DSpot dspot = new DSpot(dspotState);
        dspot.fullAmplification(testClassToBeAmplified, testListToBeAmplified, Collections.emptyList(), 1);
        assertEquals(1, DSpotState.GLOBAL_REPORT.getErrors().size());
        assertSame(ErrorEnum.ERROR_PRE_SELECTION, DSpotState.GLOBAL_REPORT.getErrors().get(0).type);
        DSpotState.GLOBAL_REPORT.reset();

        selector.setThrowsToAmplify(false);
        selector.setThrowsToKeep(true);
        dspot.fullAmplification(testClassToBeAmplified, testListToBeAmplified, Collections.emptyList(), 1);
        assertEquals(1, DSpotState.GLOBAL_REPORT.getErrors().size());
        assertSame(ErrorEnum.ERROR_SELECTION, DSpotState.GLOBAL_REPORT.getErrors().get(0).type);
        DSpotState.GLOBAL_REPORT.reset();

        final List<Amplifier> amplifiers = Collections.singletonList(new AmplifierThatThrowsError());
        dspotState.setTestSelector(new TakeAllSelector(this.builder, this.configuration));
        dspotState.setInputAmplDistributor(new TextualDistanceInputAmplDistributor(200, amplifiers));
        dspot.fullAmplification(testClassToBeAmplified, testListToBeAmplified, Collections.emptyList(), 1);
        assertEquals(1, DSpotState.GLOBAL_REPORT.getErrors().size());
        assertSame(ErrorEnum.ERROR_INPUT_AMPLIFICATION, DSpotState.GLOBAL_REPORT.getErrors().get(0).type);
        DSpotState.GLOBAL_REPORT.reset();

        dspotState.setInputAmplDistributor(new TextualDistanceInputAmplDistributor(200, Collections.emptyList()));
        dspotState.setAssertionGenerator(new AssertionGeneratorThatThrowsError(compiler));
        dspot.fullAmplification(testClassToBeAmplified, testListToBeAmplified, Collections.emptyList(), 1);
        assertEquals(1, DSpotState.GLOBAL_REPORT.getErrors().size());
        assertSame(ErrorEnum.ERROR_ASSERT_AMPLIFICATION, DSpotState.GLOBAL_REPORT.getErrors().get(0).type);
        DSpotState.GLOBAL_REPORT.reset();
    }

    @Test
    public void testNoMatchingTestClasses() {
        final TestFinder testFinder = new TestFinder(Collections.emptyList(), Collections.emptyList());
        testFinder.findTestClasses(Collections.singletonList("this.is.not.a.correct.package"));
        assertEquals(2, DSpotState.GLOBAL_REPORT.getErrors().size());
        assertSame(ErrorEnum.ERROR_NO_TEST_COULD_BE_FOUND_MATCHING_REGEX, DSpotState.GLOBAL_REPORT.getErrors().get(0).type);
        assertSame(ErrorEnum.ERROR_NO_TEST_COULD_BE_FOUND, DSpotState.GLOBAL_REPORT.getErrors().get(1).type);
    }
}
