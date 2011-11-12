// Basic Knowledge: insertion sort and binary search
// - array search interface: [first, last) range, "less than" relation
// - decompose insertion sort as search part and insert part
// - use binary search at search part 
// - use right rotate shift of array as insertion
// - binary search for insertion point: use rightmost of same values 
// - sort stability is important attribute of sort implementation
// - ability of imple rotate shift with memmove: system bulk copy function


// binary sort: insertion sort with binary search
// - use array range as [first, last). "last" is not last index.
// - range of entire array is [0, array.length)
var binarySort = function (array, first, last, lessThan) {
    for (var i = first + 1; i < last; i += 1) {
        var point = binSearch(array, first, i, array[i], lessThan);
        cyclicRShift(array, point, i + 1);
    }
    return array;
};

// binary search for insertion point
var binSearch = function (array, first, last, value, lessThan) {
    while (first < last) {
        var mid = last + ((first - last) >> 1);
        // the condition makes right most point of same values
        // leftmost if lessThan become "less than equal"
        // rightmost: mid > val
        // leftmost: mid >= val
        if (lessThan(value, array[mid])) {
            last = mid;
        } else {
            first = mid + 1;
        }
    }
    return first;
};

// 1 right cyclic shift of array range 
var cyclicRShift = function (array, first, last) {
    if (last - first <= 1) return array;
    var mostRight = array[last - 1];
    // C: memmove(first, first+1, last-first-1)
    for (var cur = last - 1; cur > first; cur -= 1) {
        array[cur] = array[cur - 1];
    }
    array[first] = mostRight;
    return array;
};

// builtin less than
var builtinLessThan = function (a, b) {
    return a < b;
};


// export interface for runner.js
var sort = this.sort = function (array) {
    var lessThan = arguments[1] || builtinLessThan;
    binarySort(array, 0, array.length, lessThan);
    return array;
};
