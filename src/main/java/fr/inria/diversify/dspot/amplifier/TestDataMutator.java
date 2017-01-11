

package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.dspot.AmplificationChecker;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.support.Counter;
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


    private Map<CtType, Set<Object>> literalByClass;

    private Map<Class<?>, List<Object>> literals;

    public TestDataMutator() {
        this.literalByClass = new HashMap<>();
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

    public void reset(CtType testClass) {
        AmplificationHelper.reset();
        Set<CtType> codeFragmentsProvide = AmplificationHelper.computeClassProvider(testClass);
        literals = getLiterals(codeFragmentsProvide).stream()
                .filter(lit -> lit != null)
                .collect(Collectors.groupingBy(lit -> lit.getClass()));
    }

    private CtMethod createNumberMutant(CtMethod method, int original_lit_index, Number newValue) {
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

        Counter.updateInputOf(cloned_method, 1);

        return cloned_method;
    }

    private List<CtMethod> createAllNumberMutant(CtMethod method, CtLiteral literal, int lit_index) {
        return numberMutated(literal).stream()
                .map(newValue -> createNumberMutant(method, lit_index, newValue))
                .collect(Collectors.toList());
    }


    private List<CtMethod> createAllStringMutant(CtMethod method, CtLiteral literal, int original_lit_index) {
        return stringMutated(literal).stream()
                .map(literalMutated -> createStringMutant(method, original_lit_index, literalMutated))
                .collect(Collectors.toList());
    }

    private CtMethod createStringMutant(CtMethod method, int original_lit_index, String newValue) {
        CtMethod cloned_method = AmplificationHelper.cloneMethod(method, "_literalMutation");
        Query.getElements(cloned_method, new TypeFilter<>(CtLiteral.class))
                .get(original_lit_index).replace(cloned_method.getFactory().Code().createLiteral(newValue));
        Counter.updateInputOf(cloned_method, 1);
        return cloned_method;
    }

    private Set<String> stringMutated(CtLiteral literal) {
        Set<String> values = new HashSet<>();
        String value = ((String) literal.getValue());
        if (value.length() > 2) {
            int length = value.length();
            int index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
            values.add(value.substring(0, index - 1) + AmplificationHelper.getRandomChar() + value.substring(index, length));

            index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
            values.add(value.substring(0, index) + AmplificationHelper.getRandomChar() + value.substring(index, length));

            index = AmplificationHelper.getRandom().nextInt(length - 2) + 1;
            values.add(value.substring(0, index) + value.substring(index + 1, length));

            values.add(AmplificationHelper.getRandomString(value.length()));
        } else {
            values.add("" + AmplificationHelper.getRandomChar());
        }

        Optional<Object> presentString = literals.get(value.getClass()).stream().filter(string ->
                !value.equals(string)
        ).findAny();

        if (presentString.isPresent()) {
            values.add((String) presentString.get());
        }

        return values;
    }

    private Set<? extends Number> numberMutated(CtLiteral literal) {
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


    private CtMethod createBooleanMutant(CtMethod test, CtLiteral booleanLiteral) {
        Boolean value = (Boolean) booleanLiteral.getValue();
        CtMethod cloned_method = AmplificationHelper.cloneMethod(test, "_literalMutation");
        CtLiteral newValue = cloned_method.getElements(new TypeFilter<CtLiteral>(CtLiteral.class) {
            @Override
            public boolean matches(CtLiteral element) {
                return element.equals(booleanLiteral);
            }
        }).get(0);
        newValue.setValue(!value);
        newValue.setTypeCasts(booleanLiteral.getTypeCasts());
        Counter.updateInputOf(cloned_method, 1);
        return cloned_method;
    }

    private Set<Object> getLiterals(Set<CtType> codeFragmentsProvide) {
        return codeFragmentsProvide.stream()
                .flatMap(cl -> getLiterals(cl).stream())
                .collect(Collectors.toSet());
    }

    private Set<Object> getLiterals(CtType type) {
        if (!literalByClass.containsKey(type)) {
            Set<Object> set = (Set<Object>) Query.getElements(type, new TypeFilter(CtLiteral.class)).stream()
                    .map(literal -> ((CtLiteral) literal).getValue())
                    .distinct()
                    .collect(Collectors.toSet());
            literalByClass.put(type, set);
        }
        return literalByClass.get(type);
    }
}
