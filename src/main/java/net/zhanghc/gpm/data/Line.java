package net.zhanghc.gpm.data;

public class Line {
	String tag;
	String author;
	String target;
	String text;

	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String toMeta() {
		return String.format("%s\t%s\t%s", tag, author, target);
	}
}
