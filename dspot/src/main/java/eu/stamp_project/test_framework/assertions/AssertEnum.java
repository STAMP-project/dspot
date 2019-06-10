package eu.stamp_project.test_framework.assertions;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/11/18
 */
public enum AssertEnum {

    ASSERT_NULL,
    ASSERT_NOT_NULL,
    ASSERT_TRUE,
    ASSERT_FALSE,
    ASSERT_EQUALS,
    ASSERT_NOT_EQUALS,
    ASSERT_ARRAY_EQUALS;

    public String toStringAccordingToClass(Class<?> clazz) {
        try {
            return (String ) clazz.getDeclaredField(this.name()).get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
