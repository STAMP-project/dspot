package fr.inria.diversify.profiling.processor.main;

import fr.inria.diversify.profiling.processor.ProcessorUtil;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.visitor.QueryVisitor;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Simon on 16/07/14.
 */
public class FieldUsedInstrumenter extends AbstractLoggingInstrumenter<CtExecutable> {
    protected String methodObserve;
    protected Map<CtFieldReference, String> alwaysObserve;
    protected Set<Integer> alreadyInstrument;

    public FieldUsedInstrumenter(InputProgram inputProgram, String outputDir) {
        super(inputProgram);
        File file = new File(outputDir + "/log/");
        if(!file.exists()) {
            file.mkdirs();
        }
        alreadyInstrument = new HashSet<>();
    }

    public void process(CtExecutable mth) {
        try {
            if(alreadyInstrument.contains(methodId(mth))) {
                return;
            }
            alreadyInstrument.add(methodId(mth));
            int methodId = methodId(mth);
            FieldReferenceVisitor scanner = getFieldUsed(mth);
            Map<CtFieldReference, String> fieldUsed = scanner.getFields();

            addAlwayLog(fieldUsed);

            String snippet = "";
            for (CtFieldReference<?> var : fieldUsed.keySet()) {
                if (!var.getSimpleName().equals("class")) {
                    try {
                        snippet += ";\n" + getLogger() + ".writeField(Thread.currentThread(),\"" +
                                methodId + "\",\"" +
                                ProcessorUtil.idFor(var.getSimpleName()) + "\"," +
                                fieldUsed.get(var) + ")";

                        if (fieldUsed.get(var).contains(".") && !fieldUsed.get(var).contains("this.")) {
                            snippet = "try {\n\t" + snippet + ";} catch (Exception eeee) {}";
                        }

                    } catch (Exception e) {}
                }
            } if(fieldUsed.size() != 0) {
                CtCodeSnippetStatement snippetStatement = getFactory().Code().createCodeSnippetStatement(snippet);
                CtTry ctTry = tryFinallyBody(mth);
                ctTry.getFinalizer().insertBegin(snippetStatement);
            }
        } catch (Exception e) {}
    }

    protected void addAlwayLog(Map<CtFieldReference, String> fieldUsed) {

    }

    protected FieldReferenceVisitor getFieldUsed(CtExecutable mth) {
        FieldReferenceVisitor scanner = new FieldReferenceVisitor(mth);
        mth.accept(scanner);
        return scanner;
    }

    protected List<CtStatement> getSubStatement(CtStatement statement) {
        QueryVisitor query = new QueryVisitor(new TypeFilter(CtStatement.class));

        statement.accept(query);
        return query.getResult();
    }
}
