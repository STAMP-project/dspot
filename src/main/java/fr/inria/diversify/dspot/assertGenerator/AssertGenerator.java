package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 12/02/16
 * Time: 10:31
 */
public class AssertGenerator {
    protected DiversifyClassLoader applicationClassLoader;
    protected InputProgram inputProgram;
    protected DiversityCompiler compiler;


    public AssertGenerator(InputProgram inputProgram, DiversityCompiler compiler, DiversifyClassLoader applicationClassLoader) {
        this.inputProgram = inputProgram;
        this.compiler = compiler;
        this.applicationClassLoader = applicationClassLoader;
    }

    public CtType generateAsserts(CtType testClass) throws IOException, ClassNotFoundException {
        return generateAsserts(testClass, testClass.getMethods(), null);
    }

    protected CtType generateAsserts(CtType testClass, Collection<CtMethod> tests, Map<CtMethod, CtMethod> parentTest) throws IOException, ClassNotFoundException {
        CtType cloneClass = inputProgram.getFactory().Core().clone(testClass);
        cloneClass.setParent(testClass.getParent());

        MethodAssertGenerator ag = new MethodAssertGenerator(testClass, inputProgram, compiler, applicationClassLoader);
        for(CtMethod test : tests) {
            CtMethod ampTest = ag.generateAssert(test, findStatementToAssert(test, parentTest));
            if(ampTest != null) {
                cloneClass.addMethod(ampTest);
            }
        }
        PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), cloneClass);

        return cloneClass;
    }

    protected List<Integer> findStatementToAssert(CtMethod test, Map<CtMethod, CtMethod> parentTest) {
        if(parentTest != null) {
            CtMethod parent = parentTest.get(test);
            while(parentTest.get(parent) != null) {
                parent = parentTest.get(parent);
            }
            return findStatementToAssertFromParent(test, parent);
        } else {
            return findStatementToAssertOnlyInvocation(test);
        }
    }

    protected List<Integer> findStatementToAssertOnlyInvocation(CtMethod test) {
        List<CtStatement> stmts = Query.getElements(test, new TypeFilter(CtStatement.class));
        List<Integer> indexs = new ArrayList<>();
        for(int i = 0; i < stmts.size(); i++) {
            if(CtInvocation.class.isInstance(stmts.get(i))) {
                indexs.add(i);
            }
        }
        return indexs;
    }

    protected List<Integer> findStatementToAssertFromParent(CtMethod test, CtMethod parentTest) {
        List<CtStatement> originalStmts = Query.getElements(parentTest, new TypeFilter(CtStatement.class));
        List<String> originalStmtStrings = originalStmts.stream()
                .map(stmt -> stmt.toString())
                .collect(Collectors.toList());

        List<CtStatement> ampStmts = Query.getElements(test, new TypeFilter(CtStatement.class));
        List<String> ampStmtStrings = ampStmts.stream()
                .map(stmt -> stmt.toString())
                .collect(Collectors.toList());

        List<Integer> indexs = new ArrayList<>();
        for(int i = 0; i < ampStmtStrings.size(); i++) {
            int index = originalStmtStrings.indexOf(ampStmtStrings.get(i));
            if(index == -1) {
                indexs.add(i);
            } else {
                originalStmtStrings.remove(index);
            }
        }
        return indexs;
    }
}
