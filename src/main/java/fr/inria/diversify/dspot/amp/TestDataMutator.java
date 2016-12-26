

package fr.inria.diversify.dspot.amp;

import fr.inria.diversify.dspot.AmplificationChecker;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.log.branch.Coverage;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;

/* This processor is meant to replace all literal values in test cases by other literal values
 * */
public class TestDataMutator implements Amplifier {

    @Deprecated
    private int dataCount = 0;

    private Map<CtType, Set<Object>> literalByClass;

    private Map<Class<?>, List<Object>> literals;

    public TestDataMutator() {
        this.literalByClass = new HashMap<>();
        this.dataCount = 0;
    }

    public List<CtMethod> apply(CtMethod method) {
        List<CtMethod> methods = new ArrayList<>();
        //get the list of literals in the method
        List<CtLiteral> literals = Query.getElements(method.getBody(), new TypeFilter(CtLiteral.class));
        //this index serves to replace ith literal is replaced by zero in the ith clone of the method
        int lit_index = 0;
        for (CtLiteral lit : literals) {
            try {
                if (!AmplificationChecker.isInAssert(lit) && !AmplificationChecker.isCase(lit) && lit.getValue() != null) {
                    if (lit.getValue() instanceof Number) {
                        methods.addAll(createAllNumberMutant(method, lit, lit_index));
                    }
                    if (lit.getValue() instanceof String) {
                        methods.addAll(createAllStringMutant(method, lit, lit_index));
                    }
                    if (lit.getValue() instanceof Boolean) {
                        methods.add(createBooleanMutant(method, lit));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            lit_index++;
        }
        return AmplificationHelper.updateAmpTestToParent(methods, method);
    }

    public CtMethod applyRandom(CtMethod method) {
        List<CtLiteral> literals = Query.getElements(method, new TypeFilter(CtLiteral.class));
        if (!literals.isEmpty()) {
            int original_lit_index = AmplificationHelper.getRandom().nextInt(literals.size());
            CtLiteral literal = literals.get(original_lit_index);

            if (literal.getValue() instanceof Number) {
                List<? extends Number> mut = new ArrayList<>(numberMutated(literal));
                return createNumberMutant(method, original_lit_index, mut.get(AmplificationHelper.getRandom().nextInt(mut.size())));
            }
            if (literal.getValue() instanceof String) {
                List<String> mut = new ArrayList<>(stringMutated(literal));
                return createStringMutant(method, original_lit_index, mut.get(AmplificationHelper.getRandom().nextInt(mut.size())));
            }
            if (literal.getValue() instanceof Boolean) {
                return createBooleanMutant(method, literal);
            }
        }
        return null;
    }

    protected CtMethod createNumberMutant(CtMethod method, int original_lit_index, Number newValue) {
        dataCount++;
        //clone the method
        CtMethod cloned_method = AmplificationHelper.cloneMethod(method, "_literalMutation");
        //get the lit_indexth literal of the cloned method
        CtLiteral newLiteral = Query.getElements(cloned_method, new TypeFilter<CtLiteral>(CtLiteral.class))
                .get(original_lit_index);

        CtElement toReplace = newLiteral;

        if (newLiteral.getValue() instanceof Integer) {
            newLiteral.setValue(newValue.intValue());
        } else if (newLiteral.getValue() instanceof Long) {
            newLiteral.setValue(newValue.longValue());
        } else if (newLiteral.getValue() instanceof Double) {
            newLiteral.setValue(newValue.doubleValue());
        } else if (newLiteral.getValue() instanceof Short) {
            newLiteral.setValue(newValue.shortValue());
        } else if (newLiteral.getValue() instanceof Float) {
            newLiteral.setValue(newValue.floatValue());
        } else if (newLiteral.getValue() instanceof Byte) {
            newLiteral.setValue(newValue.byteValue());
        }
        if (newLiteral.getParent() instanceof CtUnaryOperator) {
            CtUnaryOperator parent = (CtUnaryOperator) newLiteral.getParent();
            if (parent.getKind().equals(UnaryOperatorKind.NEG)) {
                toReplace = parent;
            }
        }
        toReplace.replace(newLiteral);

        return cloned_method;
    }

    protected List<CtMethod> createAllNumberMutant(CtMethod method, CtLiteral literal, int lit_index) {
        List<CtMethod> mutants = new ArrayList<>();
        for (Number newValue : numberMutated(literal)) {
            mutants.add(createNumberMutant(method, lit_index, newValue));
        }
        return mutants;
    }

    protected List<CtMethod> createAllStringMutant(CtMethod method, CtLiteral literal, int original_lit_index) {
        List<CtMethod> mutants = new ArrayList<>();

        for (String literalMutated : stringMutated(literal)) {
            mutants.add(createStringMutant(method, original_lit_index, literalMutated));
        }
        return mutants;
    }


    private CtMethod createStringMutant(CtMethod method, int original_lit_index, String newValue) {
        dataCount++;
        CtMethod cloned_method = AmplificationHelper.cloneMethod(method, "_literalMutation");
        Query.getElements(cloned_method, new TypeFilter<>(CtLiteral.class))
                .get(original_lit_index).replace(cloned_method.getFactory().Code().createLiteral(newValue));
        return cloned_method;
    }

    protected Set<String> stringMutated(CtLiteral literal) {
        Set<String> values = new HashSet<>();
        String value = ((String) literal.getValue());
        if (value.length() > 2) {
            int length = value.length();
            int index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
            values.add(value.substring(0, index - 1) + getRandomChar() + value.substring(index, length));

            index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
            values.add(value.substring(0, index) + getRandomChar() + value.substring(index, length));

            index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
            values.add(value.substring(0, index) + value.substring(index + 1, length));
        } else {
            values.add("" + getRandomChar());
        }

        Optional<Object> presentString = literals.get(value.getClass()).stream().filter(string ->
                !value.equals(string)
        ).findAny();

        if (presentString.isPresent()) {
            values.add((String)presentString.get());
        }

        return values;
    }

    private char getRandomChar() {
        int value = AmplificationHelper.getRandom().nextInt(94) + 32;
        char c = (char) ((value == 34 || value == 39) ? value + (AmplificationHelper.getRandom().nextBoolean() ? 1 : -1) : value);
        return c;//excluding " and '
    }

    protected Set<? extends Number> numberMutated(CtLiteral literal) {
        Set<Number> values = new HashSet<>();
        Double value = ((Number) literal.getValue()).doubleValue();
        values.add(value + 1);
        values.add(value - 1);

        values.add(value / 2);
        values.add(value * 2);

        if (this.literals.get(value.getClass()) != null) {
            Optional<Object> presentLiteral = this.literals.get(value.getClass()).stream().filter(number ->
                    !value.equals(number)
            ).findAny();

            if (presentLiteral.isPresent()) {
                values.add((Number) presentLiteral.get());
            }
        }
        return values;
    }



    protected CtMethod createBooleanMutant(CtMethod test, CtLiteral booleanLiteral) {
        Boolean value = (Boolean) booleanLiteral.getValue();

        dataCount++;
        //clone the method
        CtMethod cloned_method = AmplificationHelper.cloneMethod(test, "_literalMutation");

        CtLiteral newLiteral = test.getFactory().Core().createLiteral();
        newLiteral.setTypeCasts(booleanLiteral.getTypeCasts());

        if (value) {
            newLiteral.setValue(false);
        } else {
            newLiteral.setValue(true);
        }

        booleanLiteral.replace(newLiteral);
        return cloned_method;
    }



    protected Set<Object> getLiterals(Set<CtType> codeFragmentsProvide) {
        return codeFragmentsProvide.stream()
                .flatMap(cl -> getLiterals(cl).stream())
                .collect(Collectors.toSet());
    }

    protected Set<Object> getLiterals(CtType type) {
        if (!literalByClass.containsKey(type)) {
            Set<Object> set = (Set<Object>) Query.getElements(type, new TypeFilter(CtLiteral.class)).stream()
                    .map(literal -> ((CtLiteral) literal).getValue())
                    .distinct()
                    .collect(Collectors.toSet());
            literalByClass.put(type, set);
        }
        return literalByClass.get(type);
    }

    public void reset(Coverage coverage, CtType testClass) {
        AmplificationHelper.reset();
        Set<CtType> codeFragmentsProvide = AmplificationHelper.computeClassProvider(testClass);
        literals = getLiterals(codeFragmentsProvide).stream()
                .filter(lit -> lit != null)
                .collect(Collectors.groupingBy(lit -> lit.getClass()));
    }
}
