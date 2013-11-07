package net.zhanghc.gpm.data;

import java.util.List;

public class Line {
	String tag;
	String author;
	String target;
	String text;
	List<String> tokens;
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public void setText(String text) {
		this.text = text;
	}
	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}
	public String getTag() {
		return tag;
	}
	public String getAuthor() {
		return author;
	}
	public String getTarget() {
		return target;
	}
	public String getText() {
		return text;
	}
	public List<String> getTokens() {
		return tokens;
	}
	public String toMeta() {
		return String.format("%s\t%s\t%s", tag, author, target);
	}
}
