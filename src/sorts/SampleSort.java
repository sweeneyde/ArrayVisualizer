package sorts;

import templates.Sort;
import utils.Delays;
import utils.Highlights;
import utils.Reads;
import utils.Writes;

/* Timsort's predecessor in CPython, copied from
 * https://github.com/python/cpython/blob/56796f672fb571d80199cf08aa059db9df55257b/Objects/listobject.c
 * Mostly written by Tim Peters and Guido van Rossum
 *
 * This Hybrid SampleSort is very similar to quicksort, except instead
 * of selecting just one pivot, we randomly select MANY pivots,
 * recursively sort them, and then partition the rest of the elements
 * between those pivots, finally then sorting each bucket recursively.
 *
 * How many is "MANY"?
 * For 43--105 elements, use 15 pivots
 * For 106--250 elements, use 31 pivots
 * For 250--576 elements, use 63 pivots
 * For 576--1297 elements, use 127 pivots
 * ...
 * For n elements, use 1 less than a power of 2 close to n/ln(n)
 *
 */


public class SampleSort extends Sort {
    public SampleSort(Delays delayOps, Highlights markOps, Reads readOps, Writes writeOps) {
        super(delayOps, markOps, readOps, writeOps);

        this.setSortPromptID("Sample");
        this.setRunAllID("Sample Sort");
        this.setReportSortID("SampleSort");
        this.setCategory("Hybrid Sorts");
        this.isComparisonBased(true);
        this.isBucketSort(false);
        this.isRadixSort(false);
        this.isUnreasonablySlow(false);
        this.setUnreasonableLimit(0);
        this.isBogoSort(false);
    }

    private static int MINSIZE = 100;
    private static int MINPARTITIONSIZE = 40;
    private static int STACKSIZE = 60;

    private static double READ_DELAY = 0.02;
    private static double FAST_READ_DELAY = 0.01;
    private static double ASSIGN_DELAY = 0.2;
    private static double SWAP_DELAY = 0.3;
    private static double FAST_ASSIGN_DELAY = 0.05;
    private static double FAST_SWAP_DELAY = 0.07;
    private static double COMPARE_DELAY = 0.2;
    private static double STACK_DELAY = 0.5;

    private boolean less_than(int left, int right) {
        Delays.sleep(COMPARE_DELAY);
        return Reads.compare(left, right) < 0;
    }

    private int get(int[] arr, int i, double delay) {
        Highlights.markArray(1, i);
        Delays.sleep(delay);
        return arr[i];
    }

    public void binary_sort(int[] arr, int lo, int hi, int start) {
        int l, p, r, pivot;
        if (lo == start) {
            start++;
        }
        for (; start < hi; start++) {
            l = lo;
            r = start;
            pivot = get(arr, r, READ_DELAY);
            assert l < r;
            do {
                p = l + ((r - l) >> 1);
                if (less_than(pivot, get(arr, p, READ_DELAY))) {
                    r = p;
                } else {
                    l = p + 1;
                }
            } while (l < r);
            assert l == r;
            for (p = start; p > l; p--) {
                Writes.write(arr, p, arr[p-1], FAST_ASSIGN_DELAY, true, false);
            }
            Writes.write(arr, l, pivot, ASSIGN_DELAY, true, false);
        }
    }

    private static int CUTOFFBASE = 4;
    private static long cutoff[] = {
            43,        /* smallest N where we use 2**4-1=15 pivots*/
            106,       /* etc */
            250,
            576,
            1298,
            2885,
            6339,
            13805,
            29843,
            64116,
            137030,
            291554,
            617916,
            1305130,
            2748295,
            5771662,
            12091672,
            25276798,
            52734615,
            109820537,
            228324027,
            473977813,
            982548444,   /* smallest N such that k == 26 */
            2034159050   /* largest N that fits in signed 32-bit; k == 27 */
    };
    private int get_num_samples(int n) {
        int k;
        for (k = 0; k < cutoff.length; k++) {
            if (n < cutoff[k]) {
                break;
            }
        }
        return (1 << (k - 1 + CUTOFFBASE)) - 1;
    }

