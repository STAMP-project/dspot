package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.dspot.amplifier.value.ConstructorCreator;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 03/10/17
 */

@Deprecated // TODO WARNING this amplifier is morelikely to turn dspot into a generator of test instead of Amplifier.
public class ObjectGenerator implements Amplifier {

	private int counterGenerateNewObject = 0;

	@Override
	public List<CtMethod> apply(CtMethod method) {
		List<CtLocalVariable<?>> existingObjects = getExistingObjects(method);
		final Stream<? extends CtMethod<?>> gen_o1 = existingObjects.stream() // must use tmp variable because javac is confused
				.flatMap(localVariable -> ConstructorCreator.generateAllConstructionOf(localVariable.getType()).stream())
				.map(ctExpression -> {
							final CtMethod<?> clone = AmplificationHelper.cloneTestMethodForAmp(method, "_sd");
							clone.getBody().insertBegin(
									clone.getFactory().createLocalVariable(
											ctExpression.getType(), "__DSPOT_gen_o" + counterGenerateNewObject++, ctExpression
									)
							);
							return clone;
						}
				);
		return gen_o1.collect(Collectors.toList());
	}

	private List<CtLocalVariable<?>> getExistingObjects(CtMethod method) {
		return method.getElements(new TypeFilter<CtLocalVariable<?>>(CtLocalVariable.class) {
			@Override
			public boolean matches(CtLocalVariable<?> element) {
				return element.getType() != null &&
						!element.getType().isPrimitive() &&
						element.getType().getDeclaration() != null;
			}
		});
	}

	@Override
	public void reset(CtType testClass) {

	}
}
