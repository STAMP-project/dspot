package eu.stamp_project.prettifier.context2name.draft;

import com.github.javaparser.JavaToken;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.LabeledStmt;

import static com.github.javaparser.GeneratedJavaParserConstants.IDENTIFIER;
import static com.github.javaparser.JavaToken.Category.*;
import static java.util.Arrays.asList;

import java.util.*;

public class Scoper {
    private static int counter = 0;
    private CompilationUnit compilationUnit;
    private Map<String, List<Node>> mapName2NodeList;
    // checking & renaming
    private Map<String, Set<String>> checkingMap = new HashMap<>();
    private Map<String, String> renamingMap = new HashMap<>();

    // for identifiers we care about
    List<Node> listIdentifierNode = new ArrayList<>();
    // for all tokens
    List<Scope> listScope = new ArrayList<>();
    Map<Range, Scope> mapRange2Scope = new HashMap<>();
    Map<Range, Integer> mapRange2ScopeIdx = new HashMap<>();

    public Scoper(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
        enrichAST();
    }

    private void enrichAST() {
        mapName2NodeList();
        listTokenScope();
        listIdentifierNode();
        mapRange2ScopeIdx();
    }

    private void mapName2NodeList() {
        mapName2NodeList = new HashMap<>();
        compilationUnit.findAll(Node.class).stream()
            .filter(node -> node.getChildNodes().size() == 0)
            .filter(node -> node.getTokenRange().filter(tokenRange -> tokenRange.getBegin().getKind() == IDENTIFIER).isPresent())
            .forEach(node -> {
                mapName2NodeList.putIfAbsent(node.toString(), new ArrayList<>());
                mapName2NodeList.get(node.toString()).add(node);
            });
    }

    private void listIdentifierNode() {
        // Parameter in 'Identifier'
        compilationUnit.findAll(Parameter.class)
            .forEach(node -> listIdentifierNode.addAll(mapName2NodeList.get(node.getName().toString())));
        // MemberExpression/Property in 'Identifier'
        compilationUnit.findAll(VariableDeclarator.class)
            .forEach(node -> listIdentifierNode.addAll(mapName2NodeList.get(node.getName().toString())));
        // Label in 'Identifier'
        compilationUnit.findAll(LabeledStmt.class)
            .forEach(node -> listIdentifierNode.addAll(mapName2NodeList.get(node.getLabel().toString())));

        // prepare checkingMap
        Set<String> names = new HashSet<>();
        for (Node node : listIdentifierNode) {
            Range range = node.getRange().orElse(null);
            Scope scope = mapRange2Scope.get(range);
            names.add(scope.name);
        }
        for (String name : names) {
            // we always care about the first node
            Node firstNode = mapName2NodeList.get(name).get(0);
            Set<String> keyNames = new HashSet<>();
            while (firstNode.getParentNode().isPresent()) {
                Node parentNode = firstNode.getParentNode().get();
                parentNode.findAll(Parameter.class).forEach(childNode ->
                    childNode.getParentNode().ifPresent(anotherParentNode -> {
                        if (parentNode == anotherParentNode) {
                            keyNames.add(childNode.getName().toString());
                        }
                    })
                );
                parentNode.findAll(VariableDeclarator.class).forEach(childNode ->
                    childNode.getParentNode().ifPresent(anotherParentNode -> {
                        if (parentNode == anotherParentNode) {
                            keyNames.add(childNode.getName().toString());
                        }
                    })
                );
                parentNode.findAll(LabeledStmt.class).forEach(childNode ->
                    childNode.getParentNode().ifPresent(anotherParentNode -> {
                        if (parentNode == anotherParentNode) {
                            keyNames.add(childNode.getLabel().toString());
                        }
                    })
                );
                firstNode = parentNode;
            }
            checkingMap.put(name, keyNames);
        }
    }

    private void listTokenScope() {
        ArrayList<JavaToken.Category> IGNORED_TOKEN_CATEGORIES = new ArrayList<>(asList(WHITESPACE_NO_EOL, EOL, COMMENT));
        compilationUnit.getTokenRange().ifPresent(
            tokenRange -> {
                for (JavaToken javaToken : tokenRange) {
                    if (IGNORED_TOKEN_CATEGORIES.contains(javaToken.getCategory())) {
                        continue;
                    }
                    Scope scope = new Scope(javaToken);
                    listScope.add(scope);
                    Range range = scope.getRange().orElse(null);
                    mapRange2Scope.put(range, scope);
                }
            }
        );
    }

    private void mapRange2ScopeIdx() {
        for (int idx = 0; idx < listScope.size(); idx++) {
            Scope scope = listScope.get(idx);
            Range range = scope.getRange().orElse(null);
            mapRange2ScopeIdx.put(range, idx);
        }
    }

    public boolean check(String oldName, String newName) {
        for (String keyName : checkingMap.get(oldName)) {
            if (newName.equals(renamingMap.get(keyName))) {
                return false;
            }
        }
        return true;
    }

    public void rename(String oldName, String newName) {
        mapName2NodeList.get(oldName).forEach(
            node -> {
                Range range = node.getRange().orElse(null);
                Scope scope = mapRange2Scope.get(range);
                scope.name = newName;
            }
        );
        renamingMap.put(oldName, newName);
    }

    public class Scope {
        String name;
        Integer id = counter++;
        private JavaToken token;

        Scope(JavaToken token) {
            this.token = token;
            this.name = token.getText();
        }

        // for JavaToken
        int getKind() {
            return token.getKind();
        }

        // for Node or JavaToken
        Optional<Range> getRange() {
            return token.getRange();
        }
    }
}
