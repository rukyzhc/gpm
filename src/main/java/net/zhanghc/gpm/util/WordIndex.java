package net.zhanghc.gpm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.zhanghc.toolkit.stat.Counter;

public class WordIndex {
	Map<String, Integer> inverseIndex;
	Counter<String> wordFreq;
	List<String> index;

	public WordIndex() {
		inverseIndex = new HashMap<String, Integer>();
		index = new ArrayList<String>();
		wordFreq = new Counter<String>();
	}

	public int addWord(String word) {
		String w = word.toLowerCase();

		wordFreq.count(word);
		if(inverseIndex.containsKey(w)) {
			return inverseIndex.get(w);
		} else {
			int ni = index.size();
			index.add(w);
			inverseIndex.put(w, ni);
			return ni;
		}
	}
	
	public int frequency(String w) {
		return wordFreq.result(w);
	}
	
	public int frequency(int i) {
		return wordFreq.result(index.get(i));
	}

	public int size() {
		return index.size();
	}

	public String lookup(int i) {
		if(i < 0 || i > index.size()) {
			return null;
		} else {
			return index.get(i);
		}
	}
	
	public int lookup(String token) {
		if(inverseIndex.containsKey(token)) {
			return inverseIndex.get(token);
		} else {
			return -1;
		}
	}

}
