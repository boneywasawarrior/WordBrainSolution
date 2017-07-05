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

	private WordBrain() throws Exception {
		dictionary = new File("/usr/share/dict/words");
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

	private List<String> printWords(String[][] inputArr, int maxLen) throws Exception {
		ArrayList<Thread> threadArr = new ArrayList<>();
		ArrayList<FindWords> objList = new ArrayList<>();
		ArrayList<String> results = new ArrayList<>();

		for (int i = 0; i < inputArr.length; i++) {
			for (int j = 0; j < inputArr[i].length; j++) {
				FindWords findWords = new FindWords(inputArr, i, j, maxLen);
				Thread t = new Thread(findWords);
				threadArr.add(t);
				objList.add(findWords);
				t.start();
			}
		}

		for (Thread t : threadArr) {
			t.join();
		}

		for (FindWords findWords : objList) {
			results.addAll(findWords.getResults());
		}

		return results;
	}

	private void findUniqueN(Map<String, Integer> inputStringMap, Map<Integer, List<String>> allResults) {
		int numLists = allResults.values().size();

		int[] indexArr = new int[numLists];

		for (int bitMap = 0; bitMap < Math.pow(10,  numLists); bitMap++) {
			// Place each digit in the index array
			int i = numLists - 1;
			int tmp = bitMap;
			while (tmp != 0) {
				indexArr[i--] = tmp % 10;
				tmp = tmp / 10;
			}
			while (i >= 0) {
				indexArr[i--] = 0;
			}

			Map<String, Integer> inputMap = new HashMap<>(inputStringMap);
			validateCombination(inputMap, allResults, indexArr);
		}
	}

	private void validateCombination(Map<String, Integer> inputStringMap, Map<Integer, List<String>> allResults,
									   int[] indexArr) {
		List<List<String>> stringList = new ArrayList<>();
		stringList.addAll(allResults.values());
		for (int i = 0; i < indexArr.length; i++) {
			String string = stringList.get(i).get(indexArr[i]);

			for (Character ch : string.toCharArray()) {
				String s = "" + ch;
				int count = inputStringMap.get(s);
				if (count == 0) {
					return;
				}
				inputStringMap.put(s, count - 1);
			}
		}

		System.out.println("Found a valid combination!");

		for (int i = 0; i < indexArr.length; i++) {
			System.out.print(stringList.get(i).get(indexArr[i]) + " ");
		}
		System.out.println();
	}

	public static void main(String[] args) throws Exception {

		WordBrain wordBrain = new WordBrain();

		String[][] inputArrA = new String[][] {
				{ "y", "d", "o", "l" },
				{ "a", "p", "i", "a" },
				{ "r", "p", "r", "d" },
				{ "s", "e", "e", "m" }
		};

		//int[] lengths = { 4, 5, 6 };
		int[] lengths = { 5, 6 };
		Map<String, Integer> inputStringMap = new HashMap<>();
		for (String[] strings : inputArrA) {
			for (String string : strings) {
				Integer count = inputStringMap.get(string);
				if (count == null) {
					count = 0;
				}
				inputStringMap.put(string, count + 1);
			}
		}

		List<String> results = null;
		Map<Integer, List<String>> allResults = new HashMap();
		int prevLen = -1;
		for (int length : lengths) {
			if (length != prevLen) {
				results = wordBrain.printWords(inputArrA, length);
			}
			prevLen = length;
			allResults.put(length, results);
		}
		wordBrain.findUniqueN(inputStringMap, allResults);
	}

	private class FindWords implements Runnable {

		String[][] inputArr;
		int iIndex;
		int jIndex;
		int maxLen;
		ArrayList<String> buf;
		boolean[][] visited;
		ArrayList<String> results;

		FindWords(String[][] inputArr, int i, int j, int maxLen) {
			this.inputArr = inputArr;
			iIndex = i;
			jIndex = j;
			this.maxLen = maxLen;
			buf = new ArrayList<>();
			results = new ArrayList<>();
			visited = new boolean[inputArr.length][inputArr[i].length];
		}

		public void run() {
			try {
				printWords(inputArr, iIndex, jIndex, buf, maxLen, visited);
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		List<String> getResults() {
			return results;
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
					results.add(sb.toString());
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
