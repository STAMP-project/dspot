package eu.stamp_project.prettifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.prettifier.code2vec.Code2VecExecutor;
import eu.stamp_project.prettifier.code2vec.Code2VecParser;
import eu.stamp_project.prettifier.code2vec.Code2VecWriter;
import eu.stamp_project.prettifier.context2name.Context2Name;
import eu.stamp_project.prettifier.minimization.GeneralMinimizer;
import eu.stamp_project.prettifier.minimization.Minimizer;
import eu.stamp_project.prettifier.minimization.PitMutantMinimizer;
import eu.stamp_project.prettifier.options.UserInput;
import eu.stamp_project.prettifier.output.PrettifiedTestMethods;
import eu.stamp_project.prettifier.output.report.ReportJSON;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.InitializeDSpot;
import eu.stamp_project.dspot.common.configuration.check.Checker;
import eu.stamp_project.dspot.common.configuration.check.InputErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import spoon.Launcher;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static ReportJSON report = new ReportJSON();

    public static void main(String[] args) {
        UserInput inputConfiguration = new UserInput();
        final CommandLine commandLine = new CommandLine(inputConfiguration);
        commandLine.setUsageHelpWidth(120);
        try {
            commandLine.parseArgs(args);
        } catch (Exception e) {
            e.printStackTrace();
            commandLine.usage(System.err);
            return;
        }
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        }
        if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }
        if (inputConfiguration.shouldRunExample()) {
            inputConfiguration.configureExample();
        }
        try {
            Checker.preChecking(inputConfiguration);
        } catch (InputErrorException e) {
            e.printStackTrace();
            commandLine.usage(System.err);
            return;
        }
        DSpotState.verbose = inputConfiguration.isVerbose();
        run(inputConfiguration);
    }

    public static void run(UserInput configuration) {
        final CtType<?> amplifiedTestClass = loadAmplifiedTestClass(configuration);
        final List<CtMethod<?>> prettifiedAmplifiedTestMethods =
                run(
                        amplifiedTestClass,
                        configuration
                );
        // output now
        output(amplifiedTestClass, prettifiedAmplifiedTestMethods, configuration);
    }

    public static CtType<?> loadAmplifiedTestClass(UserInput configuration) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource(configuration.getPathToAmplifiedTestClass());
        launcher.buildModel();
        return launcher.getFactory().Class().getAll().get(0);
    }

    public static List<CtMethod<?>> run(CtType<?> amplifiedTestClass,
                                        UserInput configuration) {
        InitializeDSpot initializeDSpot = new InitializeDSpot();
        final AutomaticBuilder automaticBuilder = configuration.getBuilderEnum().getAutomaticBuilder(configuration);
        final String dependencies = initializeDSpot.completeDependencies(configuration, automaticBuilder);
        final DSpotCompiler compiler = DSpotCompiler.createDSpotCompiler(
                configuration,
                dependencies
        );
        configuration.setFactory(compiler.getLauncher().getFactory());
        initializeDSpot.initHelpers(configuration);

        final List<CtMethod<?>> testMethods = TestFramework.getAllTest(amplifiedTestClass);
        Main.report.nbTestMethods = testMethods.size();
        // 1 minimize amplified test methods
        final List<CtMethod<?>> minimizedAmplifiedTestMethods = applyMinimization(
                testMethods,
                amplifiedTestClass,
                configuration
        );
        // 2 rename test methods
        applyCode2Vec(minimizedAmplifiedTestMethods, configuration);
        // 3 Rename local variables TODO train one better model
        return applyContext2Name(minimizedAmplifiedTestMethods);
    }

    public static List<CtMethod<?>> applyMinimization(List<CtMethod<?>> amplifiedTestMethodsToBeMinimized,
                                                      CtType<?> amplifiedTestClass,
                                                      UserInput configuration) {

        Main.report.medianNbStatementBefore = Main.getMedian(amplifiedTestMethodsToBeMinimized.stream()
                .map(ctMethod -> ctMethod.getElements(new TypeFilter<>(CtStatement.class)))
                .map(List::size)
                .collect(Collectors.toList()));

        // 1rst apply a general minimization
        amplifiedTestMethodsToBeMinimized = Main.applyGivenMinimizer(new GeneralMinimizer(), amplifiedTestMethodsToBeMinimized);
        // update the test class with minimized test methods
        final ArrayList<CtMethod<?>> allMethods = new ArrayList<>(amplifiedTestClass.getMethods());
        allMethods.stream()
                .filter(TestFramework.get()::isTest)
                .forEach(amplifiedTestClass::removeMethod);
        amplifiedTestMethodsToBeMinimized.forEach(amplifiedTestClass::addMethod);

        final AutomaticBuilder automaticBuilder = configuration.getBuilderEnum().getAutomaticBuilder(configuration);
        // 2nd apply a specific minimization
        amplifiedTestMethodsToBeMinimized = Main.applyGivenMinimizer(
                new PitMutantMinimizer(
                        amplifiedTestClass,
                        automaticBuilder,
                        configuration.getAbsolutePathToProjectRoot(),
                        configuration.getClasspathClassesProject(),
                        configuration.getAbsolutePathToTestClasses()
                ),
                amplifiedTestMethodsToBeMinimized
        );

        Main.report.medianNbStatementAfter = Main.getMedian(amplifiedTestMethodsToBeMinimized.stream()
                .map(ctMethod -> ctMethod.getElements(new TypeFilter<>(CtStatement.class)))
                .map(List::size)
                .collect(Collectors.toList()));

        return amplifiedTestMethodsToBeMinimized;
    }

    private static List<CtMethod<?>> applyGivenMinimizer(Minimizer minimizer, List<CtMethod<?>> amplifiedTestMethodsToBeMinimized) {
        final List<CtMethod<?>> minimizedAmplifiedTestMethods = amplifiedTestMethodsToBeMinimized.stream()
                .map(minimizer::minimize)
                .collect(Collectors.toList());
        minimizer.updateReport(Main.report);
        return minimizedAmplifiedTestMethods;
    }

    public static void applyCode2Vec(List<CtMethod<?>> amplifiedTestMethodsToBeRenamed,
                                     UserInput configuration) {
        Code2VecWriter writer = new Code2VecWriter(configuration.getPathToRootOfCode2Vec());
        Code2VecParser parser = new Code2VecParser();
        Code2VecExecutor code2VecExecutor = null;
        try {
            code2VecExecutor = new Code2VecExecutor(
                    configuration.getPathToRootOfCode2Vec(),
                    configuration.getRelativePathToModelForCode2Vec(),
                    configuration.getTimeToWaitForCode2vecInMillis()
            );
            for (CtMethod<?> amplifiedTestMethodToBeRenamed : amplifiedTestMethodsToBeRenamed) {
                writer.writeCtMethodToInputFile(amplifiedTestMethodToBeRenamed);
                code2VecExecutor.run();
                final String code2vecOutput = code2VecExecutor.getOutput();
                final String predictedSimpleName = parser.parse(code2vecOutput);
                LOGGER.info("Code2Vec predicted {} for {} as new name", predictedSimpleName, amplifiedTestMethodToBeRenamed.getSimpleName());
                amplifiedTestMethodToBeRenamed.setSimpleName(predictedSimpleName);
            }
        } finally {
            if (code2VecExecutor != null) {
                code2VecExecutor.stop();
            }
        }
    }

    public static List<CtMethod<?>> applyContext2Name(List<CtMethod<?>> amplifiedTestMethods) {
        Context2Name context2name = new Context2Name();
        CtClass tmpClass = Launcher.parseClass("class Tmp {}");
        // remember the order
        List<String> methodNameList = new ArrayList<>();
        for (CtMethod<?> amplifiedTestMethod : amplifiedTestMethods) {
            methodNameList.add(amplifiedTestMethod.getSimpleName());
        }
        // apply Context2Name
        tmpClass.setMethods(new HashSet<>(amplifiedTestMethods));
        String strTmpClass = tmpClass.toString();
        String strProcessedClass = context2name.process(strTmpClass);
        CtClass processedClass = Launcher.parseClass(strProcessedClass);
        // restore the order
        List<CtMethod<?>> prettifiedMethodList = new ArrayList<>();
        methodNameList.forEach(methodName -> {
            prettifiedMethodList.addAll(processedClass.getMethodsByName(methodName));
        });
        return prettifiedMethodList;
    }

    public static <T extends Number & Comparable<T>> Double getMedian(List<T> list) {
        Collections.sort(list);
        return list.size() % 2 == 0 ?
                list.stream().skip(list.size() / 2 - 1).limit(2).mapToDouble(value -> new Double(value.toString())).average().getAsDouble() :
                new Double(list.stream().skip(list.size() / 2).findFirst().get().toString());
    }

    public static void output(CtType<?> amplifiedTestClass,
                              List<CtMethod<?>> prettifiedAmplifiedTestMethods,
                              UserInput configuration) {
        new PrettifiedTestMethods(configuration.getOutputDirectory())
                .output(amplifiedTestClass, prettifiedAmplifiedTestMethods);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String pathname = configuration.getOutputDirectory() +
                "/" + amplifiedTestClass.getSimpleName() + "report.json";
        LOGGER.info("Output a report in {}", pathname);
        final File file = new File(pathname);
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(Main.report));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
