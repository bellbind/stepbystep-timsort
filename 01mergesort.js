// Basic Knowledge: merge sort
// - merge of merge sort always merge naighbour chunks
// - timsort is improved merge sort for the two aspects: split and merge
//   - split: change to looply splitting monotonic sequences
//   - merge: make more efficient

// merge sort: naive recursive implementation
var mergeSort = function (array, first, last, lessThan) {
    if (last - first <= 1) return array;
    var mid = last + ((first - last) >> 1); // (first+last)/2 avoid overflow 
    mergeSort(array, first, mid, lessThan);
    mergeSort(array, mid, last, lessThan);
    mergeNeighbor(array, first, mid, last, lessThan);
    return array;
};

// merge of merge sort with addtional buffers
// - merge [first, connect) and [connect, last)
var mergeNeighbor = function (array, first, connect, last, lessThan) {
    // escape both buffers
    var left = array.slice(first, connect);
    var lcur = 0, llast = connect - first;
    var right = array.slice(connect, last);
    var rcur = 0, rlast = last - connect;
    
    var cur = first;
    while (lcur < llast && rcur < rlast) {
        // copy back every lower side
        var lval = left[lcur];
        var rval = right[rcur];
        if (!lessThan(rval, lval)) { // (lval <= rval) for sort stable
            array[cur++] = lval; lcur++;
        } else {
            array[cur++] = rval; rcur++;
        }
    }
    
    // copy back to remained side (one of the two loops is always empty)
    // C: memcpy(left+lcur, array+cur, llast-lcur)
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
