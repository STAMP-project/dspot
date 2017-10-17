package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/09/17
 */
public abstract class AbstractLiteralAmplifier<T> implements Amplifier {

	private final TypeFilter<CtLiteral> literalTypeFilter = new TypeFilter<CtLiteral>(CtLiteral.class) {
		@Override
		public boolean matches(CtLiteral literal) {
			return (literal.getParent() instanceof CtInvocation &&
					!AmplificationChecker.isAssert((CtInvocation) literal.getParent())) ||
					literal.getParent(CtAnnotation.class) == null
							&& ((T) new Object()).getClass().isAssignableFrom(literal.getValue().getClass())
							&& super.matches(literal);
		}
	};

	@Override
	public List<CtMethod> apply(CtMethod testMethod) {
		List<CtLiteral> literals = testMethod.getElements(literalTypeFilter);
		return literals.stream()
				.flatMap(literal ->
						this.amplify(literal).stream().map(newValue -> {
							CtMethod clone = AmplificationHelper.cloneMethodTest(testMethod, getSuffix());
							clone.getElements(literalTypeFilter).get(literals.indexOf(literal)).replace(newValue);
							return clone;
						})
				).collect(Collectors.toList());
	}

	@Override
	public CtMethod applyRandom(CtMethod testMethod) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset(CtType testClass) {
		AmplificationHelper.reset();
	}

	protected abstract Set<CtLiteral<T>> amplify(CtLiteral<?> existingLiteral);

	protected abstract String getSuffix();

}
