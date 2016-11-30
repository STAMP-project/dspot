package fr.inria.diversify.dspot.amp;

import fr.inria.diversify.codeFragment.*;
import fr.inria.diversify.log.branch.Coverage;
import fr.inria.diversify.util.Log;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtVariableReadImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 02/12/15
 * Time: 14:55
 */
public class StatementAdderOnAssert implements Amplifier {
    protected List<Statement> localVars;
    protected Map<CtMethod, List<CtLiteral>> literalsByMethod;
    protected Map<Statement, Double> coverageBycodeFragments;
    protected CtMethod currentMethod;

    @Override
    public List<CtMethod> apply(CtMethod method) {
        currentMethod = method;
        List<CtMethod> newMethods = new ArrayList<>();
        if(!coverageBycodeFragments.isEmpty()) {
            List<InputContext> inputContexts = getInputContexts(method);
            if (!inputContexts.isEmpty()) {
                int index = inputContexts.size() - 1;
                List<List<Statement>> statements = buildStatements(inputContexts.get(index));
                for (List<Statement> list : statements) {
                    try {
                        newMethods.add(apply(method, list, index));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.debug("");
                    }
                }
            }
        }
        return AmplifierHelper.updateAmpTestToParent(newMethods, method);
    }

    public CtMethod applyRandom(CtMethod method) {
        return null;
    }

    protected CtMethod apply(CtMethod method,  List<Statement> statements, int index) {
        CtMethod cloned_method = AmplifierHelper.cloneMethodTest(method, "_cf", 1000);
        CtStatement stmt = getAssertStatement(cloned_method)
                .get(index);
        statements.stream()
                .forEach(c ->
                {
                    stmt.insertBefore((CtStatement) c.getCtCodeFragment());
                    c.getCtCodeFragment().setParent(stmt.getParent());
                });

        return cloned_method;
    }

    protected List<InputContext> getInputContexts(CtMethod method) {
        List<InputContext> inputContexts = new ArrayList<>();

        List<CtStatement> statements = getAssertStatement(method);
        for(CtStatement stmt : statements) {
            Set<CtVariableReference> varRefs = new HashSet<>();
            for(CtLocalVariable var : getLocalVarInScope(stmt)) {
                varRefs.add(method.getFactory().Code().createLocalVariableReference(var));
            }

            inputContexts.add(new InputContext(varRefs));
        }

        return inputContexts;
    }

    protected List<List<Statement>> buildStatements(InputContext inputContext) {
        return coverageBycodeFragments.keySet().stream()
                .map(cf -> {
                    List<Statement> list = new ArrayList<>(2);
                    list.add(cf.clone());
                    return list;
                })
                .flatMap(list -> {
                    InputContext cloneInputContext = inputContext.clone();
                    return buildContext(cloneInputContext, list, list.size() -1).stream();
                })
                .collect(Collectors.toList());
    }

    protected List<List<Statement>> buildContext(InputContext inputContext, List<Statement> stmts , int targetIndex) {
        VarCartesianProduct varCartesianProduct = new VarCartesianProduct();
        Statement statement = stmts.get(targetIndex);
        ValueCreator vc = new ValueCreator();
        for(CtVariableReference var : statement.getInputContext().getVar()) {

            varCartesianProduct.addReplaceVar(var, vc.createNull(var.getType()));

            List<CtVariableReference> candidates = inputContext.allCandidate(var.getType(), true, false);
            if(!candidates.isEmpty()) {
                varCartesianProduct.addReplaceVar(var, candidates.get(AmplifierHelper.getRandom().nextInt(candidates.size())));
            }

            Statement cfLocalVar = getLocalVar(var.getType(), inputContext);
            if (cfLocalVar != null) {
                varCartesianProduct.addReplaceVar(var, cfLocalVar);
            }

            CtLocalVariable localVariable = createLocalVarFromMethodLiterals(currentMethod, var.getType());
            if(localVariable != null) {
                varCartesianProduct.addReplaceVar(var, localVariable);
            }

            CtLocalVariable randomVar = vc.createRandomLocalVar(var.getType());
            if(randomVar != null) {
                varCartesianProduct.addReplaceVar(var, randomVar);
            }
        }

       return varCartesianProduct.apply(stmts, targetIndex);
    }

