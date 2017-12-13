
Hardware mapping
---------------

Core idea: one original test method per board unit:

* knob: number of generated test cases (rational: easier to explain) (linked to the number of iterations of the core loop)
* slider: maximum number of added assertions per generated test cases 
* buttons
    * S button: not used yet
    * M button: not used yet
    * R button: not used yet

Value matrix for the input (data we compute)
* 5x5 values:
   * number of assertions: 1, 2, 3, 4, 5
   * number of generated test cases: 10, 20, 30, 40, 50 (can be modified if required) 

Visualization 
------------

Core idea: one single graph with ten columns. in each column we have two bars, one coverage and one for number of killed mutants. (We can have different scales per column).

Visualization mapping:
* Per-test Absolute Coverage (mutant coverage = executed mutant)
* Per-test Absolute number of killed mutants

See real time coverage: https://github.com/SpoonLabs/CoCoSpoon


Folder structure
---------------

All files are pushed in https://github.com/STAMP-project/dspot-experiments/
```
visualization/
visualization/2017-11-12
visualization/2017-11-12/javapoet
visualization/2017-11-12/javapoet/testmethod
visualization/2017-11-12/javapoet/testmethod/1.json
visualization/2017-11-12/javapoet/testmethod/2.json
visualization/2017-11-12/javapoet/testmethod/3.json
```


JSON structure
---------------

One JSON file per amplification step =  one JSON per generated test method (even the intermediate amplification steps):

One file contains:
```js
{
    "project_name" : "javapoet",
    "random_seed" : 484764,
    "input_configuration" : {
           "number_generated_tests" : 20,
           "max_number_assertions" : 5,
    },
    "github_link_to_original_code" : "http://github.com/foo/bar/blob/master/src/test/java/org/test/MyClass.java#456",  
    "original_test_method" : {
        "class" : "org.test.myClass", 
        "method" : "mymethod",
        "source_code" : "public void mymethod() { ... }"
    }
    "before_test_method" : {
        "class" : "org.test.myClass", 
        "method" : "mymethod_Add2",
        "source_code" : "public void mymethod_Add2() { ... }"
    }
    "after_test_method" : {
        "class" : "org.test.myClass", 
        "method" : "mymethod_Add3"
        "source_code" : "public void mymethod_Add3() {...}"
    }
    // contract:  absolute_killed_mutants == killed_mutant_identifiers.length
    "used_amplifiers" : ["MethodAdd", "MethodRemove", "StatementAdd", "TestData"]
    "covered_jacoco_instructions" : ["8","45", "24"],
    // PIT identifier 
    "absolute_coverage_before" : 45,
    "covered_mutant_identifiers_after" : ["9", "0", "10", "18", "74", "622"],
    "absolute_coverage_after" : 47,
    "absolute_killed_mutants_before" : 23,
    "killed_mutant_identifiers_after" : ["9", "0", "622", "10"],
    "absolute_killed_mutants_after" : 45, 
    "live_mutant_identifiers_after" : ["18", "74"]
}
```



Importer from JSON to database
---------------------------

For sake of performance, we may need an importer fro JSON to database:
CouchDB / Mongodb









