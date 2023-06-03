class BoyerMoore {
    private final int R;     // the radix
    private int[] right;     // the bad-character skip array

    public BoyerMoore() {
        R = 256;
        right = new int[R];
    }

    // Preprocesses the pattern and constructs the bad-character skip array
    private void preprocessPattern(char[] pattern) {
        int m = pattern.length;

        for (int i = 0; i < R; i++) {
            right[i] = -1;
        }

        for (int j = 0; j < m; j++) {
            right[pattern[j]] = j;
        }
    }

    // Returns the index of the first occurrence of the pattern in the text (or -1 if not found)
    public int search(char[] text, char[] pattern) {
        int n = text.length;
        int m = pattern.length;
        int skip;

        preprocessPattern(pattern);

        for (int i = 0; i <= n - m; i += skip) {
            skip = 0;
            for (int j = m - 1; j >= 0; j--) {
                if (pattern[j] != text[i + j]) {
                    skip = Math.max(1, j - right[text[i + j]]);
                    break;
                }
            }

            if (skip == 0) {
                return i; // found
            }
        }

        return -1; // not found
    }
}
