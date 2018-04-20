# DSpot

[![Build Status](https://travis-ci.org/STAMP-project/dspot.svg?branch=master)](https://travis-ci.org/STAMP-project/dspot)[![Coverage Status](https://coveralls.io/repos/github/STAMP-project/dspot/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/dspot?branch=master)

[Riot chat room](https://riot.im/app/#/room/!vnCyWaGJbxESAkzyqh:matrix.org)

DSpot is a tool that generates missing assertions in JUnit tests.
DSpot takes as input a Java project with an existing test suite.
As output, DSpot outputs new test cases on console. 
DSpot supports Java projects built with Maven and Gradle (see the `--automatic-builder` option)

## Getting started

### Prerequisites

You need Java and Maven.

### Compilation

1) Clone the project:
```
git clone https://github.com/STAMP-project/dspot.git
cd dspot/dspot
```

2) Compile DSpot
```
mvn compile
```

3) DSpot uses the environment variable MAVEN_HOME, ensure that this variable points to your maven installation. Example:
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
```

6) Run the jar
```
java -cp target/dspot-*-jar-with-dependencies.jar fr.inria.stamp.Main -p path/To/my.properties
```

For more info, see section **Usage** below.

### Releases

See <https://github.com/STAMP-project/dspot/releases>


## Contributing

DSpot is licensed un LGPLv3. Pull request as are welcome.

## Usage


### First Tutorial

After having cloned DSpot (see the previous section), you can run the provided example by running
`fr.inria.stamp.Main` from your IDE, or with

```
java -jar target/dspot-LATEST-jar-with-dependencies.jar --example
```

replacing `LATEST` by the latest version of DSpot, _e.g._ 1.1.0 would give :
 `dspot-1.1.0-jar-with-dependencies.jar`

This example is an implementation of the function `chartAt(s, i)` (in `src/test/resources/test-projects/`), which
returns the char at the index _i_ in the String _s_.

In this example, DSpot amplifies the tests of `chartAt(s, i)` with the `TestDataMutator`, which modifies literals inside the test and the generation of assertions.

DSpot first reads information about the project from the properties file
`src/test/resources/test-projects/test-projects.properties`.

```properties
#relative path to the project root from dspot project
project=src/test/resources/test-projects/
#relative path to the source project from the project properties
src=src/main/java/
#relative path to the test source project from the project properties
testSrc=src/test/java
#java version used
javaVersion=8
#path to the output folder
outputDirectory=target/trash/
# Constant amount of additional time to allow a test to run for
# before considering it to be stuck in an infinite loop
timeoutConstInMillis=10000
#Argument string to use when PIT launches child processes. This is most commonly used
# to increase the amount of memory available to the process,
# but may be used to pass any valid JVM argument.
# Use commas to separate multiple arguments, and put them within brackets
jvmArgs=['-Xmx2048m','-Xms1024m']
```

The result of the amplification of charAt consists of 6 new tests, as shown in the output below. These new tests are
written to the output folder specified by configuration property `outputDirectory` (`./target/trash/`).

```
======= REPORT =======
Initial instruction coverage: 33 / 37
89.19%
Amplification results with 22 amplified tests.
Amplified instruction coverage: 37 / 37
100.00%

[8411] INFO DSpot - Print TestSuiteExampleAmpl with 22 amplified test cases in target/trash/
```

### Wiki documentation

For a more step by step documentation, see the [wiki](https://github.com/STAMP-project/dspot/wiki). 

### Command Line Usage


Let's imagine you wish to run DSpot on `module1`. You need to create a properties file (e.g. `dspot.properties`),
that you can locate under `module1` and containing (for example):

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
```

You can then execute DSpot by using:

```
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --path-to-properties dspot.properties
# or in maven
mvn exec:java -Dexec.mainClass="fr.inria.stamp.Main" -Dexec.args="--path-to-properties dspot.properties"
```

Amplify a specific test class
```
java -jar /path/to/dspot-*-jar-with-dependencies.jar fr.inria.stamp.Main --path-to-properties dspot.properties --test my.package.TestClass
```
Amplify specific test classes according to a regex
```
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --path-to-properties dspot.properties --test my.package.*
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --path-to-properties dspot.properties --test my.package.Example*
```

