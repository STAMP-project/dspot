package eu.stamp_project.prettifier.code2vec.builder;

import eu.stamp_project.utils.AmplificationHelper;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/12/18
 */
public class Cloner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cloner.class);

    private Map<String, String> shaPerProject;

    private Path currentPath;

    public static String URL_GH = "https://github.com/";

    private static final String FILENAME = Main.ROOT_PATH_DATA + "shaPerProject.txt";

    public Cloner() {
        if (!new File(FILENAME).exists()) {
            this.shaPerProject = new HashMap<>();
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            this.shaPerProject = reader.lines().collect(
                    Collectors.toMap(
                            line -> line.split(AmplificationHelper.PATH_SEPARATOR)[0],
                            line -> line.split(AmplificationHelper.PATH_SEPARATOR)[1]
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setCorrectShaCommit(final Path tempDirectory, String userAndProject) throws Exception {
        final Repository repository = new FileRepositoryBuilder()
                .setGitDir(new File(tempDirectory.toString() + "/.git"))
                .build();
        final Git git = new Git(repository);
        if (!shaPerProject.containsKey(userAndProject)) {
            shaPerProject.put(userAndProject, git.log().call().iterator().next().getName());
            System.out.println(shaPerProject);
        } else {
            git.reset()
                    .setRef(shaPerProject.get(userAndProject))
                    .setMode(ResetCommand.ResetType.HARD)
                    .call();
        }
    }

    private static final String PREFIX = "tmp_post_dspot";

    /**
     * This method clone the given project.
     * It creates a tmp file using Files.createTempDirectory(
     *
     * @param userAndProject user/project to be cloned from github
     * @return the path to the tmp directory created
     */
    public Path clone(String userAndProject) throws Exception {
        this.currentPath = Files.createTempDirectory(PREFIX);
        LOGGER.info("Cloning {} in ", userAndProject, this.currentPath.toString());
        Git.cloneRepository()
                .setURI(URL_GH + userAndProject)
                .setDirectory(currentPath.toFile())
                .call();
        setCorrectShaCommit(currentPath, userAndProject);
        return currentPath;
    }

    public void output() {
        System.out.println(shaPerProject);
        try {
            FileWriter writer = new FileWriter(FILENAME, false);
            for (String key : this.shaPerProject.keySet()) {
                writer.append(key)
                        .append(AmplificationHelper.PATH_SEPARATOR)
                        .append(this.shaPerProject.get(key))
                        .append(AmplificationHelper.LINE_SEPARATOR);
            }
            writer.close();
            FileUtils.forceDelete(new File(this.currentPath.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Set<String> keySet) {
        final HashSet<String> keys = new HashSet<>(this.shaPerProject.keySet());
        keys.stream()
            .filter(key -> !keySet.contains(key))
            .forEach(this.shaPerProject::remove);
    }
}