    private void sample_sort(int[] arr, int lo, int hi) {
        int l, r, pivot, k, n, extra, top;
        boolean extraOnRight;
        int[][] stack = new int[STACKSIZE][3];
        n = hi - lo;
        if (n < MINSIZE) {
            binary_sort(arr, lo, hi, lo);
            return;
        }
        extra = get_num_samples(n);
        {
            int seed = n / extra; /* arbitrary */
            for (int i = 0; i < extra; i++) {
                seed = seed * 69069 + 7;
                int j = i + seed % (n - i);
                if (j < i) {
                    j += n - i;
                }
                Writes.swap(arr, i, j, SWAP_DELAY, true, false);
            }
        }
        sample_sort(arr, lo, lo + extra);
        top = 0;
        lo += extra;
        extraOnRight = false;
        for (;;) {
            assert lo <= hi;
            n = hi - lo;
            if (n < MINPARTITIONSIZE || extra == 0) {
                if (n >= MINSIZE) {
                    assert extra == 0;
                    sample_sort(arr, lo, hi);
                } else {
                    if (extraOnRight && extra != 0) {
                        k = extra;
                        do {
                            Writes.swap(arr, lo, hi, FAST_SWAP_DELAY, true, false);
                            ++lo; ++hi;
                        } while (--k > 0);
                    }
                    binary_sort(arr, lo - extra, hi, lo);
                }
                if (--top < 0) {
                    break;
                }
                lo = stack[top][0];
                hi = stack[top][1];
                extra = stack[top][2];
                extraOnRight = false;
                if (extra < 0) {
                    extra = -extra;
                    extraOnRight = true;
                }
                continue;
            }
            k = extra >>= 1;
            if (extraOnRight) {
                do {
                    Writes.swap(arr, lo, hi, FAST_SWAP_DELAY, true , false);
                    ++lo; ++hi;
                } while (k-- != 0);
            } else {
                while (k-- != 0) {
                    --lo; --hi;
                    Writes.swap(arr, lo, hi, FAST_SWAP_DELAY, true, false);
                }
            }
            --lo;
            pivot = get(arr, lo, READ_DELAY);

            l = lo + 1;
            r = hi - 1;
            assert lo < l && l < r && r < hi;
            do {
                do {
                    if (less_than(get(arr, l, FAST_READ_DELAY), pivot)) {
                        ++l;
                    } else {
                        break;
                    }
                } while (l < r);

                while (l < r) {
                    int rval = get(arr, r--, FAST_READ_DELAY);
                    if (less_than(rval, pivot)) {
                        Writes.write(arr, r+1, arr[l], ASSIGN_DELAY, true, false);
                        Writes.write(arr, l++, rval, ASSIGN_DELAY, true, false);
                        break;
                    }
                }
            } while (l < r);

            assert lo < r && r <= l && l < hi;
            if (l == r) {
                if (less_than(get(arr, r, READ_DELAY), pivot)) {
                    ++l;
                } else {
                    --r;
                }
            }
            assert(lo <= r && r+1 == l && l <= hi);
            assert(arr[lo] == pivot);
            Writes.write(arr, lo, get(arr, r, READ_DELAY), 0.01, true, false);
            Writes.write(arr, r, pivot, 0.01, true, false);

            while (l < hi) {
                if (less_than(pivot, get(arr, l, FAST_READ_DELAY))) {
                    break;
                } else {
                    ++l;
                }
            }

            assert(lo <= r && r < l && l <= hi);
            assert(top < STACKSIZE);
            if (r - lo <= hi - l) {
                /* second is bigger */
                stack[top][0] = l;
                stack[top][1] = hi;
                stack[top][2] = -extra;
                hi = r;
                extraOnRight = false;
            }
            else {
                /* first is bigger */
                stack[top][0] = lo;
                stack[top][1] = r;
                stack[top][2] = extra;
                lo = l;
                extraOnRight = true;
            }
            Delays.sleep(STACK_DELAY);
            ++top;
        }
    }

    @Override
    public void runSort(int[] array, int currentLength, int bucketCount) {
        sample_sort(array, 0, currentLength);
    }
}