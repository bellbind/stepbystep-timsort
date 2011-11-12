// Merge Improvement: escape only single side chunk when merging two neighbors
// - escape only shorter side
// - use different filling direction depend on which is shorter
// - e.g. [10,11,|3,4,5] => [10,11] and [>10,11, 3,4,5] =>
//        [3,>10, 3,4,5] => [3,4, >3,4,5] => [3,4, 5,>4,5] => [3,4, 5,10,11]
// - e.g. [10,11,12,|3,4] => [3,4] and [10,11,12, 3,4<] =>
//        [10,11,12, 3<,10] => [10,11,12<, 11,12] => [10,11<,10, 11,12] =>
//        [3,4,10, 11,12]
// - max size of escaped size is half of sort array length
// - in C, single max size escaped buffer can be resuable over each merge call

// merge sort
var mergeSort = function (array, first, last, lessThan) {
    if (last - first <= 1) return array;
    var mid = last + ((first - last) >> 1);
    mergeSort(array, first, mid, lessThan);
    mergeSort(array, mid, last, lessThan);
    mergeNeighbor(array, first, mid, last, lessThan);
    return array;
};

// merge neighbors
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
// - escape left side
// - fill from lowest index
var mergeIntoLeft = function (array, first, connect, last, lessThan) {
    // escape shorter buffer only
    // - array.slice is easy cloning in JS
    var left = array.slice(first, connect);
    var lcur = 0, llast = connect - first;
    var right = array;
    var rcur = connect, rlast = last;
    
    var cur = first;
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
    // C: memcpy(left+lcur, array+cur, llast-lcur)
    while (lcur < llast) array[cur++] = left[lcur++];
    // if remained larger side, copy back is needless
    return array;
};

// merge with filling to larger side
// - escape right side
// - fill from highest index
var mergeIntoRight = function (array, first, connect, last, lessThan) {
    // escape shorter buffer only
    // - array.slice is easy cloning in JS
    var left = array
    var lcur = connect, lfirst = first;
    var right = array.slice(connect, last);
    var rcur = last - connect, rfirst = 0;
    
    var cur = last;
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
    // if remained larger side, copy back is needless
    // C: memcpy(right, array+first, rcur)
    while (rfirst < rcur) array[--cur] = right[--rcur];
    return array;
};


// [by reference] previous naive merge: filling from lowest index
var mergeNeighborNaive = function (array, first, connect, last, lessThan) {
    // escape both buffers
    var left = array.slice(first, connect);
    var right = array.slice(connect, last);
    var lcur = 0, llast = connect - first;
    var rcur = 0, rlast = last - connect;
    
    var cur = first;
    while (lcur < llast && rcur < rlast) {
        var lval = left[lcur];
        var rval = right[rcur];
        if (!lessThan(rval, lval)) { // (lval <= rval) for sort stable
            array[cur++] = lval; lcur++;
        } else {
            array[cur++] = rval; rcur++;
        }
    }
    
    // copy back to remained side (one of the two loops is always empty)
    while (lcur < llast) array[cur++] = left[lcur++];
    while (rcur < rlast) array[cur++] = right[rcur++];
    return array;
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
