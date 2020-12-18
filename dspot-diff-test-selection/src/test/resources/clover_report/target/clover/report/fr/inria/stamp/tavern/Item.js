var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":23,"id":0,"methods":[{"el":17,"sc":2,"sl":14},{"el":22,"sc":2,"sl":19}],"name":"Item","sl":8}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_0":{"methods":[{"sl":14},{"sl":19}],"name":"test","pass":true,"statements":[{"sl":15},{"sl":16},{"sl":21}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [0], [0], [0], [], [], [0], [], [0], [], []]
