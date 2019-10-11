# DSpot Maven Plugin

This plugin let you integrate unit test amplification in your Maven build process. You can use this plugin on the command line as the jar:

```bash
# this amplifies the Junit tests to kill more mutants
mvn eu.stamp-project:dspot-maven:amplify-unit-tests

# this amplifies the Junit tests to improve coverage
mvn eu.stamp-project:dspot-maven:amplify-unit-tests -Dtest-criterion=JacocoCoverageSelector

```  

All the option can be pass through command line by prefixing the option with `-D`.
For example: 

```bash
mvn eu.stamp-project:dspot-maven:amplify-unit-tests -Dtest=my.package.TestClass -Dcases=testMethod
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

In case your project is a multi-module, we advise you to configure DSpot in the highest `pom.xml` and use the dedicated command-line option `-DtargetModule=<Your_Module>` to name the module you want to amplify.

After setting up your `pom.xml` and add your configuration with different options, run:

```bash
mvn dspot:amplify-unit-tests
``` 

/!\ This command-line works only if you defined DSpot in your `pom.xml`. If you do not, please use the complete command-line:

```
mvn eu.stamp-project:dspot-maven:amplify-unit-tests
```  

# Advantages

The advantages of using the maven plugin is that maven will automatically the latest version deployed on maven central, even the SNAPSHOT built at each commits on the master!

Second, is that the maven-plugin automatically infers from the pom.xml some required information such as the path of the project root directory.
