# diff-test-selection[![Build Status](https://travis-ci.org/STAMP-project/diff-test-selection.svg?branch=master)](https://travis-ci.org/STAMP-project/diff-test-selection)[![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.stamp-project/diff-test-selection/badge.svg)](https://mavenbadges.herokuapp.com/maven-central/eu.stamp-project/diff-test-selection)

A maven plugin that gives the list of test classes, and their test methods, that execute a provided change (as an Unix diff)

## Usage

This plugin is meant to be run without modifying your `pom.xml`.

You can run it with:

```shell
mvn clean eu.stamp-project:diff-test-selection:list -DpathToDiff="<pathToDiff>" -DpathToOtherVersion="<pathToSecondVersion>"
```

at the root (where your `pom.xml` is) of your project.

The two properties `pathToDiff` and `pathToOtherVersion` are mandatory, see below for more information.

## Properties

* `pathToDiff`: the path (can be relative from the root of the project) of a `.diff` file containing the changes.

* `pathToOtherVersion`: the path (can be relative from the root of the project) of the second version of the program. You should obtain it by applying the changes on this project.

* `report`: the kind of report you want to generate. Value: (CSV) (default: CSV)

* `outputPath`: the path (can be relative from the root of the project) of the output. The output is dependent of the `report` property

## Running Example

You can try the plugin on the provided commons-math example (thanks to [bugs-dot-jar](https://github.com/bugs-dot-jar/bugs-dot-jar).

1. clone:
```shell
git clone --recursive https://github.com/STAMP-project/diff-test-selection.git
```
2. build:
```shell
mvn install
```
3. prepare `commons-math` project:
```shell
./src/main/bash/setup-commons-math.sh
```
this script makes a copy of commons-math and apply the provided diff.

4. launch the plugin:
```shell
cd commons-math
mvn clean eu.stamp-project:diff-test-selection:list -DpathToDiff=".bugs-dot-jar/developer-patch.diff" -DpathToOtherVersion="../commons-math_fixed" -Dreport=CSV -DoutputPath="testsThatExecuteTheChange.csv"
```
You will see a build failure, which is normal and not important (this version of `commons-math` has a bug)

5. you will find in `commons-math` the following file: `testsThatExecuteTheChange.csv` with the content
```
org.apache.commons.math.optimization.linear.SimplexSolverTest;testRestrictVariablesToNonNegative;testInfeasibleSolution;testSimplexSolver;testMath272;testModelWithNoArtificialVars;testEpsilon;testSolutionWithNegativeDecisionVariable;testLargeModel;testMath286;testMinimization;testSingleVariableAndConstraint;testTrivialModel;testUnboundedSolution
org.apache.commons.math.optimization.linear.SimplexTableauTest;testInitialization;testSerial;testdiscardArtificialVariables
```

which is the list of full qualified test classes (1rst column), and their test method names that execute the provided diff.

## Support on diff

On Linux, you can obtain a diff easily with:
```shell
diff -ru folder1 folder2 > patch.diff
```
`r` option to be run recursively
`u` option to unified the diff

You can get the `.diff` file from git by redirect the `stdout` of the `git diff` command
```shell
git diff > patch.diff
```

To apply the `.diff` file run:
```shell
patch -p1 <patch.diff
```
at the root of the diff (make sure by checking the path in the diff).


Please, open an issue if you have any question / suggestion. Pull request are welcome! 😃

### Licence

Diff-Test-Selection is published under LGPL-3.0 (see [Licence.md](https://github.com/STAMP-project/testrunner/blob/master/LICENSE) for
further details).

### Funding

Diff-Test-Selection is partially funded by research project STAMP (European Commission - H2020)
![STAMP - European Commission - H2020](docs/logo_readme_md.png)
