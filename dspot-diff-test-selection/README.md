[![Build Status](https://travis-ci.org/STAMP-project/diff-test-selection.svg?branch=master)](https://travis-ci.org/STAMP-project/diff-test-selection) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.stamp-project/diff-test-selection/badge.svg)](https://mavenbadges.herokuapp.com/maven-central/eu.stamp-project/diff-test-selection)
# diff-test-selection

Diff Test Selection aims at selecting the subset of test classes and their test methods that execute changes between two versions of the same program.

This artifact relies on Clover and Maven to compute the coverage and returns this subset.

We provide a maven plugin that allows you to execute Diff Test Selection directly from the command line, without modifying your `pom.xml`. See below for more details.

## Usage

We advice you to use the maven plugin. This plugin is meant to be run without modifying your `pom.xml`.

You can run it with:

```shell
mvn clean eu.stamp-project:dspot-diff-test-selection:list -Dpath-dir-second-version="<pathToSecondVersion>"
```

at the root (where your `pom.xml` is) of your project.

Only the property `path-dir-second-version` is mandatory, see below for more information.

## Properties

* `path-dir-second-version` \[mandatory\]: the path (can be relative from the root of the project) of the second version of the program. You should obtain it by applying the changes on this project.

* `output-format`: the kind of report you want to generate. Value: (CSV) (default: CSV)

* `output-path`: the path (can be relative from the root of the project) of the output. The output is dependent of the `report` property

* `path-to-diff`: the path (can be relative from the root of the project) of a `.diff` file containing the changes. If no diff is provided, Diff Test Selection will use unix diff command to compute it.

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

You should observe something like:

```text
[INFO] Saving result in /home/bdanglot/workspace/dspot/dspot-diff-test-selection/src/test/resources/tavern/testsThatExecuteTheChange.csv ...
[INFO] fr.inria.stamp.MainTest;test
[INFO] Writing Coverage in /home/bdanglot/workspace/dspot/dspot-diff-test-selection/src/test/resources/tavern/testsThatExecuteTheChange_coverage.csv
[INFO] fr.inria.stamp.tavern.Seller;12;13
```

On the standard output.

This means that `fr.inria.stamp.MainTest#test` executes the changes introduced by the refactor.

You have also two files `testsThatExecuteTheChange.csv` and `testsThatExecuteTheChange_coverage.csv` which are respectively the subset of test classes and their test methods that execute the changes in a CSV format, and the changed line coverage of each test class.

## Use case example with DSpot

In this section, we expose an use case example that uses DSpot and `dspot-diff-test-selection` in order to detect regression introduced in the changes. This is meant to be used in the CI.

First, let's have a look to the test resources of `dspot-diff-test-selection`:

```bash
cd dspot-diff-test-selection/src/test/resources/
``` 

In this folder, you have two versions of the same program: `tavern` and `tavern-refactor`.

Let's consider the first one, `tavern` as the `master` branch and `tavern-refactor` as a refactor branch that a developer created. This developer wants to merge its changes with a pull request for example.

Imagine that the CI is triggered when the pull request is opened. Typically, the CI will execute the test suite and a bunch of scripts to verify that the program is correct.

Here, we can enhance this verification with `DSpot` and `dspot-diff-test-selection` as follow:

1. We compute the subset of test classes and their test methods that execute the changes with `dspot-diff-test-selection`.
2. We amplify the selected test methods using `DSpot` and the `ChangeDetectorSelector`.
3. The amplified test methods are test methods that pass on the master but fail on the refactor branch, meaning that catching the behavioral change.

Since the proposed changes are a refactoring, and thus a refactoring should not modify the behavior of the program, it seems that the changes contains a regression.

This is done with one single command line as follow:

```bash
mvn clean eu.stamp-project:dspot-diff-test-selection:list \
    -Dpath-dir-second-version="../tavern-refactor" \
    eu.stamp-project:dspot-maven:amplify-unit-tests \
    -Dpath-to-test-list-csv=testsThatExecuteTheChange.csv \
    -Dverbose -Dtest-criterion=ChangeDetectorSelector \
    -Dpath-to-properties=src/main/resources/tavern.properties \
    -Damplifiers=NumberLiteralAmplifier -Diteration=2
```

This command line use both maven plugins of `DSpot` and `dspot-diff-test-selection`.

At the end, you should obtain something like:

```text
======= REPORT =======
5 amplified test fails on the new versions.
testlitNum10litNum158(fr.inria.stamp.MainTest): expected:<Seller{gold=[-2147483548], items=[Potion]}> but was:<Seller{gold=[100], items=[Potion]}>
testlitNum11litNum117(fr.inria.stamp.MainTest): expected:<...ayer{gold=0, items=[[Potion]]}> but was:<...ayer{gold=0, items=[[]]}>
testlitNum16litNum107(fr.inria.stamp.MainTest): expected:<Seller{gold=[-2147483548], items=[Potion]}> but was:<Seller{gold=[100], items=[Potion]}>
testlitNum15litNum155(fr.inria.stamp.MainTest): expected:<Seller{gold=[-2147483549], items=[Potion]}> but was:<Seller{gold=[100], items=[Potion]}>
testlitNum9litNum210(fr.inria.stamp.MainTest): expected:<Seller{gold=[-2147483549], items=[Potion]}> but was:<Seller{gold=[100], items=[Potion]}>
```

On the standard output. This means that DSpot obtained 5 amplified test methods that detect the regression.  

Note: The `ChangeDetectorSelector` keeps amplified test methods that pass on one version of the program but fail on another one, _c.f._  `DSpot`'s[`README.md`](https://github.com/STAMP-project/dspot/blob/master/README.md) for more infos.  

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
