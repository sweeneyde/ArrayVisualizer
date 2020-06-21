package sorts;

import templates.Sort;
import utils.Delays;
import utils.Highlights;
import utils.Reads;
import utils.Writes;

final public class UnaryHeapSort extends Sort {
    public UnaryHeapSort(Delays delayOps, Highlights markOps, Reads readOps, Writes writeOps) {
        super(delayOps, markOps, readOps, writeOps);
        this.setSortPromptID("Unary Heap");
        this.setRunAllID("Unary Heap Sort");
        this.setReportSortID("Unary Heapsort");
        this.setCategory("Impractical Sorts");
        this.isComparisonBased(true);
        this.isBucketSort(false);
        this.isRadixSort(false);
        this.isUnreasonablySlow(true);
        this.setUnreasonableLimit(0); //TODO
        this.isBogoSort(false);
    }
    private void siftDown(int[] array, int root, int dist) {
        while (root < dist) {
            Highlights.markArray(1, root - 1);
            Highlights.markArray(2, root);
            Delays.sleep(1);
            if (Reads.compare(array[root - 1], array[root]) >= 0) {
                break;
            }
            Writes.swap(array, root - 1, root++, 0, true, false);
        }
    }
    @Override
    public void runSort(int[] array, int length, int bucketCount) {
        for (int i = length; i > 0; i--) {
            siftDown(array, i, length);
        }
        // at this point the array is sorted but reversed. heaps!!
        for (int i = length - 1; i > 0; i--) {
            Writes.swap(array, 0, i, 1, true, false);
            siftDown(array, 1, i);
        }
    }
}