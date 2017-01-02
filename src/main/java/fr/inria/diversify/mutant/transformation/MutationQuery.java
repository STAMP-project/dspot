package fr.inria.diversify.mutant.transformation;

import fr.inria.diversify.coverage.ICoverageReport;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.transformation.Transformation;
import fr.inria.diversify.transformation.query.TransformationQuery;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 12/02/14
 * Time: 14:31
 */
public class MutationQuery extends TransformationQuery {
    protected ICoverageReport coverageReport;
    protected List<CtBinaryOperator> binaryOperators;
    protected List<CtUnaryOperator> unaryOperators;
    protected List<CtLiteral> numbers;
    protected List<CtReturn> returns;
    protected List<CtLocalVariable> inlineConstant;
    protected List<CtInvocation> voidMethodCall;
    protected List<CtConstructorCall> constructorCalls;

    protected static List<UnaryOperatorKind> increment = Arrays.asList(
            new UnaryOperatorKind[]{UnaryOperatorKind.POSTINC,
                    UnaryOperatorKind.PREINC,UnaryOperatorKind.POSTDEC,
                    UnaryOperatorKind.PREDEC});

    protected static List<BinaryOperatorKind> negateConditional = Arrays.asList(
            new BinaryOperatorKind[]{BinaryOperatorKind.EQ,
                    BinaryOperatorKind.NE, BinaryOperatorKind.LE,
                    BinaryOperatorKind.GT, BinaryOperatorKind.LT,
                    BinaryOperatorKind.GE});

    protected static List<BinaryOperatorKind> conditionalBoundary = Arrays.asList(
            new BinaryOperatorKind[]{BinaryOperatorKind.LT,
                    BinaryOperatorKind.GT,BinaryOperatorKind.LE,
                    BinaryOperatorKind.GE});

    protected static List<BinaryOperatorKind> math = Arrays.asList(
            new BinaryOperatorKind[]{BinaryOperatorKind.PLUS,
                    BinaryOperatorKind.MINUS,BinaryOperatorKind.MUL,
                    BinaryOperatorKind.DIV,BinaryOperatorKind.MOD,
                    BinaryOperatorKind.BITAND, BinaryOperatorKind.BITOR,
                    BinaryOperatorKind.SL,BinaryOperatorKind.SR,
                    BinaryOperatorKind.USR});

    public MutationQuery(InputProgram inputProgram) {
        super(inputProgram);
        this.coverageReport = inputProgram.getCoverageReport();
        init();
    }

    protected void init() {
        binaryOperators = getInputProgram().getAllElement(CtBinaryOperator.class);
        unaryOperators = getInputProgram().getAllElement(CtUnaryOperator.class);
        List<CtReturn> rets = getInputProgram().getAllElement(CtReturn.class);
        returns = rets.stream()
            .filter(ret -> ret.getReturnedExpression() != null)
            .collect(Collectors.toList());
        inlineConstant = getInputProgram().getAllElement(CtLocalVariable.class);

        List<CtLiteral> literals = getInputProgram().getAllElement(CtLiteral.class);
        numbers = literals.stream()
                .filter(lit -> lit.getType() != null)
                .filter(lit -> lit.getValue() != null)
                .filter(lit -> Number.class.isAssignableFrom(lit.getType().box().getActualClass()))
                .filter(lit -> ((Number)lit.getValue()).doubleValue() != 0)
                .collect(Collectors.toList());

        List<CtStatement> stmts = getInputProgram().getAllElement(CtStatement.class);
        voidMethodCall = stmts.stream()
                .filter(stmt -> stmt instanceof CtInvocation)
                .filter(stmt -> stmt.getParent() instanceof CtBlock)
                .filter(stmt -> !stmt.toString().startsWith("super("))
                .map(stmt -> (CtInvocation)stmt)
                .filter(invocation -> invocation.getType().toString().toLowerCase().equals("void"))
                .collect(Collectors.toList());

        List<CtConstructorCall> tmp = getInputProgram().getAllElement(CtConstructorCall.class);
        constructorCalls = tmp.stream()
            .filter(cc -> !(cc.getParent() instanceof CtThrow))
            .collect(Collectors.toList());
    }

