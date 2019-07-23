package eu.stamp_project.prettifier.context2name.draft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import tech.sourced.siva.IndexEntry;
import tech.sourced.siva.SivaReader;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// https://github.com/src-d/datasets/tree/master/PublicGitArchive/pga
// https://pga.sourced.tech
// https://stedolan.github.io/jq/manual/
// https://github.com/src-d/siva-java
// https://github.com/eclipse/jgit
// https://github.com/centic9/jgit-cookbook
/*
pga list -l java -f json | jq -r 'select(.commitsCount >= 1000) | select(.commitsCount <= 5000) | select(.langsFilesCount[.langs | index("Java")] == (.langsFilesCount | max)) | select(.langsFilesCount[.langs | index("Java")] >= 10000) | select(.langsFilesCount[.langs | index("Java")] <= 50000) | .url, .langsFilesCount[.langs | index("Java")], .sivaFilenames'
https://github.com/OpenLiberty/open-liberty
18057
[
  "d6b44c34ac2b9a2900f9125b92bbf0ecd3416837.siva"
]
https://github.com/apache/incubator-netbeans
25488
[
  "6daa72c9819847bb4f71ee6aba6d30d5ffaca41a.siva"
]
https://github.com/openjdk-mirror/jdk7u-jdk
14161
[
  "59308f67f9b7038cfa2ceb9ee9ba27645b927cb5.siva"
]
https://github.com/aws/aws-sdk-java
34613
[
  "6b026a40dc03371ac084b046480a4c1761f38864.siva"
]
https://github.com/robovm/robovm
10037
[
  "3a81d86938f479d6791e2ce3be768a4fb83bb2c7.siva"
]
pga list -l java -f json | jq -r 'select(.commitsCount >= 1000) | select(.commitsCount <= 5000) | select(.langsFilesCount[.langs | index("Java")] == (.langsFilesCount | max)) | select(.langsFilesCount[.langs | index("Java")] >= 10000) | select(.langsFilesCount[.langs | index("Java")] <= 50000) | .sivaFilenames[]'
d6b44c34ac2b9a2900f9125b92bbf0ecd3416837.siva
6daa72c9819847bb4f71ee6aba6d30d5ffaca41a.siva
59308f67f9b7038cfa2ceb9ee9ba27645b927cb5.siva
6b026a40dc03371ac084b046480a4c1761f38864.siva
3a81d86938f479d6791e2ce3be768a4fb83bb2c7.siva
pga list -l java -f json | jq -r 'select(.commitsCount >= 1000) | select(.commitsCount <= 5000) | select(.langsFilesCount[.langs | index("Java")] == (.langsFilesCount | max)) | select(.langsFilesCount[.langs | index("Java")] >= 10000) | select(.langsFilesCount[.langs | index("Java")] <= 50000) | .sivaFilenames[]' | pga get -i -o repositories
 */
public class PGA {
    private static final Logger LOGGER = LoggerFactory.getLogger(PGA.class);

    private final String CONTEXT2NAME_DIR = "src/main/resources/context2name/";
    private final String SIVA_FILES_DIR = CONTEXT2NAME_DIR + "siva_files/";
    private final String SIVA_UNPACKED_DIR = CONTEXT2NAME_DIR + "siva_unpacked/";
    private final String SIVA_EXPLORED_DIR = CONTEXT2NAME_DIR + "data/";

    private void unpack() {
        LOGGER.info("unpacking siva files");
        try {
            File parentSivaDir = new File(SIVA_FILES_DIR);
            File[] childFiles = parentSivaDir.listFiles((dir, name) -> name.endsWith(".siva"));
            if (childFiles != null) {
                for (File childFile : childFiles) {
                    SivaReader sivaReader = new SivaReader(childFile);
                    List<IndexEntry> index = sivaReader.getIndex().getFilteredIndex().getEntries();
                    String childUnpackDir = SIVA_UNPACKED_DIR + childFile.getName().replaceAll(".siva", "/");
                    for (IndexEntry indexEntry : index) {
                        InputStream entry = sivaReader.getEntry(indexEntry);
                        Path outPath = Paths.get(childUnpackDir.concat(indexEntry.getName()));
                        FileUtils.copyInputStreamToFile(entry, new File(outPath.toString()));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
    }

    public void explore() {
        try (FileWriter fw4T = new FileWriter(CONTEXT2NAME_DIR + "training.txt");
             FileWriter fw4V = new FileWriter(CONTEXT2NAME_DIR + "validation.txt");
             BufferedWriter bw4T = new BufferedWriter(fw4T);
             BufferedWriter bw4V = new BufferedWriter(fw4V)) {
            // if siva-unpacked files do not exist then uncommented the next line
            File unpackDir = new File(SIVA_UNPACKED_DIR);
            if (!unpackDir.exists()) {
                unpack();
            }
            File[] repoDirs = unpackDir.listFiles((dir, name) -> dir.isDirectory());

            List<String> pathList = new ArrayList<>();
            for (File repoDir : repoDirs) {
                // now open the resulting repository with a FileRepositoryBuilder
                FileRepositoryBuilder builder = new FileRepositoryBuilder();
                Repository repository = builder.setGitDir(repoDir)
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
                System.out.println("Exploring repo: " + repository.getDirectory());

                Git git = new Git(repository);
                Iterable<RevCommit> commits = git.log().all().call();
                RevCommit revCommit = null;
                for (RevCommit commit : commits) {
                    revCommit = commit;
                }
                assert revCommit != null;
                RevTree revTree = revCommit.getTree();

                TreeWalk treeWalk = new TreeWalk(repository);
                treeWalk.addTree(revTree);
                treeWalk.setRecursive(true);
                int number = 0;
                while (treeWalk.next()) {
                    String name = treeWalk.getNameString();
                    String path = treeWalk.getPathString();
                    FileMode fileMode = treeWalk.getFileMode(0);
                    ObjectId objectId = treeWalk.getObjectId(0);
                    if (fileMode.equals(FileMode.REGULAR_FILE) && name.endsWith(".java")) {
                        String filePath = SIVA_EXPLORED_DIR + repoDir.getName() + "/" + path;
                        pathList.add(filePath.substring(CONTEXT2NAME_DIR.length()));
                        File file = new File(filePath);
                        if (!file.exists()) {
                            File parentFile = file.getParentFile();
                            if (!parentFile.exists()) {
                                parentFile.mkdirs();
                            }
                            file.createNewFile();

                            ObjectLoader loader = repository.open(objectId);
                            FileOutputStream fileStream = new FileOutputStream(file);
                            loader.copyTo(fileStream);
                            fileStream.close();
                        }
                        number++;
                    }
                }
                System.out.println(number + " Java files in total");
            }
            Collections.sort(pathList);

            BufferedWriter bw;
            for (int idx = 0; idx < pathList.size(); idx++) {
                bw = idx % 5 == 0 ? bw4V : bw4T;
                bw.write(pathList.get(idx));
                bw.newLine();
                bw.flush();
            }
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
    }

    public static void main(String[] args) {
        // TODO check the issue of inconsistent numbers
        new PGA().explore();
    }
}
