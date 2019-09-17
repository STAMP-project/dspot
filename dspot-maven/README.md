# DSpot-Maven

You can use this plugin on the command line as the jar:

```bash
# this amplifies the Junit tests to kill more mutants
mvn eu.stamp-project:dspot-maven:amplify-unit-tests

# this amplifies the Junit tests to improve coverage
mvn eu.stamp-project:dspot-maven:amplify-unit-tests -Dtest-criterion=JacocoCoverageSelector

```  

All the option can be pass through command line by prefixing the option with `-D`.
For example: 

```bash
mvn eu.stamp-project:dspot-maven:amplify-unit-tests -Dpath-to-properties=dspot.properties -Dtest=my.package.TestClass -Dcases=testMethod
```

or, you can add the following to your `pom.xml`, in the plugins section of the build:

```xml
<plugin>
    <groupId>eu.stamp-project</groupId>
    <artifactId>dspot-maven</artifactId>
    <version>LATEST</version>
    <configuration>
        <!-- your configuration --> 
    </configuration>
</plugin>
```
Replace `LATEST` with the latest DSpot version number available at Maven central: `2.0.0`

In case your project is a multi-module, we advise you to configure DSpot in the highest `pom.xml` and use the dedicated property `targetModule` to name the module you want to amplify

After setting up your `pom.xml` and add your configuration with different options,run:

```bash
mvn dspot:amplify-unit-tests
``` 

# Advantages

The advantages of using the maven plugin is that maven will automatically the latest version deployed on maven central, even the SNAPSHOT built at each commits on the master!

Second, is that the properties is optional and the maven-plugin to automatically infer from the pom.xml some required information such as the path of the project root directory.

**!! WARNING !!** Since DSpot can be executed on one module at the time, we advise you to the same with the maven plugin by using the dedicated command line option `-DtargetModule=<module>`.

The best way is to execute dspot-maven on the parent project and specifying the targeted module using the options mentioned above. In this way, DSpot will be able to collect all the dependencies.