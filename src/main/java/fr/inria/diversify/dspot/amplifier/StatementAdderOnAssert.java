package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.codeFragment.InputContext;
import fr.inria.diversify.codeFragment.Statement;
import fr.inria.diversify.dspot.AmplificationChecker;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.dspot.support.Counter;
import fr.inria.diversify.dspot.value.ValueCreator;
import fr.inria.diversify.dspot.value.VarCartesianProduct;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtVariableReadImpl;

import java.util.*;
import java.util.stream.Collectors;

import static spoon.reflect.visitor.Query.getElements;

/**
 * User: Simon
 * Date: 02/12/15
 * Time: 14:55
 */
public class StatementAdderOnAssert implements Amplifier {

    private List<Statement> localVars;
    private Map<CtMethod, List<CtLiteral>> literalsByMethod;
    private List<Statement> codeFragments;
    private CtMethod currentMethod;
    private ValueCreator valueCreator;
    private int count;

    public StatementAdderOnAssert() {
        valueCreator = new ValueCreator();
    }

    @Override
    public List<CtMethod> apply(CtMethod method) {
        currentMethod = method;
        List<CtMethod> newMethods = new ArrayList<>();
        if (!codeFragments.isEmpty()) {
            List<InputContext> inputContexts = getInputContexts(method);
            if (!inputContexts.isEmpty()) {
                int index = inputContexts.size() - 1;
                List<List<Statement>> statements = buildStatements(inputContexts.get(index));
                for (List<Statement> list : statements) {
                    try {
                        newMethods.add(apply(method, list, index));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return AmplificationHelper.updateAmpTestToParent(newMethods, method);
    }

    public CtMethod applyRandom(CtMethod method) {
        throw new UnsupportedOperationException();
    }

    public void reset(CtType testClass) {
        AmplificationHelper.reset();
        literalsByMethod = new HashMap<>();

        Set<CtType> codeFragmentsProvide = AmplificationHelper.computeClassProvider(testClass);

        List<Statement> codeFragmentsByClass = codeFragmentsProvide.stream()
                .flatMap(cl -> {
                    List<CtStatement> list = Query.getElements(cl, new TypeFilter(CtStatement.class));
                    return list.stream();
                })
                .filter(stmt -> {
                    try {
                        return stmt.getParent() != null;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(stmt -> stmt.getParent(CtClass.class) != null)
                .filter(stmt -> stmt.getParent(CtClass.class).getQualifiedName().equals(testClass.getQualifiedName()))
                .filter(stmt -> stmt.getParent() instanceof CtBlock)
                .filter(stmt -> !stmt.toString().startsWith("super"))
                .filter(stmt -> !stmt.toString().startsWith("this("))
                .map(Statement::new)
                .collect(Collectors.toList());

        if (findClassUnderTest(testClass) != null) {
            codeFragments = buildCodeFragmentFor(findClassUnderTest(testClass));
        } else {
            codeFragments = new ArrayList<>();
        }

        Set<Integer> ids = new HashSet<>();
        localVars = codeFragmentsByClass.stream()
                .filter(cf -> isValidCodeFragment(cf))
                .filter(cf -> ids.add(cf.toString().hashCode()))// TODO Warning this usage of HashCode
                .collect(Collectors.toList());
    }

    private CtMethod apply(CtMethod method, List<Statement> statements, int index) {
        CtMethod cloned_method = AmplificationHelper.cloneMethodTest(method, "_cf");
        CtStatement stmt = getAssertStatement(cloned_method).get(index);
        statements.forEach(c -> {
                    stmt.insertBefore(c.getCtCodeFragment());
                    c.getCtCodeFragment().setParent(stmt.getParent());
                }
        );
        Counter.updateInputOf(cloned_method, statements.size());
        return cloned_method;
    }

    private List<InputContext> getInputContexts(CtMethod method) {
        List<InputContext> inputContexts = new ArrayList<>();

        List<CtStatement> statements = getAssertStatement(method);
        for (CtStatement stmt : statements) {
            Set<CtVariableReference> varRefs = new HashSet<>();
            for (CtLocalVariable var : getLocalVarInScope(stmt)) {
                varRefs.add(method.getFactory().Code().createLocalVariableReference(var));
            }

            inputContexts.add(new InputContext(varRefs));
        }

        return inputContexts;
    }

    private List<List<Statement>> buildStatements(InputContext inputContext) {
        return codeFragments.stream()
                .map(cf -> Collections.singletonList(new Statement(cf.getCtCodeFragment().clone())))
                .flatMap(list -> buildContext(inputContext.clone(), list, list.size() - 1).stream())
                .collect(Collectors.toList());
    }

    private List<List<Statement>> buildContext(InputContext inputContext, List<Statement> stmts, int targetIndex) {
        VarCartesianProduct varCartesianProduct = new VarCartesianProduct();
        Statement statement = stmts.get(targetIndex);

        for (CtVariableReference var : statement.getInputContext().getVar()) {

            if (!var.getType().isPrimitive()) {
                CtLocalVariable aNull = valueCreator.createNull(var.getType());
                DSpotUtils.addComment(aNull, "StatementAdderOnAssert create null value", CtComment.CommentType.INLINE);
                varCartesianProduct.addReplaceVar(var, aNull);
            }

            List<CtVariableReference> candidates = inputContext.allCandidate(var.getType(), true, false);
            if (!candidates.isEmpty()) {
                CtVariableReference replacement = candidates.get(AmplificationHelper.getRandom().nextInt(candidates.size()));
                DSpotUtils.addComment(replacement, "StatementAdderOnAssert reuse existing variable", CtComment.CommentType.INLINE);
                varCartesianProduct.addReplaceVar(var, replacement);
            }

            Statement cfLocalVar = getLocalVar(var.getType(), inputContext);
            if (cfLocalVar != null) {
                DSpotUtils.addComment(cfLocalVar.getCtCodeFragment(), "StatementAddOnAssert local variable replacement", CtComment.CommentType.INLINE);
                varCartesianProduct.addReplaceVar(var, cfLocalVar);
            }

            CtLocalVariable localVariable = createLocalVarFromMethodLiterals(currentMethod, var.getType());
            if (localVariable != null) {
                DSpotUtils.addComment(localVariable, "StatementAdderOnAssert create literal from method", CtComment.CommentType.INLINE);
                varCartesianProduct.addReplaceVar(var, localVariable);
            }
            CtLocalVariable randomVar = valueCreator.createRandomLocalVar(var.getType());
            if (randomVar != null) {
                DSpotUtils.addComment(randomVar, "StatementAdderOnAssert create random local variable", CtComment.CommentType.INLINE);
                varCartesianProduct.addReplaceVar(var, randomVar);
            }
        }

        return varCartesianProduct.apply(stmts, targetIndex);
    }

    private Statement getLocalVar(CtTypeReference type, InputContext inputContext) {

        List<Statement> list = localVars.stream()
                .filter(var -> var.getCtCodeFragment() != null)
                .filter(var -> type.equals(((CtLocalVariable) var.getCtCodeFragment()).getType()))
                .filter(var -> inputContext.getVariableOrFieldNamed(((CtLocalVariable) var.getCtCodeFragment()).getSimpleName()) == null)
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            return null;
        } else {
            boolean localVarFind;
            while (!list.isEmpty()) {
                Statement localVar = list.remove(AmplificationHelper.getRandom().nextInt(list.size()));
                localVarFind = true;
                for (CtVariableReference var : localVar.getInputContext().getVar()) {
                    CtVariableReference<?> candidate = inputContext.candidate(var.getType(), true);
                    if (candidate == null) {
                        localVarFind = false;
                        break;
                    }
                }
                if (localVarFind) {
                    Statement cloneLocalVar = new Statement(localVar.getCtCodeFragment().clone());
                    for (CtVariableReference var : localVar.getInputContext().getVar()) {
                        try {
                            CtVariableReference variable = cloneLocalVar.getInputContext().getVariableOrFieldNamed(var.getSimpleName());
                            var.replace(variable);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return cloneLocalVar;
                    }
                }
            }
            return null;
        }
    }

    private List<CtStatement> getAssertStatement(CtMethod method) {
        List<CtStatement> statements = getElements(method, new TypeFilter(CtStatement.class));
        return statements.stream()
                .filter(stmt -> stmt.getParent() instanceof CtBlock)
                .filter(AmplificationChecker::isAssert)
                .collect(Collectors.toList());
    }

    private List<CtLocalVariable> getLocalVarInScope(CtStatement stmt) {
        List<CtLocalVariable> vars = new ArrayList<>();
        try {
            CtBlock parentBlock = stmt.getParent(CtBlock.class);
            if (parentBlock != null) {
                boolean beforeCurrentStmt = true;
                int i = 0;
                List<CtStatement> stmts = parentBlock.getStatements();

                while (beforeCurrentStmt && i < stmts.size()) {
                    CtStatement currentStatement = stmts.get(i);
                    i++;
                    beforeCurrentStmt = beforeCurrentStmt && currentStatement != stmt;
                    if (currentStatement instanceof CtLocalVariable) {
                        vars.add((CtLocalVariable) currentStatement);
                    }
                }
                vars.addAll(getLocalVarInScope(parentBlock));

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return vars;
    }

    private boolean isValidCodeFragment(Statement cf) {
        CtCodeElement codeElement = cf.getCtCodeFragment();

        if (CtLocalVariable.class.isInstance(codeElement)) {
            Object defaultExpression = ((CtLocalVariable) codeElement).getDefaultExpression();
            return defaultExpression != null;
        }
        return false;
    }

    private List<Statement> buildCodeFragmentFor(CtType cl) {
        Factory factory = cl.getFactory();
        List<Statement> codeFragments = new ArrayList<>();

        for (CtMethod<?> mth : (Set<CtMethod>) cl.getMethods()) {
            if (!mth.getModifiers().contains(ModifierKind.ABSTRACT)
                    && !mth.getModifiers().contains(ModifierKind.PRIVATE)) {

                CtExecutableReference executableRef = factory.Executable().createReference(mth);
                executableRef.setStatic(mth.getModifiers().contains(ModifierKind.STATIC));
                CtInvocation invocation = factory.Code().createInvocation(buildVarRef(cl.getReference(), factory), executableRef);
                invocation.setArguments(mth.getParameters().stream()
                        .map(param -> buildVarRef(param.getType(), factory))
                        .collect(Collectors.toList()));
                invocation.setType(mth.getType());
                Statement stmt = new Statement(invocation);
                codeFragments.add(stmt);
            }

        }
        return codeFragments;
    }

    private CtVariableRead buildVarRef(CtTypeReference type, Factory factory) {
        CtTypeReference<Object> typeRef = type.clone();

        CtLocalVariable<Object> localVar = factory.Core().createLocalVariable();
        localVar.setType(typeRef);
        localVar.setSimpleName("var_" + type.getSimpleName() + "_" + System.currentTimeMillis());

        CtVariableReference localVariableReference = factory.Code().createLocalVariableReference(localVar);

        CtVariableReadImpl varRead = new CtVariableReadImpl();
        varRead.setVariable(localVariableReference);
        varRead.setFactory(factory);

        return varRead;
    }

    private CtType findClassUnderTest(CtType testClass) {
        String testClassName = testClass.getQualifiedName();
        return AmplificationHelper.computeClassProvider(testClass).stream()
                .filter(cl -> cl != null)
                .filter(cl -> !cl.equals(testClass))
                .filter(cl -> testClassName.contains(cl.getSimpleName()))
                .findFirst()
                .orElse(null);
    }

    private CtLocalVariable createLocalVarFromMethodLiterals(CtMethod method, CtTypeReference type) {
        List<CtLiteral> literals = getLiterals(method).stream()
                .filter(lit -> lit.getType() != null)
                .filter(lit -> lit.getType().equals(type))
                .collect(Collectors.toList());

        if (literals.isEmpty()) {
            return null;
        }

        CtLiteral lit = literals.get(AmplificationHelper.getRandom().nextInt(literals.size()));
        CtLocalVariable localVariable = type.getFactory().Code().createLocalVariable(type, type.getSimpleName() + "_vc_" + count++, lit);
        return localVariable;
    }

    private List<CtLiteral> getLiterals(CtMethod method) {
        if (!literalsByMethod.containsKey(method)) {
            literalsByMethod.put(method, getElements(method, new TypeFilter<>(CtLiteral.class)));
        }
        return literalsByMethod.get(method);
    }

}
