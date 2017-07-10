package fr.inria.diversify.testRunner;

import spoon.reflect.declaration.CtType;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fr.inria.diversify.dspot.AmplificationHelper.PATH_SEPARATOR;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/04/17
 */
public abstract class JunitRunner {

    protected ClassLoader classLoader;

    public JunitRunner(String classpath) {
        final List<URL> tmp = Arrays.stream(classpath.split(PATH_SEPARATOR))
                .map(File::new)
                .map(File::toURI)
                .map(uri -> {
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        classLoader = new URLClassLoader(tmp.toArray(new URL[tmp.size()]));
    }

    public abstract JunitResult run(List<CtType<?>> tests, List<String> methodsToRun);

    protected Class<?>[] loadClass(List<CtType<?>> tests) throws ClassNotFoundException {
        Class<?>[] testClasses = new Class<?>[tests.size()];
        for (int i = 0; i < tests.size(); i++) {
            try {
                testClasses[i] = classLoader.loadClass(tests.get(i).getQualifiedName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return testClasses;
    }

}
