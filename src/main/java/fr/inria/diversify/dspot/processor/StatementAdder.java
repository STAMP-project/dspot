package fr.inria.diversify.dspot.processor;

import fr.inria.diversify.codeFragment.*;
import fr.inria.diversify.codeFragmentProcessor.*;
import fr.inria.diversify.profiling.coverage.Coverage;
import fr.inria.diversify.runner.InputProgram;
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
public class StatementAdder extends AbstractAmp {
    protected List<CodeFragment> localVars;
    protected Map<CtMethod, List<CtLiteral>> literalsByMethod;
    protected Map<CodeFragment, Double> coverageBycodeFragments;
    protected CtMethod currentMethod;

    @Override
    public List<CtMethod> apply(CtMethod method) {
        currentMethod = method;
        List<CtMethod> newMethods = new ArrayList<>();
        if(!coverageBycodeFragments.isEmpty()) {
            List<InputContext> inputContexts = getInputContexts(method);
            for(int i = 0; i < inputContexts.size(); i++) {
                InputContext inputContext = inputContexts.get(i);
                List<CodeFragment> statements = getRandomInvocation(inputContext);
                try {
                    newMethods.add(apply(method, statements, i));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.debug("");
                }
            }
        }
        return filterAmpTest(newMethods, method);
    }

    public CtMethod applyRandom(CtMethod method) {
        currentMethod = method;
        List<InputContext> inputContexts = getInputContexts(method);

        while(!inputContexts.isEmpty()) {
            try {
                int index = getRandom().nextInt(inputContexts.size());
                List<CodeFragment> statements = getRandomInvocation(inputContexts.get(index));
                return apply(method, statements, index);
            } catch (Exception e) {}
        }
        return null;
    }

