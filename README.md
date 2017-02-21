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
git clone https://github.com/STAMP-project/dspot.git
cd dspot
```

2) Install the dependencies of dspot by running the provided script:
```
# first, local install of dependencies not in Maven Central
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

### Command Line Usage
```
Usage: java -jar target/dspot-1.0.0-jar-with-dependencies.jar
                          [(-p|--path) <path>] [(-a|--amplifiers) Amplifier1:Amplifier2:...:AmplifierN ] [(-i|--iteration) <iteration>] [(-s|--selector) <BranchCoverageTestSelector | PitMutantScoreSelector>] [(-t|--test) test1:test2:...:testN ] [(-o|--output) <output>] [(-m|--mutant) <mutant>] [-e|--example] [-h|--help]

      [(-p|--path-to-propeties) <./path/to/myproject.properties>]
            [mandatory] specify the path to the configuration file (format Java
            properties) of the target project (e.g. ./foo.properties).
    
      [(-a|--amplifiers) Amplifier1:Amplifier2:...:AmplifierN ]
            [optional] specify the list of amplifiers to use. Default with all
            available amplifiers. Possible values:
            MethodAdd|MethodRemove|StatementAdderOnAssert|TestDataMutator
    
      [(-i|--iteration) <iteration>]
            [optional] specify the number of amplification iteration. A larger
            number may help to improve the test criterion (eg a larger number of
            iterations mah help to kill more mutants). This has an impact on the
            execution time: the more iterations, the longer DSpot runs. (default: 3)
    
      [(-s|--test-criterion) <PitMutantScoreSelector | BranchCoverageTestSelector>]
            [optional] specify the test adequacy criterion to be maximized with
            amplification (default: PitMutantScoreSelector)
    
      [(-t|--test) my.package.MyClassTest1:my.package.MyClassTest2:...:my.package.MyClassTestN ]
            [optional] fully qualified names of test classes to be amplified. If the
            value is all, DSpot will amplify the whole test suite. (default: all)
    
      [(-o|--output-path) <output>]
            [optional] specify the output folder (default: dspot-report)
    
      [(-m|--path-pit-result) <./path/to/mutations.csv>]
            [optional, expert mode] specify the path to the .csv of the original
            result of Pit Test. If you use this option the selector will be forced
            to PitMutantScoreSelector
            
      [(-r|--randomSeed) <long integer>]
            specify a seed for the random object (used for all randomized operation) (default: 23)

      [(-v|--timeOut) <long integer>]
            specify the timeout value of the degenerated tests in millisecond
            (default: 10000)
    
      [-e|--example]
            run the example of DSpot and leave
    
      [-h|--help]
            shows this help
```

### Getting Started Example

You can run the provided example by running `fr.inria.diversify.Main` from your IDE, or with
```
mvn exec:java -Dexec.mainClass="fr.inria.diversify.Main" -Dexec.args="--example"
```
or
```
java -jar target/dspot-1.0.0-jar-with-dependencies.jar --example
```

This example is an implementation of the function `chartAt(s, i)` (in `src/test/resources/test-projects/`), which returns the char at the index _i_ in the String _s_.

In this example, DSpot amplifies the tests of `chartAt(s, i)` with defaults amplifiers `TestDataMutator`, `TestMethodCallAdder`, `TestMethodCallRemover` and `StatementAdderOnAssert`, which changes literals (add 1 to integer, remove one char in a string, etc...), and with generation of assertions.

DSpot first reads information about the project from the properties file `src/test/resources/test-projects/test-projects.properties`
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

The result of the amplification of charAt consists of 6 new tests, as shown in the output below. Those new tests are written to the output folder specified by configuration property `outputDirectory` (`./dspot-out/`). 

```
======= REPORT =======
Branch Coverage Selector:
Initial coverage: 83.33%
There is 3 unique path in the original test suite
The amplification results with 6 new tests
The branch coverage obtained is: 100.00%
There is 4 new unique path


Print TestSuiteExampleAmpl with 6 amplified test cases in dspot-out/
```


### Output of DSpot

DSpot produces 3 outputs in the <outputDirectory> (default: `output_diversify`) specified in the properties file.

* a textual report of the result of the amplification also printed  on the standard output 
* a json file summarizing the amplification 
* the amplified tests augmented with comments (see `DSpotUtils.printJavaFileWithComment()`)

### Running on your own project

You can run DSpot on your own project by running `fr.inria.diversify.Main` and specifying the path to the properties file as first argument:

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

You can implement you own amplifier by implementing the `fr.inria.diversify.dspot.amplifier.Amplifier` interface and giving it to DSpot.

#### Test Selectors

A test selector is responsible the tests to be amplified in an amplification iteration.
There are two test selectors:

* BranchCoverageTestSelector: it selects amplified tests that increase the coverage, or produce a new unique execution path. BranchCoverageTestSelector produces a json which contains for each amplified test class, the name of the generated tests. For each test, the json file gives the number of added inputs, added assertions and the coverage measured in \# of method calls

* PitMutantScoreSelector: it selects amplified tests that increase the mutant score, _i.e._ kills more mutants than the original tests. The mutants are generated with [Pitest](http://pitest.org/). Warning, the selector takes more time than the first one. This selector produces a json which contains for the amplified classed, the name of of each generated test, the number of added inputs, of added assertions, and the number of newly killed mutants. For each newly killed  mutant killed, it gives:
        * the ID of the mutant operators (see [Mutator](http://pitest.org/quickstart/mutators/))
        * the name of the method where the mutant is inserted.
        * the line where the mutant is inserted.

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

Here is the list of configuration properties of DSpot:

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
 * excludedClasses: dspot will not amplify the excluded test classes.
 * additionalClasspathElements: add elements to the classpath. (e.g. a jar file)

### Licence

DSpot is published under LGPL-3.0 (see [Licence.md](https://github.com/STAMP-project/dspot/blob/master/Licence.md) for further details).
