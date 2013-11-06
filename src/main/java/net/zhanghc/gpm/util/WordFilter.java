package net.zhanghc.gpm.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.zhanghc.toolkit.io.QuickFileReader;

public class WordFilter {
	
	public final static String DefaultEnglishStopword = "stopword_english";
	
	public static WordFilter create(String pattern, String stopword) {
		WordFilter wf = new WordFilter();
		
		//pattern
		if(pattern != null) {
			wf.regex = Pattern.compile(pattern);
		} else {
			wf.regex = null;
		}
		
		//stopword
		if(stopword == null) {
			wf.stopwords = null;
		} else {
			try {
				QuickFileReader qr = new QuickFileReader(stopword, "utf-8");
				String line = null;
				wf.stopwords = new HashSet<String>();
				while((line = qr.readLine()) != null) {
					wf.stopwords.add(line.trim().toLowerCase());
				}
				qr.close();
			} catch (UnsupportedEncodingException e) {
				wf.stopwords = null;
			} catch (FileNotFoundException e) {
				wf.stopwords = null;
			} catch (IOException e) {
				wf.stopwords = null;
			}
		}
		return wf;
	}
	
	Pattern regex;
	Set<String> stopwords;
	
	protected WordFilter() {
	}
	
	public boolean accept(String token) {
		if(stopwords != null && stopwords.contains(token.toLowerCase())) {
			return false;
		}
		
		return regex == null ? true : regex.matcher(token).matches();
	}

}
