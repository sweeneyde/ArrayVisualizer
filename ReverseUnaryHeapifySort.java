package sorts;

import templates.Sort;
import utils.Delays;
import utils.Highlights;
import utils.Reads;
import utils.Writes;

final public class ReverseUnaryHeapifySort extends Sort {
    public ReverseUnaryHeapifySort(Delays delayOps, Highlights markOps, Reads readOps, Writes writeOps) {
        super(delayOps, markOps, readOps, writeOps);
        this.setSortPromptID("Reverse Unary Heapify");
        this.setRunAllID("Reverse Unary Heapify Sort");
        this.setReportSortID("Reverse Unary Heapify Sort");
        this.setCategory("Impractical Sorts");
        this.isComparisonBased(true);
        this.isBucketSort(false);
        this.isRadixSort(false);
        this.isUnreasonablySlow(true);
        this.setUnreasonableLimit(0); //TODO
        this.isBogoSort(false);
    }
    @Override
    public void runSort(int[] array, int length, int bucketCount) {
        for (int i = length; i > 0; i--) {
            for (int root = i; root < length; root++) {
                Highlights.markArray(1, root - 1);
                Highlights.markArray(2, root);
                Delays.sleep(1);
                if (Reads.compare(array[root - 1], array[root]) >= 0) {
                    break;
                }
                Writes.swap(array, root - 1, root, 0, true, false);
            }
        }
    }
}