    @Override
    public Transformation query() {
        try {
            Random r = new Random();
            int i = r.nextInt(7);

            Transformation t = null;
            switch (i) {
                case 0:
                    t = getNegateConditionalMutation();
                    break;
                case 1:
                    t = getConditionalBoundaryMutation();
                    break;
                case 2:
                case 3:
                case 4:
                    t = getMathMutation();
                    break;
                case 5:
                    t = getRemoveConditionalMutation();
                    break;
                case 6:
                    t = getReturnValueMutation();
                    break;
            }
            return t;
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    public IncrementMutation getIncrementMutationMutation() throws Exception {
        CtUnaryOperator operator = unaryOperators.get(random.nextInt(unaryOperators.size()));
        while (coverageReport.elementCoverage(operator) == 0 || !increment.contains(operator.getKind())) {
            operator = unaryOperators.get(random.nextInt(binaryOperators.size()));
        }
        return new IncrementMutation(operator);
    }

    public InvertNegativeMutation getInvertNegativeMutation() throws Exception {
        CtLiteral lit = numbers.get(random.nextInt(numbers.size()));
        while (coverageReport.elementCoverage(lit) == 0) {
            lit = numbers.get(random.nextInt(numbers.size()));
        }
        return new InvertNegativeMutation(lit);
    }

    public VoidMethodCallMutation getVoidMethodCallMutation() throws Exception {
        CtInvocation voidCall = voidMethodCall.get(random.nextInt(voidMethodCall.size()));
        while (coverageReport.elementCoverage(voidCall) == 0) {
            voidCall = voidMethodCall.get(random.nextInt(voidMethodCall.size()));
        }
        return new VoidMethodCallMutation(voidCall);
    }

    public ConstructorCallMutation getConstructorCallMutation() throws Exception {
        CtConstructorCall constructorCall = constructorCalls.get(random.nextInt(constructorCalls.size()));
        while (coverageReport.elementCoverage(constructorCall) == 0) {
            constructorCall = constructorCalls.get(random.nextInt(constructorCalls.size()));
        }
        return new ConstructorCallMutation(constructorCall);
    }

    public NegateConditionalMutation getNegateConditionalMutation() throws Exception {
        CtBinaryOperator operator = binaryOperators.get(random.nextInt(binaryOperators.size()));
        while (coverageReport.elementCoverage(operator) == 0 || !negateConditional.contains(operator.getKind())) {
            operator = binaryOperators.get(random.nextInt(binaryOperators.size()));
        }
        return new NegateConditionalMutation(operator);
    }

    public ConditionalBoundaryMutation getConditionalBoundaryMutation() throws Exception {
        CtBinaryOperator operator = binaryOperators.get(random.nextInt(binaryOperators.size()));
        while (coverageReport.elementCoverage(operator) == 0 || !conditionalBoundary.contains(operator.getKind())) {
            operator = binaryOperators.get(random.nextInt(binaryOperators.size()));
        }
        return new ConditionalBoundaryMutation(operator);
    }

    public MathMutation getMathMutation() throws Exception {
        CtBinaryOperator operator = binaryOperators.get(random.nextInt(binaryOperators.size()));
        while (coverageReport.elementCoverage(operator) == 0 || !math.contains(operator.getKind())) {
            operator = binaryOperators.get(random.nextInt(binaryOperators.size()));
        }
        return new MathMutation(operator);
    }

    public RemoveConditionalMutation getRemoveConditionalMutation() throws Exception {
        CtBinaryOperator operator = binaryOperators.get(random.nextInt(binaryOperators.size()));
        while (coverageReport.elementCoverage(operator) == 0 || !negateConditional.contains(operator.getKind())) {
            operator = binaryOperators.get(random.nextInt(binaryOperators.size()));
        }
        return new RemoveConditionalMutation(operator);
    }

    public ReturnValueMutation getReturnValueMutation() {
        CtReturn ret = returns.get(random.nextInt(returns.size()));
        while (coverageReport.elementCoverage(ret) == 0) {
            ret = returns.get(random.nextInt(returns.size()));
        }
        return new ReturnValueMutation(ret);
    }

    public Map<String, fr.inria.diversify.mutant.transformation.MutationTransformation> getAllTransformationFor(CtClass cl) {
        Map<String, MutationTransformation> transformations = new HashMap<>();

        List<CtBinaryOperator> operators = Query.getElements(cl, new TypeFilter(CtBinaryOperator.class));
        for(CtBinaryOperator operator : operators) {
            String position = cl.getQualifiedName() + ":" + operator.getPosition().getLine();
            BinaryOperatorKind operatorKind = operator.getKind();

            if(negateConditional.contains(operatorKind)) {
                String id = "NegateConditional_" + operator.toString() + "_" + position;
                transformations.put(id, new NegateConditionalMutation(operator));
            }

            if(conditionalBoundary.contains(operatorKind)) {
                String id = "ConditionalBoundary_" + operator.toString() + "_" + position;
                transformations.put(id, new ConditionalBoundaryMutation(operator));
            }

            if(math.contains(operatorKind)) {
                String id = "Math_" + operator.toString() + "_" + position;
                transformations.put(id, new MathMutation(operator));
            }

            if(negateConditional.contains(operatorKind)) {
                String id = "RemoveConditional_" + operator.toString() + "_" + position;
                transformations.put(id, new RemoveConditionalMutation(operator));
            }
        }

        List<CtUnaryOperator> UOperators = Query.getElements(cl, new TypeFilter(CtUnaryOperator.class));
        for(CtUnaryOperator operator : UOperators) {
            String position = cl.getQualifiedName() + ":" + operator.getPosition().getLine();
            UnaryOperatorKind operatorKind = operator.getKind();

            if(increment.contains(operatorKind)) {
                String id = "Increment_" + operator.toString() + "_" + position;
                transformations.put(id, new IncrementMutation(operator));
            }
        }

        List<CtReturn> rets = Query.getElements(cl, new TypeFilter(CtReturn.class));
        List<CtReturn> returns = rets.stream()
                .filter(ret -> ret.getReturnedExpression() != null)
                .collect(Collectors.toList());
        for(CtReturn ret : returns) {
            String position = cl.getQualifiedName() + ":" + ret.getPosition().getLine();
            String id = "ReturnValue_" + ret.toString() + "_" + position;
            transformations.put(id, new ReturnValueMutation(ret));
        }

        List<CtLiteral> literals = Query.getElements(cl, new TypeFilter(CtLiteral.class));
        List<CtLiteral> numberLit = literals.stream()
                .filter(lit -> lit.getType() != null)
                .filter(lit -> lit.getValue() != null)
                .filter(lit -> Number.class.isAssignableFrom(lit.getType().box().getActualClass()))
                .filter(lit -> ((Number) lit.getValue()).doubleValue() != 0)
                .collect(Collectors.toList());

        for(CtLiteral nb : numberLit) {
            String position = cl.getQualifiedName() + ":" + nb.getPosition().getLine();
            String id = "InvertNegative_" + nb.toString() + "_" + position;
            transformations.put(id, new InvertNegativeMutation(nb));
        }

        List<CtStatement> stmts = Query.getElements(cl, new TypeFilter(CtStatement.class));
        List<CtInvocation> voidCalls = stmts.stream()
                .filter(stmt -> stmt instanceof CtInvocation)
                .filter(stmt -> stmt.getParent() instanceof CtBlock)
                .filter(stmt -> !stmt.toString().startsWith("super("))
                .map(stmt -> (CtInvocation) stmt)
                .filter(invocation -> invocation.getType().toString().toLowerCase().equals("void"))
                .collect(Collectors.toList());

        for(CtInvocation voidCall : voidCalls) {
            String position = cl.getQualifiedName() + ":" + voidCall.getPosition().getLine();
            String id = "VoidMethodCall_" + voidCall.toString() + "_" + position;
            transformations.put(id, new VoidMethodCallMutation(voidCall));
        }

        List<CtConstructorCall> tmp = Query.getElements(cl, new TypeFilter(CtConstructorCall.class));
        List<CtConstructorCall> cc = tmp.stream()
                .filter(c -> !(c.getParent() instanceof CtThrow))
                .collect(Collectors.toList());
        for(CtConstructorCall cCall : cc) {
            String position = cl.getQualifiedName() + ":" + cCall.getPosition().getLine();
            String id = "ConstructorCall_" + cCall.toString() + "_" + position;
            transformations.put(id, new ConstructorCallMutation(cCall));
        }

        return transformations;
    }
}
