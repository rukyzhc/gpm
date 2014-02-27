package net.zhanghc.gpm.data;

public abstract class Document {
	int[] words;
	
	public abstract String getDocID();

	public Document(int size) {
		this.words = new int[size];
	}

	public void set(int i, int w) {
		words[i] = w;
	}

	public int[] getWords() {
		return words;
	}

}
