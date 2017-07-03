import java.io.File;
import java.util.*;

/**
 * Given an MxN set of letters, finds all possible words of a given length where each word is formed
 * by linking adjacent letters on the grid. (Adjacent = 8 directions)
 * This game does not allow repeating letters.
 */
public class WordBrain {

	private File dictionary;

	private Map<Character, List<String>> dictMap;

	private ArrayList<Thread> threadArr;

	private WordBrain() throws Exception {
		dictionary = new File("/usr/share/dict/words");
		threadArr = new ArrayList<>();
		setupDict();
	}

	private void setupDict() throws Exception {
		dictMap = new HashMap<>();
		Scanner scanner = new Scanner(dictionary);
		String dictWord;
		while (scanner.hasNext() && (dictWord = scanner.next()) != null) {
			dictMap.computeIfAbsent(dictWord.charAt(0), v -> new LinkedList<>()).add(dictWord);
		}
	}

	private boolean lookupDict(String key) {
		List<String> bucket = dictMap.get(key.charAt(0));

		return bucket != null && bucket.contains(key);
	}

	private void printWords(String[][] inputArr, int maxLen) throws Exception {
		for (int i = 0; i < inputArr.length; i++) {
			for (int j = 0; j < inputArr[i].length; j++) {
				FindWords findWords = new FindWords(inputArr, i, j, maxLen);
				Thread t = new Thread(findWords);
				threadArr.add(t);
				t.start();
			}
		}

		for (Thread t : threadArr) {
			t.join();
		}
	}

	public static void main(String[] args) throws Exception {

		WordBrain wordBrain = new WordBrain();

		String[][] inputArrA = new String[][] {
				{ "w", "a" },
				{ "y", "r" },
				{ "t", "k" },
				{ "e", "s" }
		};

		final long startTime = System.currentTimeMillis();
		wordBrain.printWords(inputArrA, 5);
		final long endTime = System.currentTimeMillis();

		System.out.println("Word search complete!");
		System.out.println("Took " + (endTime - startTime) + "ms");
	}

	private class FindWords implements Runnable {

		String[][] inputArr;
		int iIndex;
		int jIndex;
		int maxLen;
		ArrayList<String> buf;
		boolean[][] visited;

		FindWords(String[][] inputArr, int i, int j, int maxLen) {
			this.inputArr = inputArr;
			iIndex = i;
			jIndex = j;
			this.maxLen = maxLen;
			buf = new ArrayList<>();
			visited = new boolean[inputArr.length][inputArr[i].length];
		}

		public void run() {
			try {
				System.out.println("Running thread " + Thread.currentThread().getId());
				printWords(inputArr, iIndex, jIndex, buf, maxLen, visited);
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		private void printWords(String[][] inputArr, int i, int j, ArrayList<String> buf,
								int maxLen, boolean[][] visited) throws Exception {

			if (i < 0 || i >= inputArr.length || j < 0 || j >= inputArr[i].length || visited[i][j]) {
				return;
			}

			buf.add(inputArr[i][j]);
			visited[i][j] = true;

			if (buf.size() >= maxLen) {
				StringBuilder sb = new StringBuilder();
				for (String s : buf) {
					sb.append(s);
				}

				if (lookupDict(sb.toString())) {
					System.out.println("FOUND! - " + sb);
				}

				buf.remove(buf.size() - 1);

				visited[i][j] = false;
				return;
			}

			printWords(inputArr, i, j+1, buf, maxLen, visited);
			printWords(inputArr, i, j-1, buf, maxLen, visited);
			printWords(inputArr, i-1, j-1, buf, maxLen, visited);
			printWords(inputArr, i-1, j, buf, maxLen, visited);
			printWords(inputArr, i-1, j+1, buf, maxLen, visited);
			printWords(inputArr, i+1, j-1, buf, maxLen, visited);
			printWords(inputArr, i+1, j, buf, maxLen, visited);
			printWords(inputArr, i+1, j+1, buf, maxLen, visited);

			buf.remove(buf.size() - 1);
			visited[i][j] = false;
		}
	}
}
