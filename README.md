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

replacing `LATEST` by the latest version of DSpot, _e.g._ `2.2.1` would give :
 `dspot-2.2.1-jar-with-dependencies.jar`

This example is an implementation of the function `chartAt(s, i)` (in `src/test/resources/test-projects/`), which
returns the char at the index _i_ in the String _s_.

In this example, DSpot amplifies the tests of `chartAt(s, i)` with the `FastLiteralAmplifier,`, which modifies literals inside the test and the generation of assertions.

The result of the amplification of charAt consists of 6 new tests, as shown in the output below. These new tests are
written to the output folder specified by configuration property `outputDirectory` (`./target/dspot/output/`).

```
Initial instruction coverage: 30 / 34
88.24%
Amplification results with 5 amplified tests.
Amplified instruction coverage: 34 / 34
100.00%
``` 

### Command Line Usage

You can then execute DSpot by using:

```bash
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --absolute-path-to-project-root <path>
```

Amplify a specific test class
```bash
java -jar /path/to/dspot-*-jar-with-dependencies.jar eu.stamp_project.Main --absolute-path-to-project-root <path> --test my.package.TestClass
```
Amplify specific test classes according to a regex
```bash
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --absolute-path-to-project-root <path> --test my.package.*
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --absolute-path-to-project-root <path> --test my.package.Example*
```

Amplify a specific test method from a specific test class
```bash
java -jar /path/to/dspot-LATEST-jar-with-dependencies.jar --absolute-path-to-project-root <path> --test my.package.TestClass --cases testMethod
```

### Command line options