    protected CtMethod apply(CtMethod method,  List<CodeFragment> statements, int index) {
        CtMethod cloned_method = cloneMethodTest(method, "_cf", 1000);
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

    protected List<CodeFragment> getRandomInvocation(InputContext inputContext) {
        List<CodeFragment> stmts = new ArrayList<>();

        CodeFragment codeFragment = getRandomCodeFragment(1.1);
        Factory factory = codeFragment.getCtCodeFragment().getFactory();

        CodeFragment clone = factory.Core().clone(codeFragment);
        InputContext cloneInputContext = inputContext.clone();
        findLocalVar(cloneInputContext, clone, stmts);

        codeFragment = getRandomCodeFragment(1d);
        clone = factory.Core().clone(codeFragment);
        findLocalVar(cloneInputContext, clone,stmts);

        return stmts;
    }

    protected void findLocalVar(InputContext inputContext, CodeFragment stmt, List<CodeFragment> stmts) {
        Factory factory = stmt.getCtCodeFragment().getFactory();

        for(CtVariableReference var : stmt.getInputContext().getVar()) {
            List<CtVariableReference> candidates = inputContext.allCandidate(var.getType(), true, false);
            CtVariableReference candidate;
            if(!candidates.isEmpty()) {
                candidate = candidates.get(getRandom().nextInt(candidates.size()));
            } else {
                CtLocalVariable localVariable = createLocalVarFromMethodLiterals(currentMethod, var.getType());
                if(false && localVariable != null ) {
                    candidate = factory.Code().createLocalVariableReference(localVariable);
                } else {
                    CodeFragment cfLocalVar = getLocalVar(var.getType(), inputContext);
                    if (cfLocalVar != null) {
                        stmts.add(cfLocalVar);
                        localVariable = (CtLocalVariable) cfLocalVar.getCtCodeFragment();
                        candidate = factory.Code().createLocalVariableReference(localVariable);
                    } else {
                        ValueCreator vc = new ValueCreator();
                        CtLocalVariable localVar = vc.createRandomLocalVar(var.getType());
                        stmts.add(new Statement(localVar));
                        candidate = factory.Code().createLocalVariableReference(localVar);
                    }
                }
                inputContext.addVariableRef(candidate);
            }
            stmt.getInputContext().getVariableOrFieldNamed(var.getSimpleName()).replace(candidate);
        }

        stmts.add(stmt);
    }

    protected CodeFragment getLocalVar(CtTypeReference type, InputContext inputContext) {
        List<CodeFragment> list = localVars.stream()
                .filter(var -> var.getCtCodeFragment() != null)
                .filter(var -> type.equals(((CtLocalVariable) var.getCtCodeFragment()).getType()))
                .collect(Collectors.toList());

        if(list.isEmpty()) {
            return null;
        } else {
            Factory factory = type.getFactory();
            boolean localVarFind;
            while(!list.isEmpty()) {
                CodeFragment localVar = list.remove(getRandom().nextInt(list.size()));
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
                        CodeFragment cloneLocalVar = factory.Core().clone(localVar);
                        for (CtVariableReference var : localVar.getInputContext().getVar()) {

                            CtVariableReference variable = cloneLocalVar.getInputContext().getVariableOrFieldNamed(var.getSimpleName());
                            var.replace(variable);
                        }
                        return cloneLocalVar;
                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Log.debug("");
                    }
                }
            }
            return null;
        }
    }

    protected CodeFragment getRandomCodeFragment(double maxCoverage) {
        List<CodeFragment> codeFragments = coverageBycodeFragments.keySet().stream()
                .filter(cf -> coverageBycodeFragments.get(cf) < maxCoverage)
                .collect(Collectors.toList());

        return codeFragments.get(getRandom().nextInt(codeFragments.size()));
    }

    protected  List<CtStatement> getAssertStatement(CtMethod method) {
        List<CtStatement> statements = Query.getElements(method, new TypeFilter(CtStatement.class));
        return statements.stream()
                .filter(stmt -> stmt.getParent() instanceof CtBlock)
                .filter(stmt -> isAssert(stmt))
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

    protected boolean isValidCodeFragment(CodeFragment cf) {
        CtCodeElement codeElement = cf.getCtCodeFragment();

        if(CtLocalVariable.class.isInstance(codeElement) ) {
            Object defaultExpression = ((CtLocalVariable) codeElement).getDefaultExpression();
            return defaultExpression != null;
        }
        return false;
    }

    protected Map<CodeFragment, Double> buildCodeFragmentFor(CtType cl, Coverage coverage) {
        Factory factory = cl.getFactory();
        Map<CodeFragment, Double> codeFragments = new IdentityHashMap<>();

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

    protected CtType findClassUnderTest(CtClass testClass) {
        return computeClassProvider(testClass).stream()
                .filter(cl -> testClass.getQualifiedName().contains(cl.getQualifiedName()) && cl != testClass)
                .findFirst()
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

        CtLiteral lit = literals.get(getRandom().nextInt(literals.size()));
        return type.getFactory().Code().createLocalVariable(type, "vc_"+count++,lit);
    }

    protected List<CtLiteral> getLiterals(CtMethod method) {
        if(!literalsByMethod.containsKey(method)) {
            literalsByMethod.put(method, Query.getElements(method, new TypeFilter<CtLiteral>(CtLiteral.class)));
        }
        return literalsByMethod.get(method);
    }

    public void reset(InputProgram inputProgram, Coverage coverage, CtClass testClass) {
        super.reset(inputProgram, coverage, testClass);
        literalsByMethod = new HashMap<>();

        Set<CtType> codeFragmentsProvide = computeClassProvider(testClass);

        StatementProcessor codeFragmentProcessor = (StatementProcessor) inputProgram.getCodeFragmentProcessor();
        codeFragmentsProvide.stream()
                .flatMap(cl -> {
                    List<CtStatement> list = Query.getElements(cl, new TypeFilter(CtStatement.class));
                    return list.stream();
                })
                .filter(stmt -> codeFragmentProcessor.isToBeProcessed(stmt))
                .forEach(stmt -> codeFragmentProcessor.process(stmt));

        HashMap<String, CodeFragmentList> codeFragmentsByClass = codeFragmentProcessor.getCodeFragmentsByClass();
        if(findClassUnderTest(testClass) != null) {
            coverageBycodeFragments = buildCodeFragmentFor(findClassUnderTest(testClass), coverage);
        } else {
            coverageBycodeFragments = new HashMap<>();
        }

        Set<Integer> ids = new HashSet<>();
        localVars = codeFragmentsProvide.stream()
                .map(cl -> cl.getQualifiedName())
                .filter(cl -> codeFragmentsByClass.containsKey(cl))
                .flatMap(cl -> codeFragmentsByClass.get(cl).stream()
                        .filter(cf -> isValidCodeFragment(cf)))
                .filter(cf -> ids.add(cf.id()))
                .collect(Collectors.toList());
    }
}
