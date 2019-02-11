package eu.stamp_project.prettifier.code2vec.builder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/12/18
 */
public class Reader {

    // this file contains the user/project of repositories that will clone and extract test methods
    public static final String FILE_PROJECTS = Main.ROOT_PATH_DATA + "projects.txt";

    /**
     * read the file FILE_PROJECTS and return all the user/project inside.
     * each of them should be separated by line separator
     * The output should be used by {@link Cloner#clone(String)}
     * @return the list of user/project selected from github.
     */
    public static List<String> readAll() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PROJECTS))) {
            return reader.lines().collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