Amplify a specific test method from a specific test class
```
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --path-to-properties dspot.properties --test my.package.TestClass --cases testMethod
```

```
Usage: java -jar target/dspot-<version>-jar-with-dependencies.jar
                          [(-p|--path-to-properties) <./path/to/myproject.properties>] [(-a|--amplifiers) Amplifier1:Amplifier2:...:AmplifierN ] [(-i|--iteration) <iteration>] [(-s|--test-criterion) <PitMutantScoreSelector | ExecutedMutantSelector | CloverCoverageSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector>] [--max-test-amplified <integer>] [(-t|--test) my.package.MyClassTest1:my.package.MyClassTest2:...:my.package.MyClassTestN ] [(-c|--cases) testCases1:testCases2:...:testCasesN ] [(-o|--output-path) <output>] [--clean] [(-m|--path-pit-result) <./path/to/mutations.csv>] [--descartes] [--automatic-builder <MavenBuilder | GradleBuilder>] [--maven-home <path to maven home>] [--randomSeed <long integer>] [--timeOut <long integer>] [--verbose] [--with-comment] [--no-minimize] [-e|--example] [-h|--help]

  [(-p|--path-to-properties) <./path/to/myproject.properties>]
        [mandatory] specify the path to the configuration file (format Java
        properties) of the target project (e.g. ./foo.properties).

  [(-a|--amplifiers) Amplifier1:Amplifier2:...:AmplifierN ]
        [optional] specify the list of amplifiers to use. Default with all
        available amplifiers.
        		 - StringLiteralAmplifier
        		 - NumberLiteralAmplifier
        		 - CharLiteralAmplifier
        		 - BooleanLiteralAmplifier
        		 - AllLiteralAmplifiers
        		 - MethodAdd
        		 - MethodRemove
        		 - TestDataMutator (deprecated)
        		 - StatementAdd
        		 - ReplacementAmplifier
        		 - None (default: None)

  [(-i|--iteration) <iteration>]
        [optional] specify the number of amplification iterations. A larger
        number may help to improve the test criterion (eg a larger number of
        iterations may help to kill more mutants). This has an impact on the
        execution time: the more iterations, the longer DSpot runs. (default: 3)

  [(-s|--test-criterion) <PitMutantScoreSelector | ExecutedMutantSelector | CloverCoverageSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector>]
        [optional] specify the test adequacy criterion to be maximized with
        amplification (default: PitMutantScoreSelector)

  [--max-test-amplified <integer>]
        [optional] specify the maximum number of amplified tests that dspot
        keeps (before generating assertion) (default: 200)

  [(-t|--test) my.package.MyClassTest1:my.package.MyClassTest2:...:my.package.MyClassTestN ]
        [optional] fully qualified names of test classes to be amplified. If the
        value is all, DSpot will amplify the whole test suite. You can also use
        regex to describe a set of test classes. (default: all)

  [(-c|--cases) testCases1:testCases2:...:testCasesN ]
        specify the test cases to amplify

  [(-o|--output-path) <output>]
        [optional] specify the output folder (default: dspot-report)

  [--clean]
        [optional] if enabled, DSpot will remove the out directory if exists,
        else it will append the results to the exist files. (default: off)

  [(-m|--path-pit-result) <./path/to/mutations.csv>]
        [optional, expert mode] specify the path to the .csv of the original
        result of Pit Test. If you use this option the selector will be forced
        to PitMutantScoreSelector

  [--descartes]
        Enable the descartes engine for Pit Mutant Score Selector.

  [--automatic-builder <MavenBuilder | GradleBuilder>]
        [optional] specify the automatic builder to build the project (default:
        MavenBuilder)

  [--maven-home <path to maven home>]
        specify the path to the maven home

  [--randomSeed <long integer>]
        specify a seed for the random object (used for all randomized operation)
        (default: 23)

  [--timeOut <long integer>]
        specify the timeout value of the degenerated tests in millisecond
        (default: 10000)

  [--verbose]
        Enable verbose mode of DSpot.

  [--with-comment]
        Enable comment on amplified test: details steps of the Amplification.

  [--no-minimize]
        Disable the minimization of amplified tests.

  [-e|--example]
        run the example of DSpot and leave

  [-h|--help]
        show this help

```

