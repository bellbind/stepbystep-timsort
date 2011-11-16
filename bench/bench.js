var fs = require("fs");
var path = require("path");

if (process.argv.length < 3) {
    console.log("usage: node bench.js data.js [XXimple_sort.js]");
    process.exit(1);
}
var dataFile = process.argv[2];
var dataText = fs.readFileSync(dataFile);
var data = JSON.parse(dataText);
// data.{nat,object}.{random,asc,desc}

if (process.argv.length === 4) {
    var sortImplFile = process.argv[3];
    var sortModule = require(path.resolve(sortImplFile));
    var sort = sortModule.sort;
} else {
    var sort = function (array) {
        var lt = arguments[1];
        var cmp = lt ? function (a, b) {
                if (lt(a, b)) return -1;
                if (lt(b, a)) return 1;
                return 0;
        } : undefined;
        array.sort(cmp);
        return array;
    }
}

var objectLessThan = function (a, b) {return a.value < b.value;};

var bench = function (sort, array, lessThan) {
    var start = new Date();
    sort(array, lessThan);
    return (new Date() - start);
};

console.log("nat.random: " + bench(sort, data.nat.random, undefined) + "ms");
console.log("nat.asc: " + bench(sort, data.nat.asc, undefined) + "ms");
console.log("nat.desc: " + bench(sort, data.nat.desc, undefined) + "ms");

console.log("object.random: " + 
            bench(sort, data.object.random, objectLessThan) + "ms");
console.log("object.asc: " + 
            bench(sort, data.object.asc, objectLessThan) + "ms");
console.log("object.desc: " + 
            bench(sort, data.object.desc, objectLessThan) + "ms");


