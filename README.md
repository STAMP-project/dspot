# DSpot

[![Build Status](https://travis-ci.org/STAMP-project/dspot.svg?branch=master)](https://travis-ci.org/STAMP-project/dspot) [![Coverage Status](https://coveralls.io/repos/github/STAMP-project/dspot/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/dspot?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.stamp-project/dspot/badge.svg)](https://mavenbadges.herokuapp.com/maven-central/eu.stamp-project/dspot)

DSpot is a tool that generates missing assertions in JUnit tests.
DSpot takes as input a Java project with an existing test suite.
DSpot generates new test cases from existing ones and write them on disk. 
DSpot supports Java projects built with Maven and Gradle (see the `--automatic-builder` option)

DSpot ecosystem:

* [Jenkins plugin](https://github.com/STAMP-project/dspot-jenkins-plugin)
* [Eclipse plugin](https://github.com/STAMP-project/stamp-ide)
* Travis integration: TBD

## Getting started

### Prerequisites

You need Java and Maven.

DSpot uses the environment variable MAVEN_HOME, ensure that this variable points to your maven installation. Example:
```
export MAVEN_HOME=path/to/maven/
```

DSpot uses maven to compile, and build the classpath of your project. The environment variable JAVA_HOME must point to a valid JDK installation (and not a JRE).

### Releases

We advise you to start by downloading the latest release, see <https://github.com/STAMP-project/dspot/releases>.

## Usage

### First Tutorial

After having downloaded DSpot (see the previous section), you can run the provided example by running
`eu.stamp_project.Main` from your IDE, or with

```
java -jar target/dspot-LATEST-jar-with-dependencies.jar --example
```

replacing `LATEST` by the latest version of DSpot, _e.g._ 2.0.0 would give :
 `dspot-2.0.0-jar-with-dependencies.jar`

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
# Use commas to separate multiple arguments
jvmArgs=-Xmx2048m,-Xms1024m
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

```bash
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --path-to-properties dspot.properties
```

Amplify a specific test class
```bash
java -jar /path/to/dspot-*-jar-with-dependencies.jar eu.stamp_project.Main --path-to-properties dspot.properties --test my.package.TestClass
```
Amplify specific test classes according to a regex
```bash
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --path-to-properties dspot.properties --test my.package.*
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --path-to-properties dspot.properties --test my.package.Example*
```

Amplify a specific test method from a specific test class
```bash
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --path-to-properties dspot.properties --test my.package.TestClass --cases testMethod
```

### Maven plugin usage

You can execute DSpot using the maven plugin. You can use this plugin on the command line as the jar:

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

### Command line options

```
Usage: java -jar target/dspot-<version>-jar-with-dependencies.jar
                          [(-p|--path-to-properties) <./path/to/myproject.properties>] [(-a|--amplifiers) Amplifier1:Amplifier2:...:AmplifierN ] [(-i|--iteration) <iteration>] [(-s|--test-criterion) <PitMutantScoreSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector>] [--pit-output-format <XML | CSV>] [--budgetizer <RandomBudgetizer | TextualDistanceBudgetizer | SimpleBudgetizer>] [--max-test-amplified <integer>] [(-t|--test) my.package.MyClassTest | all1:my.package.MyClassTest | all2:...:my.package.MyClassTest | allN ] [(-c|--test-cases) test-cases1:test-cases2:...:test-casesN ] [(-o|--output-path) <output-path>] [--clean] [(-m|--path-pit-result) <./path/to/mutations.csv>] [--target-one-test-class] [--descartes] [--gregor] [--automatic-builder <MavenBuilder | GradleBuilder>] [--maven-home <path to maven home>] [--random-seed <long integer>] [--time-out <long integer>] [--verbose] [--with-comment] [--no-minimize] [--working-directory] [--generate-new-test-class] [--keep-original-test-methods] [--use-maven-to-exe-test] [--allow-path-in-assertions] [--execute-test-parallel-with-number-processors <execute-test-parallel-with-number-processors>] [-e|--example] [-h|--help]

  [(-p|--path-to-properties) <./path/to/myproject.properties>]
        [mandatory] specify the path to the configuration file (format Java
        properties) of the target project (e.g. ./foo.properties).

  [(-a|--amplifiers) Amplifier1:Amplifier2:...:AmplifierN ]
        [optional] specify the list of amplifiers to use. By default, DSpot does
        not use any amplifiers (None) and applies only assertion amplification.
        Possible values are: 
        		 - MethodAdd
        		 - MethodRemove
        		 - TestDataMutator
        		 - MethodGeneratorAmplifier
        		 - ReturnValueAmplifier
        		 - StringLiteralAmplifier
        		 - NumberLiteralAmplifier
        		 - BooleanLiteralAmplifier
        		 - CharLiteralAmplifier
        		 - AllLiteralAmplifiers
        		 - NullifierAmplifier
        		 - None
        (default: None)

  [(-i|--iteration) <iteration>]
        [optional] specify the number of amplification iterations. A larger
        number may help to improve the test criterion (e.g. a larger number of
        iterations may help to kill more mutants). This has an impact on the
        execution time: the more iterations, the longer DSpot runs. (default: 3)

  [(-s|--test-criterion) <PitMutantScoreSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector>]
        [optional] specify the test adequacy criterion to be maximized with
        amplification.
        Possible values are: 
        		 - PitMutantScoreSelector
        		 - JacocoCoverageSelector
        		 - TakeAllSelector
        		 - ChangeDetectorSelector
        (default: PitMutantScoreSelector)

  [--pit-output-format <XML | CSV>]
        [optional] specify the Pit output format.
        Possible values are: 
        		 - XML
        		 - CSV
        (default: XML)

  [--budgetizer <RandomBudgetizer | TextualDistanceBudgetizer | SimpleBudgetizer>]
        [optional] specify a Bugdetizer.
        Possible values are: 
        		 - RandomBudgetizer
        		 - TextualDistanceBudgetizer
        		 - SimpleBudgetizer
        (default: RandomBudgetizer)

  [--max-test-amplified <integer>]
        [optional] specify the maximum number of amplified tests that dspot
        keeps (before generating assertion) (default: 200)

  [(-t|--test) my.package.MyClassTest | all1:my.package.MyClassTest | all2:...:my.package.MyClassTest | allN ]
        [optional] fully qualified names of test classes to be amplified. If the
        value is all, DSpot will amplify the whole test suite. You can also use
        regex to describe a set of test classes. By default, DSpot selects all
        the tests (value all). (default: all)

  [(-c|--test-cases) test-cases1:test-cases2:...:test-casesN ]
        specify the test cases to amplify

  [(-o|--output-path) <output-path>]
        [optional] specify the output folder (default: target/dspot/output)

  [--clean]
        [optional] if enabled, DSpot will remove the out directory if exists,
        else it will append the results to the exist files.

  [(-m|--path-pit-result) <./path/to/mutations.csv>]
        [optional, expert mode] specify the path to the .xml or .csv of the
        original result of Pit Test. If you use this option the selector will be
        forced to PitMutantScoreSelector

  [--target-one-test-class]
        [optional, expert] enable this option will make DSpot computing the
        mutation score of only one test class (the first pass through --test
        command line option)

  [--descartes]
        Enable the descartes engine for Pit Mutant Score Selector.

  [--gregor]
        Enable the gregor engine for Pit Mutant Score Selector.

  [--automatic-builder <MavenBuilder | GradleBuilder>]
        [optional] specify the automatic builder to build the project (default:
        )

  [--maven-home <path to maven home>]
        specify the path to the maven home

  [--random-seed <long integer>]
        specify a seed for the random object (used for all randomized operation)
        (default: 23)

  [--time-out <long integer>]
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

  [--keep-original-test-methods]
        If enabled, DSpot keeps original test methods of the amplified test
        class.

  [--use-maven-to-exe-test]
        If enabled, DSpot will use maven to execute the tests.

  [--allow-path-in-assertions]
        If enabled, DSpot will generate assertions for values that seems like to
        be paths.

  [--execute-test-parallel-with-number-processors <execute-test-parallel-with-number-processors>]
        [optional] If enabled, DSpot will execute the tests in parallel. For
        JUnit5 tests it will use the number of given processors (specify 0 to
        take the number of available core processors). For JUnit4 tests, it will
        use the number of available CPU processors (given number of processors
        is ignored). (default: 0)

  [-e|--example]
        run the example of DSpot and leave

  [-h|--help]
        show this help
```

Each command line options is translated into an option for the maven plugin. You must prefix each of them with `-D`. Examples:

    * `--path-to-properties dspot.properties` gives `-Dpath-to-properties=dspot.properties`
    * `--test my.package.MyTestClass1:my.package.MyTestClass2` gives `-Dtest=my.package.MyTestClass1,my.package.MyTestClass2`
    * `--output-path output` gives `-Doutput-path=output`
    
For options that take list, the used separator is a comma `,`, whatever the platform you use.

### Specific options for dspot-maven

* `path-to-test-list-csv`: Enable the selection of the test to be amplified from a csv file.
    This parameter is a path that must point to a csv file that list test classes and their test methods in the following format:
    test-class-name;test-method-1;test-method-2;test-method-3;...
    If this parameter is used, DSpot will ignore the value used in the parameter test and cases
    It is recommended to use an absolute path
    Such files can be computed by the dedicated module [dspot-diff-test-selection](https://github.com/STAMP-project/dspot/tree/master/dspot-diff-test-selection).

* `path-to-second-version`: Allows to specify the path to the second version through command line, rather than using properties file.
    This parameter is the same than [ConstantsProperties#PATH_TO_SECOND_VERSION](https://github.com/STAMP-project/dspot/blob/master/dspot/src/main/java/eu/stamp_project/utils/program/ConstantsProperties.java#L97)
    If this parameter is used, DSpot will ignore the value used in the properties file.
    It is recommended to use an absolute path.

    To be used, maven options must be precede by a `-D` and must be separated from their value with `=`, _e.g._ `-Dpath-to-test-list-csv=testsThatExecuteTheChange.csv` 

### Configuration

Here is the list of configuration properties of DSpot:

* Required properties
	* `project`: specify the path to the root of the project. This path can be either absolute (recommended) or relative to the working directory of the DSpot process. We consider as root of the project folder that contain the top-most parent in a multi-module project.
* Optional properties
	* `targetModule`: specify the module to be amplified. This value must be a relative path from the property project. If your project is multi-module, you must use this property because DSpot works at module level.
	* `src`: specify the relative path from project/targetModule of the folder that contain sources (.java).(default: src/main/java/)
	* `testSrc`: specify the relative path from project/targetModule of the folder that contain test sources (.java).(default: src/test/java/)
	* `classes`: specify the relative path from project/targetModule of the folder that contain binaries of the source program (.class).(default: target/classes/)
	* `testClasses`: specify the relative path from project/targetModule of the folder that contain binaries of the test source program (.class).(default: target/test-classes/)
	* `additionalClasspathElements`: specify additional classpath elements. (_e.g._ a jar file) This value should be a list of relative paths from project/targetModule. Elements of the list must be separated by a comma ','.
	* `systemProperties`: specify system properties. This value should be a list of couple property;value, separated by a comma ','. For example, systemProperties=admin=toto,passwd=tata. This define two system properties.
	* `outputDirectory`: specify a path folder for the output.
	* `delta`: specify the delta value for the assertions of floating-point numbers. If DSpot generates assertions for float, it uses Assert.assertEquals(expected, actual, delta). This property specify the delta value.(default: 0.1)
	* `excludedClasses`: specify the full qualified name of excluded test classes. Each qualified name must be separated by a comma ','. These classes won't be amplified, nor executed during the mutation analysis, if the PitMutantScoreSelector is used.This property can be valued by a regex.
	* `excludedTestCases`: specify the list of test cases to be excluded. Each is the name of a test case, separated by a comma ','.
	* `mavenHome`: specify the maven home directory. This property is redundant with the command line option `--maven-home`. This property has the priority over the command line. If this property is not specified, nor the command line option `--maven-home,` `DSpot` DSpot will first look in both MAVEN_HOME and M2_HOME environment variables. If these variables are not set, DSpot will look for a maven home at default locations /usr/share/maven/, /usr/local/maven-3.3.9/ and /usr/share/maven3/.
	* `mavenPreGoals`: specify pre goals to run before executing test with maven.This property will used as follow: the elements, separated by a comma,must be valid maven goals and they will be placed just before the "test" goal, _e.g._maven.pre.goals=preGoal1,preGoal2 will give "mvn preGoal1 preGoal2 test"
	* `pathToSecondVersion`: when using the ChangeDetectorSelector, you must specify this property. This property should have for value the path to the root of the second version of the project. It is recommended to give an absolute path
	* `automaticBuilderName`: specify the type of automatic builder. This properties is redundant with the command line option `--automatic-builder`. It should have also the same value: (MavenBuilder | GradleBuilder). This property has the priority over the command line.
	* `pitVersion`: specify the version of PIT to use.(default: 1.4.0)
	* `jvmArgs`: specify JVM args to use when executing the test, PIT or other java process. This arguments should be a list, separated by a comma ',', _e.g._ jvmArgs=Xmx2048m,-Xms1024m',-Dis.admin.user=admin,-Dis.admin.passwd=$2pRSid#
	* `pitFilterClassesToKeep`: specify the filter of classes to keep used by PIT. If you use PitMutantScoreSelector, we recommend you to set this property to your top-most package. This value will allow PIT to mutant all your code. However, if you want to restrict the scope of the mutation, you can specify a custom regex. If you do not specify any value, DSpot will compute a filter of classes to keep on the fly, trying to match the most of your classes, _i.e._ your top-most package.
	* `descartesVersion`: specify the version of pit-descartes to use.(default: 1.2.4)
	* `descartesMutators`: specify the list of descartes mutators to be used separated by comma. Please refer to the descartes documentation for more details: https://github.com/STAMP-project/pitest-descartes
You can find an example of properties file [here](https://github.com/STAMP-project/dspot/blob/master/dspot/src/test/resources/sample/sample.properties)).

#### Amplifiers (-a | --amplifiers)

By default, **DSpot** uses no amplifier because the simplest amplification that can be done is the generation of assertions on existing tests, _i.e._ it will improve the oracle and the potential of the test suite to capture bugs.

However, **DSpot** provide different kind of `Amplifier`:

   * `StringLiteralAmplifier`: modifies string literals: remove, add and replace one random char, generate random string and empty string
   * `NumberLiteralAmplifier`: modifies number literals: replace by boundaries (_e.g._ Integer_MAX_VALUE for int), increment and decrement values
   * `CharLiteralAmplifier`: modifies char literals: replace by special chars (_e.g._ '\0')
   * `BooleanLiteralAmplifier`: modifies boolean literals: nagate the value
   * `AllLiteralAmplifiers`: combines all literals amplifiers, _i.e._ StringLiteralAmplifier, NumberLiteralAmplifier, CharLiteralAmplifier and BooleanLiteralAmplifier
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

#### Supported Features

DSpot supports:

* JUnit3
* JUnit4
* JUnit5

and

* Google Truth assertions

However, DSpot detects the used test framework at test class level. 

Please, do not amplify test classes that mix test frameworks (test methods in JUnit4 and JUnit5 within the same test class.)

If you have such test class, please amplify the different test framework separately.

## Contributing

DSpot is licensed under LGPLv3. Contributors and pull requests are welcome :-).
Tell us what you think and what you expect in the next release using the [STAMP feedback form](https://www.stamp-project.eu/view/main/betatestingsurvey/).
As a recognition for your useful feedback, you might receive a limited edition “STAMP Software Test Pilot” gift and be added as a STAMP contributor.
This offer is limited to the beta testers interacting with the STAMP project team, by 31 September 2019. 
You will be contacted individually for a customized gift and for contribution opportunities.

For more information on development, see the dedicated [README-developers.md](https://github.com/STAMP-project/dspot/blob/master/README-developers.md)

### Acknowledgement

Dspot is funded by [EU H2020 research project STAMP](https://www.stamp-project.eu/).

