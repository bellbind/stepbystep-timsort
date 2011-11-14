// TimSort: Put It Together
// - combine codes of both split and merge improvement 

var MIN_GALLOP = 7;

// ===  [SPLIT IMPROVEMENTS] == //
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
        minGallop: MIN_GALLOP,
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
    
    // similar sized runs should be merged: it introduces log(n) merge count
    var curRun = state.runStack[state.runStack.length - 1];
    var preRun = state.runStack[state.runStack.length - 2];
    if (state.runStack.length === 2) return preRun.length <= curRun.length;
    var pre2Run = state.runStack[state.runStack.length - 3];
    return pre2Run.length <= preRun.length + curRun.length;
};

// merge neighbor runs
var mergeTwoRuns = function (state) {
    var mergerRun = state.runStack.pop();
    var mergedRun = state.runStack[state.runStack.length - 1];
    // assert mergedRun.last === mergerRun.first
    mergeNeighbor(
        state.array, mergedRun.first, mergerRun.first, mergerRun.last, 
        state);
    mergedRun.last = mergerRun.last;
    mergedRun.length += mergerRun.length;
};


// ===  [MERGE IMPROVEMENTS] === //

// merge neighbor chunks
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

    // states for mode control
    m.galloping = false;
    m.gallopingOut = false;
    m.selectLeft = true;
    m.selectCount = 0;
    while (m.lcur < m.llast && m.rcur < m.rlast) {
        if (!m.galloping) {
            mergeLeftOnePairMode(array, state, m);
        } else {
            mergeLeftGallopingMode(array, state, m);
        }
    }
    
    // copy back to escaped side (the loop may be empty)
    while (m.lcur < m.llast) array[m.cur++] = m.left[m.lcur++];
    return array;
};

// one pair mode when filling to smaller side
var mergeLeftOnePairMode = function (array, state, m) {
    var lval = m.left[m.lcur];
    var rval = m.right[m.rcur];
    if (state.lessThanEqual(lval, rval)) { // for sort stable
        array[m.cur++] = lval; m.lcur++;
        modeControlInOnePairMode(state, m, !m.selectLeft);
    } else {
        array[m.cur++] = rval; m.rcur++;
        modeControlInOnePairMode(state, m, m.selectLeft);
    }
};

// mode control for one pair mode
var modeControlInOnePairMode = function (state, m, selectSwitched) {
    if (selectSwitched) {
        m.selectLeft = !m.selectLeft;
        m.selectCount = 0;
    }
    m.selectCount++;
    if (m.selectCount >= state.minGallop) {
        m.galloping = true;
        m.selectCount = 0;
    }
};

// galloping mode when filling to smaller side
var mergeLeftGallopingMode = function (array, state, m) {
    if (state.minGallop > 0) state.minGallop--;
    var lval = m.left[m.lcur];
    var rval = m.right[m.rcur];
    if (state.lessThanEqual(lval, rval)) {
        // left(shorter) side gallop includes right side first (rightmost)
        var end = gallopSearch(
            m.left, m.lcur, m.llast, rval, state.lessThan); // rightmost
        modeControlInGallopingMode(state, m, end - m.lcur);
        while (m.lcur < end) array[m.cur++] = m.left[m.lcur++];
    } else {
         // right(longer) side gallop excludes left side first (leftmost)
        var end = gallopSearch(
            m.right, m.rcur, m.rlast, lval, state.lessThanEqual); // leftmost
        modeControlInGallopingMode(state, m, end - m.rcur);
        while (m.rcur < end) array[m.cur++] = m.right[m.rcur++];
    }
};

// mode control for galloping mode
var modeControlInGallopingMode = function (state, m, gallopSize) {
    if (gallopSize < MIN_GALLOP) {
       if (m.gallopOut) { // exit galloping mode if gallop out at both sides 
           m.galloping = false;
           m.gallopOut = false;
           state.minGallop++;
       } else {
           m.gallopOut = true;
       }
    } else {
        m.gallopOut = false;
    }
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
    
    // states for mode control
    m.galloping = false;
    m.gallopingOut = false;
    m.selectLeft = true;
    m.selectCount = 0;
    while (m.lfirst < m.lcur && m.rfirst < m.rcur) {
        if (!m.galloping) {
            mergeRightOnePairMode(array, state, m);
        } else {
            mergeRightGallopingMode(array, state, m);
        }
    }
    
    // copy back to escaped side (the loop may be empty)
    while (m.rfirst < m.rcur) array[--m.cur] = m.right[--m.rcur];
    return array;
};

// one pair mode when filling to larger side
var mergeRightOnePairMode = function (array, state, m) {
    var lval = m.left[m.lcur - 1];
    var rval = m.right[m.rcur - 1];
    if (state.lessThan(rval, lval)) { // (lval > rval) for sort stable
        array[--m.cur] = lval; --m.lcur;
        modeControlInOnePairMode(state, m, !m.selectLeft);
    } else {
        array[--m.cur] = rval; --m.rcur;
        modeControlInOnePairMode(state, m, m.selectLeft);
    }
};

// galloping mode when filling to larger side
var mergeRightGallopingMode = function (array, state, m) {
    if (state.minGallop > 0) state.minGallop--;
    var lval = m.left[m.lcur - 1];
    var rval = m.right[m.rcur - 1];
    if (state.lessThan(rval, lval)) {
        // left(longer) side gallop excludes right side last (rightmost)
        var begin = gallopSearch(
            m.left, m.lfirst, m.lcur - 1, rval, state.lessThan); // rightmost
        modeControlInGallopingMode(state, m, m.lcur - begin);
        while (begin < m.lcur) array[--m.cur] = m.left[--m.lcur];
    } else {
        // right(shorter) side gallop includes left side last (leftmost)
        var begin = gallopSearch(
            m.right, m.rfirst, m.rcur, lval, state.lessThanEqual); // leftmost
        modeControlInGallopingMode(state, m, m.rcur - begin);
        while (begin < m.rcur) array[--m.cur] = m.right[--m.rcur];
    }
};

// binsearch for gallop mode
// search to one of regions [0,1) [1,3),[3,7),[7,15),...
var gallopSearch = function (array, first, last, value, lessThan) {
    var pre = 0;
    var offset = 1;
    while (first + offset < last) {
        if (lessThan(value, array[first + offset])) break;
        pre = offset;
        offset = (offset << 1) + 1;
    }
    var searchFirst = first + pre;
    var searchLast = (first + offset < last) ? first + offset : last;
    return binSearch(array, searchFirst, searchLast, value, lessThan);
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

// binary sort: insertion sort with binary search
var binarySort = function (array, first, last, lessThan, sortStart) {
    sortStart = sortStart || first + 1;
    for (var i = sortStart; i < last; i += 1) {
        var point = binSearch(array, first, i, array[i], lessThan);
        cyclicRShift(array, point, i + 1);
    }
    return array;
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
