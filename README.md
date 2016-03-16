DSpot
=====
DSpot automatically generates new JUnit tests from a existing test suites. For this, DSpot apply transformation operators on existing tests and selected the new tests  whit coverage criteria such as branch coverage or call graph coverage.


Transformation Operators
------------------

- Literal transformation: numerical value i is transformed in four ways: i+1, i−1, i×2, i÷2.

- Statement Duplication: a no assert statement is duplicate. 

- Statement Delete: a no assert statement is delete.

- Add a new method call: a new method call is add. The method is declared in a class used by the test. 

## Usage
```sh
mvn package
java -cp target/dspot-1.0.0-jar-with-dependencies.jar fr.inria.diversify.dspot.DSpot conf.properties
```

The properties file contains the configuration:
```properties
#project root directory
project=example-commons-collections/commons-collections
#application class root directory (src/main/java by default)
scr=src/main/java
#test class root directory (src/test/java by default)
testScr=src/test/java
#result directory
result=example-commons-collections/result
#target test class for DSpot
testClass=org.apache.commons.collections4.map.SingletonMapTest
```


Example
-------
Run DSpot on test class SingletonMapTest from commons-collections. 

```sh
sh example.sh
```