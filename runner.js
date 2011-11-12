// [node.js] runner for sort implementation files
var path = require("path");

if (process.argv.length === 2) {
    console.log("usage: node runner.js XXimple_sort.js");
    process.exit(1);
}
var sortImplFile = process.argv[process.argv.length - 1];
var sortModule = require(path.resolve(sortImplFile));
var sort = sortModule.sort;

var runBasic = function () {
    console.log(sort([]));
    console.log(sort([1]));
    console.log(sort([1, 2]));
    console.log(sort([2, 1]));
    console.log(sort([1, 2, 3]));
    console.log(sort([3, 2, 1]));
};

var runMiniSize = function () {
    console.log(sort(
        [1, 2, 3, 9, 8, 7, 10, 11, 12]
    ));
    console.log(sort(
        [1, 2, 3, 21, 22, 23, 4, 5, 13, 6, 7, 8, 9, 0, 11, 12, 33,
         14, 15, 16, 17, 18, 19, 20, 24, 30, 25, 26, 27, 28, 29, 31, 32,]
    ));
};

var runLargeRandom = function () {
    var a = [];
    for (var i = 0; i < 15000; i++) {
        a.push(0|Math.random() * 1000);
    }
    sort(a);
    var failures = [];;
    for (var i = 1; i < a.length; i++) {
        if (a[i-1] > a[i]) {
            failures.push({at: i, pre: a[i-1], cur: a[i]});
        }
    }
    if (failures.length === 0) {
        console.log("large sort success");
    } else {
        console.log("large sort failed");
        console.log(failures);
    }
};

if (require.main === module) {
    runBasic();
    runMiniSize();
    runLargeRandom();
}