### Configuration

Here is the list of configuration properties of DSpot:

* required properties:
  * project: path to the project root directory.
  * src: relative path (from project properties) to the source root directory.
  * testSrc: relative path (from project properties) to the test source root directory.
* recommended properties:
  * outputDirectory: path to the out of dspot. (default: output)
  * javaVersion: version used of Java (default: 5)
  * maven.home: path to the executable maven. If no value is specified, it will try some defaults values
    (for instance: `/usr/share/maven/`, `usr/local/Cellar/maven/3.3.9/libexec/` ...).
* optional properties:
  * filter: filter on the package name containing tests to be executed (_e.g._ `example.*`) passed to Pitest. If not set, the default value is set automatically by PIT based on the pom.xml data as follows: `filter=<groupid>.<artifactid>.*`. If you set it, we recommand to give the top-most package of your project. (_e.g._ for the test-projects it would be `example.*`)
  * maven.localRepository: path to the local repository of Maven (.m2), if you need specific settings.
  * excludedClasses: DSpot will not amplify the excluded test classes.
  * additionalClasspathElements: add elements to the classpath. (e.g. a jar file)
  * excludedClasses: list of full qualified name of test classes to be excluded
   by DSpot (see this [property file](https://github.com/STAMP-project/dspot/blob/master/dspot/src/test/resources/sample/sample.properties))
  * excludedTestCases: list of test name method to be excluded
  by DSpot (see this [property file](https://github.com/STAMP-project/dspot/blob/master/dspot/src/test/resources/sample/sample.properties))

#### Amplifiers (-a | --amplifiers)

By default, **DSpot** uses no amplifier because the simplest amplification that can be done is the generation of assertions on existing tests, _i.e._ it will improve the oracle and the potential of the test suite to capture bugs.

However, **DSpot** provide different kind of `Amplifier`:

   * `StringLiteralAmplifier`: modifies string literals: remove, add and replace one random char, generate random string and empty string
   * `NumberLiteralAmplifier`: modifies number literals: replace by boundaries (_e.g._ Integer_MAX_VALUE for int), increment and decrement values
   * `CharLiteralAmplifier`: modifies char literals: replace by special chars (_e.g._ '\0')
   * `BooleanLiteralAmplifier`: modifies boolean literals: nagate the value
   * `AllLiteralAmplifier`: combines all literals amplifiers, _i.e._ StringLiteralAmplifier, NumberLiteralAmplifier, CharLiteralAmplifier and BooleanLiteralAmplifier
   * `MethodAdd`: duplicates an existing method call
   * `MethodRemove`: removes an existing method call
   * `StatementAdd`: adds a method call, and generate required parameter
   * `ReplacementAmplifier`: replaces a local variable by a generated one
   * `TestDataMutator`: old amplifier of literals (all types, deprecated)

All amplifiers are just instanciable without any parameters, _e.g._ new StringLiteralAmplifier.

For the amplifiers, you can give them at the constructor of your `DSpot` object, or use the `DSpot#addAmplifier(Amplifier)` method.

#### Test Selectors (-s | --test-criterion)

In **DSpot**, test selectors can be seen as a fitness: it measures the quality of amplified, and keeps only amplified tests that are worthy according to this selector.

The default selector is `CloverCoverageSelector`. This selector is based on [**OpenClover**](http://openclover.org/), which is a tool to compute the coverage. **DSpot** will keep only tests that increase the code coverage.

Following the list of avalaible test selector:

   * `CloverCoverageSelector`: uses [**OpenClover**](http://openclover.org/) to compute branch coverage, and selects amplified tests that increase it.
   * `JacocoCoverageSelector`: uses [**JaCoCo**](http://www.eclemma.org/jacoco/) to compute instruction coverage and executed paths (the order matters). Selects test that increase the coverage and has unique executed path.
   * `PitMutantScoreSelector`: uses [**PIT**](http://pitest.org/) to computes the mutation score, and selects amplified tests that kill mutants that was not kill by the original test suite.
   * `ExecutedMutantSelector`: uses [**PIT**](http://pitest.org/) to computes the number of executed mutants. It uses the number of mutants as a proxy for the instruction coverage. It selects amplfied test that execute new mutants. **WARNING!!** this selector takes a lot of time, and is not worth it, please look at CloverCoverageSelector or JacocoCoverageSelector.
   * `TakeAllSelector`: keeps all amplified tests not matter the quality.
   * `ChangeDetectorSelector`: runs against a second version of the same program, and selects amplified tests that fail. This selector selects only amplified test that are able to show a difference of a behavior betweeen two versions of the same program.

#### Selector on Diff from GitHub

**DSpot** can perform amplification according to a diff, from github. This is useful to enhance existing test suite on pull request for instance.

**DSpot** will select existing test classes according to a diff as follow:

1. If any test class has been modified between the two versions, **DSpot** selects them.
2. If there is not, **DSpot** selects test cases (and so, their test classes) according to the nem. If a test contain the name of a modified method, this test is selected.
3. If there is not, **DSpot** analyzes statical the test suite and selects test classes that invoke modified methods. The maximum number of selected test classes is the value of `fr.inria.stamp.diff.SelectorOnDiff.MAX_NUMBER_TEST_CLASSES`, randomly.

The requirements this feature is the following:

1. You must enable the mode by giving `diff` as value of the flag --test (-t), _i.e._ `--test diff`
2. You must have specify baseSha, on which **DSpot** must compute the diff. **DSpot** executes: `git diff <baseSha>` to find which java file has been modified. To do this, you must specify the sha in the property file, by setting the property: `baseSha`, _e.g._ `baseSha=97393d96ea58110785e342fade2e054925c608ad`
3. You must have locally both version of the program. One that you want to use amplify, and the other on which you want to compute the diff. The first is specified using the property `project` as explained in a classical way to amplify. The second is specified using the property `folderPath`.

You can specify a maximum number of selected test classes using the property `maxSelectedTestClasses`.

### Using DSpot as an API

In this section, we explain the API of **DSpot**. To amplify your tests with **DSpot** you must do 3 steps:
First of all, you have to create an `InputConfiguration`. Only the path to your _properties_ is required:

```java
// 1. Instantiate `InputConfiguration` and `InputProgram`
String propertiesFilePath = <pathToYourPropertiesFile>;
InputConfiguration inputConfiguration = new InputConfiguration(propertiesFilePath);
```

Then you have to build the `InputProgram`, this is done by attaching the `InputProgram` to your `InputConfiguration`:

```java
InputProgram program = new InputProgram();
inputConfiguration.setInputProgram(program);
```
Then, you are ready to construct the `DSpot` object that will allow you to amplify your test.
There are a lot of constructor available, all of them allow you to custom your `DSpot` object, and so your amplification.
Following the shortest constructor with all default values of `DSpot`, and the longest, which allows to custom all values of `DSpot`:

```java
// 2. Instantiate `DSpot` object
DSpot dspot = new DSpot(InputConfiguration);
DSpot dspot = new DSpot(
    InputConfiguration inputConfiguration, // input configuration built at step 1
    int numberOfIterations, // number of time that the main loop will be applied (-i | --iteration option of the CLI)
    List<Amplifier> amplifiers, // list of the amplifiers to be used (-a | --amplifiers option of the CLI)
    TestSelector testSelector // test selector criterion (-s | --test-selector option of the CLI)
);
```

Now that you have your `DSpot`, you will be able to amplify your tests.
`DSpot` has several methods to amplify, but all of them starts with amplify key-word:

```java
// 3. start ampification
dspot.amplifyTest(String regex); // will amplify all test classes that match the given regex
dspot.amplifyTest(String fulQualifiedName, List<String> testCasesName); // will amplify test cases that have their name in testCasesName in the test class fulQualifiedName
dspot.amplifyAllTests(); // will amplify all test in the test suite.
```


### Acknowledgement

Dspot is partially funded by research project H2020 STAMP.