```
Usage: eu.stamp_project.Main [-hvV] [--allow-path-in-assertions] [--clean] [--example] [--execute-test-parallel]
                             [--generate-new-test-class] [--gregor-mode] [--keep-original-test-methods] [--restful]
                             [--smtp-auth] [--target-one-test-class] [--use-maven-to-exe-test]
                             [--use-working-directory] [--with-comment]
                             [--absolute-path-to-project-root=<absolutePathToProjectRoot>]
                             [--absolute-path-to-second-version=<absolutePathToSecondVersionProjectRoot>]
                             [--automatic-builder=<automaticBuilder>] [--cache-size=<cacheSize>]
                             [--collector=<collector>] [--delta=<delta>] [--descartes-mutators=<descartesMutators>]
                             [--descartes-version=<descartesVersion>] [--excluded-classes=<excludedClasses>]
                             [--excluded-test-cases=<excludedTestCases>] [--full-classpath=<dependencies>]
                             [-i=<nbIteration>] [--input-ampl-distributor=<inputAmplDistributor>]
                             [--jvm-args=<JVMArgs>] [--maven-home=<mavenHome>]
                             [--maven-pre-goals-test-execution=<preGoalsTestExecution>]
                             [--max-test-amplified=<maxTestAmplified>] [--mongo-colname=<mongoColName>]
                             [--mongo-dbname=<mongoDbName>] [--mongo-url=<mongoUrl>]
                             [--nb-parallel-exe-processors=<numberParallelExecutionProcessors>]
                             [--output-path=<outputDirectory>] [--path-pit-result=<pathPitResult>]
                             [--path-to-additional-classpath-elements=<additionalClasspathElements>]
                             [--path-to-test-list-csv=<pathToTestListCSV>] [--pit-filter-classes-to-keep=<filter>]
                             [--pit-output-format=<pitOutputFormat>] [--pit-version=<pitVersion>]
                             [--random-seed=<seed>] [--relative-path-to-classes=<pathToClasses>]
                             [--relative-path-to-source-code=<pathToSourceCode>]
                             [--relative-path-to-test-classes=<pathToTestClasses>]
                             [--relative-path-to-test-code=<pathToTestSourceCode>] [--repo-branch=<repoBranch>]
                             [--repo-slug=<repoSlug>] [-s=<selector>] [--smtp-host=<smtpHost>]
                             [--smtp-password=<smtpPassword>] [--smtp-port=<smtpPort>] [--smtp-tls=<smtpTls>]
                             [--smtp-username=<smtpUsername>] [--system-properties=<systemProperties>]
                             [--target-module=<targetModule>] [--time-out=<timeOutInMs>] [-a=<amplifiers>[,
                             <amplifiers>...]]... [-c=<testCases>[,<testCases>...]]... [-t=<testClasses>[,
                             <testClasses>...]]...
  -a, --amplifiers=<amplifiers>[,<amplifiers>...]
                             Specify the list of amplifiers to use. By default, DSpot does not use any amplifiers
                               (None) and applies only assertion amplification. Valid values:
                               MethodDuplicationAmplifier, MethodRemove, FastLiteralAmplifier,
                               MethodAdderOnExistingObjectsAmplifier, ReturnValueAmplifier, StringLiteralAmplifier,
                               NumberLiteralAmplifier, BooleanLiteralAmplifier, CharLiteralAmplifier,
                               AllLiteralAmplifiers, NullifierAmplifier, ArrayAmplifier, None Default value: None
      --absolute-path-to-project-root=<absolutePathToProjectRoot>
                             Specify the path to the root of the project. This path must be absolute.We consider as
                               root of the project folder that contain the top-most parent in a multi-module project.
      --absolute-path-to-second-version=<absolutePathToSecondVersionProjectRoot>
                             When using the ChangeDetectorSelector, you must specify this option. It should have for
                               value the path to the root of the second version of the project. It is recommended to
                               give an absolute path
      --allow-path-in-assertions
                             If enabled, DSpot will generate assertions for values that seems like to be paths. Default
                               value: false
      --automatic-builder=<automaticBuilder>
                             Specify the automatic builder to be used. Valid values: Maven, Gradle Default value: Maven
  -c, --cases, --test-cases, --test-methods=<testCases>[,<testCases>...]
                             Specify the test cases to amplify.
      --cache-size=<cacheSize>
                             Specify the size of the memory cache in terms of the number of store entries Default
                               value: 10000
      --clean                If enabled, DSpot will remove the out directory if exists, else it will append the results
                               to the exist files. Default value: false
      --collector=<collector>
                             Set a collector: MongodbCollector to send info to Mongodb at end process, NullCollector
                               which does nothing.Valid values: NullCollector, MongodbCollector Default value:
                               NullCollector
      --delta=<delta>        Specify the delta value for the assertions of floating-point numbers. If DSpot generates
                               assertions for float, it uses Assert.assertEquals(expected, actual, delta). It specifies
                               the delta value. Default value: 0.1
      --descartes-mutators=<descartesMutators>
                             Specify the list of descartes mutators to be used separated by comma. Please refer to the
                               descartes documentation for more details: https://github.
                               com/STAMP-project/pitest-descartes
      --descartes-version=<descartesVersion>
                             Specify the version of pit-descartes to use. Default value: 1.2.4
      --example              Run the example of DSpot and leave.
      --excluded-classes=<excludedClasses>
                             Specify the full qualified name of excluded test classes. Each qualified name must be
                               separated by a comma ','. These classes won't be amplified, nor executed during the
                               mutation analysis, if the PitMutantScoreSelector is used.This option can be valued by a
                               regex.
      --excluded-test-cases=<excludedTestCases>
                             Specify the list of test cases to be excluded. Each is the name of a test case, separated
                               by a comma ','.
      --execute-test-parallel
                             If enabled, DSpot will execute the tests in parallel. For JUnit5 tests it will use the
                               number of given processors (specify 0 to take the number of available core processors).
                               For JUnit4 tests, it will use the number of available CPU processors (given number of
                               processors is ignored). Default value: false
      --full-classpath=<dependencies>
                             Specify the classpath of the project. If this option is used, DSpot won't use an
                               AutomaticBuilder (e.g. Maven) to clean, compile and get the classpath of the project.
                               Please ensure that your project is in a good shape, i.e. clean and correctly compiled,
                               sources and test sources.
      --generate-new-test-class
                             Enable the creation of a new test class. Default value: false
      --gregor-mode          Enable the gregor engine for Pit Mutant Score Selector. Default value: false
  -h, --help                 Show this help message and exit.
  -i, --iteration=<nbIteration>
                             Specify the number of amplification iterations. A larger number may help to improve the
                               test criterion (e.g. a larger number of iterations may help to kill more mutants). This
                               has an impact on the execution time: the more iterations, the longer DSpot runs. Default
                               value: 1
      --input-ampl-distributor=<inputAmplDistributor>
                             Specify an input amplification distributor.Valid values: RandomInputAmplDistributor,
                               TextualDistanceInputAmplDistributor, SimpleInputAmplDistributor Default value:
                               RandomInputAmplDistributor
      --jvm-args=<JVMArgs>   Specify JVM args to use when executing the test, PIT or other java process. This arguments
                               should be a list, separated by a comma ',', e.g. jvmArgs=Xmx2048m,-Xms1024m',-Dis.admin.
                               user=admin,-Dis.admin.passwd=$2pRSid#
      --keep-original-test-methods
                             If enabled, DSpot keeps original test methods of the amplified test class. Default value:
                               false
      --maven-home=<mavenHome>
                             Specify the maven home directory. If it is not specified DSpot will first look in both
                               MAVEN_HOME and M2_HOME environment variables. If these variables are not set, DSpot will
                               look for a maven home at default locations /usr/share/maven/, /usr/local/maven-3.3.9/
                               and /usr/share/maven3/.
      --maven-pre-goals-test-execution=<preGoalsTestExecution>
                             Specify pre goals to run before executing test with maven.It will be used as follow: the
                               elements, separated by a comma,must be valid maven goals and they will be placed just
                               before the "test" goal, e.g.--maven-pre-goals-test-execution preGoal1,preGoal2 will give
                               "mvn preGoal1 preGoal2 test"
      --max-test-amplified=<maxTestAmplified>
                             Specify the maximum number of amplified tests that dspot keeps (before generating
                               assertion). Default value: 200
      --mongo-colname=<mongoColName>
                             If valid mongo-url and a mongo-dbname are provided, DSpot will submit result to the
                               provided collection name.. Default value: AmpRecords
      --mongo-dbname=<mongoDbName>
                             If a valid mongo-url is provided, DSpot will submit result to the database indicated by
                               this name. Default value: Dspot
      --mongo-url=<mongoUrl> If valid url, DSpot will submit to Mongodb database. Default value: mongodb://localhost:
                               27017
      --nb-parallel-exe-processors=<numberParallelExecutionProcessors>
                             Specify the number of processor to use for the parallel execution.0 will make DSpot use
                               all processors available. Default value: 0
      --output-path, --output-directory=<outputDirectory>
                             specify a path folder for the output. Default value: target/dspot/output/
      --path-pit-result=<pathPitResult>
                             Specify the path to the .xml or .csv of the original result of Pit Test. If you use this
                               option the selector will be forced to PitMutantScoreSelector.
      --path-to-additional-classpath-elements=<additionalClasspathElements>
                             Specify additional classpath elements (e.g. a jar files). Elements of this list must be
                               separated by a comma ','.
      --path-to-test-list-csv=<pathToTestListCSV>
                             Enable the selection of the test to be amplified from a csv file. This parameter is a path
                               that must point to a csv file that list test classes and their test methods in the
                               following format: test-class-name;test-method-1;test-method-2;test-method-3;... If this
                               parameter is used, DSpot will ignore the value used in the parameter test and cases It
                               is recommended to use an absolute path.
      --pit-filter-classes-to-keep=<filter>
                             Specify the filter of classes to keep used by PIT. This allow you restrict the scope of
                               the mutation done by PIT. If this is not specified, DSpot will try to build on the fly a
                               filter that takes into account the largest number of classes, e.g. the topest package.
      --pit-output-format=<pitOutputFormat>
                             Specify the Pit output format.Valid values: XML, CSV Default value: XML
      --pit-version=<pitVersion>
                             Specify the version of PIT to use. Default value: 1.4.0
      --random-seed=<seed>   Specify a seed for the random object (used for all randomized operation). Default value: 23
      --relative-path-to-classes=<pathToClasses>
                             Specify the relative path from --absolute-path-to-project-root/--target-module
                               command-line options that points to the folder that contains binaries of the source (.
                               class). Default value: target/classes/
      --relative-path-to-source-code=<pathToSourceCode>
                             Specify the relative path from --absolute-path-to-project-root/--target-module
                               command-line options that points to the folder that contains sources (.java). Default
                               value: src/main/java/
      --relative-path-to-test-classes=<pathToTestClasses>
                             Specify the relative path from --absolute-path-to-project-root/--target-module
                               command-line options that points to the folder that contains binaries of the test source
                               (.class). Default value: target/test-classes/
      --relative-path-to-test-code=<pathToTestSourceCode>
                             Specify the relative path from --absolute-path-to-project-root/--target-module
                               command-line options that points to the folder that contains test sources (.java).
                               Default value: src/test/java/
      --repo-branch=<repoBranch>
                             Branch name of the submitted repo, This is used by mongodb as a identifier for analyzed
                               repo's submitted data. Default value: UnknownBranch
      --repo-slug=<repoSlug> Slug of the repo for instance Stamp/Dspot. This is used by mongodb as a identifier for
                               analyzed repo's submitted data. Default value: UnknownSlug
      --restful              If true, DSpot will enable restful mode for web Interface. It will look for a pending
                               document in Mongodb with the corresponding slug and branch provided instead of creating
                               a completely new one. Default value: false
  -s, --test-selector, --test-criterion=<selector>
                             Specify the test adequacy criterion to be maximized with amplification. Valid values:
                               PitMutantScoreSelector, JacocoCoverageSelector, TakeAllSelector, ChangeDetectorSelector
                               Default value: PitMutantScoreSelector
      --smtp-auth            Enable this if the smtp host server require auth. Default value: false
      --smtp-host=<smtpHost> Host server name. Default value: smtp.gmail.com
      --smtp-password=<smtpPassword>
                             Password for Gmail, used for submit email at end-process. Default value: Unknown
      --smtp-port=<smtpPort> Host server port. Default value: 587
      --smtp-tls=<smtpTls>   Enable this if the smtp host server require secure tls transport. Default value: false
      --smtp-username=<smtpUsername>
                             Username for Gmail, used for submit email at end-process. Default value: Unknown@gmail.com
      --system-properties=<systemProperties>
                             Specify system properties. This value should be a list of couple property=value, separated
                               by a comma ','. For example, systemProperties=admin=toto,passwd=tata. This defines two
                               system properties.
  -t, --test=<testClasses>[,<testClasses>...]
                             Fully qualified names of test classes to be amplified. If the value is all, DSpot will
                               amplify the whole test suite. You can also use regex to describe a set of test classes.
                               By default, DSpot selects all the tests.
      --target-module=<targetModule>
                             Specify the module to be amplified. This value must be a relative path from value
                               specified by --absolute-path-to-project-root command-line option. If your project is
                               multi-module, you must use this property because DSpot works at module level.
      --target-one-test-class
                             Enable this option will make DSpot computing the mutation score of only one test class
                               (the first pass through --test command line option). Default value: false
      --time-out=<timeOutInMs>
                             Specify the timeout value of the degenerated tests in millisecond. Default value: 10000
      --use-maven-to-exe-test
                             If enabled, DSpot will use maven to execute the tests. Default value: false
      --use-working-directory
                             Enable this option to change working directory with the root of the project. Default
                               value: false
  -v, --verbose              Enable verbose mode of DSpot. Default value: false
  -V, --version              Print version information and exit.
      --with-comment         Enable comment on amplified test: details steps of the Amplification. Default value: false
```
    