    protected Statement getLocalVar(CtTypeReference type, InputContext inputContext) {
        List<Statement> list = localVars.stream()
                .filter(var -> var.getCtCodeFragment() != null)
                .filter(var -> type.equals(((CtLocalVariable) var.getCtCodeFragment()).getType()))
                .filter(var -> inputContext.getVariableOrFieldNamed(((CtLocalVariable)var.getCtCodeFragment()).getSimpleName()) == null)
                .collect(Collectors.toList());

        if(list.isEmpty()) {
            return null;
        } else {
            Factory factory = type.getFactory();
            boolean localVarFind;
            while(!list.isEmpty()) {
                Statement localVar = list.remove(AmplifierHelper.getRandom().nextInt(list.size()));
                localVarFind = true;
                for (CtVariableReference var : localVar.getInputContext().getVar()) {
                    CtVariableReference<?> candidate = inputContext.candidate(var.getType(), true);
                    if (candidate == null) {
                        localVarFind = false;
                        break;
                    }
                }
                if(localVarFind) {
                    try {
                        Statement cloneLocalVar = localVar.clone();
                        for (CtVariableReference var : localVar.getInputContext().getVar()) {
                            CtVariableReference variable = cloneLocalVar.getInputContext().getVariableOrFieldNamed(var.getSimpleName());
                            cloneLocalVar.getInputContext().getVariableOrFieldNamed(var.getSimpleName()).replace(variable);
                        }
                        return cloneLocalVar;
                    } catch (Exception e) {}
                }
            }
            return null;
        }
    }

    protected  List<CtStatement> getAssertStatement(CtMethod method) {
        List<CtStatement> statements = Query.getElements(method, new TypeFilter(CtStatement.class));
        return statements.stream()
                .filter(stmt -> stmt.getParent() instanceof CtBlock)
                .filter(stmt -> AmplifierChecker.isAssert(stmt))
                .collect(Collectors.toList());
    }

