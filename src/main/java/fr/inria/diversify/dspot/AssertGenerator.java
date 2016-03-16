package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.amp.AbstractAmp;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    protected CtType makeDSpotClassTest(CtType originalClass, Collection<CtMethod> ampTests) throws IOException, ClassNotFoundException {
        CtType cloneClass = originalClass.getFactory().Core().clone(originalClass);
        cloneClass.setParent(originalClass.getParent());

        MethodAssertGenerator ag = new MethodAssertGenerator(originalClass, inputProgram, compiler, applicationClassLoader);
        for(CtMethod test : ampTests) {
            CtMethod ampTest = ag.generateAssert(test, findStatementToAssert(test));
            if(ampTest != null) {
                cloneClass.addMethod(ampTest);
            }
        }
        PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), cloneClass);

        return cloneClass;
    }

    protected List<Integer> findStatementToAssert(CtMethod test) {
        CtMethod originalTest = getOriginalTest(test);
        List<CtStatement> originalStmts = Query.getElements(originalTest, new TypeFilter(CtStatement.class));
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

    protected CtMethod getOriginalTest(CtMethod test) {
        CtMethod parent = AbstractAmp.getAmpTestToParent().get(test);
        while(AbstractAmp.getAmpTestToParent().get(parent) != null) {
            parent = AbstractAmp.getAmpTestToParent().get(parent);
        }
        return parent;
    }
}
