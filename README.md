# DSpot

[![Build Status](https://travis-ci.org/STAMP-project/dspot.svg?branch=master)](https://travis-ci.org/STAMP-project/dspot)[![Coverage Status](https://coveralls.io/repos/github/STAMP-project/dspot/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/dspot?branch=master)[![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.stamp-project/dspot/badge.svg)](https://mavenbadges.herokuapp.com/maven-central/eu.stamp-project/dspot)

[Riot chat room](https://riot.im/app/#/room/!vnCyWaGJbxESAkzyqh:matrix.org)

DSpot is a tool that generates missing assertions in JUnit tests.
DSpot takes as input a Java project with an existing test suite.
As output, DSpot outputs new test cases on console. 
DSpot supports Java projects built with Maven and Gradle (see the `--automatic-builder` option)

DSpot ecosystem:

* Eclipse plugin: https://github.com/STAMP-project/eclipse-ide/
* Jenkins plugin: TBD
* Travis integration: TBD

## Getting started

### Prerequisites

You need Java and Maven.

DSpot uses the environment variable MAVEN_HOME, ensure that this variable points to your maven installation. Example:
```
export MAVEN_HOME=path/to/maven/
```

DSpot uses maven to compile, and build the classpath of your project. The environment variable JAVA_HOME must point to a valid JDK installation (and not a JRE).

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

3) Run the tests
```
mvn test
```

4) Create the jar (_e.g._ `target/dspot-1.0.0-jar-with-dependencies.jar`)
```
mvn package
# check that this is successful
ls target/dspot-*-jar-with-dependencies.jar
```

5) Run the jar
```
java -cp target/dspot-*-jar-with-dependencies.jar eu.stamp_project.Main -p path/To/my.properties
```

For more info, see section **Usage** below.

### Releases

See <https://github.com/STAMP-project/dspot/releases>


## Contributing

DSpot is licensed un LGPLv3. Pull request as are welcome.

## Usage


### First Tutorial

After having cloned DSpot (see the previous section), you can run the provided example by running
`eu.stamp_project.Main` from your IDE, or with

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
mvn exec:java -Dexec.mainClass="eu.stamp_project.Main" -Dexec.args="--path-to-properties dspot.properties"
```

Amplify a specific test class
```
java -jar /path/to/dspot-*-jar-with-dependencies.jar eu.stamp_project.Main --path-to-properties dspot.properties --test my.package.TestClass
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
                          [(-p|--path-to-properties) <./path/to/myproject.properties>] [(-a|--amplifiers) Amplifier1:Amplifier2:...:AmplifierN ] [(-i|--iteration) <iteration>] [(-s|--test-criterion) <PitMutantScoreSelector | ExecutedMutantSelector | CloverCoverageSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector>] [--budgetizer <NoBudgetizer | SimpleBuddgetizer>] [--max-test-amplified <integer>] [(-t|--test) my.package.MyClassTest | all | diff1:my.package.MyClassTest | all | diff2:...:my.package.MyClassTest | all | diffN ] [(-c|--cases) testCases1:testCases2:...:testCasesN ] [(-o|--output-path) <output>] [--clean] [(-m|--path-pit-result) <./path/to/mutations.csv>] [--descartes] [--automatic-builder <MavenBuilder | GradleBuilder>] [--maven-home <path to maven home>] [--randomSeed <long integer>] [--timeOut <long integer>] [--verbose] [--with-comment] [--no-minimize] [--working-directory] [-e|--example] [-h|--help]

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
        		 - MethodGeneratorAmplifier
        		 - ReturnValueAmplifier
        		 - ReplacementAmplifier
        		 - NullifierAmplifier
        		 - None (default: None)

  [(-i|--iteration) <iteration>]
        [optional] specify the number of amplification iterations. A larger
        number may help to improve the test criterion (e.g. a larger number of
        iterations may help to kill more mutants). This has an impact on the
        execution time: the more iterations, the longer DSpot runs. (default: 3)

  [(-s|--test-criterion) <PitMutantScoreSelector | ExecutedMutantSelector | CloverCoverageSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector>]
        [optional] specify the test adequacy criterion to be maximized with
        amplification (default: PitMutantScoreSelector)

  [--budgetizer <NoBudgetizer | SimpleBuddgetizer>]
        [optional] specify a Bugdetizer. (default: NoBudgetizer)

  [--max-test-amplified <integer>]
        [optional] specify the maximum number of amplified tests that dspot
        keeps (before generating assertion) (default: 200)

  [(-t|--test) my.package.MyClassTest | all | diff1:my.package.MyClassTest | all | diff2:...:my.package.MyClassTest | all | diffN ]
        [optional] fully qualified names of test classes to be amplified. If the
        value is all, DSpot will amplify the whole test suite. You can also use
        regex to describe a set of test classes. By default, DSpot selects all
        the tests (value all). You can use the value diff, to select tests
        according to a diff between two versions of the same program. Be
        careful, using --test diff, you must specify both properties folderPath
        and baseSha. (default: all)

  [(-c|--cases) testCases1:testCases2:...:testCasesN ]
        specify the test cases to amplify

  [(-o|--output-path) <output>]
        [optional] specify the output folder (default: target/dspot/output)

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

  [--working-directory]
        Enable this option to change working directory with the root of the
        project.

  [--generate-new-test-class]
        Enable the creation of a new test class.

  [-e|--example]
        run the example of DSpot and leave

  [-h|--help]
        show this help
