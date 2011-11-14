// Split Improvement: implement merge sort by loop

// merge sort by loop with chunk stack
// - neighbor two chunks in stack are neighbors in the array
// - max chunk stack size: log(n)
// - e.g. trace of stack state of chunk length
//   [1] => [1,1] => [2] => [2,1] => [2,1,1] => [2,2] => [4] => ...
var mergeSort = function (array, first, last, lessThan) {
    if (last - first <= 1) return array;
    // in C
    // - max stack size is: smax = 0; while (len > 0) {smax++;len>>1;}
    // - make stack as maxsize array of chunk struct. not pointer of the struct
    var stack = [];
    var remain = first;
    while (remain < last) {
        // cut 1 element chunk
        stack.push({
            first: remain,
            last: remain + 1,
            length: 1,
        });
        remain++;
        
        // merge conditions: 
        // - last two chunk become same size
        // - no more chunk remained on array
        while (stack.length > 1 && 
               (remain >= last || 
                stack[stack.length-2].length < 
                stack[stack.length-1].length * 2)) {
            var pre = stack[stack.length-2];
            var cur = stack.pop();
            // assert pre.last === cur.first
            
            // merge two chunks
            mergeNeighbor(array, pre.first, pre.last, cur.last, lessThan);
            // add two chunks as single chunk
            pre.last = cur.last;
            pre.length += cur.length;
        }
    }
    return array;
};


// merge of merge sort with addtional buffers
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
