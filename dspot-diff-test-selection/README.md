[![Build Status](https://travis-ci.org/STAMP-project/diff-test-selection.svg?branch=master)](https://travis-ci.org/STAMP-project/diff-test-selection) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.stamp-project/diff-test-selection/badge.svg)](https://mavenbadges.herokuapp.com/maven-central/eu.stamp-project/diff-test-selection)
# diff-test-selection

Diff-Test-Selection aims at selecting the subset of test classes and methods that execute the changed code between two versions of the same program. It relies on Clover and Maven to compute the coverage and returns this subset of tests.

Diff-Test-Selection is implemented as a maven plugin that directly works from the command line, without modifying your `pom.xml`. See below for more details.

## Usage

We advise you to use the maven plugin. Note that this does not require modifying your `pom.xml`.

You can run it with:

```shell
mvn clean eu.stamp-project:dspot-diff-test-selection:list -Dpath-dir-second-version="<pathToSecondVersion>"
```

at the root of your project, where your `pom.xml` is.

Only the command-line argument `path-dir-second-version` is mandatory, see below for more information.

## Properties

* `path-dir-second-version` \[mandatory\]: the path of the second version of the program (can be relative from the root of the project) 

* `output-format`: the kind of report you want to generate. Value: (CSV) (default: CSV)

* `output-path`: the path of the output. The output is dependent of the `report` property  (can be relative from the root of the project)

* `module`: the relative path of the targeted module from the root of the project.

## Running Example

We provide an example to try the this plugin. 

First clone this repository.

```bash
git clone https://github.com/STAMP-project/dspot.git
```

Then, go to the test resources of `diff-test-selection`:

```bash
cd dspot-diff-test-selection/src/test/resources/
```

In this folder, you have two versions of the same program: `tavern` and `tavern-refactor`.

Now, we will execute `diff-test-selection` on tavern and selects the subset of test that execute the changes:

```bash
cd tavern
mvn clean eu.stamp-project:dspot-diff-test-selection:list -Dpath-dir-second-version=../tavern-refactor
```

You should observe the following on the standard output:

```text
[INFO] Saving result in /home/bdanglot/workspace/dspot/dspot-diff-test-selection/src/test/resources/tavern/testsThatExecuteTheChange.csv ...
[INFO] fr.inria.stamp.MainTest;test
[INFO] Writing Coverage in /home/bdanglot/workspace/dspot/dspot-diff-test-selection/src/test/resources/tavern/testsThatExecuteTheChange_coverage.csv
[INFO] fr.inria.stamp.tavern.Seller;12;13
```

This means that test method `fr.inria.stamp.MainTest#test` is the only one that executes the changes introduced by the refactoring.

Two files are produced `testsThatExecuteTheChange.csv` and `testsThatExecuteTheChange_coverage.csv` which are respectively the subset of test classes and their test methods that execute the changes in a CSV format, and the changed line coverage of each test class.

## Use case example with DSpot

In this section, we present a use case example that uses DSpot and `dspot-diff-test-selection` in order to detect some regression introduced in the changes. This is meant to be used in continuous integration.

First, let's have a look to the test resources of `dspot-diff-test-selection`:

```bash
cd dspot-diff-test-selection/src/test/resources/
``` 

In this folder, you have two versions of the same program: `tavern` and `tavern-refactor`.

Let's consider the first one, `tavern` as the `master` branch and `tavern-refactor` as a refactor branch that a developer created. This developer wants to merge its changes in a pull request.
The CI is triggered when the pull request is opened. Typically, the CI will execute the test suite and a bunch of scripts to verify that the program is correct.

Here, we can enhance this verification with `DSpot` and `dspot-diff-test-selection` as follow:

1. We compute the subset of test classes and their test methods that execute the changes with `dspot-diff-test-selection`.
2. We amplify the selected test methods using `DSpot` and the `ChangeDetectorSelector`.
3. The amplified test methods are test methods that pass on the master but fail on the refactor branch, meaning that they catch the behavioral change.

Since the proposed change is a refactoring, and thus a refactoring should not modify the behavior of the program, it means that the changes contains a regression.

This is done with one single command line as follow:

```bash
mvn clean eu.stamp-project:dspot-diff-test-selection:list \
-Dpath-dir-second-version="../tavern-refactor" \
eu.stamp-project:dspot-maven:amplify-unit-tests \
-Dpath-to-test-list-csv=testsThatExecuteTheChange.csv \
-Dverbose -Dtest-criterion=ChangeDetectorSelector \
-Dabsolute-path-to-second-version=`pwd`/../tavern-refactor \
-Damplifiers=NumberLiteralAmplifier -Diteration=2
```

This results in on the standard output:

```text
======= REPORT =======
5 amplified test fails on the new versions.
testlitNum18litNum149(fr.inria.stamp.MainTest): expected:<Player{gold=[0, items=[Potion]]}> but was:<Player{gold=[2147483647, items=[]]}>
testlitNum14litNum132(fr.inria.stamp.MainTest): expected:<...ayer{gold=0, items=[[Potion]]}> but was:<...ayer{gold=0, items=[[]]}>
testlitNum19litNum169(fr.inria.stamp.MainTest): expected:<Player{gold=[0, items=[Potion]]}> but was:<Player{gold=[-2147483648, items=[]]}>
testlitNum12litNum179(fr.inria.stamp.MainTest): expected:<Player{gold=[0, items=[Potion]]}> but was:<Player{gold=[2147483647, items=[]]}>
testlitNum13litNum197(fr.inria.stamp.MainTest): expected:<Player{gold=[0, items=[Potion]]}> but was:<Player{gold=[-2147483648, items=[]]}>
```

This means that DSpot obtained 5 amplified test methods that detect the regression.  

Note: The `ChangeDetectorSelector` keeps amplified test methods that pass on one version of the program but fail on another one, _c.f._  `DSpot`'s[`README.md`](https://github.com/STAMP-project/dspot/blob/master/README.md) for more information.  

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

Diff-Test-Selection is published under LGPL-3.0 (see [Licence.md](https://github.com/STAMP-project/testrunner/blob/master/LICENSE) for further details).

### Funding

Diff-Test-Selection is funded by research project STAMP (European Commission - H2020)
![STAMP - European Commission - H2020](docs/logo_readme_md.png)
