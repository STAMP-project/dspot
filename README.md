DSpot [![Build Status](https://travis-ci.org/STAMP-project/dspot.svg?branch=master)](https://travis-ci.org/STAMP-project/dspot)[![Coverage Status](https://coveralls.io/repos/github/STAMP-project/dspot/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/dspot?branch=master)
=====================================================================================================================

### What is Dspot?

The goal of DSpot is to automatically improve existing JUnit test suites.
It automatically generates new JUnit tests by modifying existing  existing test cases.

- Input: DSpot take as input a Java project with an existing test suite..
- Output: DSpot produces a new test suite, containing both the existing tests and the new generated ones.

**How does it work?** DSpot applies transformation operators on existing tests in order to create new observation points on the state of the system. Those observation points are used to generate new assertion statements. The generated tests are then selected and ordered according to a specific criterion, such as the branch coverage or mutation score.

### Compile DSpot

1) Clone the project:
```
git clone http://github.com/STAMP-project/dspot.git
cd dspot
```

2) Install the dependencies of dspot by running the provided script:
```
# first, local install of dependencies not in Maven Central
chmod +x install.sh
./install.sh
# second, the other dependencies
mvn dependency:resolve
```

3) Compile DSpot
```
mvn compile
```

4) Run the tests
```
mvn test
```

5) Create the jar (eg `target/dspot-1.0.0-jar-with-dependencies.jar`)
```
mvn package
# check that this is successful
ls target/dspot-*-jar-with-dependencies.jar
java -cp target/dspot-*-jar-with-dependencies.jar fr.inria.diversify.Main path/To/my.properties
```


### Running example

You can run the provide example by running `fr.inria.diversify.Main` from your IDE, or with
```
mvn exec.java -Dexec.mainClass="fr.inria.diversify.Main"
```

DSpot will amplify an example test project (see `src/test/resources/test-projects/`) with one simple amplifier (`TestDataMutator`, which schange literals) and using the generation of assertions.
It will print on the standard output the resulting amplified class `example.TestSuiteExample` containing the amplified tests cases.

DSpot reads information about the project from a properties file. Here the properties file used for the example: (see `src/test/resources/test-projects/test-projects.properties`)

```properties
#relative path to the project root from dspot project
project=src/test/resources/test-projects
#relative path to the source project from the project properties
src=src/main/java/
#relative path to the test source project from the project properties
testSrc=src/test/java
#java version used
javaVersion=8
# (optional) path to the output folder, default to "output_diversify"
result=dspot-out/
# (optional) filter on the package name containing tests to be amplified ("example" => "example.*"
filter=example
```

### Running on your own project

You can run DSpot on your own project by running the `fr.inria.diversify.Main` and specifying the path to the properties file as first argument:
```
java -cp target/dspot-*-jar-with-dependencies.jar fr.inria.diversify.Main path/To/my.properties

# or in maven
mvn exec.java -Dexec.mainClass="fr.inria.diversify.Main" -Dexec.args="<pathToPropertiesFile>"
```

### API

The whole procedure of amplification is done by the `fr.inria.diversify.dspot.DSpot` class. 
You must at least provide the path to the properties file of your project at the construction of the object.
You can specify the number of times each amplifier will be applies to the test cases (default 3).
You can specify which amplifiers (as a list) you want to use. By default, DSpot uses: 

    * TestDataMutator: which transforms literals.
    * TestMethodCallAdder: which duplicatse an existing method call in the test case.
    * TestMethodCallRemover: which removes a method call in the test case.
    * StatementAdderOnAssert: which adds calls to accessible methods on existing objects and creates new instances.
    * StatementAdd: which reuses existing objects and return values to add method calls to accessible methods.

##### Amplifiers

You can implement you own amplifier by implemening the `fr.inria.diversify.dspot.amp.Amplifier` interface and giving it to DSpot.


### Licence

DSpot is published under LGPL-3.0 (see [Licence.md](https://github.com/STAMP-project/dspot/blob/master/Licence.md) for further details).
