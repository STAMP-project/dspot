package eu.stamp_project.prettifier.context2name.C2N.java;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.JavaToken.Category;
import com.github.javaparser.ast.stmt.LabeledStmt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static com.github.javaparser.GeneratedJavaParserConstants.*;
import static com.github.javaparser.GeneratedJavaParserConstants.IDENTIFIER;
import static com.github.javaparser.JavaToken.Category.*;
import static com.github.javaparser.Providers.provider;

public class Tmp {
    private final String CONTEXT2NAME_DIR = "src/main/resources/context2name/";

    private final ArrayList<Integer> IGNORED_TOKEN_KINDS = new ArrayList<>(Arrays.asList(LPAREN/*(*/, RPAREN/*)*/, DOT/*.*/));
    private final ArrayList<Category> IGNORED_TOKEN_CATEGORIES = new ArrayList<>(Arrays.asList(WHITESPACE_NO_EOL, EOL, COMMENT));

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

    void type() {
        CompilationUnit compilationUnit = parseFile("data/Demo.java");
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(node -> System.out.println(node.getName()));
        System.out.println("========");

        List<Node> nodesList = new ArrayList<>();
        Map<String, List<Node>> nodesMap = new HashMap<>();
        compilationUnit.findAll(Node.class).stream()
            .filter(node -> node.getChildNodes().size() == 0)
            .filter(node -> node.getTokenRange().filter(tokenRange -> tokenRange.getBegin().getKind() == IDENTIFIER).isPresent()).forEach(node -> {
            nodesMap.putIfAbsent(node.toString(), new ArrayList<>());
            nodesMap.get(node.toString()).add(node);
        });
        System.out.println("nodesMap\n" + nodesMap);

        // Parameter in 'Identifier'
        compilationUnit.findAll(Parameter.class)
            .forEach(node -> nodesList.addAll(nodesMap.get(node.getName().getIdentifier())));
        // MemberExpression/Property in 'Identifier'
        compilationUnit.findAll(VariableDeclarator.class)
            .forEach(node -> nodesList.addAll(nodesMap.get(node.getName().getIdentifier())));
        // Label in 'Identifier'
        compilationUnit.findAll(LabeledStmt.class)
            .forEach(node -> nodesList.addAll(nodesMap.get(node.getLabel().getIdentifier())));
        // SimpleName in 'Identifier'
        compilationUnit.findAll(SimpleName.class)
            .forEach(node -> nodesList.addAll(nodesMap.get(node.getIdentifier())));
        nodesList.forEach(
            node -> System.out.println(node.toString() + node.getRange())
        );

//        nodesList.forEach(
//            node -> {
//                System.out.println(getPath(node));
//                node.getParentNode().ifPresent(parent -> {
//                    System.out.println(getPath(parent));
//                    System.out.println("================");
//                });
//            }
//        );
//        Node messNode0 = nodesList.get(5);
//        Node messNode1 = nodesList.get(6);
//        Node messNode2 = nodesList.get(7);
//        Node messNode3 = nodesList.get(8);
//        Node messNode4 = nodesList.get(9);
//        System.out.println(getPath(messNode0));
//        System.out.println(getPath(messNode1));
//        System.out.println(getPath(messNode2));
//        System.out.println(getPath(messNode3));
//        System.out.println(getPath(messNode4));
    }

    private String getPath(Node node) {
        StringBuilder stringBuilder = new StringBuilder();
        while (node != null) {
            stringBuilder.append(node.getRange().orElse(null));
            node = node.getParentNode().orElse(null);
        }
        return stringBuilder.toString();
    }

    void tmp() {
        HashMap<Range, JavaToken> mapRange2JavaToken = new HashMap<>();
        CompilationUnit compilationUnit = parseFile("data/Demo.java");
        compilationUnit.getTokenRange().ifPresent(
            tokenRange -> {
                for (JavaToken javaToken : tokenRange) {
                    if (IGNORED_TOKEN_KINDS.contains(javaToken.getKind()) || IGNORED_TOKEN_CATEGORIES.contains(javaToken.getCategory())) {
                        continue;
                    }
                    mapRange2JavaToken.put(javaToken.getRange().orElse(null), javaToken);
                }
                System.out.println(mapRange2JavaToken);
            }
        );
    }

    void test() {
        ParseResult<Expression> result = new JavaParser().parse(ParseStart.EXPRESSION, provider("1 +/*2*/1 "));
        Optional<Expression> optionalExpression = result.getResult();
        if (optionalExpression.isPresent()) {
            Optional<TokenRange> optionalTokenRange = optionalExpression.get().getTokenRange();
            if (optionalTokenRange.isPresent()) {
                Iterator<JavaToken> iterator = optionalTokenRange.get().iterator();

                assertToken("1", Range.range(1, 1, 1, 1), INTEGER_LITERAL, LITERAL, iterator.next());
                assertToken(" ", Range.range(1, 2, 1, 2), SPACE, WHITESPACE_NO_EOL, iterator.next());
                assertToken("+", Range.range(1, 3, 1, 3), PLUS, OPERATOR, iterator.next());
                assertToken("/*2*/", Range.range(1, 4, 1, 8), MULTI_LINE_COMMENT, COMMENT, iterator.next());
                assertToken("1", Range.range(1, 9, 1, 9), INTEGER_LITERAL, LITERAL, iterator.next());
                assertToken(" ", Range.range(1, 10, 1, 10), SPACE, WHITESPACE_NO_EOL, iterator.next());
                assertToken("", Range.range(1, 10, 1, 10), EOF, WHITESPACE_NO_EOL, iterator.next());
            }
        }
    }

    private void assertToken(String image, Range range, int kind, JavaToken.Category category, JavaToken token) {
        System.out.println(image.equals(token.getText()));
        System.out.println(range.equals(token.getRange().orElse(null)));
        System.out.println(kind == token.getKind());
        System.out.println(category.equals(token.getCategory()));
        token.getNextToken().ifPresent(nt -> System.out.println(token.equals(nt.getPreviousToken().orElse(null))));
        token.getPreviousToken().ifPresent(pt -> System.out.println(token.equals(pt.getNextToken().orElse(null))));
    }

    public static void main(String[] args) {
//        new Tmp().tmp();
        new Tmp().type();
    }
}
