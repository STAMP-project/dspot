package org.mockito.configuration;

/*
    This overload the default configuration of Mockito.
        It disable the class cache which cause class loading problems when using a custom classloader.
 */
public class MockitoConfiguration extends DefaultMockitoConfiguration {
    @Override
    public boolean enableClassCache() {
        return false;
    }
}