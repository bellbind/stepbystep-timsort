// Split Improvement: cutting chunk as monotonic ascendant sequence
// - from single element chunks to monotonic ascendant sequences: "run"
// - each "run" length is varied 
//   - length may not be power2, but merging condition is similar as merge sort
//   - it also efficent: O(Nlog(N))
// - handling sequence has advantage for almost all sorted or reversed case

// timsort: merge sort use basic chunk as "run": monotonic ascendant  sequence
var timSort = function (array, first, last, lessThan) {
    if (last - first <= 1) return array;
    // portable struct for handling "run"s
    var state = {
        array: array,
        lessThan: lessThan,
        lessThanEqual: function (a, b) {return !lessThan(b, a);},
        runStack: [],
        remain: first,
        last: last,
    };
    while (nextRun(state)) {
        while (whenMerge(state)) {
            mergeTwoRuns(state);
        }
    }
    return array;
};

// cut array to monotonic chunks named (natural) "run"
var nextRun = function (state) {
    if (state.remain >= state.last) return false;
    if (state.last - state.remain <= 1) {
        // when last one element remained
        cutRun(state, state.last);
        return true;
    }
    
    var last = state.remain;
    var prev = state.array[last++];
    var lastVal = state.array[last++];
    if (state.lessThanEqual(prev, lastVal)) {
        // ascendant seqence
        prev = lastVal;
        while (last < state.last) {
            var val = state.array[last];
            // inc-run elems allowed prev == val
            if (!state.lessThanEqual(prev, val)) break;
            prev = val;
            last++;
        }
    } else {
        // descendant seqence
        prev = lastVal;
        while (last < state.last) {
            var val = state.array[last];
            // dec-run elems must prev > val, prev == val not allowed
            if (!state.lessThan(val, prev)) break; 
            prev = val;
            last++;
        }
        // reversing desc to asc
        reverse(state.array, state.remain, last);
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
// - implementation is not stable
// - in timsort , apply reverse to array: for all i in range, a[i-1] < a[i]
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
    if (state.runStack.length === 2) {
        return curRun.length < preRun.length;
    }
    var pre2Run = state.runStack[state.runStack.length - 3];
    return preRun.length + curRun.length < pre2Run.length;
};

// merge neighbor chunks and add two stacked runs to single run
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

var builtinLessThan = function (a, b) {
    return a < b;
};


// export interface for runner.js
var sort = this.sort = function (array) {
    var lessThan = arguments[1] || builtinLessThan;
    timSort(array, 0, array.length, lessThan);
    return array;
};
