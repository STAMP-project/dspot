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
mvn exec:java -Dexec.mainClass="fr.inria.diversify.Main"
```

This example is an implementation of the function `chartAt(s, i)`, which return the char at the index _i_ in the String _s_.

DSpot will amplify it (see `src/test/resources/test-projects/`) with one simple amplifier (`TestDataMutator`, which change literals: add 1 to integer, remove one char in a string etc...)
and using the generation of assertions.

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
outputDirectory=dspot-out/
# (optional) filter on the package name containing tests to be amplified ("example" => "example.*"
filter=example
```

### Output

DSpot will produce 3 outputs in the <outputDirectory> (default: output_diversify) specified in the properties file.

* report: a general report of the result of the amplification. It will print it on the standard output 
* a json file (see Selector section for further information).

The Main class uses the `DSpotUtils.printJavaFileWithComment()` to print the amplified test (without original test cases) in the <outputDirectory>. 

### Running on your own project

You can run DSpot on your own project by running the `fr.inria.diversify.Main` and specifying the path to the properties file as first argument:
```
java -cp target/dspot-*-jar-with-dependencies.jar fr.inria.diversify.Main path/To/my.properties

# or in maven
mvn exec:java -Dexec.mainClass="fr.inria.diversify.Main" -Dexec.args="<pathToPropertiesFile>"
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
  
The amplifier `StatementAdd` (unstable): which reuses existing objects and return values to add method calls to accessible methods.
Need the initialized InputProgram if DSpot to be well constructed.

##### Amplifiers

You can implement you own amplifier by implementing the `fr.inria.diversify.dspot.amp.Amplifier` interface and giving it to DSpot.

#### TestSelector

There is two implementation of the TestSelector:

* BranchCoverageTestSelector: it selects generated tests that increase the coverage, or produce a new unique path (execution path, good for unit test).
   * The BranchCoverageTestSelector produces a json which contains for the amplified class, the name of each generated test. For each, there is the number of input added, the assertions added and the length of the path covered (in method calls)

* PitMutantScoreSelector: it selects generated tests that increase the mutant score, _i.e._ kills more mutants generated with [Pitest](http://pitest.org/). (It can take a while)
    * PitMutantScoreSelector produces a json which contains for the amplified class, the name of of each generated test.  For each, there is the number of input added, the assertions added, the number of mutant that it kills and the list of the mutant killed with:
        * the ID of the mutant operators (see [Mutator](http://pitest.org/quickstart/mutators/))
        * the line where the mutant is inserted.
        * the name of the method where it is inserted.

#### Snippets

The simplest snippets is:
```java
InputConfiguration configuration = new InputConfiguration(<pathToPropertiesFile>);
DSpot dspot = new DSpot(configuration);
List<CtType> amplifiedTestClass = dspot.amplifiyAllTests();
```
Which will run the default amplifiers, 3 times on the whole existing test suite, with the BranchCoverageTestSelector.

To customize your DSpot, you can use several constructors:

```java
new DSpot(configuration);
new DSpot(configuration, 1); //1 iteration
new DSpot(configuration, 1, Collections.singletonList(new TestDataMutator)); //1 iteration, one specific amplifier
DSpot dspot = new DSpot(configuration, 2, Arrays.asList(new TestDataMutator, new StatementAdderOnAssert())); //1 iteration, two specified amplifiers
dspot.addAmplifier(new StatementAdd(dspot.getInputProgram())); // or add an amplifiers after the construction.
new DSpot(configuration, new PitMutantScoreSelector());//3 iterations, default amplifier, PitMutantScoreSelector
```

###### Available Properties

Find here, the list of available properties:

* required properties:
 * project: path to the project root directory.
 * src: relative path (from project properties) to the source root directory.
 * testSrc: relative path (from project properties) to the test source root directory.

* recommended properties:
 * outputDirectory: path to the out of dspot. (default: output)
 * javaVersion: version used of java (default: 5)
 * maven.home: path to the executable maven. If no value is specified, it will try some defaults values (for instance: `/usr/share/maven/`, `usr/local/Cellar/maven/3.3.9/libexec/` ...).

* optional properties:
 * filter: string to filter on package or classes.
 * maven.localRepository: path to the local repository of maven (.m2), if you need specific settings. 

###### Printing

In order to print, you can use the following snippets:
```java
DSpotUtils.printJavaFile(outputDirectory, testClass);
```
where _outputDirectory_ is a `java.io.File` pointing to the directory of the output(for instance in the test-projects example: `dspot-out/`)
and where _testClass_ is a CtType (the type of object returned by DSpot). 

### Licence

DSpot is published under LGPL-3.0 (see [Licence.md](https://github.com/STAMP-project/dspot/blob/master/Licence.md) for further details).
