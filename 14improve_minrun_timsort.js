// Split Improvement: minrun sort more efficient
// - minrun is already partially sorted
// - use binary sort start from sorted last
// - sort become binarysort when array is smaller than minrun    

// timsort: main loop
var timSort = function (array, first, last, lessThan) {
    if (last - first <= 1) return array;
    var state = {
        array: array,
        lessThan: lessThan,
        lessThanEqual: function (a, b) {return !lessThan(b, a);},
        runStack: [],
        remain: first,
        last: last,
        minrun: calcMinrun(last - first),
    };
    while (nextRun(state)) {
        while (whenMerge(state)) {
            mergeTwoRuns(state);
        }
    }
    return array;
};

// calculate minimum run size
var calcMinrun = function (n) {
    // from python listsort
    // e.g. 1=>1, ..., 63=>63, 64=>32, 65=>33, ..., 127=>64, 128=>32, ...
    var r = 0;
    while (n >= 64) {
        r = r | n & 1;
        n = n >> 1;
    }
    return n + r;
};


// cut array to monotonic chunks named (natural) "run"
var nextRun = function (state) {
    if (state.remain >= state.last) return false;
    if (state.last - state.remain <= 1) {
        cutRun(state, state.last);
        return true;
    }
    
    var last = state.remain;
    var prev = state.array[last++];
    var lastVal = state.array[last++];
    if (state.lessThanEqual(prev, lastVal)) {
        prev = lastVal;
        while (last < state.last) {
            var val = state.array[last];
            // inc-run elems allowed prev == val
            if (!state.lessThanEqual(prev, val)) break;
            prev = val;
            last++;
        }
    } else {
        prev = lastVal;
        while (last < state.last) {
            var val = state.array[last];
            // dec-run elems must prev > val, prev == val not allowed
            if (!state.lessThan(val, prev)) break; 
            prev = val;
            last++;
        }
        reverse(state.array, state.remain, last);
    }
    
    if (last - state.remain < state.minrun) {
        // replace binary sorted minrun
        var minrun = state.remain + state.minrun;
        var sortStart = last;
        last = (minrun > state.last) ? state.last : minrun;
        binarySort(state.array, state.remain, last, state.lessThan, sortStart);
    }
    cutRun(state, last);
    return true;
};

// cut and stack a run
var cutRun = function (state, last) {
    var run = {
        first: state.remain,
        last: last,
        length: last - state.remain,
    };
    state.runStack.push(run);
    state.remain = last;
};

// reverse elements in array range
var reverse = function (array, first, last) {
    last--;
    while (first < last) {
        swap(array, first++, last--);
    }
};

// swap array elements
var swap = function (array, a, b) {
    var tmp = array[a];
    array[a] = array[b];
    array[b] = tmp;
};

// loop condition when merge neighbors
var whenMerge = function (state) {
    if (state.remain === state.last) {
        return state.runStack.length > 1;
    }
    if (state.runStack.length <= 1) {
        return false;        
    }
    
    var curRun = state.runStack[state.runStack.length - 1];
    var preRun = state.runStack[state.runStack.length - 2];
    return preRun.length < curRun.length * 2;
};

// merge neighbor
var mergeTwoRuns = function (state) {
    var mergerRun = state.runStack.pop();
    var mergedRun = state.runStack[state.runStack.length - 1];
    // assert mergedRun.last === mergerRun.first
    mergeNeighbor(
        state.array, mergedRun.first, mergerRun.first, mergerRun.last, 
        state.lessThan);
    mergedRun.last = mergerRun.last;
    mergedRun.length += mergerRun.length;
};

// same as merge function of merge sort 
var mergeNeighbor = function (array, first, connect, last, lessThan) {
    // escape both buffers
    var left = array.slice(first, connect);
    var lcur = 0, llast = connect - first;
    var right = array.slice(connect, last);
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

// binary sort: insertion sort with binary search
// - option: sort start from sortStart
// - sortStart preconditions:
//   - first < sortStart < last
//   - sorted [first, sortStart)
var binarySort = function (array, first, last, lessThan, sortStart) {
    sortStart = sortStart || first + 1;
    for (var i = sortStart; i < last; i += 1) {
        var point = binSearch(array, first, i, array[i], lessThan);
        cyclicRShift(array, point, i + 1);
    }
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



var builtinLessThan = function (a, b) {
    return a < b;
};


// export interface for runner.js
var sort = this.sort = function (array) {
    var lessThan = arguments[1] || builtinLessThan;
    timSort(array, 0, array.length, lessThan);
    return array;
};
