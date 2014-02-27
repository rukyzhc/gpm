package net.zhanghc.gpm.data;

public class SimpleDocument extends Document {
	protected String id;

	public SimpleDocument(String tag, int size) {
		super(size);
		this.id = tag;
	}

	@Override
	public String getDocID() {
		return id;
	}

}