```

### Configuration

Here is the list of configuration properties of DSpot:

* Required properties
	* project: specify the path to the root of the project. This path can be either absolute (recommended) or relative to the working directory of the DSpot process. We consider as root of the project folder that contain the top-most parent in a multi-module project.
* Required properties
	* targetModule: specify the module to be amplified. This value must be a relative path from the property project. If your project is multi-module, you must use this property because DSpot works at module level.
	* src: specify the relative path from project/targetModule of the folder that contain sources (.java).(default: src/main/java/)
	* testSrc: specify the relative path from project/targetModule of the folder that contain test sources (.java).(default: src/test/java/)
	* classes: specify the relative path from project/targetModule of the folder that contain binaries of the source program (.class).(default: target/classes/)
	* testclasses: specify the relative path from project/targetModule of the folder that contain binaries of the test source program (.class).(default: target/test-classes/)
	* additionalClasspathElements: specify additional classpath elements. (_e.g._ a jar file) This value should be a list of relative paths from project/targetModule. Elements of the list must be separated by a comma ','.
	* systemProperties: specify system properties. This value should be a list of couple property;value, separated by a comma ','. For example, systemProperties=admin=toto,passwd=tata. This define two system properties.
	* outputDirectory: specify a path folder for the output.(default: target/dspot/output)
	* delta: specify the delta value for the assertions of floating-point numbers. If DSpot generates assertions for float, it uses Assert.assertEquals(expected, actual, delta). This property specify the delta value.(default: 0.1)
	* excludedClasses: specify the full qualified name of excluded test classes. Each qualified name must be separated by a comma ','. These classes won't be amplified, nor executed during the mutation analysis, if the PitMutantScoreSelector is used.This property can be valued by a regex.
	* excludedTestCases: specify the list of test cases to be excluded. Each is the name of a test case, separated by a comma ','.
	* maven.home: specify the maven home directory. This properties is redundant with the command line option `--maven-home`. This property has the priority over the command line. If this property is not specified, nor the command line option `--maven-home,` `DSpot` will first look in both MAVEN_HOME and M2_HOME environment variables. If these variables are not set, DSpot will look for a maven home at default locations /usr/share/maven/, /usr/local/maven-3.3.9/ and /usr/share/maven3/.
	* folderPath: when using the ChangeDetectorSelector or the command-line option-value `--test diff`, you must specify this property. This property should have for value the path to the root of the second version of the project. It is recommended to give an absolute path
	* baseSha: when using the command-line option-value  `--test diff`, which select tests to be amplified according a diff, you must specify this property.This property should have for value the commit sha of the base branch, _i.e._ the version of the to project to be merged.
	* automaticBuilderName: specify the type of automatic builder. This properties is redundant with the command line option `--automatic-builder`. It should have also the same value: (MavenBuilder | GradleBuilder). This property has the priority over the command line.
	* pitVersion: specify the version of PIT to use.(default: 1.3.0)
	* pitTimeout: specify the time out of PIT, if the PitMutantScoreSelector.
	* jvmArgs: specify JVM args to use when executing the test, PIT or other java process
	* filter: specify the filter used by PIT. If you use PitMutantScoreSelector, we recommend you to set this property to your top-most package. This value will allow PIT to mutant all your code. However, if you want to restrict the scope of the mutation, you can specify a custom regex. If you do not specify any value, PIT will use the following filter: <groupId>.<artifactId>.* which might not match your packages.
	* descartesVersion: specify the version of pit-descartes to use.(default: 1.2)
	* descartesMutators: specify the list of descartes mutators to be used. Please refer to the descartes documentation for more details: https://github.com/STAMP-project/pitest-descartes
You can find an example of properties file [here](https://github.com/STAMP-project/dspot/blob/master/dspot/src/test/resources/sample/sample.properties)).

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

The
The default selector is `PitMutantScoreSelector`. This selector is based on [**PIT**](http://pitest.org/), which is a tool to computation mutation analysis. **DSpot** will keep only tests that increase the mutation score.

Following the list of avalaible test selector:

   * `PitMutantScoreSelector`: uses [**PIT**](http://pitest.org/) to computes the mutation score, and selects amplified tests that kill mutants that was not kill by the original test suite.
   * `CloverCoverageSelector`: uses [**OpenClover**](http://openclover.org/) to compute branch coverage, and selects amplified tests that increase it.
   * `JacocoCoverageSelector`: uses [**JaCoCo**](http://www.eclemma.org/jacoco/) to compute instruction coverage and executed paths (the order matters). Selects test that increase the coverage and has unique executed path.
   * `ExecutedMutantSelector`: uses [**PIT**](http://pitest.org/) to computes the number of executed mutants. It uses the number of mutants as a proxy for the instruction coverage. It selects amplfied test that execute new mutants. **WARNING!!** this selector takes a lot of time, and is not worth it, please look at CloverCoverageSelector or JacocoCoverageSelector.
   * `TakeAllSelector`: keeps all amplified tests not matter the quality.
   * `ChangeDetectorSelector`: runs against a second version of the same program, and selects amplified tests that fail. This selector selects only amplified test that are able to show a difference of a behavior betweeen two versions of the same program.

#### Budgetizer

In **DSpot**, the Budgetizer is a way to select the amplified test methods after the input amplification. It allows to keep interesting and discard unwanted amplified test method.

For now, there is two implementation of the Budgetizer:

1. NoBudgetizer: This Budgetizer selects by maximize their distance of string representation among all the input amplified test methods. The number of amplified selected test methods is specified by the command line option `--max-test-amplified`.
2. SimpleBudgetizer: This Budgetizer selects a fair number of amplified test method per Amplifier per test methods, if possible. The total budget is specified by the command line option ``--max-test-amplified`, and is the total number of amplified test methods to keep, _i.e._ it will be divide by the number of Amplifiers and by the number of test methods to be amplified.
Example: We have 2 Amplifiers. We apply them to 2 test methods. For each test methods, amplifiers generate 4 new test methods, totally 8 amplified test methods. If the budget is 6, it will select: 3 amplified test methods per amplifier, and 2 for one test method and 2 for the other.

#### Selector on Diff from GitHub

**DSpot** can perform amplification according to a diff, from github. This is useful to enhance existing test suite on pull request for instance.

**DSpot** will select existing test classes according to a diff as follow:

1. If any test class has been modified between the two versions, **DSpot** selects them.
2. If there is not, **DSpot** selects test cases (and so, their test classes) according to the nem. If a test contain the name of a modified method, this test is selected.
3. If there is not, **DSpot** analyzes statical the test suite and selects test classes that invoke modified methods. The maximum number of selected test classes is the value of `eu.stamp_project.diff.SelectorOnDiff.MAX_NUMBER_TEST_CLASSES`, randomly.

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

