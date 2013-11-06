package net.zhanghc.gpm.data;

public class AuthorTargetDocument extends SimpleDocument {
	protected String author;
	protected String target;

	public AuthorTargetDocument(String author, String target, String tag, int size) {
		super(tag, size);
		this.author = author;
		this.target = target;
	}

}
