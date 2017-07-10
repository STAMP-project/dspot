package fr.inria.stamp.test.launcher;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/17
 */
public enum TypeTestEnum {
	DEFAULT,
	MOCKITO;
	public static TypeTestEnum getTypeTest(CtType<?> testClass) {
		if (!testClass.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
			@Override
			public boolean matches(CtInvocation invocation) {
				return invocation.getExecutable() != null &&
						invocation.getExecutable().getDeclaringType() != null &&
						invocation.getExecutable().getDeclaringType().getPackage() != null &&
						"org.mockito".equals(
								invocation.getExecutable().getDeclaringType().getPackage().getSimpleName()
						);
			}
		}).isEmpty()) {
			return TypeTestEnum.MOCKITO;
		} else {
			return TypeTestEnum.DEFAULT;
		}
	}
}
