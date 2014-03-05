package net.zhanghc.gpm.gibbs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.zhanghc.gpm.data.Document;
import net.zhanghc.gpm.data.Line;
import net.zhanghc.gpm.data.SimpleDocument;
import net.zhanghc.toolkit.collections.Value;
import net.zhanghc.toolkit.io.QuickFileWriter;

public class GibbsJST extends GibbsSampling {
	Map<Integer, Integer> lexicon = new HashMap<Integer, Integer>();
	public boolean Prior = false;
	
	public void setPrior(String token, int priorSentiLabel) {
		Prior = true;
		lexicon.put(wrdIndex.addWord(token), priorSentiLabel);
	}

	@Override
	public Document createDocument(Line line) {
		return new SimpleDocument(line.toMeta(), line.getTokens().size());
	}

	public int Topic = 50;
	public double Alpha = 50.0 / Topic;
	public double Beta = 0.01;
	public double[] Gamma = new double[]{0.5, 0.5};
	public int TopNWord = 20;
	
	private int[][] z;
	private int[][] l;

	private int T = Topic, S = Gamma.length, V, M;
	//i : index of term
	//j : index of topic
	//k : index of sentiment
	//d : index of document
	private int[][][] N_ijk;
	private int[][] N_jk;
	private int[][][] N_jkd;
	private int[][] N_kd;
	private int[] N_d;

	//parameters
	private double[][][] phi;
	private double[][][] theta;
	private double[][] pi;
	
	@Override
	public void initial() {
		V = wrdIndex.size();
		M = documents.size();

		N_ijk = new int[V][T][S];
		N_jk = new int[T][S];
		N_jkd = new int[T][S][M];
		N_kd = new int[S][M];
		N_d = new int[M];

		z = new int[M][];
		l = new int[M][];

		for(int m = 0; m < M; m++) {
			int N = documents.get(m).getWords().length;
			z[m] = new int[N];
			l[m] = new int[N];
			for(int n = 0; n < N; n++) {
				int iz = (int)(Math.random() * T);
				int il = (int)(Math.random() * S);
				int w = documents.get(m).getWords()[n];
				
				if(Prior && lexicon.containsKey(w)) {
					il = lexicon.get(w);
				}
				
				z[m][n] = iz;
				l[m][n] = il;

				N_ijk[w][iz][il]++;
				N_jk[iz][il]++;
				N_jkd[iz][il][m]++;
				N_kd[il][m]++;
			}
			N_d[m] = N;
		}
		
		phi = new double[V][T][S];
		theta = new double[T][S][M];
		pi = new double[S][M];
	}

	@Override
	public void update() {
		for(int m = 0; m < M; m++) {
			int[] words = documents.get(m).getWords();
			for(int n = 0; n < words.length; n++) {
				remove(m, n, words[n]);
				int s = sample(m, n, words[n]);
				z[m][n] = topic(s);
				l[m][n] = sentiment(s);
				resume(m, n, words[n]);
			}
		}
	}
	
	public int sample(int m, int n, int w) {		
		double[] prob = new double[T * S];
		for(int t = 0; t < T; t++) {
			for(int s = 0; s < S; s++) {
				prob[encode(t, s)] = 
						((N_ijk[w][t][s] + Beta) / (N_jk[t][s] + V * Beta)) *
						((N_jkd[t][s][m] + Alpha) / (N_kd[s][m] + T * Alpha)) *
						((N_kd[s][m] + Gamma[s]) / (N_d[m] + S * Gamma[s]));
			}
		}

		return super.sample(prob);
	}

	@Override
	public void parameter() {
		for(int i = 0; i < V; i++) {
			for(int j = 0; j < T; j++) {
				for(int k = 0; k < S; k++) {
					phi[i][j][k] = (N_ijk[i][j][k] + Beta) / (N_jk[j][k] + V * Beta); 
				}
			}
		}
		for(int j = 0; j < T; j++) {
			for(int k = 0; k < S; k++) {
				for(int d = 0; d < M; d++) {
					theta[j][k][d] = (N_jkd[j][k][d] + Alpha) / (N_kd[k][d] + T * Alpha);
				}
			}
		}
		for(int k = 0; k < S; k++) {
			for(int d = 0; d < M; d++) {
				pi[k][d] = (N_kd[k][d] + Gamma[k]) / (N_d[d] + S * Gamma[k]);
			}
		}
	}

	@Override
	protected void export(int iter, String path) throws IOException {
		export(String.format("%s/iter_%d.theta", path, iter), 
				String.format("%s/iter_%d.phi", path, iter), 
				String.format("%s/iter_%d.pi", path, iter), TopNWord);
	}

	@Override
	public void export(String path, String name) throws IOException {
		export(String.format("%s/%s.theta", path, name), 
				String.format("%s/%s.phi", path, name),
				String.format("%s/%s.pi", path, name), TopNWord);
	}
	
	private void export(String thetaName, String phiName, String piName, int maxWordPerTopic) throws IOException {
		QuickFileWriter tsw = new QuickFileWriter(phiName, "utf-8");
		for(int j = 0; j < T; j++) {
			for(int k = 0; k < S; k++) {
				tsw.printf("[Topic%d|Senti%d]", j, k);
				
				Value<String> value = new Value<String>();
				for(int v = 0; v < V; v++) {
					value.record(wrdIndex.lookup(v), phi[v][j][k]);
				}
				int c = 0;
				for(Map.Entry<String, Double> entry : value.sortedValues(true)) {
					if(++c > maxWordPerTopic)
						break;
					
					tsw.printf("\t[%s:%.3e]", entry.getKey(), entry.getValue());
				}
				tsw.newLine();
			}
		}
		tsw.close();

		QuickFileWriter tsd = new QuickFileWriter(thetaName, "utf-8");
		for(int m = 0; m < M; m++) {
			tsd.printf("[%s]", documents.get(m).getDocID());
			for(int j = 0; j < T; j++) {
				for(int k = 0; k < S; k++) {
					tsd.printf("\t[%d|%d:%.3e]", j, k, theta[j][k][m]);
				}
			}
			tsd.newLine();
		}
		tsd.close();
		
		QuickFileWriter kd = new QuickFileWriter(piName, "utf-8");
		for(int m = 0; m < M; m++) {
			kd.printf("[%s]", documents.get(m).getDocID());
			for(int k = 0; k < S; k++) {
				kd.printf("\t[%d:%.3e]", k, pi[k][m]);
			}
			kd.newLine();
		}
		kd.close();
	}
	
	private void remove(int m, int n, int w) {
		int _z = z[m][n];
		int _l = l[m][n];

		N_ijk[w][_z][_l]--;
		N_jk[_z][_l]--;
		N_jkd[_z][_l][m]--;
		N_kd[_l][m]--;
		N_d[m]--;
	}

	private void resume(int m, int n, int w) {
		int _z = z[m][n];
		int _l = l[m][n];

		N_ijk[w][_z][_l]++;
		N_jk[_z][_l]++;
		N_jkd[_z][_l][m]++;
		N_kd[_l][m]++;
		N_d[m]++;
	}
	
	private int encode(int t, int s) {
		return t * S + s;
	}

	private int topic(int sample) {
		return sample / S;
	}

	private int sentiment(int sample) {
		return sample % S;
	}

}
