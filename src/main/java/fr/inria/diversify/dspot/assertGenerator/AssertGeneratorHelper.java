package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.compare.ObjectLog;
import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import org.kevoree.log.Log;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.inria.diversify.utils.AmplificationChecker.isAssert;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/3/17
 */
public class AssertGeneratorHelper {

	static boolean isVoidReturn(CtInvocation invocation) {
		return (invocation.getType() != null && (invocation.getType().equals(invocation.getFactory().Type().voidType()) ||
				invocation.getType().equals(invocation.getFactory().Type().voidPrimitiveType())));
	}

	static CtMethod<?> createTestWithoutAssert(CtMethod<?> test, List<Integer> assertIndexToKeep) {
		CtMethod newTest = AmplificationHelper.cloneMethodTest(test, "");
		newTest.setSimpleName(test.getSimpleName() + "_withoutAssert");
		int stmtIndex = 0;
		List<CtStatement> statements = Query.getElements(newTest, new TypeFilter(CtStatement.class));
		for (CtStatement statement : statements) {
			try {
				if (!assertIndexToKeep.contains(stmtIndex) && isAssert(statement)) {
					CtBlock block = buildRemoveAssertBlock(test.getFactory(), (CtInvocation) statement, stmtIndex);
					if (statement.getParent() instanceof CtCase) {
						CtCase ctCase = (CtCase) statement.getParent();
						int index = ctCase.getStatements().indexOf(statement);
						ctCase.getStatements().add(index, block);
						ctCase.getStatements().remove(statement);
					} else {
						if (block.getStatements().size() == 0) {
							statement.delete();
						} else if (block.getStatements().size() == 1) {
							statement.replace(block.getStatement(0));
						} else {
							replaceStatementByListOfStatements(statement, block.getStatements());
						}
					}
				}
				stmtIndex++;
			} catch (Exception ignored) {
				//ignored, skipping to the next statement
			}
		}
		return newTest;
	}

	private static CtLocalVariable<?> buildVarStatement(Factory factory, CtExpression arg, String id) {
		CtTypeReference<?> objectType;
		if (arg.getType() == null) {
			objectType = factory.Type().createReference(Object.class);
		} else {
			objectType = arg.getType();
		}
		CtLocalVariable<?> localVar = factory.Code().createLocalVariable(objectType,
				"o_" + id + "_" + ValueCreator.count++, arg);
		DSpotUtils.addComment(localVar, "MethodAssertGenerator build local variable", CtComment.CommentType.INLINE);
		return localVar;
	}

	private static List<CtExpression> getNotLiteralArgs(CtInvocation invocation) {
		List<CtExpression> args = invocation.getArguments();
		return args.stream()
				.filter(arg -> !(arg instanceof CtLiteral))
				.collect(Collectors.toList());
	}

	static void replaceStatementByListOfStatements(CtStatement statement, List<CtStatement> statements) {
		String oldStatement = statement.toString();
		statement.replace(statements.get(0));
		for (int i = 1; i < statements.size(); i++) {
			statement.insertAfter(statements.get(i));
		}
		DSpotUtils.addComment(statement, "MethodAssertion Generator replaced " + oldStatement, CtComment.CommentType.BLOCK);
	}

	static CtBlock buildRemoveAssertBlock(Factory factory, CtInvocation assertInvocation, int blockId) {
		CtBlock block = factory.Core().createBlock();
		int[] idx = {0};
		getNotLiteralArgs(assertInvocation).stream()
				.filter(arg -> !(arg instanceof CtVariableAccess))
				.map(arg -> buildVarStatement(factory, arg, blockId + "_" + (idx[0]++)))
				.forEach(stmt -> block.addStatement(stmt));

		block.setParent(assertInvocation.getParent());
		return block;
	}

	static Map<CtMethod<?>, List<Integer>> takeAllStatementToAssert(CtType testClass, List<CtMethod<?>> tests) {
		return tests.stream()
				.collect(Collectors.toMap(Function.identity(),
						ctMethod -> {
							List<Integer> indices = new ArrayList<>();
							for (int i = 0; i < Query.getElements(testClass, new TypeFilter(CtStatement.class)).size(); i++) {
								indices.add(i);
							}
							return indices;
						}
				));
	}

	static List<Integer> findStatementToAssert(CtMethod<?> test) {
		if (AmplificationHelper.getAmpTestToParent() != null
				&& !AmplificationHelper.getAmpTestToParent().isEmpty()
				&& AmplificationHelper.getAmpTestToParent().get(test) != null) {
			CtMethod parent = AmplificationHelper.getAmpTestToParent().get(test);
			while (AmplificationHelper.getAmpTestToParent().get(parent) != null) {
				parent = AmplificationHelper.getAmpTestToParent().get(parent);
			}
			return findStatementToAssertFromParent(test, parent);
		} else {
			return findStatementToAssertOnlyInvocation(test);
		}
	}

	static List<Integer> findStatementToAssertOnlyInvocation(CtMethod<?> test) {
		List<CtStatement> stmts = Query.getElements(test, new TypeFilter(CtStatement.class));
		List<Integer> indexs = new ArrayList<>();
		for (int i = 0; i < stmts.size(); i++) {
			if (CtInvocation.class.isInstance(stmts.get(i))) {
				indexs.add(i);
			}
		}
		return indexs;
	}

