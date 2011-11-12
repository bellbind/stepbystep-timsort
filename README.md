# timsort: step by step implementation

Understanding timsort features by implementing step by step manner.

## Overview

All imprementations are written by JavaScript.
It can be written as compact code, 
and has primitive functionoalities: e.g. shift ops, for loop.

Implementation files has numbering file name.
Each implementation is improvement of former implementation.

At first, prepare the basic knowledge for implementing timsort.

- 00binarysort.js
- 01mergesort.js

Timsort is improvement of merge sort for two aspects: split and merge.

- split improvement has 1X numbered file name
- merge improvement has 2X numbered file name

At last, merged both improvements as

- 30timsort.js

Every implementation files are executable.
runner.js executes the sort in the file with several examples:

    node runner.js 01mergesort.js

I checked by node-v0.6.0

## 00binarysort.js

Binary sort is improvement of insertion sort.

In usual insertion sort, comparing and swap are mixed in the inner loop.
To divide them as searching inserting point and inserting a element,
the former can apply binary search,
and the latter is just a right cyclic shift.
It is named "binary sort".

The key is

- range replesentation of array and algorithm
- relationship with relation lessThan and stable insertion point 
  when included same values.
- change to apply binary search to sort impl
- change to apply bulk copy (not swap) to sort impl

The binary sort is also one of the part of timsort.

## 01mergesort.js

The merge sort is the base of improvements to timsort.

The key of merge sort is

- merge only "neighbor" two chunks.

Split improvements keeps the chunk neighborness.
Merge improvements also depends on the neighborness.

## 11loop_mergesort.js

The improvement changes recursive splitting to looped split and merge.
Looped one uses chunk stack.
Merge strategy is designed merge count as O(Nlog(N)).

The key is

- split each 1 element chunks
- merge strategy is based on chunk size of last 2 chunks in the stacks.

## 12runs_timsort.js

Timsort uses monotonic part(asc or desc) as basic splitted chunk.
It is named to "run".

The ascendant run are already sorted.
The descendant run shoud be reversed, so becomes also sorted part.

Entire array parted sorted chunks.
it is same as mid state of merge sort execution.
So merging strategy and method of merge neighbor is same as merge sort.

The key is

- Timsort can be applyed same improvement of merge of mergesort.

## 13minrun_timsort.js

Handling shorter runs is not efficient.
It introduce minimun run size.

If forcely run exprode minimun run size, 
the run(minrun) is not orderd elements.
So, the minrun is sorted by the binary sort.

## 14improve_minrun_timsort.js

Whole minrun is not orderd.
But former side of minrun(part of before extended run) ensures orderd.

Binary sort can start at after the extended part.

## 21onebuffer_mergesort.js

Apply merge improvement to basic merge sort (not timsort).
One reason is timsort to smaller array is not enter the merge process
(perform minrun binary sort only).

First improvement is reduce merging escaped buffer to one.
It escapes only shorter side chunk.

Merge process split two. left side is shorter or right side is shorter.
The difference is direction of iterating and compare relation.

## 22onebuffer_with_searchmergestart_mergesort.js

Escaping shorter side chunk does not need to escape entire chunk.
To seek a merge point of first of the larger side,
The point is used as escape range start.

## 23refactored_mergesort.js

This improvement is refactored to packing state to a struct.
It is for passing functions.
It makes functions dividable.

## 24galloping_mergesort.js

Usual merge is comparing heads of both side and copy back one of heads.
It named "one-pair mode".

Galloping mode is copy back as block manner.

The improvement mixes one-pair mode and galloping mode.
Mode switching is based on counts of copy back of eachcountinuous side.

## 25improve_gallop_search_mergesort.js

In galloping mode, bianry sort is used for finding gallop point.

The improvement is using special binary sort.
For each n=1,2,3,..., 
searching  just in [2**(n-1), 2**n) range which includes the result.

## 26varying_galloping_mergesort.js

This improvement makes continuous count of mode switching variable.

## 30timsort.js

It is mixed merge improvements until 26varying_galloping?mergesort.js 
to timsort until 14improve_minrun_timsort.js.

## resources

- [http://en.wikipedia.org/wiki/Timsort](http://en.wikipedia.org/wiki/Timsort)
- [http://svn.python.org/projects/python/trunk/Objects/listsort.txt](http://svn.python.org/projects/python/trunk/Objects/listsort.txt)
- [http://svn.python.org/projects/python/trunk/Objects/listobject.c](http://svn.python.org/projects/python/trunk/Objects/listobject.c)
- [https://github.com/DRMacIver/understanding-timsort](https://github.com/DRMacIver/understanding-timsort)
