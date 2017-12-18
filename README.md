DSpot: a Test Amplification Tool for Java [![Build Status](https://travis-ci.org/STAMP-project/dspot.svg?branch=master)](https://travis-ci.org/STAMP-project/dspot)[![Coverage Status](https://coveralls.io/repos/github/STAMP-project/dspot/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/dspot?branch=master)
=====================================================================================================================

### What is Dspot?

DSpot automatically improves existing JUnit test suites.
It automatically generates new JUnit tests by modifying existing ones.

- Input: DSpot take as input a Java project with an existing test suite.
- Output: DSpot produces a new test cases, given on the command line and in a new file. Those new test cases kill new mutants, which means that Dspot help you to catch more regressions and to improve your mutation score.

**How does Dspot work?** DSpot applies transformation operators on existing tests.
The transformations result in new inputs and new explored paths. They also consist of adding new assertions.

### Output of DSpot

DSpot produces 3 outputs in the <outputDirectory> (default: `output_diversify`) specified in the properties file.

* a textual report of the result of the amplification also printed  on the standard output 
* a json file summarizing the amplification 
* the amplified tests augmented with comments (see `DSpotUtils.printJavaFileWithComment()`)

### Running on your own project

First, you should either [download the DSpot JAR](https://github.com/STAMP-project/dspot/releases) or build DSpot from 
its sources (see below the section about compiling DSpot).

You'll also need to create a properties file providing information to Dspot about where to find sources and more. 
Let's imagine that we have a Maven multimodule project as follows:

```
myproject/
  |_ module1/
    |_ pom.xml
  |_ module 2/
    |_ pom.xml
```

Let's imagine you wish to run DSpot on `module1`. You'd need to create a properties file (e.g. `dspot.properties`),
that you can located under `module1` and containing (for example):

```properties
# Relative path to the project root.
project=..
# Path to the current module where we want to execute DSpot, relative to the project root
targetModule=module1/
# Relative path to the source project from this properties file
src=src/main/java/
# Relative path to the test source project from this properties file
testSrc=src/test/java
# Java version used
javaVersion=8
# (Optional) Path to the output folder, default to "output_diversify"
outputDirectory=dspot-out/
# (Optional) Filter on the package name containing tests to be amplified ("example" => "example.*")
filter=example
```

You can then execute DSpot by using:

```
java -cp /path/to/dspot-*-jar-with-dependencies.jar fr.inria.stamp.Main --path-to-properties dspot.properties
# or in maven
mvn exec:java -Dexec.mainClass="fr.inria.stamp.Main" -Dexec.args="--path-to-properties dspot.properties"
```

Amplify a specific test class
```
java -cp /path/to/dspot-*-jar-with-dependencies.jar fr.inria.stamp.Main --path-to-properties dspot.properties --test my.package.TestClass
```
Amplify specific test classes according to a regex
```
java -cp /path/to/dspot-*-jar-with-dependencies.jar fr.inria.stamp.Main --path-to-properties dspot.properties --test my.package.*
java -cp /path/to/dspot-*-jar-with-dependencies.jar fr.inria.stamp.Main --path-to-properties dspot.properties --test my.package.Example*
```

Amplify a specific test method from a specific test class
```
java -cp /path/to/dspot-*-jar-with-dependencies.jar fr.inria.stamp.Main --path-to-properties dspot.properties --test my.package.TestClass --cases testMethod
```

### Getting Started Example

After having cloned DSpot (see the previous section), you can run the provided example by running
`fr.inria.stamp.Main` from your IDE, or with

```
java -jar target/dspot-LATEST-jar-with-dependencies.jar --example
```

replacing `LATEST` by the latest version of **Dspot**, _e.g._ 1.0.4 would give :
 `dspot-1.0.4-jar-with-dependencies.jar`

This example is an implementation of the function `chartAt(s, i)` (in `src/test/resources/test-projects/`), which
returns the char at the index _i_ in the String _s_.

In this example, DSpot amplifies the tests of `chartAt(s, i)` with the `TestDataMutator`, which modifies literals inside the test and the generation of assertions.

DSpot first reads information about the project from the properties file
`src/test/resources/test-projects/test-projects.properties`.

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
# (optional) filter on the package name containing tests to be amplified ("example" => "example.*")
filter=example
```

The result of the amplification of charAt consists of 6 new tests, as shown in the output below. Those new tests are
written to the output folder specified by configuration property `outputDirectory` (`./dspot-out/`).

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

### Compiling DSpot

1) Clone the project:
```
git clone https://github.com/STAMP-project/dspot.git
cd dspot
```

2) Compile DSpot
```
mvn compile
```

3) DSpot use the environnment variable MAVEN_HOME, ensure that this variable points to your maven installation. Example:
```
export MAVEN_HOME=path/to/maven/
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
java -cp target/dspot-*-jar-with-dependencies.jar fr.inria.stamp.Main -p path/To/my.properties
```

### Command Line Usage
```
Usage: java -jar target/dspot-1.0.0-jar-with-dependencies.jar
                          [(-p|--path-to-properties) <./path/to/myproject.properties>] [(-a|--amplifiers) Amplifier1:Amplifier2:...:AmplifierN ] [(-i|--iteration) <iteration>] [(-s|--test-criterion) <PitMutantScoreSelector | ExecutedMutantSelector | BranchCoverageTestSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector>] [(-g|--max-test-amplified) <integer>] [-d|--descartes] [-k|--evosuite] [(-t|--test) my.package.MyClassTest1:my.package.MyClassTest2:...:my.package.MyClassTestN ] [(-c|--cases) testCases1:testCases2:...:testCasesN ] [(-o|--output-path) <output>] [(-m|--path-pit-result) <./path/to/mutations.csv>] [(-b|--automatic-builder) <MavenBuilder | GradleBuilder>] [--useReflection] [(-j|--maven-home) <path to maven home>] [(-r|--randomSeed) <long integer>] [(-v|--timeOut) <long integer>] [--verbose] [-e|--example] [-h|--help]

  [(-p|--path-to-properties) <./path/to/myproject.properties>]
        [mandatory] specify the path to the configuration file (format Java
        properties) of the target project (e.g. ./foo.properties).

  [(-a|--amplifiers) Amplifier1:Amplifier2:...:AmplifierN ]
        [optional] specify the list of amplifiers to use. Default with all
        available amplifiers. Possible values: NumberLiteralAmplifier |
        MethodAdd | MethodRemove | TestDataMutator | StatementAdd | None
        (default: None)

  [(-i|--iteration) <iteration>]
        [optional] specify the number of amplification iteration. A larger
        number may help to improve the test criterion (eg a larger number of
        iterations mah help to kill more mutants). This has an impact on the
        execution time: the more iterations, the longer DSpot runs. (default: 3)

  [(-s|--test-criterion) <PitMutantScoreSelector | ExecutedMutantSelector | BranchCoverageTestSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector>]
        [optional] specify the test adequacy criterion to be maximized with
        amplification (default: PitMutantScoreSelector)

  [(-g|--max-test-amplified) <integer>]
        [optional] specify the maximum number of amplified test that dspot keep
        (before generating assertion) (default: 200)

  [(-t|--test) my.package.MyClassTest1:my.package.MyClassTest2:...:my.package.MyClassTestN ]
        [optional] fully qualified names of test classes to be amplified. If the
        value is all, DSpot will amplify the whole test suite. You can also use
        regex to describe a set of test classes. (default: all)

  [(-c|--cases) testCases1:testCases2:...:testCasesN ]
        specify the test cases to amplify

  [(-o|--output-path) <output>]
        [optional] specify the output folder (default: dspot-report)

  [(-m|--path-pit-result) <./path/to/mutations.csv>]
        [optional, expert mode] specify the path to the .csv of the original
        result of Pit Test. If you use this option the selector will be forced
        to PitMutantScoreSelector

  [(-b|--automatic-builder) <MavenBuilder | GradleBuilder>]
        [optional] specify the automatic builder to build the project (default:
        MavenBuilder)

  [--useReflection]
        Use a totally isolate test runner. WARNING this test runner does not
        support the usage of the JacocoCoverageSelector

  [(-j|--maven-home) <path to maven home>]
        specify the path to the maven home

  [(-r|--randomSeed) <long integer>]
        specify a seed for the random object (used for all randomized operation)
        (default: 23)

  [(-v|--timeOut) <long integer>]
        specify the timeout value of the degenerated tests in millisecond
        (default: 10000)

  [--verbose]

  [-e|--example]
        run the example of DSpot and leave

  [-h|--help]
        shows this help
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
  * maven.home: path to the executable maven. If no value is specified, it will try some defaults values
    (for instance: `/usr/share/maven/`, `usr/local/Cellar/maven/3.3.9/libexec/` ...).
* optional properties:
  * filter: string to filter on package or classes.
  * maven.localRepository: path to the local repository of maven (.m2), if you need specific settings.
  * excludedClasses: dspot will not amplify the excluded test classes.
  * additionalClasspathElements: add elements to the classpath. (e.g. a jar file)
  * excludedClasses: list of full qualified name of test classes to be excluded
   by DSpot (see this [property file](https://github.com/STAMP-project/dspot/blob/master/dspot/src/test/resources/sample/sample.properties))
  * excludedTestCases: list of test name method to be excluded
  by DSpot (see this [property file](https://github.com/STAMP-project/dspot/blob/master/dspot/src/test/resources/sample/sample.properties))

### API

The whole procedure of amplification is done by the `fr.inria.diversify.dspot.DSpot` class. 
You must at least provide the path to the properties file of your project at the construction of the object.
You can specify the number of times each amplifier will be applies to the test cases (default 3).
You can specify which amplifiers (as a list) you want to use. By default, DSpot uses: 

    * TestDataMutator: which transforms literals.
    * TestMethodCallAdder: which duplicatse an existing method call in the test case.
    * TestMethodCallRemover: which removes a method call in the test case.
    * StatementAdd: which adds calls to accessible methods on existing objects and creates new instances.

#### Test Selectors

A test selector is responsible the tests to be amplified in an amplification iteration.
There are two test selectors:

* BranchCoverageTestSelector: it selects amplified tests that increase the coverage, or produce a new unique execution
path. BranchCoverageTestSelector produces a json which contains for each amplified test class, the name of the generated
tests. For each test, the json file gives the number of added inputs, added assertions and the coverage measured in \# 
of method calls
* PitMutantScoreSelector: it selects amplified tests that increase the mutant score, _i.e._ kills more mutants than the 
original tests. The mutants are generated with [Pitest](http://pitest.org/). Warning, the selector takes more time than
the first one. This selector produces a json which contains for the amplified classed, the name of of each generated 
test, the number of added inputs, of added assertions, and the number of newly killed mutants. For each newly killed 
mutant killed, it gives:
    * the ID of the mutant operators (see [Mutator](http://pitest.org/quickstart/mutators/))
    * the name of the method where the mutant is inserted.
    * the line where the mutant is inserted.

### Licence

DSpot is published under LGPL-3.0 (see [Licence.md](https://github.com/STAMP-project/dspot/blob/master/Licence.md) for 
further details).

### Funding

Dspot is partially funded by research project STAMP (European Commission - H2020)
![STAMP - European Commission - H2020](docs/logo_readme_md.png)
