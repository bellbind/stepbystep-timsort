var data = {nat: {}, object: {}};

var size = 150000;
var range = 1000;
data.nat.random = [];
data.object.random = [];
for (var i = 0; i < size; i++) {
    var value = 0|Math.random() * range;
    data.nat.random.push(value);
    data.object.random.push({value: value, index: i});
};

data.nat.asc = [];
data.object.asc = [];
var value = 0;
var rate = size / range;
for (var i = 0; i < size; i++) {
    if (Math.random() > rate) value++;
    data.nat.asc.push(value);
    data.object.asc.push({value: value, index: i});
};

data.nat.desc = [];
data.object.desc = [];
var value = size;
var rate = size / range;
for (var i = 0; i < size; i++) {
    if (Math.random() > rate) value--;
    data.nat.desc.push(value);
    data.object.desc.push({value: value, index: i});
};

var fs = require("fs");
var outfile = process.argv[2];
fs.open(outfile, "w", function (err, fd) {
    var buffer = new Buffer(JSON.stringify(data));
    fs.write(
        fd, buffer, 0, buffer.length, null, function (err, written, buffer) {
                 fs.close(fd);
        });
});
