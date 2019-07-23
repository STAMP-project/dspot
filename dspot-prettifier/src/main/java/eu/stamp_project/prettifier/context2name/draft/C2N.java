package eu.stamp_project.prettifier.context2name.draft;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static com.github.javaparser.GeneratedJavaParserConstants.*;
import static java.util.Arrays.asList;

public class C2N {
    private static final Logger LOGGER = LoggerFactory.getLogger(C2N.class);

    private final int WIDTH = 5;
    private final String CONTEXT2NAME_DIR = "src/main/resources/context2name/";

    private Scoper scoper;
    private CompilationUnit compilationUnit;

    private CompilationUnit parseFile(String fileName) {
        try {
            File file = new File(CONTEXT2NAME_DIR + fileName);
            ParseResult<CompilationUnit> parseResult = new JavaParser().parse(file);
            Optional<CompilationUnit> optionalCompilationUnit = parseResult.getResult();
            if (optionalCompilationUnit.isPresent()) {
                return optionalCompilationUnit.get();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<Scoper.Scope, List<String>> extractSequences() {
        Map<Scoper.Scope, List<String>> sequences = new HashMap<>();
        scoper.listIdentifierNode.forEach(node -> {
            Range range = node.getRange().orElse(null);
            if (scoper.mapRange2Scope.containsKey(range)) {
                Scoper.Scope scope = scoper.mapRange2Scope.get(range);
                int index = scoper.mapRange2ScopeIdx.get(range);

                if (scoper.listScope.get(index - 1).getKind() == DOT) {
                    return; // we do not handle static variables
                }
                if (scope.id > 0) {
                    List<String> arr = new ArrayList<>();
                    for (int i = index - 1; arr.size() < WIDTH; i--) {
                        if (i <= 0) {
                            arr.add("0START");
                        } else if (i >= scoper.listScope.size()) {
                            arr.add("0END");
                        } else {
                            Scoper.Scope t = scoper.listScope.get(i);
                            Scoper.Scope prev = scoper.listScope.get(i - 1);
                            if (t.getKind() == IDENTIFIER && prev.getKind() != DOT) {
                                if (t.id != null) {
                                    arr.add("1ID:" + t.id + ":" + t.name);
                                } else {
                                    arr.add("1ID:-1:" + t.name);
                                }
                            } else if (t.getKind() != LPAREN/*(*/ && t.getKind() != RPAREN/*)*/ && t.getKind() != DOT/*.*/) {
                                arr.add(t.name);
                            }
                        }
                    }
                    Collections.reverse(arr);
                    arr.add(0, scope.name);
                    arr.add(0, scope.id.toString());
                    for (int i = index + 1; arr.size() < 2 + 2 * WIDTH; i++) {
                        if (i < 0) {
                            arr.add("0START");
                        } else if (i >= scoper.listScope.size()) {
                            arr.add("0END");
                        } else {
                            Scoper.Scope t = scoper.listScope.get(i);
                            Scoper.Scope prev = scoper.listScope.get(i - 1);
                            if (t.getKind() == IDENTIFIER && prev.getKind() != DOT) {
                                if (t.id > 0) {
                                    arr.add("1ID:" + t.id + ":" + t.name);
                                } else {
                                    arr.add("1ID:-1:" + t.name);
                                }
                            } else if (t.getKind() != LPAREN/*(*/ && t.getKind() != RPAREN/*)*/ && t.getKind() != DOT/*.*/) {
                                arr.add(t.name);
                            }
                        }
                    }
                    sequences.put(scope, arr);
                }
            }
        });
        return sequences;
    }

    private void dumpSequences(String fileName, boolean recovery) {
        Map<Scoper.Scope, List<String>> sequences = extractSequences();
        Map<Scoper.Scope, List<String>> seqMap = new HashMap<>();
        for (Map.Entry<Scoper.Scope, List<String>> entry : sequences.entrySet()) {
            Scoper.Scope key = entry.getKey();
            List<String> value = entry.getValue();
            seqMap.putIfAbsent(key, new ArrayList<>(asList("", value.get(1), value.get(0))));
            List<String> val = seqMap.get(key);
            for (int j = 2; j < value.size(); j++) {
                String token = value.get(j);
                String[] tokens = token.split("/(\\s+)/");
                token = tokens[0];
                if (val.get(0).length() > 0) {
                    val.set(0, val.get(0) + " ");
                }
                val.set(0, val.get(0) + token);
            }
        }

        if (recovery) {
            List<String> targets = new ArrayList<>();
            for (Scoper.Scope key : seqMap.keySet()) {
                targets.add(fileName.replace(" ", "_") + " 1ID:" + seqMap.get(key).get(2) + ":" + seqMap.get(key).get(1) + " " + seqMap.get(key).get(0));
            }
            System.out.println(targets);
            // Start Recovery
            recover(targets);
            try (FileWriter fileWriter = new FileWriter(fileName.replace(".java", ".c2n.java"));
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(compilationUnit.toString());
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        } else {
            try (FileWriter fileWriter = new FileWriter(fileName.replace(".txt", ".csv"), true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                for (Scoper.Scope key : seqMap.keySet()) {
                    bufferedWriter.write(fileName.replace(" ", "_") + " 1ID:" + seqMap.get(key).get(2) + ":" + seqMap.get(key).get(1) + " " + seqMap.get(key).get(0));
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }
    }

    class Prediction implements Comparable<Prediction> {
        float probability;
        String newName;
        int index;

        public Prediction(float probability, String newName, int index) {
            this.probability = probability;
            this.newName = newName;
            this.index = index;
        }

        public int compareTo(Prediction another) {
            if (probability > another.probability) {
                return -1;
            } else if (probability < another.probability) {
                return 1;
            } else {
                return newName.compareTo(another.newName);
            }
        }
    }

    class Result {
        List<List<Prediction>> predictions;
        List<String> oldNames;

        public Result(List<List<Prediction>> predictions, List<String> oldNames) {
            this.predictions = predictions;
            this.oldNames = oldNames;
        }
    }

    private void recover(List<String> targets) {
        Gson gson = new Gson();
        String jsonOutput;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String jsonInput = gson.toJson(targets);
            StringJoiner stringJoiner = new StringJoiner(";");
            stringJoiner.add("cd src/main/python");
//            stringJoiner.add("ls -a");
            stringJoiner.add("python3 context2name.py -d " + jsonInput);
            final String command = stringJoiner.toString();
            System.out.println(command);
            Process context2nameProcess = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context2nameProcess.getInputStream()));
            bufferedReader.lines().forEach(stringBuilder::append);
            jsonOutput = stringBuilder.toString();
        } catch (IOException var12) {
            throw new RuntimeException(var12);
        }

        System.out.println("================");
        System.out.println(jsonOutput);
        Result res = gson.fromJson(jsonOutput, Result.class);
        // res format : [prediction_arrays, the original names in the file]
        // prediction arrays format : array of arrays, each inner array containing 10 tuples
        // inner prediction tuple format : [probability, new name, index of name in the original array of names]

        // Begin assignment of new names using a priority queue
        Queue<Prediction> queue = new PriorityQueue<>();
        // Captures the number of names tried for each variable
        List<Integer> namingIndexList = new ArrayList<>();
        for (int i = 0; i < res.oldNames.size(); i++) {
            queue.add(res.predictions.get(i).get(0)); // first prediction tuple for each variable
            namingIndexList.add(1);
        }

        while (queue.size() != 0) {
            Prediction elem = queue.remove();
            String oldName = res.oldNames.get(elem.index).split(":")[2];
            if (scoper.check(oldName, elem.newName)) {
                scoper.rename(oldName, elem.newName);
            } else {
                Integer namingIndex = namingIndexList.get(elem.index);
                if (namingIndex > 9) { // no more predictions left
                    if (scoper.check(oldName, oldName)) {
                        scoper.rename(oldName, oldName);
                    } else {
                        scoper.rename(oldName, "C2N_" + oldName);
                    }
                } else {
                    queue.add(res.predictions.get(elem.index).get(namingIndex));
                    namingIndexList.set(elem.index, namingIndex + 1);
                }
            }
        }
    }

    private void process(String fileName, boolean recovery) {
        List<String> lines = new ArrayList<>();
        try {
            FileReader fr = new FileReader(CONTEXT2NAME_DIR + fileName);
            BufferedReader br = new BufferedReader(fr);
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                lines.add(line);
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String line : lines) {
            LOGGER.info(line);
            compilationUnit = parseFile(line);
            if (compilationUnit != null) {
                scoper = new Scoper(compilationUnit);
                dumpSequences(line, recovery);
            }
        }
    }

    public static void main(String[] args) {
        C2N c2n = new C2N();
        c2n.process("training.txt", false);
        c2n.process("validation.txt", false);
//        c2n.process("testing.txt", true);
    }

    // todo
    //  0 FileNotFoundException
    //  1 ID or JSON issue?
    //  2 check the entire process
}
