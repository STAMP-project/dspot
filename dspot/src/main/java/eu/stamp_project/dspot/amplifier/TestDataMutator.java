

package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.RandomHelper;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* This processor is meant to replace all literal values in test cases by other literal values
 * */
public class TestDataMutator implements Amplifier {


	private Map<CtType, Set<Object>> literalByClass;

	private Map<Class<?>, List<Object>> literals;

	public TestDataMutator() {
		this.literalByClass = new HashMap<>();
	}

	private class LiteralToBeMutedFilter extends TypeFilter<CtLiteral> {
		public LiteralToBeMutedFilter() {
			super(CtLiteral.class);
		}

		@Override
		public boolean matches(CtLiteral literal) {
			return literal.getParent(CtAnnotation.class) == null && super.matches(literal);
		}
	}

	public Stream<CtMethod<?>> amplify(CtMethod<?> method, int iteration) {
		List<CtMethod<?>> methods = new ArrayList<>();
		//get the list of literals in the method
		List<CtLiteral> literals = Query.getElements(method.getBody(), new LiteralToBeMutedFilter());
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
					if (lit.getValue() instanceof Character) {
						methods.addAll(createAllCharacterMutant(method, lit, lit_index));
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			lit_index++;
		}
		return methods.stream();
	}

	public void reset(CtType testClass) {
		AmplificationHelper.reset();
		Set<CtType> codeFragmentsProvide = AmplificationHelper.computeClassProvider(testClass);
		literals = getLiterals(codeFragmentsProvide).stream()
				.filter(lit -> lit != null)
				.collect(Collectors.groupingBy(lit -> lit.getClass()));
	}

	private CtMethod<?> createNumberMutant(CtMethod<?> method, int original_lit_index, Number newValue) {
		//clone the method
		CtMethod cloned_method = CloneHelper.cloneTestMethodForAmp(method, "_literalMutationNumber");
		//get the lit_indexth literal of the cloned method
		CtLiteral newLiteral = Query.getElements(cloned_method.getBody(), new LiteralToBeMutedFilter())
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
		DSpotUtils.addComment(toReplace, "TestDataMutator on numbers", CtComment.CommentType.INLINE);
		return cloned_method;
	}

	private List<CtMethod<?>> createAllNumberMutant(CtMethod<?> method, CtLiteral literal, int lit_index) {
		return numberMutated(literal).stream()
				.map(newValue -> createNumberMutant(method, lit_index, newValue))
				.collect(Collectors.toList());
	}


	private List<CtMethod<?>> createAllStringMutant(CtMethod<?> method, CtLiteral literal, int original_lit_index) {
		return stringMutated(literal).stream()
				.map(literalMutated -> createStringMutant(method, original_lit_index, literalMutated))
				.collect(Collectors.toList());
	}

	private CtMethod<?> createStringMutant(CtMethod<?> method, int original_lit_index, String newValue) {
		CtMethod<?> cloned_method = CloneHelper.cloneTestMethodForAmp(method, "_literalMutationString");
		Counter.updateInputOf(cloned_method, 1);
		CtLiteral toReplace = Query.getElements(cloned_method.getBody(), new LiteralToBeMutedFilter())
				.get(original_lit_index);
		toReplace.replace(cloned_method.getFactory().Code().createLiteral(newValue));
		DSpotUtils.addComment(toReplace, "TestDataMutator on strings", CtComment.CommentType.INLINE);
		return cloned_method;
	}

	private List<CtMethod<?>> createAllCharacterMutant(CtMethod method, CtLiteral lit, int original_lit_index) {
		return characterMutated(lit).stream()
				.map(character -> createCharacterMutant(method, original_lit_index, character))
				.collect(Collectors.toList());
	}

	private CtMethod<?> createCharacterMutant(CtMethod method, int original_lit_index, Character newValue) {
		CtMethod cloned_method = CloneHelper.cloneTestMethodForAmp(method, "_literalMutationChar");
		Counter.updateInputOf(cloned_method, 1);
		CtLiteral toReplace = Query.getElements(cloned_method.getBody(), new LiteralToBeMutedFilter())
				.get(original_lit_index);
		toReplace.replace(cloned_method.getFactory().Code().createLiteral(newValue));
		DSpotUtils.addComment(toReplace, "TestDataMutator on strings", CtComment.CommentType.INLINE);
		return cloned_method;
	}

	private List<Character> characterMutated(CtLiteral lit) {
		final Character value = (Character) lit.getValue();
		return Arrays.asList('\0', ' ', RandomHelper.getRandomChar(),
				(char) (value + 1),
				(char) (value - 1),
				System.getProperty("line.separator").charAt(0)
		); // TODO checks some bound values
	}

	private Set<String> stringMutated(CtLiteral literal) {
		Set<String> values = new HashSet<>();
		String value = ((String) literal.getValue());
		if (value.length() > 2) {
			int length = value.length();
			int index = RandomHelper.getRandom().nextInt(length - 2) + 1;
			values.add(value.substring(0, index - 1) + RandomHelper.getRandomChar() + value.substring(index, length));

			index = RandomHelper.getRandom().nextInt(length - 2) + 1;
			values.add(value.substring(0, index) + RandomHelper.getRandomChar() + value.substring(index, length));

			index = RandomHelper.getRandom().nextInt(length - 2) + 1;
			values.add(value.substring(0, index) + value.substring(index + 1, length));

			values.add(RandomHelper.getRandomString(value.length()));
		} else {
			values.add("" + RandomHelper.getRandomChar());
		}
		values.add("");
		literals.get(value.getClass())
				.stream()
				.filter(string -> !value.equals(string))
				.filter(string -> !values.contains(string))
				.findAny().ifPresent(o -> values.add((String) o));

		return values;
	}

	// TODO We should generate bound values such as Integer.MAX_VALUE and Integer.MIN_VALUE
	private Set<? extends Number> numberMutated(CtLiteral literal) {
		Set<Number> values = new HashSet<>();
		Double value = ((Number) literal.getValue()).doubleValue();
		values.add(value + 1);
		values.add(value - 1);

		values.add(value / 2);
		values.add(value * 2);

		values.add(0);

		if (this.literals.get(value.getClass()) != null) {
			this.literals.get(value.getClass()).stream().filter(number ->
					!value.equals(number)
			).findAny().ifPresent(o -> values.add((Number) o));
		}
		return values;
	}


	private CtMethod<?> createBooleanMutant(CtMethod test, CtLiteral booleanLiteral) {
		Boolean value = (Boolean) booleanLiteral.getValue();
		CtMethod cloned_method = CloneHelper.cloneTestMethodForAmp(test, "_literalMutationBoolean");
		CtLiteral newValue = cloned_method.getElements(new TypeFilter<CtLiteral>(CtLiteral.class) {
			@Override
			public boolean matches(CtLiteral element) {
				return element.equals(booleanLiteral);
			}
		}).get(0);
		newValue.setValue(!value);
		newValue.setTypeCasts(booleanLiteral.getTypeCasts());
		Counter.updateInputOf(cloned_method, 1);
		DSpotUtils.addComment(newValue, "TestDataMutator on boolean", CtComment.CommentType.INLINE);
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
