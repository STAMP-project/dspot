package fr.inria.diversify.utils;

import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.processor.ProcessorUtil;
import fr.inria.diversify.processor.main.AddBlockEverywhereProcessor;
import fr.inria.diversify.processor.main.BranchCoverageProcessor;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import org.jacoco.core.data.ExecutionDataStore;
import org.kevoree.log.Log;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.processing.Processor;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;

import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * User: Simon
 * Date: 18/05/16
 * Time: 16:10
 */
public class DSpotUtils {

	private static StringBuilder progress = new StringBuilder(60);

	public static void printProgress(int done, int total) {
		char[] workchars = {'|', '/', '-', '\\'};
		String format = "\r%3d%% |%s ]%c";
		int percent = (++done * 100) / total;
		int extrachars = (percent / 2) - progress.length();
		while (extrachars-- > 0) {
			progress.append('=');
		}
		System.out.printf(format, percent, progress,
				workchars[done % workchars.length]);
		if (done == total) {
			System.out.flush();
			System.out.println();
			progress = new StringBuilder(60);
		}
	}

	public static void addBranchLogger(InputProgram inputProgram, Factory factory) {
		try {
			applyProcessor(factory, new AddBlockEverywhereProcessor(inputProgram));

			BranchCoverageProcessor branchCoverageProcessor = new BranchCoverageProcessor(inputProgram, inputProgram.getProgramDir(), true);
			branchCoverageProcessor.setLogger(Logger.class.getCanonicalName());

			applyProcessor(factory, branchCoverageProcessor);

			File fileFrom = new File(inputProgram.getAbsoluteSourceCodeDir());
			printAllClasses(factory, fileFrom);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void printJavaFileWithComment(CtType<?> type, File directory) {
		Factory factory = type.getFactory();
		Environment env = factory.getEnvironment();
		env.setCommentEnabled(true);
		JavaOutputProcessor processor = new JavaOutputProcessor(directory, new DefaultJavaPrettyPrinter(env));
		processor.setFactory(factory);
		processor.createJavaFile(type);
	}

	public static void printAmplifiedTestClass(CtType<?> type, File directory) {
		final String pathname = directory.getAbsolutePath() + "/" + type.getQualifiedName().replaceAll("\\.", "/") + ".java";
		if (new File(pathname).exists()) {
			printJavaFileWithComment(addGeneratedTestToExistingClass(type, pathname), directory);
		} else {
			printJavaFileWithComment(type, directory);
		}
	}

	private static CtClass<?> addGeneratedTestToExistingClass(CtType<?> type, String pathname) {
		Launcher launcher = new Launcher();
		launcher.getEnvironment().setNoClasspath(true);
		launcher.addInputResource(pathname);
		launcher.buildModel();
		final CtClass<?> existingAmplifiedTest = launcher.getFactory().Class().get(type.getQualifiedName());
		type.getMethods().stream()
				.filter(testCase -> !existingAmplifiedTest.getMethods().contains(testCase))
				.forEach(existingAmplifiedTest::addMethod);
		return existingAmplifiedTest;
	}

	public static void printAllClasses(Factory factory, File out) {
		factory.Class().getAll().forEach(type -> printJavaFileWithComment(type, out));
	}

	public static void addComment(CtElement element, String content, CtComment.CommentType type) {
		CtComment comment = element.getFactory().createComment(content, type);
		if (!element.getComments().contains(comment)) {
			element.addComment(comment);
		}
	}

	public static String mavenHome;

	public static String buildMavenHome(InputConfiguration inputConfiguration) {
		if (mavenHome == null) {
			if (inputConfiguration != null && inputConfiguration.getProperty("maven.home") != null) {
				mavenHome = inputConfiguration.getProperty("maven.home");
			} else {
				if(!setMavenHome(envVariable -> System.getenv().get(envVariable) != null,
						"MAVEN_HOME", "M2_HOME")) {
					if (!setMavenHome(path -> new File(path).exists(),
							"/usr/share/maven/", "/usr/local/maven-3.3.9/", "/usr/share/maven3/")) {
						throw new RuntimeException("Maven home not found");
					}
				}
			}
		}
		return mavenHome;
	}

	private static boolean setMavenHome(Predicate<String> conditional, String... possibleValues) {
		Arrays.stream(possibleValues)
				.filter(conditional)
				.findFirst()
				.ifPresent(s -> mavenHome = s);
		return mavenHome != null;
	}

	private static void applyProcessor(Factory factory, Processor processor) {
		QueueProcessingManager pm = new QueueProcessingManager(factory);
		pm.addProcessor(processor);
		pm.process(factory.Package().getRootPackage());
	}

	public static void copyPackageFromResources(String directory, String packagePath, String... classToCopy) {
		final String pathToTestClassesDirectory = directory + "/" + packagePath + "/";
		try {
			org.apache.commons.io.FileUtils.forceMkdir(new File(pathToTestClassesDirectory));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Arrays.stream(classToCopy).forEach(file -> {
			OutputStream resStreamOut = null;
			try {
				final InputStream resourceAsStream = Thread.currentThread()
						.getContextClassLoader()
						.getResourceAsStream(packagePath + "/" + file + ".class");
				resStreamOut =
						new FileOutputStream(pathToTestClassesDirectory + file + ".class");
				int readBytes;
				byte[] buffer = new byte[4096];
				while ((readBytes = resourceAsStream.read(buffer)) > 0) {
					resStreamOut.write(buffer, 0, readBytes);
				}
				resStreamOut.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
}
