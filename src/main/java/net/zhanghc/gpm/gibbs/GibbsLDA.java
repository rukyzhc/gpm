package net.zhanghc.gpm.gibbs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.tartarus.snowball.ext.EnglishStemmer;

import net.zhanghc.gpm.data.Document;
import net.zhanghc.gpm.data.Line;
import net.zhanghc.gpm.data.SimpleDocument;
import net.zhanghc.gpm.util.WordFilter;
import net.zhanghc.gpm.util.WordIndex;
import net.zhanghc.toolkit.io.QuickFileWriter;
import net.zhanghc.toolkit.stat.Value;

public final class GibbsLDA extends GibbsSampling {
	private WordFilter filter = WordFilter.create(null, null);

	public void setWordFilter(WordFilter filter) {
		this.filter = filter;
	}

	@Override
	public Document createDocument(Line line, WordIndex index) {
		String text = line.getText();
		String[] raw = text.split(" ");
		List<String> words = new ArrayList<String>();

		for(String s : raw) {
			if(filter.accept(s)) {
				words.add(s);
			}
		}

		Document d = new SimpleDocument(line.toMeta(), words.size());
		EnglishStemmer stemmer = new EnglishStemmer();
		int i = 0;
		for(String s : words) {
			stemmer.setCurrent(s);
			stemmer.stem();

			d.set(i++, index.addWord(stemmer.getCurrent()));
		}
		return d;
	}

	public int Topic = 20;
	public double Alpha = 50.0 / Topic;
	public double Beta = 0.1;

	private int [][] z; //topic label array

	private int T, V, M; //vocabulary size, topic number, document number  
	private int [][] N_mk; //given document m, count times of topic k. M*K  
	private int [][] N_kt; //given topic k, count times of term t. K*V  
	private int [] N_m; //Sum for each row in nmk  
	private int [] N_k; //Sum for each row in nkt

	//parameters
	private double [][] phi; //Parameters for topic-word distribution K*V  
	private double [][] theta; //Parameters for doc-topic distribution M*K  

	@Override
	public void initial() {
		V = index.size();
		M = documents.size();
		T = Topic;

		N_mk = new int [M][T];  
		N_kt = new int[T][V];  
		N_m = new int[M];  
		N_k = new int[T];  
		phi = new double[T][V];  
		theta = new double[M][T];

		//initialize topic lable z for each word  
		z = new int[M][];  
		for(int m = 0; m < M; m++){  
			int N = documents.get(m).getWords().length;  
			z[m] = new int[N];  
			for(int n = 0; n < N; n++){  
				int initTopic = (int)(Math.random() * T);// From 0 to K - 1  
				z[m][n] = initTopic;  
				//number of words in doc m assigned to topic initTopic add 1  
				N_mk[m][initTopic]++;  
				//number of terms doc[m][n] assigned to topic initTopic add 1  
				N_kt[initTopic][documents.get(m).getWords()[n]]++;
				// total number of words assigned to topic initTopic add 1  
				N_k[initTopic]++;  
			}  
			// total number of words in document m is N  
			N_m[m] = N;  
		}
	}

	@Override
	public void update() {
		for(int m = 0; m < M; m++) {
			int[] words = documents.get(m).getWords();
			for(int n = 0; n < words.length; n++) {
				z[m][n] = sample(m, n, words[n]);
			}
		}
	}

	private int sample(int m, int n, int w) {
		int _z = z[m][n];

		N_mk[m][_z]--;
		N_kt[_z][w]--;
		N_m[m]--;
		N_k[_z]--;

		double[] prob = new double[T];
		for(int k = 0; k < T; k++) {
			prob[k] = ((N_kt[k][w] + Beta) / (N_k[k] + V * Beta)) * ((N_mk[m][k] + Alpha) / (N_m[m] + T * Alpha));
		}

		int nz = super.sample(prob);

		N_mk[m][nz]++;
		N_m[m]++;
		N_kt[nz][w]++;
		N_k[nz]++;

		return nz;
	}

	@Override
	public void parameter() {
		for(int k = 0; k < T; k++) {
			for(int n = 0; n < V; n++) {
				phi[k][n] = (N_kt[k][n] + Beta) / (N_k[k] + V * Beta);
			}
		}
		for(int m = 0; m < M; m++) {
			for(int k = 0; k < T; k++) {
				theta[m][k] = (N_mk[m][k] + Alpha) / (N_m[m] + T * Alpha);
			}
		}
	}

	@Override
	protected void export(int iter, String path) throws IOException {
		export(String.format("%s/iter_%d.theta", path, iter), String.format("%s/iter_%d.phi", path, iter), 20);
	}

	@Override
	public void export(String path, String name) throws IOException {
		export(String.format("%s/%s.theta", path, name), String.format("%s/%s.phi", path, name), 20);

	}

	private void export(String thetaName, String phiName, int maxWordPerTopic) throws IOException {
		QuickFileWriter t = new QuickFileWriter(thetaName, "utf-8");
		for(int k = 0; k < T; k++) {
			t.printf("[Topic%d]", k);

			Value<String> value = new Value<String>();
			for(int m = 0; m < V; m++) {
				value.record(index.lookup(m), phi[k][m]);
			}
			int c = 0;
			for(Map.Entry<String, Double> entry : value.sortedValues(true)) {
				if(++c > maxWordPerTopic)
					break;

				t.printf("\t[%s:%.3e]", entry.getKey(), entry.getValue());
			}
			t.newLine();
		}
		t.close();

		QuickFileWriter p = new QuickFileWriter(phiName, "utf-8");
		for(int m = 0; m < M; m++) {
			p.printf("[%s]", ((SimpleDocument)documents.get(m)).getTag());
			for(int k = 0; k < T; k++) {
				p.printf("\t[%d:%.3e]", k, theta[m][k]);
			}
			p.newLine();
		}
		p.close();
	}

}