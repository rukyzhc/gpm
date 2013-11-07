package net.zhanghc.gpm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordIndex {
	Map<String, Integer> inverseIndex;
	List<String> index;

	public WordIndex() {
		inverseIndex = new HashMap<String, Integer>();
		index = new ArrayList<String>();
	}

	public int addWord(String word) {
		String w = word.toLowerCase();

		if(inverseIndex.containsKey(w)) {
			return inverseIndex.get(w);
		} else {
			int ni = index.size();
			index.add(w);
			inverseIndex.put(w, ni);
			return ni;
		}
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
