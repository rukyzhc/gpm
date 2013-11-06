package net.zhanghc.gpm.data;

public class SimpleDocument extends Document {
	protected String tag;

	public SimpleDocument(String tag, int size) {
		super(size);
		this.tag = tag;
	}
	
	public String getTag() {
		return tag;
	}

}
