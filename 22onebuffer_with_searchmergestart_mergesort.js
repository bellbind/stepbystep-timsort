// Merge Improvement: add searching merge start position for escaped side
// - if left side is shorter, escape elements only larger than 
//   first right side elem 
// - if right side is shorter, escape elements only smaller than equal 
//   last left side elem 
// - e.g. [1,5,|2,3,6] => [5] and [1,>5, 2,3,6] => [1,2, >2,3,6] =>
//        [1,2, 3,>3,6] => [1,2, 3,5,>6]
// - e.g. [1,3,4,|2,5] => [2] and [1,3,4, 2<,5] => [1,3,4<, 4,5] =>
//        [1,3<,3, 4,5] => [1,<2,3, 4,5]
// - e.g. [1,2,3,|2,3,6,7] => [3] and [1,2,>3, 2,3,6,7] => ...
// - e.g. [1,2,2,3,|2,3,6] => [2] and [1,2,2,3, 2<,3,6] => ...

// merge sort
var mergeSort = function (array, first, last, lessThan) {
    if (last - first <= 1) return array;
    var mid = last + ((first - last) >> 1);
    mergeSort(array, first, mid, lessThan);
    mergeSort(array, mid, last, lessThan);
    mergeNeighbor(array, first, mid, last, lessThan);
    return array;
};

// merge neighbor
var mergeNeighbor = function (array, first, connect, last, lessThan) {
    var llength = connect - first;
    var rlength = last - connect;
    if (llength < rlength) {
        return mergeIntoLeft(array, first, connect, last, lessThan);
    } else {
        return mergeIntoRight(array, first, connect, last, lessThan);
    }
};

// merge with filling to smaller side
var mergeIntoLeft = function (array, first, connect, last, lessThan) {
    // escape shorter buffer only
    var right = array;
    var rcur = connect, rlast = last;
    
    // find merge start point which is insert point for first of larger side 
    // (rightmost point)
    var cur = binSearch(array, first, connect, right[rcur], lessThan); 
    var left = array.slice(cur, connect);
    var lcur = 0, llast = connect - cur;
    
    while (lcur < llast && rcur < rlast) {
        var lval = left[lcur];
        var rval = right[rcur];
        if (!lessThan(rval, lval)) { // (lval <= rval) for sort stable
            array[cur++] = lval; lcur++;
        } else {
            array[cur++] = rval; rcur++;
        }
    }
    
    // copy back to escaped side (the loop may be empty)
    while (lcur < llast) array[cur++] = left[lcur++];
    return array;
};


// merge with filling to larger side
var mergeIntoRight = function (array, first, connect, last, lessThan) {
    // escape shorter buffer only
    var left = array
    var lcur = connect, lfirst = first;
    
    // find merge start point which is insert point for first of larger side
    var lessThanEqual = function (a, b) {
        return !lessThan(b, a);
    };
    // (leftmost point)
    var cur = binSearch(array, connect, last, left[lcur - 1], lessThanEqual);
    var right = array.slice(connect, cur);
    var rcur = cur - connect, rfirst = 0;
    
    while (lfirst < lcur && rfirst < rcur) {
        var lval = left[lcur - 1];
        var rval = right[rcur - 1];
        if (lessThan(rval, lval)) { // (lval > rval) for sort stable
            array[--cur] = lval; --lcur;
        } else {
            array[--cur] = rval; --rcur;
        }
    }
    
    // copy back to escaped side (the loop may be empty)
    while (rfirst < rcur) array[--cur] = right[--rcur];
    return array;
};

// binary search
var binSearch = function (array, first, last, value, lessThan) {
    while (first < last) {
        var mid = last + ((first - last) >> 1);
        if (lessThan(value, array[mid])) { 
            last = mid;
        } else {
            first = mid + 1;
        }
    }
    return first;
};

var builtinLessThan = function (a, b) {
    return a < b;
};

// export interface for runner.js
var sort = this.sort = function (array) {
    var lessThan = arguments[1] || builtinLessThan;
    mergeSort(array, 0, array.length, lessThan);
    return array;
};
