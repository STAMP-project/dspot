package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.Counter;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/07/18
 */
public abstract class AbstractAmplifier<T extends CtElement> implements Amplifier {

    /**
     * String used to mark an element as amplified
     */
    protected final String METADATA_KEY = "amplified";

    /**
     * Checks if the given element has already been amplified
     *
     * @param element
     * @return
     */
    protected boolean hasBeenAmplified(CtElement element) {
        return (element.getMetadata(METADATA_KEY) != null &&
                (boolean) element.getMetadata(METADATA_KEY));
    }

    /**
     * This methods aims at reducing the given list of elements in order to avoid redundant amplification
     * For most of amplification, amplify elements that are before the latest amplified elements produce
     * redundant amplifications.
     * For more information, and a provided example,
     * see <url>https://github.com/STAMP-project/dspot/issues/454</url>
     *
     * @param elementsToBeReduced list of elements to be reduced
     * @return a reduced list of elements, according to previous amplification.
     * If this is the first amplification, no element would be marked as amplified
     */
    protected List<T> reduceAlreadyAmplifiedElements(List<T> elementsToBeReduced) {
        List<T> reducedElements = new ArrayList<>(elementsToBeReduced);
        // we now reduce the literals list, see https://github.com/STAMP-project/dspot/issues/454
        final Integer maxIndex = reducedElements.stream()
                .filter(this::hasBeenAmplified)
                .map(reducedElements::indexOf)
                .max(Integer::compareTo)
                .orElse(-1);
        if (maxIndex > -1 && maxIndex <= reducedElements.size()) {
            reducedElements = reducedElements.subList(maxIndex + 1, reducedElements.size());
        }
        return reducedElements;
    }

    /**
     * This method replace the given original element by the amplified one, by producing a clone of the given test method.
     * The amplified element would be marked as amplified, with the METADATA_KEY,
     * <i>i.e.</i> calling {@link #hasBeenAmplified(CtElement)} returns true.
     *
     * @param originalElement  element to be replaced
     * @param amplifiedElement new element to be used
     * @param testMethod       test method to be cloned
     * @return a clone of the given test method with an amplified element that replaces the original element
     */
    protected CtMethod<?> replace(T originalElement, T amplifiedElement, CtMethod<?> testMethod) {
        originalElement.replace(amplifiedElement);
        amplifiedElement.putMetadata(this.METADATA_KEY, true);
        CtMethod<?> clone = CloneHelper.cloneTestMethodForAmp(testMethod, getSuffix());
        amplifiedElement.replace(originalElement);
        Counter.updateInputOf(clone, 1);
        return clone;
    }

    /**
     * @return This method aims at giving a specific name per Amplifier to the amplified test method
     */
    protected abstract String getSuffix();

    protected abstract List<T> getOriginals(CtMethod<?> testMethod);

    protected abstract Set<T> amplify(T original, CtMethod<?> testMethod);

    @Override
    public Stream<CtMethod<?>> amplify(CtMethod<?> testMethod, int iteration) {
        List<T> originals = this.getOriginals(testMethod);
        List<T> reducedOriginals = this.reduceAlreadyAmplifiedElements(originals);
        return reducedOriginals.stream()
                .filter(reducedOriginal ->
                        reducedOriginal.getMetadata(METADATA_KEY) == null ||
                                !(boolean) reducedOriginal.getMetadata(METADATA_KEY)
                ).flatMap(original ->
                        this.amplify(original, testMethod)
                                .stream()
                                .map(amplified -> this.replace(original, amplified, testMethod))
                );
    }
}
