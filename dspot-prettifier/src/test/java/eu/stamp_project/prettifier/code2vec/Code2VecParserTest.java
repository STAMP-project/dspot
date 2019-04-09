package eu.stamp_project.prettifier.code2vec;

import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/02/19
 */
public class Code2VecParserTest {

    private static final String outputOfCode2Vec = "Original name:\tf" + AmplificationHelper.LINE_SEPARATOR +
            "\t(0.277656) predicted: ['test', 'equalslit', 'string']" + AmplificationHelper.LINE_SEPARATOR +
            "\t(0.245518) predicted: ['test', 'convert', 'to', 'string']" + AmplificationHelper.LINE_SEPARATOR +
            "\t(0.115852) predicted: ['test', 'convert']" + AmplificationHelper.LINE_SEPARATOR +
            "\t(0.064386) predicted: ['restricted', 'html', 'literal', 'mutation', 'string', 'literal', 'mutation', 'string']" + AmplificationHelper.LINE_SEPARATOR +
            "\t(0.063261) predicted: ['explicit', 'filter', 'list', 'literal', 'mutation', 'string']" + AmplificationHelper.LINE_SEPARATOR +
            "\t(0.050013) predicted: ['assert', 'html']" + AmplificationHelper.LINE_SEPARATOR +
            "\t(0.047732) predicted: ['explicit', 'filter', 'list', 'literal', 'mutation', 'string', 'literal', 'mutation', 'string', 'literal', 'mutation', 'string']" + AmplificationHelper.LINE_SEPARATOR +
            "\t(0.047235) predicted: ['assert', 'html', 'with', 'head', 'content']" + AmplificationHelper.LINE_SEPARATOR +
            "\t(0.047199) predicted: ['clean', 'title', 'with', 'namespace']" + AmplificationHelper.LINE_SEPARATOR +
            "\t(0.041149) predicted: ['explicit', 'filter', 'list', 'literal', 'mutation', 'string', 'literal', 'mutation', 'string']" + AmplificationHelper.LINE_SEPARATOR +
            "Attention:" + AmplificationHelper.LINE_SEPARATOR +
            "0.327380\tcontext: htmlutils,(NameExpr0)^(MethodCallExpr)^(VariableDeclarator)^(VariableDeclarationExpr)^(ExpressionStmt)^(BlockStmt)_(ExpressionStmt)_(MethodCallExpr0)_(NameExpr4),assertequals" + AmplificationHelper.LINE_SEPARATOR +
            "0.139097\tcontext: tostring,(NameExpr3)^(MethodCallExpr)^(VariableDeclarator)^(VariableDeclarationExpr)^(ExpressionStmt)^(BlockStmt)_(ExpressionStmt)_(MethodCallExpr0)_(NameExpr4),assertequals" + AmplificationHelper.LINE_SEPARATOR +
            "0.138897\tcontext: xmlversionencodingutfdoctypehtmlpublicwcdtdxhtmlst,(StringLiteralExpr2)^(MethodCallExpr)^(ExpressionStmt)^(BlockStmt)_(ExpressionStmt)_(MethodCallExpr0)_(NameExpr4),assertequals" + AmplificationHelper.LINE_SEPARATOR +
            "0.083175\tcontext: collections,(NameExpr0)^(MethodCallExpr2)^(MethodCallExpr)^(ExpressionStmt)^(BlockStmt)_(ExpressionStmt)_(MethodCallExpr0)_(NameExpr4),assertequals" + AmplificationHelper.LINE_SEPARATOR +
            "0.067213\tcontext: emptylist,(NameExpr2)^(MethodCallExpr2)^(MethodCallExpr)^(ExpressionStmt)^(BlockStmt)_(ExpressionStmt)_(MethodCallExpr0)_(NameExpr4),assertequals" + AmplificationHelper.LINE_SEPARATOR +
            "0.047436\tcontext: xmlversionencodingutfdoctypehtmlpublicwcdtdxhtmlst,(StringLiteralExpr2)^(MethodCallExpr)_(NameExpr4),assertequals" + AmplificationHelper.LINE_SEPARATOR +
            "0.027899\tcontext: setfilters,(NameExpr3)^(MethodCallExpr)^(ExpressionStmt)^(BlockStmt)_(ExpressionStmt)_(VariableDeclarationExpr)_(VariableDeclarator)_(MethodCallExpr1)_(NameExpr3),tostring" + AmplificationHelper.LINE_SEPARATOR +
            "0.025320\tcontext: configuration,(NameExpr0)^(MethodCallExpr)^(ExpressionStmt)^(BlockStmt)_(ExpressionStmt)_(MethodCallExpr0)_(NameExpr4),assertequals" + AmplificationHelper.LINE_SEPARATOR +
            "0.018143\tcontext: htmlcleanerconfiguration,(ClassOrInterfaceType0)^(VariableDeclarationExpr)^(ExpressionStmt)^(BlockStmt)_(ExpressionStmt)_(VariableDeclarationExpr)_(VariableDeclarator)_(MethodCallExpr1)_(NameExpr3),tostring" + AmplificationHelper.LINE_SEPARATOR +
            "0.012331\tcontext: setfilters,(NameExpr3)^(MethodCallExpr)^(ExpressionStmt)^(BlockStmt)_(ExpressionStmt)_(MethodCallExpr0)_(NameExpr4),assertequals" + AmplificationHelper.LINE_SEPARATOR +
            "Modify the file: \"Input.java\" and press any key when ready, or \"q\" / \"quit\" / \"exit\" to exit" + AmplificationHelper.LINE_SEPARATOR;

    @Test
    public void testParsing() {

        /*
            Test the parser. It should returns the concatenation of predicted labels that have the most probability
                In case of it predicts twice the same name, we:
                    add an index at the end of the name in order to make it unique
                        TODO check if this is the best strategy
         */

        final Code2VecParser code2VecParser = new Code2VecParser();
        String parse = code2VecParser.parse(outputOfCode2Vec);
        assertEquals("testEqualslitString" , parse);
        for (int i = 1 ; i < 11 ; i ++) {
            parse = code2VecParser.parse(outputOfCode2Vec);
            assertEquals("testEqualslitString" + i , parse);
        }
    }
}