For options that take list, the used separator is a comma `,`, whatever the platform you use.

### Maven plugin usage

You can execute DSpot using the maven plugin. For more details, see the dedicated [README](https://github.com/STAMP-project/dspot/blob/master/dspot-maven/README.md).

Each command line options is translated into an option for the maven plugin. You must prefix each of them with `-D`. Examples:

    * `--test my.package.MyTestClass1:my.package.MyTestClass2` gives `-Dtest=my.package.MyTestClass1,my.package.MyTestClass2`
    * `--output-path output` gives `-Doutput-path=output` 

### Configuration

#### Amplifiers (-a | --amplifiers)

By default, **DSpot** uses no amplifier because the simplest amplification that can be done is the generation of assertions on existing tests, _i.e._ it will improve the oracle and the potential of the test suite to capture bugs.

However, **DSpot** provide different kind of `Amplifier`:

   * `StringLiteralAmplifier`: modifies string literals: remove, add and replace one random char, generate random string and empty string
   * `NumberLiteralAmplifier`: modifies number literals: replace by boundaries (_e.g._ Integer_MAX_VALUE for int), increment and decrement values
   * `CharLiteralAmplifier`: modifies char literals: replace by special chars (_e.g._ '\0')
   * `BooleanLiteralAmplifier`: modifies boolean literals: negate the value
   * `ArrayLiteralAmplifier`: modifies array literals: remove and add element in literal, replace literal with empty literal and null
   * `AllLiteralAmplifiers`: combines all literals amplifiers, _i.e._ StringLiteralAmplifier, NumberLiteralAmplifier, CharLiteralAmplifier, BooleanLiteralAmplifier and ArrayLiteralAmplifier
   * `MethodDuplicationAmplifier`: duplicates an existing method call
   * `MethodRemove`: removes an existing method call
   * `MethodAdderOnExistingObjectsAmplifier`: adds a method call, and generate required parameter
   * `ReplacementAmplifier`: replaces a local variable by a generated one
   * `FastLiteralAmplifier`: a faster amplifier for the literals
   * `NullifierAmplifier`: replaces value with null
   * `ArrayAmplifier`: replaces value of arrays 
   * `ReturnValueAmplifier`: creates objects based on the returned value by existing method call
   * `None`: do nothing

#### Test Selectors (-s | --test-criterion)

In **DSpot**, test selectors can be seen as a fitness: it measures the quality of amplified, and keeps only amplified tests that are worthy according to this selector.

The
The default selector is `PitMutantScoreSelector`. This selector is based on [**PIT**](http://pitest.org/), which is a tool to computation mutation analysis. **DSpot** will keep only tests that increase the mutation score.

Following the list of avalaible test selector:

   * `PitMutantScoreSelector`: uses [**PIT**](http://pitest.org/) to computes the mutation score, and selects amplified tests that kill mutants that was not kill by the original test suite.
   * `JacocoCoverageSelector`: uses [**JaCoCo**](http://www.eclemma.org/jacoco/) to compute instruction coverage and executed paths (the order matters). Selects test that increase the coverage and has unique executed path.
   * `TakeAllSelector`: keeps all amplified tests not matter the quality.
   * `ChangeDetectorSelector`: runs against a second version of the same program, and selects amplified tests that fail. This selector selects only amplified test that are able to show a difference of a behavior betweeen two versions of the same program.

#### Input Ampl Distributor

In **DSpot**, the Input Ampl Distributor is a way to select the amplified test methods after the input amplification. It allows to keep interesting and discard unwanted amplified test method.

For now, there is two implementation of the Input Ampl Distributor:

   * `RandomInputAmplDistributor`: This distributor selects randonly amplified test methods.
   * `TextualDistanceInputAmplDistributor`: This distributor selects by maximize their distance of string representation among all the input amplified test methods. The number of amplified selected test methods is specified by the command line option `--max-test-amplified`.
   * `SimpleInputAmplDistributor`: This distributor selects a fair number of amplified test method per Amplifier per test methods, if possible. The total budget is specified by the command line option ``--max-test-amplified`, and is the total number of amplified test methods to keep, _i.e._ it will be divide by the number of Amplifiers and by the number of test methods to be amplified.
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

