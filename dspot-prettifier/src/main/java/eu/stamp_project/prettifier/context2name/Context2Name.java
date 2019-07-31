package eu.stamp_project.prettifier.context2name;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static com.github.javaparser.GeneratedJavaParserConstants.*;
import static java.util.Arrays.asList;

public class Context2Name {
    private static final Logger LOGGER = LoggerFactory.getLogger(Context2Name.class);

    private final int WIDTH = 5;
    private String RESOURCES_DIR = getClass().getClassLoader().getResource("").getPath().replace("/target/test-classes/", "/target/classes/");
    private final String CONTEXT2NAME_DIR = RESOURCES_DIR + "context2name/";

    private Scoper scoper;
    private CompilationUnit compilationUnit;
    private Gson gson = new Gson();
    private JsonParser jsonParser = new JsonParser();

    private CompilationUnit parseCode(String code) {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(code);
        Optional<CompilationUnit> optionalCompilationUnit = parseResult.getResult();
        return optionalCompilationUnit.orElse(null);
    }

    private CompilationUnit parseFile(String fileName) {
        try {
            File file = new File(CONTEXT2NAME_DIR + fileName);
            ParseResult<CompilationUnit> parseResult = new JavaParser().parse(file);
            Optional<CompilationUnit> optionalCompilationUnit = parseResult.getResult();
            return optionalCompilationUnit.orElse(null);
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
                    scoper.setTabooIdentifier.add(scoper.listScope.get(index).name);
                    return; // we do not handle this kind of variables
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
                    if (!scoper.setTabooIdentifier.contains(scope.name)) {
                        sequences.put(scope, arr);
                    }
                }
            }
        });
        // ignore identifiers in tabooSet
        Map<Scoper.Scope, List<String>> checkedSequences = new HashMap<>();
        sequences.forEach((scope, arr) -> {
            if (!scoper.setTabooIdentifier.contains(scope.name)) {
                checkedSequences.put(scope, arr);
            }
        });
        return checkedSequences;
    }

    private String dumpSequences() {
        String line = "Tmp.java";

        Map<Scoper.Scope, List<String>> sequences = extractSequences();
        Map<Scoper.Scope, List<String>> seqMap = new HashMap<>();
        sequences.forEach((key, value) -> {
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
        });

        {
            List<String> targets = new ArrayList<>();
            for (Scoper.Scope key : seqMap.keySet()) {
                targets.add(line.replace(" ", "_") + " 1ID:" + seqMap.get(key).get(2) + ":" + seqMap.get(key).get(1) + " " + seqMap.get(key).get(0));
            }

            // invoke Python scripts
            String jsonInput = gson.toJson(targets);
            String jsonOutput = invoke(jsonInput);
            LOGGER.info(jsonOutput);

            // parse Json
            JsonElement je = jsonParser.parse(jsonOutput);
            if (!je.isJsonNull()) {
                JsonArray jsonArray = jsonParser.parse(jsonOutput).getAsJsonArray();
                if (jsonArray.size() == 2) {
                    List<List<Prediction>> predictions4all = new ArrayList<>();
                    JsonElement je4all = jsonArray.get(0);
                    for (JsonElement je4each : je4all.getAsJsonArray()) {
                        List<Prediction> predictions4each = new ArrayList<>();
                        for (JsonElement je4p : je4each.getAsJsonArray()) {
                            JsonArray jaPrediction = je4p.getAsJsonArray();
                            if (jaPrediction.size() == 3) {
                                float probability = jaPrediction.get(0).getAsFloat();
                                String name = jaPrediction.get(1).getAsString();
                                int index = jaPrediction.get(2).getAsInt();
                                predictions4each.add(new Prediction(probability, name, index));
                            }
                        }
                        predictions4all.add(predictions4each);
                    }

                    List<String> names = new ArrayList<>();
                    JsonElement je4names = jsonArray.get(1);
                    for (JsonElement je4name : je4names.getAsJsonArray()) {
                        names.add(je4name.getAsString());
                    }
                    Result result = new Result(predictions4all, names);
                    recover(result);
                }
            }
            return compilationUnit.toString();
        }
    }

    private void dumpSequences(String fileName, String line, boolean recovery) {
        Map<Scoper.Scope, List<String>> sequences = extractSequences();
        Map<Scoper.Scope, List<String>> seqMap = new HashMap<>();
        sequences.forEach((key, value) -> {
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
        });

        if (recovery) {
            List<String> targets = new ArrayList<>();
            for (Scoper.Scope key : seqMap.keySet()) {
                targets.add(line.replace(" ", "_") + " 1ID:" + seqMap.get(key).get(2) + ":" + seqMap.get(key).get(1) + " " + seqMap.get(key).get(0));
            }

            // invoke Python scripts
            String jsonInput = gson.toJson(targets);
            String jsonOutput = invoke(jsonInput);
            LOGGER.info(jsonOutput);

            // parse Json
            JsonElement je = jsonParser.parse(jsonOutput);
            if (!je.isJsonNull()) {
                JsonArray jsonArray = jsonParser.parse(jsonOutput).getAsJsonArray();
                if (jsonArray.size() == 2) {
                    List<List<Prediction>> predictions4all = new ArrayList<>();
                    JsonElement je4all = jsonArray.get(0);
                    for (JsonElement je4each : je4all.getAsJsonArray()) {
                        List<Prediction> predictions4each = new ArrayList<>();
                        for (JsonElement je4p : je4each.getAsJsonArray()) {
                            JsonArray jaPrediction = je4p.getAsJsonArray();
                            if (jaPrediction.size() == 3) {
                                float probability = jaPrediction.get(0).getAsFloat();
                                String name = jaPrediction.get(1).getAsString();
                                int index = jaPrediction.get(2).getAsInt();
                                predictions4each.add(new Prediction(probability, name, index));
                            }
                        }
                        predictions4all.add(predictions4each);
                    }

                    List<String> names = new ArrayList<>();
                    JsonElement je4names = jsonArray.get(1);
                    for (JsonElement je4name : je4names.getAsJsonArray()) {
                        names.add(je4name.getAsString());
                    }
                    Result result = new Result(predictions4all, names);
                    recover(result);
                }
            }

            try (FileWriter fileWriter = new FileWriter(CONTEXT2NAME_DIR + line.replace(".java", ".c2n.java"));
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(compilationUnit.toString());
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try (FileWriter fileWriter = new FileWriter(CONTEXT2NAME_DIR + fileName.replace(".txt", ".csv"), true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                for (Scoper.Scope key : seqMap.keySet()) {
                    bufferedWriter.write(line.replace(" ", "_") + " 1ID:" + seqMap.get(key).get(2) + ":" + seqMap.get(key).get(1) + " " + seqMap.get(key).get(0));
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Prediction implements Comparable<Prediction> {
        float probability;
        String newName;
        int index;

        Prediction(float probability, String newName, int index) {
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

    static class Result {
        List<List<Prediction>> predictions;
        List<String> oldNames;

        Result(List<List<Prediction>> predictions, List<String> oldNames) {
            this.predictions = predictions;
            this.oldNames = oldNames;
        }
    }

    private String nestStr(String str) {
        String nestedStr = str;
        nestedStr = nestedStr.replaceAll("(\\\\+)\"", "$1$1\"");
        nestedStr = nestedStr.replaceAll("\"", "\\\\\"");
        nestedStr = "\"" + nestedStr + "\"";
        return nestedStr;
    }

    private String invoke(String jsonInput) {
        try {
            StringJoiner stringJoiner = new StringJoiner(";");
            stringJoiner.add("cd " + CONTEXT2NAME_DIR + "python");
            stringJoiner.add("python3 c2n_apply.py -d " + nestStr(jsonInput));
            final String command = stringJoiner.toString();
            System.out.println(command);

            Process context2nameProcess = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context2nameProcess.getInputStream()));

            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader.lines().forEach(stringBuilder::append);
            return stringBuilder.toString();
        } catch (IOException var12) {
            throw new RuntimeException(var12);
        }
    }

    // result format : [prediction_arrays, the original names in the file]
    // prediction arrays format : array of arrays, each inner array containing 10 tuples
    // inner prediction tuple format : [probability, new name, index of name in the original array of names]
    private void recover(Result result) {
        // as we need to make sure program still valid after rename
        // also it seems inconvenient to trace scopes efficiently
        // here we append one extra step to:
        // 1 make sure same variables will never be renamed differently
        // 2 avoid complex approach to trace scopes of different variables
        // by replace same/different old names with same/different new names
        Map<String, List<Integer>> indicesMap = new HashMap<>();
        // gather results for same oldNames
        for (int i = 0; i < result.oldNames.size(); i++) {
            String oldName = result.oldNames.get(i).split(":")[2];
            indicesMap.putIfAbsent(oldName, new ArrayList<>());
            indicesMap.get(oldName).add(i);
        }
        // map oldName with 10 most promising newNames
        Map<String, List<String>> namesMap = new HashMap<>();
        for (String oldName : indicesMap.keySet()) {
            List<Integer> indices = indicesMap.get(oldName);
            Map<String, Prediction> predictionMap = new HashMap<>();
            // gather newNames with highest pr for each oldName
            for (int index : indices) {
                List<Prediction> predictions = result.predictions.get(index);
                for (Prediction prediction : predictions) {
                    predictionMap.putIfAbsent(prediction.newName, prediction);
                    Prediction existingPrediction = predictionMap.get(prediction.newName);
                    if (prediction.probability > existingPrediction.probability) {
                        predictionMap.put(prediction.newName, prediction);
                    }
                }
            }
            // focus on the top 10 newNames for each oldName
            List<Map.Entry<String, Prediction>> predictionEntryList = new ArrayList<>(predictionMap.entrySet());
            predictionEntryList.sort((entry1, entry2) -> {
                float pr1 = entry1.getValue().probability;
                float pr2 = entry2.getValue().probability;
                return -Float.compare(pr1, pr2);
            });
            predictionEntryList.subList(0, 10).forEach(prediction -> {
                namesMap.putIfAbsent(oldName, new ArrayList<>());
                namesMap.get(oldName).add(prediction.getKey());
            });
        }

        namesMap.forEach((oldName, newNames) -> {
            for (int index = 0; index < 10; index++) {
                String newName = newNames.get(index);
                if (scoper.check(oldName, newName)) {
                    scoper.link(oldName, newName);
                    break;
                }
                if (index == 9) { // no more predictions left
                    if (scoper.check(oldName, oldName)) {
                        scoper.link(oldName, oldName);
                    } else {
                        scoper.link(oldName, "C2N_" + oldName);
                    }
                }
            }
        });
        scoper.transform();

//        // Begin assignment of new names using a priority queue
//        Queue<Prediction> queue = new PriorityQueue<>();
//        // Captures the number of names tried for each variable
//        List<Integer> namingIndexList = new ArrayList<>();
//        for (int i = 0; i < result.oldNames.size(); i++) {
//            queue.add(result.predictions.get(i).get(0)); // first prediction tuple for each variable
//            namingIndexList.add(1);
//        }
//
//        while (queue.size() != 0) {
//            Prediction elem = queue.remove();
//            String oldName = result.oldNames.get(elem.index).split(":")[2];
//            if (scoper.check(oldName, elem.newName)) {
//                scoper.link(oldName, elem.newName);
//            } else {
//                Integer namingIndex = namingIndexList.get(elem.index);
//                if (namingIndex > 9) { // no more predictions left
//                    if (scoper.check(oldName, oldName)) {
//                        scoper.link(oldName, oldName);
//                    } else {
//                        scoper.link(oldName, "C2N_" + oldName);
//                    }
//                } else {
//                    queue.add(result.predictions.get(elem.index).get(namingIndex));
//                    namingIndexList.set(elem.index, namingIndex + 1);
//                }
//            }
//        }
//        scoper.transform();
    }

    public String process(String code) {
        compilationUnit = parseCode(code);
        if (compilationUnit != null) {
            scoper = new Scoper(compilationUnit);
            return dumpSequences();
        }
        return null;
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
                dumpSequences(fileName, line, recovery);
            }
        }
    }

    public void fnCorpus() {
        // if you want to process corpus
        process("training.txt", false);
        process("validation.txt", false);
    }

    public void fnDemo() {
        // if you want to run one demo
        process("demo.txt", true);
    }

//    public static void main(String[] args) {
//        Context2Name context2Name = new Context2Name();
//        context2Name.fnCorpus();
//        context2Name.fnDemo();
//    }
}
