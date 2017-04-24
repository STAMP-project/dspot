package fr.inria.diversify.dspot.support;

import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.Log;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/17/17
 */
public class MavenDependenciesResolver {

    private static String PATH_SEPARATOR = System.getProperty("path.separator");

    private static String FILE_SEPARATOR = System.getProperty("file.separator");

    private static String NAME_FILE_CLASSPATH = "cp";

    public static URL[] resolveDependencies(@Deprecated InputConfiguration configuration, InputProgram program, String mavenHome) {
        Set<URL> classpath = buildClasspath(program.getProgramDir(), mavenHome);
        return classpath.toArray(new URL[classpath.size()]);
    }

    private static Set<URL> buildClasspath(String programDir, String pathToMavenHome) {
        try {
            MavenBuilder builder = new MavenBuilder(programDir);
            builder.setBuilderPath(pathToMavenHome);
            String[] phases = new String[]{"dependency:build-classpath", "-Dmdep.outputFile=" + NAME_FILE_CLASSPATH};
            builder.runGoals(phases, false);
            final File fileClasspath = new File(programDir + FILE_SEPARATOR + NAME_FILE_CLASSPATH);
            try (BufferedReader buffer = new BufferedReader(new FileReader(fileClasspath))) {
                return Arrays.stream(buffer.lines()
                        .findFirst()
                        .orElseThrow(RuntimeException::new)
                        .split(PATH_SEPARATOR))
                        .map(mapStringToURl)
                        .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final Function<String, URL> mapStringToURl = (string -> {
        try {
            return new URL("file:" + string);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    });

}
