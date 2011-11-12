// Merge Improvement: refactoring
// - introduce portable sort state struct
//   - prepare lessThanEqual relation from lessThan relation
// - introduce portable merge state struct
//   - it is for splitting part of merge functions 

// merge sort as interface
var mergeSort = function (array, first, last, lessThan) {
    var state = {
        lessThan: lessThan,
        lessThanEqual: function (a, b) {return !lessThan(b, a);},
    };
    return mergeSortImpl(array, first, last, state);
};

// merge sort with sort state
var mergeSortImpl = function (array, first, last, state) {
    if (last - first <= 1) return array;
    var mid = last + ((first - last) >> 1);
    mergeSortImpl(array, first, mid, state);
    mergeSortImpl(array, mid, last, state);
    mergeNeighbor(array, first, mid, last, state);
    return array;
};

// merge neighbors
var mergeNeighbor = function (array, first, connect, last, state) {
    var llength = connect - first;
    var rlength = last - connect;
    if (llength < rlength) {
        return mergeIntoLeft(array, first, connect, last, state);
    } else {
        return mergeIntoRight(array, first, connect, last, state);
    }
};

// merge with filling to smaller side
var mergeIntoLeft = function (array, first, connect, last, state) {
    // packed states of the function
    var m = {}; 
    // escape shorter buffer only
    m.right = array;
    m.rcur = connect; m.rlast = last;
    
    // find merge start point which is insert point for first of larger side 
    m.cur = binSearch(array, first, connect, m.right[m.rcur], state.lessThan);
    m.left = array.slice(m.cur, connect);
    m.lcur = 0; m.llast = connect - m.cur;
    
    while (m.lcur < m.llast && m.rcur < m.rlast) {
        var lval = m.left[m.lcur];
        var rval = m.right[m.rcur];
        if (state.lessThanEqual(lval, rval)) { // for sort stable
            array[m.cur++] = lval; m.lcur++;
        } else {
            array[m.cur++] = rval; m.rcur++;
        }
    }
    
    // copy back to escaped side (the loop may be empty)
    while (m.lcur < m.llast) array[m.cur++] = m.left[m.lcur++];
    return array;
};


// merge with filling to larger side
var mergeIntoRight = function (array, first, connect, last, state) {
    // packed states of the function
    var m = {};
    // escape shorter buffer only
    m.left = array
    m.lcur = connect; m.lfirst = first;
    
    // find merge start point which is insert point for first of larger side
    m.cur = binSearch(
        array, connect, last, m.left[m.lcur - 1], state.lessThanEqual);
    m.right = array.slice(connect, m.cur);
    m.rcur = m.cur - connect; m.rfirst = 0;
    
    while (m.lfirst < m.lcur && m.rfirst < m.rcur) {
        var lval = m.left[m.lcur - 1];
        var rval = m.right[m.rcur - 1];
        if (state.lessThan(rval, lval)) { // for sort stable
            array[--m.cur] = lval; --m.lcur;
        } else {
            array[--m.cur] = rval; --m.rcur;
        }
    }
    
    // copy back to escaped side (the loop may be empty)
    while (m.rfirst < m.rcur) array[--m.cur] = m.right[--m.rcur];
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