    protected List<CtLocalVariable> getLocalVarInScope(CtStatement stmt) {
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
        } catch (Exception e) {}
        return vars;
    }

    protected boolean isValidCodeFragment(Statement cf) {
        CtCodeElement codeElement = cf.getCtCodeFragment();

        if(CtLocalVariable.class.isInstance(codeElement) ) {
            Object defaultExpression = ((CtLocalVariable) codeElement).getDefaultExpression();
            return defaultExpression != null;
        }
        return false;
    }

    protected Map<Statement, Double> buildCodeFragmentFor(CtType cl, Coverage coverage) {
        Factory factory = cl.getFactory();
        Map<Statement, Double> codeFragments = new IdentityHashMap<>();

        for(CtMethod<?> mth : (Set<CtMethod>)cl.getAllMethods()) {
            if(! mth.getModifiers().contains(ModifierKind.ABSTRACT)
                    && !mth.getModifiers().contains(ModifierKind.PRIVATE)) {
//                    && getCoverageForMethod(coverage, cl, mth) != 1.0) {

                CtExecutableReference executableRef = factory.Executable().createReference(mth);
                CtInvocation invocation;
                if (mth.getModifiers().contains(ModifierKind.STATIC)) {
                    executableRef.setStatic(true);
                    invocation = factory.Code().createInvocation(null, executableRef);
                } else {
                    executableRef.setStatic(false);
                    invocation = factory.Code().createInvocation(null, executableRef);
                    invocation.setTarget(buildVarRef(cl.getReference(), factory));
                }
                invocation.setArguments(mth.getParameters().stream()
                                .map(param -> buildVarRef(param.getType(), factory))
                        .collect(Collectors.toList()));
                invocation.setType(mth.getType());
                Statement stmt = new Statement(invocation);
                codeFragments.put(stmt, getCoverageForMethod(coverage, cl, mth));
            }

        }
        return codeFragments;
    }

    protected CtVariableRead buildVarRef(CtTypeReference type, Factory factory) {
        CtTypeReference<Object> typeRef = factory.Core().clone(type);

        CtLocalVariable<Object> localVar = factory.Core().createLocalVariable();
        localVar.setType(typeRef);
        localVar.setSimpleName("var_" + type.getSimpleName() + "_" + System.currentTimeMillis());

        CtVariableReadImpl varRead = new CtVariableReadImpl();
        varRead.setVariable(factory.Code().createLocalVariableReference(localVar));
        return varRead;
    }

    protected CtType findClassUnderTest(CtType testClass) {
        String testClassName = testClass.getQualifiedName();
        return AmplifierHelper.computeClassProvider(testClass).stream()
                .filter(cl -> cl != null)
                .filter(cl -> cl != testClass)
                //.filter(cl -> testClassName.contains(cl.getQualifiedName()))
                .findFirst() // TODO
                .orElse(null);
    }

    protected double getCoverageForMethod(Coverage coverage, CtType cl, CtMethod mth) {
        if(coverage == null) {
            return 0d;
        }

        String key = mth.getDeclaringType().getQualifiedName() + "_"
                +  mth.getType().getQualifiedName()  + "_"
                + mth.getSimpleName() + "("
                + mth.getParameters().stream()
                    .map(param -> ((CtParameter)param).getType().getQualifiedName())
                    .collect(Collectors.joining(","))
                + ")";

        if(coverage.getMethodCoverage(key) != null) {
            return  coverage.getMethodCoverage(key).coverage();
        } else {
            key = cl.getQualifiedName() + "_"
                    +  mth.getType().getQualifiedName()  + "_"
                    + mth.getSimpleName() + "("
                    + mth.getParameters().stream()
                    .map(param -> ((CtParameter)param).getType().getQualifiedName())
                    .collect(Collectors.joining(","))
                    + ")";
            if(coverage.getMethodCoverage(key) != null) {
                return coverage.getMethodCoverage(key).coverage();
            } else {
                return 0d;
            }
        }
    }

    protected int count;
    protected CtLocalVariable createLocalVarFromMethodLiterals(CtMethod method, CtTypeReference type) {
        List<CtLiteral> literals = getLiterals(method).stream()
                .filter(lit -> lit.getType() != null)
                .filter(lit -> lit.getType().equals(type))
                .collect(Collectors.toList());

        if(literals.isEmpty()) {
            return null;
        }

        CtLiteral lit = literals.get(AmplifierHelper.getRandom().nextInt(literals.size()));
        return type.getFactory().Code().createLocalVariable(type, "vc_"+count++,lit);
    }

    protected List<CtLiteral> getLiterals(CtMethod method) {
        if(!literalsByMethod.containsKey(method)) {
            literalsByMethod.put(method, Query.getElements(method, new TypeFilter<CtLiteral>(CtLiteral.class)));
        }
        return literalsByMethod.get(method);
    }

    public void reset(Coverage coverage, CtType testClass) {
        AmplifierHelper.reset();
        literalsByMethod = new HashMap<>();

        Set<CtType> codeFragmentsProvide = AmplifierHelper.computeClassProvider(testClass);

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
                .filter(stmt -> stmt.getParent() instanceof CtBlock)
                .filter(stmt -> !stmt.toString().startsWith("super"))
                .filter(stmt -> !stmt.toString().startsWith("this("))
                .map(stmt -> new Statement(stmt))
                .collect(Collectors.toList());

        if(findClassUnderTest(testClass) != null) {
            coverageBycodeFragments = buildCodeFragmentFor(findClassUnderTest(testClass), coverage);
        } else {
            coverageBycodeFragments = new HashMap<>();
        }

        Set<Integer> ids = new HashSet<>();
        localVars = codeFragmentsByClass.stream()
                .filter(cf -> isValidCodeFragment(cf))
                .filter(cf -> ids.add(cf.id()))
                .collect(Collectors.toList());
    }
}
