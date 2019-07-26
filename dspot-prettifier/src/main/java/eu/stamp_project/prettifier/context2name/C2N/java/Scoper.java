import com.github.javaparser.JavaToken;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.LabeledStmt;

import java.util.*;

import static com.github.javaparser.GeneratedJavaParserConstants.IDENTIFIER;
import static com.github.javaparser.JavaToken.Category.*;
import static java.util.Arrays.asList;

/**
 * here explains the reason of using ...
 * using JavaToken is to index tokens
 * using NameExpr is to find all identifiers
 * using Node is to utilize Range
 * using SimpleName is to retrieve by name
 * using specific classes is to find sign identifiers
 */
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

    Scoper(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
        mapName2NodeList();
        listTokenScope();
        listIdentifierNode();
        mapRange2ScopeIdx();
    }

    private void mapName2NodeList() {
        mapName2NodeList = new HashMap<>();
        compilationUnit.findAll(Node.class).stream()
            .filter(node -> node.getChildNodes().size() == 0)
            .filter(node -> node.getTokenRange().filter(tokenRange -> tokenRange.getBegin().getKind() == IDENTIFIER).isPresent()).forEach(node -> {
            mapName2NodeList.putIfAbsent(node.toString(), new ArrayList<>());
            mapName2NodeList.get(node.toString()).add(node);
        });
    }

    private void listIdentifierNode() {
        // Parameter in 'Identifier'
        compilationUnit.findAll(Parameter.class).forEach(node ->
            listIdentifierNode.addAll(mapName2NodeList.get(node.getName().toString()))
        );
        // MemberExpression/Property in 'Identifier'
        compilationUnit.findAll(VariableDeclarator.class).forEach(node ->
            listIdentifierNode.addAll(mapName2NodeList.get(node.getName().toString()))
        );
        // Label in 'Identifier'
        compilationUnit.findAll(LabeledStmt.class).forEach(node ->
            listIdentifierNode.addAll(mapName2NodeList.get(node.getLabel().toString()))
        );

        // prepare checkingMap
        Set<String> names = new HashSet<>();
        for (Node node : listIdentifierNode) {
            Range range = node.getRange().orElse(null);
            // sometimes we meet issues of JavaParser, such as
            // https://github.com/javaparser/javaparser/issues/2310
            if (mapRange2Scope.containsKey(range)) {
                Scope scope = mapRange2Scope.get(range);
                names.add(scope.name);
            }
        }
        for (String name : names) {
            Set<String> notableNames = new HashSet<>();
            // we should not only examine the first node
            mapName2NodeList.get(name).forEach(node -> {
                while (node.getParentNode().isPresent()) {
                    Node parentNode = node.getParentNode().get();
                    // Parameter in 'Identifier'
                    parentNode.findAll(Parameter.class).forEach(childNode ->
                        childNode.getParentNode().ifPresent(anotherParentNode -> {
                            if (parentNode == anotherParentNode) {
                                notableNames.add(childNode.getName().toString());
                            }
                        })
                    );
                    // MemberExpression/Property in 'Identifier'
                    parentNode.findAll(VariableDeclarator.class).forEach(childNode ->
                        childNode.getParentNode().ifPresent(anotherParentNode -> {
                            if (parentNode == anotherParentNode) {
                                notableNames.add(childNode.getName().toString());
                            }
                        })
                    );
                    // Label in 'Identifier'
                    parentNode.findAll(LabeledStmt.class).forEach(childNode ->
                        childNode.getParentNode().ifPresent(anotherParentNode -> {
                            if (parentNode == anotherParentNode) {
                                notableNames.add(childNode.getLabel().toString());
                            }
                        })
                    );
                    node = parentNode;
                }
            });
            notableNames.remove(name);
            checkingMap.put(name, notableNames);
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
        for (String notableName : checkingMap.get(oldName)) {
            if (renamingMap.getOrDefault(notableName, "@").equals(newName)) {
                return false;
            }
        }
        return true;
    }

    public void link(String oldName, String newName) {
        renamingMap.put(oldName, newName);
    }

    public void transform() {
        // Parameter in 'Identifier'
        compilationUnit.findAll(Parameter.class).forEach(node -> {
            String name = node.getName().toString();
            String renaming = renamingMap.get(name);
            node.setName(renaming);
        });
        // MemberExpression/Property in 'Identifier'
        compilationUnit.findAll(VariableDeclarator.class).forEach(node -> {
            String name = node.getName().toString();
            String renaming = renamingMap.get(name);
            node.setName(renaming);
        });
        // Label in 'Identifier'
        compilationUnit.findAll(LabeledStmt.class).forEach(node -> {
            String name = node.getLabel().toString();
            String renaming = renamingMap.get(name);
            System.out.println(node.getRange());
            node.setLabel(new SimpleName(renaming));
        });
        // fixme L49 L92 L100 L108 ...
        // NameExpr in 'Identifier'
        renamingMap.keySet().forEach(oldName -> {
            String newName = renamingMap.get(oldName);
            mapName2NodeList.get(oldName).forEach(node -> {
                if (node instanceof NameExpr){
                    System.out.println("###" + ((NameExpr) node).getName());
                    ((NameExpr) node).setName(newName);
                }
            });
        });
    }

    public static class Scope {
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
