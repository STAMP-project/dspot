DSpot [![Build Status](https://travis-ci.org/STAMP-project/dspot.svg?branch=master)](https://travis-ci.org/STAMP-project/dspot)[![Coverage Status](https://coveralls.io/repos/github/STAMP-project/dspot/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/dspot?branch=master)
=====================================================================================================================

### What is Dspot?

DSpot automatically generates new JUnit tests from a existing test suites.
- Input: DSpot take as input a project with an existing test suite.(JUnit test).
- Output: DSpot produce new test suite, with existing test and generated test.
- For this, DSpot apply transformation operators on existing tests in order to create new observation points of the state of the system. Those observation points are used to generate new assertion statement. Generated tests are then selected according a specific criteria, such as the branch coverage or mutation score.

### Compile and Build

Retrieve the project:
```
git clone http://github.com/STAMP-project/dspot.git
```

Then, install dspot by running the provided script:
```
cd dspot
chmod +x install.sh
./install.sh
```

### Running example

You can run the provide example by running `fr.inria.diversify.Main` from your IDE, or with
```
mvn exec.java -Dexec.mainClass="fr.inria.diversify.Main"
```

DSpot will be run on the example (see _src/test/resources/test-projects/_) with one amplifier (`TestDataMutator`, which change literals) and the generation of assertion.
It will print on the standard output the class `example.TestSuiteExample` with amplified tests cases.

DSpot read information about the project from a properties file. Here the properties file used for the example: (see _src/test/resources/test-projects/test-projects.properties) 
```properties
#relative path to the project root from dspot project
project=src/test/resources/test-projects
#relative path to the source project from the project properties
src=src/main/java/
#relative path to the test source project from the project properties
testSrc=src/test/java
#java version used
javaVersion=8
#filter used to amplify specific test cases
filter=example
#path to the output folder
result=dspot-out/
```

### Running on your own project

Such as the example you can run dspot on your own project by running the `fr.inria.diversify.Main` from you IDE by specifying the path to your properties file, or with:
```
mvn exec.java -Dexec.mainClass="fr.inria.diversify.Main" -Dexec.args="<pathToPropertiesFile>"
```

### API

The whole procedure of amplification is done by the `fr.inria.diversify.dspot.DSpot` object. 
You must at least provide the path to the properties file of your project at the construction.
You can specify how many times each of amplifiers will be apply to test cases (default 3).
You can specify which amplifiers (as a list) you want to use. By default, DSpot uses: 

    * TestDataMutator: which transform literals.
    * TestMethodCallAdder: which duplicate an existing method call in the test case.
    * TestMethodCallRemover: which remove a method call in the test case.
    * StatementAdderOnAssert: which will add call of accessible method on existing object and create new instance. The parameter for those method call
     are: null, simple value such as 1 and a random one.
    * StatementAdd: which reuse existing object and return value to add method call of accessible method.

##### Amplifiers

You can implement you own amplifier by implementating the `fr.inria.diversify.dspot.amp.Amplifier` interface and giving it to DSpot.


### Licence

DSpot is published under LGPL-3.0 (see [Licence.md](https://github.com/STAMP-project/dspot/blob/master/Licence.md) for further details).
