package eu.stamp_project.utils;

import eu.stamp_project.dspot.amplifier.StringLiteralAmplifier;
import eu.stamp_project.utils.program.InputConfiguration;
import spoon.Launcher;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.util.EmptyClearableList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/10/19
 */
public class DSpotMojoGenerator extends AbstractProcessor<CtClass<?>> {

    private static Map<String, String> commandLineMatchingMavenProperties = new HashMap<>();

    static {
        commandLineMatchingMavenProperties.put("absolute-path-to-project-root", "this.absolutePathToProjectRoot");
        commandLineMatchingMavenProperties.put("relative-path-to-source-code", "mavenProject.getBuild().getSourceDirectory().substring(mavenProject.getBasedir().getAbsolutePath().length() + 1)");
        commandLineMatchingMavenProperties.put("relative-path-to-test-code", "mavenProject.getBuild().getTestSourceDirectory().substring(mavenProject.getBasedir().getAbsolutePath().length() + 1)");
        commandLineMatchingMavenProperties.put("relative-path-to-classes", "mavenProject.getBuild().getOutputDirectory().substring(mavenProject.getBasedir().getAbsolutePath().length() + 1)");
        commandLineMatchingMavenProperties.put("relative-path-to-test-classes", "mavenProject.getBuild().getTestOutputDirectory().substring(mavenProject.getBasedir().getAbsolutePath().length() + 1)");
    }

    private static final String DSPOT_MOJO_QUALIFIED_NAME = "eu.stamp_project.DSpotMojo";

    private static final String PARAMETER_QUALIFIED_NAME = "org.apache.maven.plugins.annotations.Parameter";

    private static final String OPTIONS_QUALIFIED_NAME = "CommandLine.Option";

    private static final Predicate<CtAnnotation> MATCH_OPTION_NAME =
            ctAnnotation ->
                    OPTIONS_QUALIFIED_NAME.equals(ctAnnotation.getAnnotationType().getQualifiedName()) ||
            "picocli.CommandLine$Option".equals(ctAnnotation.getAnnotationType().getQualifiedName());

    private class Arguments {
        private final CtField field;
        private final String commandLine;

        private Arguments(CtField field, String commandLine) {
            this.field = field;
            this.commandLine = commandLine;
        }
    }

    @Override
    public void process(CtClass<?> ctClass) {
        if (DSPOT_MOJO_QUALIFIED_NAME.equals(ctClass.getQualifiedName())) {
            final CtField<?> mavenProject = ctClass.getField("mavenProject").clone();
            final CtField<?> absolutePathToProjectRoot = ctClass.getField("absolutePathToProjectRoot").clone();
            ctClass.setFields(EmptyClearableList.instance());
            ctClass.addField(mavenProject);
            ctClass.addField(absolutePathToProjectRoot);
            final CtMethod<?> executeMethod = ctClass.getMethodsByName("execute").get(0);
            final CtInvocation<?> invokationToRemoveBlank = executeMethod.getBody().getElements(new TypeFilter<CtInvocation>(CtInvocation.class){
                @Override
                public boolean matches(CtInvocation candidate) {
                    return "removeBlank".equals(candidate.getExecutable().getSimpleName());
                }
            }).get(0);
            invokationToRemoveBlank.setArguments(EmptyClearableList.instance());
            final Launcher launcher = getLauncher();
            final CtClass<?> configurationCtClass = launcher.getFactory().Class().get(InputConfiguration.class);
            final CtNewArray<String> newArray = getFactory().createNewArray();
            newArray.setType(getFactory().Type().createReference("java.lang.String[]"));
            for (CtField<?> field : configurationCtClass.getFields()) {
                final Arguments arguments = processField(launcher, field);
                if (arguments != null) {
                    if (commandLineMatchingMavenProperties.containsKey(arguments.commandLine)) {
                        newArray.addElement(getFactory().createLiteral("--" + arguments.commandLine));
                        newArray.addElement(getFactory().createCodeSnippetExpression(commandLineMatchingMavenProperties.get(arguments.commandLine)));
                    } else {
                        if (field.getType().getQualifiedName().toLowerCase().endsWith("boolean")) {
                            newArray.addElement(getFactory().createCodeSnippetExpression(
                                    arguments.field.getSimpleName() + " ? " + "\"--" + arguments.commandLine + "\" : \"\""
                                    )
                            );
                        } else {
                            // adding the command option
                            newArray.addElement(getFactory().createCodeSnippetExpression(
                                    "!" + arguments.field.getSimpleName() + ".isEmpty() ? "
                                            + "\"--" + arguments.commandLine + "\" : \"\""
                                    )
                            );
                            // adding the command value
                            newArray.addElement(getFactory().createCodeSnippetExpression(
                                    "!" + arguments.field.getSimpleName() + ".isEmpty() ? "
                                            + arguments.field.getSimpleName() + " : \"\""
                                    )
                            );
                        }
                        ctClass.addField(arguments.field);
                    }
                }
            }
            ((CtInvocation) invokationToRemoveBlank).addArgument(newArray);
        }
    }

