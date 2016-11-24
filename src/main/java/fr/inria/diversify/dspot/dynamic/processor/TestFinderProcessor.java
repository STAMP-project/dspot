package fr.inria.diversify.dspot.dynamic.processor;

import fr.inria.diversify.dspot.dynamic.logger.KeyWord;
import fr.inria.diversify.dspot.value.ValueFactory;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.utils.CtTypeUtils;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 21/03/16
 * Time: 10:34
 */
public class TestFinderProcessor extends AbstractLoggingInstrumenter<CtMethod> {
    protected Map<CtType, Boolean> hasConstructor;
    protected ValueFactory valueFactory;

    public TestFinderProcessor(InputProgram inputProgram, ValueFactory valueFactory) {
        super(inputProgram);
        this.valueFactory = valueFactory;
        this.hasConstructor = new HashMap<>();
    }

    public boolean isToBeProcessed(CtMethod mth) {
        CtType declaringClass = mth.getDeclaringType();
        if(!declaringClass.isTopLevel()) {
            return false;
        }
        List<CtParameter> params = mth.getParameters();
        boolean condition = params.stream()
                .map(param -> param.getType())
                .allMatch(param -> CtTypeUtils.isPrimitive(param)
                        || CtTypeUtils.isString(param)
                        || CtTypeUtils.isPrimitiveArray(param)
                        || CtTypeUtils.isPrimitiveCollection(param)
                        || CtTypeUtils.isPrimitiveMap(param)
                        || isSerializable(param)
                );

        if(condition) {
//            return checkVisibility(mth)
                return  !Query.getElements(mth, new TypeFilter(CtInvocation.class)).isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public void process(CtMethod method) {
        String id = method.getDeclaringType().getQualifiedName() + "." + method.getSimpleName();
        CtTry ctTry = tryFinallyBody(method);
        Factory factory = getFactory();

        String snippet = getLogger()
                + ".startLog(Thread.currentThread(), \"" + id;
        if(method.getParameters().isEmpty()) {
            snippet += "\")";
        } else {
            snippet += "\", " + method.getParameters().stream()
                    .map(param -> ((CtParameter)param).getSimpleName() + ", " + paramType(((CtParameter) param).getType()))
                    .collect(Collectors.joining(", "))
                    + ")";
        }
        CtCodeSnippetStatement beginStmt = factory.Code().createCodeSnippetStatement(snippet);
        ctTry.getBody().insertBegin(beginStmt);

        snippet = getLogger() + ".stopLog(Thread.currentThread(),\"" + id + "\"";
        if(method.getModifiers().contains(ModifierKind.STATIC)) {
            snippet += ", null)";
        } else {
            snippet +=", this)";
        }

        CtCodeSnippetStatement stmt = factory.Code().createCodeSnippetStatement(snippet);
        ctTry.getFinalizer().addStatement(stmt);
    }

    protected String paramType(CtTypeReference param) {
        if(CtTypeUtils.isPrimitive(param)
                || CtTypeUtils.isString(param)
                || CtTypeUtils.isPrimitiveArray(param)) {
            return "\'" + KeyWord.primitiveType +"\'";
        } else if(CtTypeUtils.isPrimitiveCollection(param)) {
            return "\'" + KeyWord.collectionType +"\'";
        } else if(CtTypeUtils.isPrimitiveMap(param)) {
            return "\'" + KeyWord.mapType +"\'";
        } else {
            return "\'" + KeyWord.objectType +"\'";
        }
    }

    protected boolean isGetFor(CtMethod mth, String field) {
        int size = mth.getBody().getStatements().size();
        return mth.getSimpleName().startsWith("get")
            && mth.getBody().getStatement(size - 1).toString().equals("return "+field);
    }

    protected boolean isSerializable(CtTypeReference type) {
        if(!hasConstructor.containsKey(type.getDeclaration())) {
            if(type.getDeclaration() instanceof CtClass) {
                CtClass cl = (CtClass) type.getDeclaration();
                hasConstructor.put(type.getDeclaration(),
                        cl.isTopLevel() && valueFactory.hasConstructorCall(cl, true));
            } else {
                hasConstructor.put(type.getDeclaration(), false);
            }
        }
        return hasConstructor.get(type.getDeclaration());
    }

    public static boolean isPrimitive(Class type) {
        return type.isPrimitive()
                || String.class.equals(type)
                || getWrapperTypes().contains(type);
    }

    protected static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        return ret;
    }

    protected boolean checkVisibility(CtTypeMember method) {
        try {
            if (method.getModifiers().contains(ModifierKind.PUBLIC)) {
                return true;
            } else {
                List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));
                return invocations.stream()
                        .allMatch(invocation -> {
                            try {
                                return Modifier.isPublic(invocation.getExecutable().getActualMethod().getModifiers());
                            } catch (Throwable e) {
                                CtExecutable exe = invocation.getExecutable().getDeclaration();
                                if (exe != null) {
                                    if (exe instanceof CtMethod) {
                                        return ((CtMethod) exe).getModifiers().contains(ModifierKind.PUBLIC);
                                    } else {
                                        return ((CtConstructor) exe).getModifiers() != null && ((CtConstructor) exe).getModifiers().contains(ModifierKind.PUBLIC);
                                    }
                                } else {
                                    return false;
                                }
                            }
                        });
            }
        } catch (Exception e) {
            return false;
        }
    }
}