	static List<Integer> findStatementToAssertFromParent(CtMethod<?> test, CtMethod<?> parentTest) {
		List<CtStatement> originalStmts = Query.getElements(parentTest, new TypeFilter(CtStatement.class));
		List<String> originalStmtStrings = originalStmts.stream()
				.map(Object::toString)
				.collect(Collectors.toList());

		List<CtStatement> ampStmts = Query.getElements(test, new TypeFilter(CtStatement.class));
		List<String> ampStmtStrings = ampStmts.stream()
				.map(Object::toString)
				.collect(Collectors.toList());

		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < ampStmtStrings.size(); i++) {
			int index = originalStmtStrings.indexOf(ampStmtStrings.get(i));
			if (index == -1) {
				indices.add(i);
			} else {
				originalStmtStrings.remove(index);
			}
		}
		return indices;
	}

	static CtMethod<?> createTestWithLog(CtMethod test, List<Integer> statementsIndexToAssert, final String simpleNameTestClass) {
		CtMethod clone = AmplificationHelper.cloneMethodTest(test, "");
		clone.setSimpleName(test.getSimpleName() + "_withlog");
		final List<CtStatement> allStatement = clone.getElements(new TypeFilter<>(CtStatement.class));
		allStatement.stream()
				.filter(statement -> isStmtToLog(simpleNameTestClass, statement))
				.forEach(statement ->
						addLogStmt(statement,
								test.getSimpleName() + "__" + indexOfByRef(allStatement, statement),
								statementsIndexToAssert != null &&
										statementsIndexToAssert.contains(allStatement.indexOf(statement)))
				);
		return clone;
	}

	private static int indexOfByRef(List<CtStatement> statements, CtStatement statement) {
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i) == statement) {
				return i;
			}
		}
		return -1;
	}

	static boolean isStmtToLog(String nameOfOriginalClass, CtStatement statement) {
		if (!(statement.getParent() instanceof CtBlock)) {
			return false;
		}
		if (statement instanceof CtInvocation) {
			CtInvocation invocation = (CtInvocation) statement;

			//type tested by the test class
			String targetType = "";
			if (invocation.getTarget() != null &&
					invocation.getTarget().getType() != null) {
				targetType = invocation.getTarget().getType().getSimpleName();
			}
			return (nameOfOriginalClass.startsWith(targetType)
					|| !isVoidReturn(invocation));
		}
		return statement instanceof CtVariableWrite
				|| statement instanceof CtAssignment
				|| statement instanceof CtLocalVariable;
	}


	@SuppressWarnings("unchecked")
	private static void addLogStmt(CtStatement stmt, String id, boolean forAssert) {
		if (stmt instanceof CtLocalVariable && ((CtLocalVariable) stmt).getDefaultExpression() == null) {
			return;
		}

		final CtTypeAccess<ObjectLog> typeAccess = stmt.getFactory().createTypeAccess(
				stmt.getFactory().Type().createReference(ObjectLog.class)
		);

		final CtExecutableReference objectLogExecRef = stmt.getFactory().createExecutableReference()
				.setStatic(true)
				.setDeclaringType(stmt.getFactory().Type().createReference(ObjectLog.class))
				.setSimpleName(forAssert ? "log" : "logObject");
		objectLogExecRef.setType(stmt.getFactory().Type().voidPrimitiveType());

		final CtInvocation invocationToObjectLog = stmt.getFactory().createInvocation(typeAccess, objectLogExecRef);

		CtStatement insertAfter;
		if (stmt instanceof CtVariableWrite) {//TODO
			CtVariableWrite varWrite = (CtVariableWrite) stmt;
			insertAfter = stmt;
		} else if (stmt instanceof CtLocalVariable) {
			CtLocalVariable localVar = (CtLocalVariable) stmt;
			final CtVariableAccess variableRead = stmt.getFactory().createVariableRead(localVar.getReference(), false);// TODO checks static
			invocationToObjectLog.addArgument(variableRead);
			invocationToObjectLog.addArgument(stmt.getFactory().createLiteral(localVar.getSimpleName()));
			insertAfter = stmt;
		} else if (stmt instanceof CtAssignment) {
			CtAssignment localVar = (CtAssignment) stmt;
			invocationToObjectLog.addArgument(localVar.getAssigned());
			invocationToObjectLog.addArgument(stmt.getFactory().createLiteral(localVar.getAssigned().toString()));
			insertAfter = stmt;
		} else if (stmt instanceof CtInvocation) {
			CtInvocation invocation = (CtInvocation) stmt;
			if (isVoidReturn(invocation)) {
				invocationToObjectLog.addArgument(invocation.getTarget());
				invocationToObjectLog.addArgument(stmt.getFactory().createLiteral(
						invocation.getTarget().toString().replace("\"", "\\\""))
				);
				insertAfter = invocation;
			} else {
				final CtLocalVariable localVariable = stmt.getFactory().createLocalVariable(invocation.getType(),
						"o_" + id, invocation.clone());
				try {
					stmt.replace(localVariable);
				} catch (ClassCastException e) {
					throw new RuntimeException(e);
				}
				invocationToObjectLog.addArgument(stmt.getFactory().createVariableRead(localVariable.getReference(), false));
				invocationToObjectLog.addArgument(stmt.getFactory().createLiteral("o_" + id));
				insertAfter = localVariable;
			}
		} else {
			throw new RuntimeException("Could not find the proper type to add log statement" + stmt.toString());
		}
		invocationToObjectLog.addArgument(stmt.getFactory().createLiteral(id));
		insertAfter.insertAfter(invocationToObjectLog);
	}

}