    private Arguments processField(Launcher launcher, CtField<?> field) {
        final Optional<CtAnnotation<?>> optionalCtAnnotation = field.getAnnotations()
                .stream()
                .filter(MATCH_OPTION_NAME).findFirst();
        if (optionalCtAnnotation.isPresent()) {
            final CtAnnotation<?> commandLineOptionAnnotation = optionalCtAnnotation.get();
            StringLiteralAmplifier.flatStringLiterals(commandLineOptionAnnotation);
            final String propertyName = commandLineOptionAnnotation.getValue(NAMES_VALUE_NAME).toString();
            final String defaultValue = commandLineOptionAnnotation.getValues().containsKey(DEFAULT_VALUE_NAME) ?
                    convertDefaultValue(commandLineOptionAnnotation.getValue(DEFAULT_VALUE_NAME).toString()) : null;
            final CtAnnotation ctAnnotation = generateParameterAnnotation(
                    launcher.getFactory(),
                    propertyName,
                    defaultValue
            );
            final String convertedDescription = convertDescription(
                    commandLineOptionAnnotation.getValue(DESCRIPTION_VALUE_NAME).toString(),
                    defaultValue
            );
            final CtField clone = field.clone();
            clone.setComments(Collections.singletonList(
                    getFactory().createComment(convertedDescription, CtComment.CommentType.BLOCK))
            );
            clone.removeAnnotation(commandLineOptionAnnotation);
            clone.addAnnotation(ctAnnotation);
            if (!clone.getType().getQualifiedName().toLowerCase().endsWith("boolean")) {
                clone.setType(getFactory().Type().createReference("java.lang.String"));
                if (defaultValue != null) {
                    clone.setDefaultExpression(getFactory().createLiteral(defaultValue));
                } else {
                    clone.setDefaultExpression(getFactory().createLiteral(""));
                }
            } else {
                clone.setDefaultExpression(getFactory().createLiteral(false));
            }
            return new Arguments(clone, convertNameToPropertyName(propertyName));
        } else {
            return null;
        }
    }

    private Launcher getLauncher() {
        Launcher launcher = new Launcher();
        launcher.addInputResource("../dspot/src/main/java/");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setLevel("OFF");
        launcher.buildModel();
        return launcher;
    }

    private static final String NAMES_VALUE_NAME = "names";

    private static final String PROPERTY_VALUE_NAME = "property";

    private static final String DEFAULT_VALUE_NAME = "defaultValue";

    private static final String DESCRIPTION_VALUE_NAME = "description";

    private CtAnnotation generateParameterAnnotation(Factory factory,
                                                     String propertyName,
                                                     String defaultValue) {

        final CtAnnotation annotation = factory.createAnnotation();
        annotation.setAnnotationType(factory.Type().createReference(PARAMETER_QUALIFIED_NAME));
        final String convertedName = convertNameToPropertyName(propertyName);
        annotation.addValue(PROPERTY_VALUE_NAME, factory.createLiteral(convertedName));
        if (defaultValue != null) {
            annotation.addValue(DEFAULT_VALUE_NAME, factory.createLiteral(defaultValue));
        }
        return annotation;
    }

    private String convertDefaultValue(String defaultValue) {
        final String[] splittedDefaultValue = defaultValue.split("\\\"");
        if (splittedDefaultValue.length > 0) {
            defaultValue = splittedDefaultValue[1];
        }
        if ("\"\"".equals(defaultValue)) {
            defaultValue = "";
        }
        return defaultValue;
    }

    private String convertDescription(String description, String defaultValue) {
        if (defaultValue != null) {
            return description.replace("${DEFAULT-VALUE}", defaultValue).split("\\\"")[1];
        } else {
            return description.split("\\\"")[1];
        }
    }

    private String convertNameToPropertyName(String names) {
        return names.split("--")[1].split("\\\"")[0];
    }

}
