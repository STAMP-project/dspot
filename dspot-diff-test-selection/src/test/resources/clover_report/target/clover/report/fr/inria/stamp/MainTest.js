var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":34,"id":50,"methods":[{"el":32,"sc":2,"sl":20}],"name":"MainTest","sl":18}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_0":{"methods":[{"sl":20}],"name":"test","pass":true,"statements":[{"sl":22},{"sl":23},{"sl":25},{"sl":26},{"sl":28},{"sl":30},{"sl":31}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [0], [], [0], [0], [], [0], [0], [], [0], [], [0], [0], [], [], []]
