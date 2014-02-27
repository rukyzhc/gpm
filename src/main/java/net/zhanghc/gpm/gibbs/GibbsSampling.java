package net.zhanghc.gpm.gibbs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import net.zhanghc.gpm.data.Document;
import net.zhanghc.gpm.data.Line;
import net.zhanghc.gpm.data.SimpleDocument;
import net.zhanghc.gpm.util.WordIndex;
import net.zhanghc.toolkit.io.QuickFileReader;
import net.zhanghc.toolkit.util.Progress;

public abstract class GibbsSampling {
	protected List<Document> documents;
	protected WordIndex wrdIndex;
	protected Map<String, Integer> docIndex;
	
	public int docSize() {
		return documents.size();
	}
	
	public int vocSize() {
		return wrdIndex.size();
	}
	
	public Set<String> allDocID() {
		return docIndex.keySet();
	}
	
	public List<String> loadText(String input) throws IOException {
		List<String> ret = new ArrayList<String>();
		
		QuickFileReader qr = new QuickFileReader(input, "utf-8");
		String line = null;
		
		documents = new ArrayList<Document>();
		wrdIndex = new WordIndex();
		docIndex = new HashMap<String, Integer>();
		
		int lno = 0;
		while((line = qr.readLine()) != null) {
			String[] words = line.split(" ");
			if(words.length == 0) {
				if(Verbose) {
					System.err.printf("Empty document in line %d, skip..", lno);
					System.err.println();
				}
			} else {
				Document doc = new SimpleDocument(String.format("%d", lno), words.length);
				for(int i = 0; i < words.length; i++) {
					doc.set(i, wrdIndex.addWord(words[i]));
				}
				docIndex.put(doc.getDocID(), documents.size());
				documents.add(doc);
				ret.add(line);
			}
			lno++;
		}
		
		if(Verbose) {
			System.err.printf("Loaded %d documents and %d words", documents.size(), wrdIndex.size());
			System.err.println();
		}
		qr.close();
		
		return ret;
	}
	
	public List<Line> load(String input) throws IOException {
		List<Line> ret = new ArrayList<Line>();
		
		QuickFileReader qr = new QuickFileReader(input, "utf-8");
		Gson gson = new Gson();
		String line = null;
		
		documents = new ArrayList<Document>();
		wrdIndex = new WordIndex();
		docIndex = new HashMap<String, Integer>();
		
		while((line = qr.readLine()) != null) {
			Line l = gson.fromJson(line, Line.class);
			ret.add(l);
			
			Document doc = createDocument(l);
			int i = 0;
			for(String token : l.getTokens()) {
				doc.set(i++, wrdIndex.addWord(token));
			}
			docIndex.put(doc.getDocID(), documents.size());
			documents.add(doc);
		}
		if(Verbose) {
			System.err.printf("Loaded %d documents and %d words", documents.size(), wrdIndex.size());
			System.err.println();
		}
		qr.close();
		
		return ret;
	}
	
	public abstract Document createDocument(Line line);
	
	public int MaxIteration = 1000;
	public boolean Verbose = false;
	
	public int DumpInterval = -1;
	public String DumpPath = null;
	
	public void inference() {
		initial();
		Progress p = new Progress(MaxIteration, 1);
		p.start();
		for(int i = 0; i < MaxIteration; i++) {
			if(Verbose) {
	    		System.err.printf("Iteration %d... ", i);
			}
			update();
			if(Verbose) {
				p.process();
	    		System.err.printf("ETA %.1f mins", p.eta(Progress.MINITE));
	    		System.err.println();
			}
			if(DumpInterval > 0 && (i + 1) % DumpInterval == 0) {
				parameter();
				try {
					export(i, DumpPath);
				} catch (IOException e) {
					System.err.println("Cannot dump temporal results. Caused by " + e.getMessage());
				}
			}
		}
		parameter();
	}

	public abstract void initial();

	public abstract void update();

	public abstract void parameter();

	protected abstract void export(int iter, String path) throws IOException;

	public abstract void export(String path, String name) throws IOException;

	protected int sample(double[] prob) {
		int T = prob.length;
		double[] p = new double[T];
		p[0] = prob[0];
		for(int i = 1; i < T; i++) {
			p[i] = prob[i] + p[i - 1];
		}
		
		double s = Math.random() * p[T - 1];
		
		int nz = 0;
    	for(nz = 0; nz < T; nz++) {
    		if(s < p[nz])
    			break;
    	}
    	
    	if(nz == T) {
    		nz = T - 1;
    	}
    	
    	return nz;
	}
	
}
