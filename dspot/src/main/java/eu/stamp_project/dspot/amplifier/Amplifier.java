package eu.stamp_project.dspot.amplifier;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 *
 * Amplifier define a way to amplify the given test method.
 *
 */
public interface Amplifier {

    /**
     * Input amplify the given test method. The resulting stream contains all the amplified test methods
     * @param testMethod to be amplified
     * @param iteration of the main loop of DSpot
     * @return all the input amplified test methods
     */
    Stream<CtMethod<?>> amplify(CtMethod<?> testMethod, int iteration);

    void reset(CtType<?> testClass);

}
