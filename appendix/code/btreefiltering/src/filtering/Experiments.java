package filtering;

import java.io.IOException;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import filtering.Grammar.Production;

public class Experiment {
	public static final void main(String[] args) throws IOException {
		runExperiment2();
//		Grammar g = new Grammar("grammars/small_full_grammar.txt", true, false);
//		g.filter("a b", true);
//		System.out.println(g.activeRules);
	}
	
	private static void runExperiment3() throws IOException {
		// Show median filter time and worst-case filter time for
		// terminal tree filter w/ early termination
		
		PrintWriter pw = new PrintWriter(new FileWriter(new File("terminating_tree_stats_tmp.csv")));
		
		String header = "|g|,|t|,|p|,worst_case,|s|_worst,median,|s|_median";
		pw.println(header);
		
		File dir = new File("grammars/bad_grammars");
		File[] directoryListing = dir.listFiles();
		int i = 0;
		if (directoryListing != null) {
			for (File child : directoryListing) {
				double start =System.currentTimeMillis();
				System.out.println("~~~~~ grammar " + i + " ~~~~~");
				Grammar g = new Grammar(child.getAbsolutePath(), true, true);
				System.out.println("Read grammar " + child.getName() + " in " + (System.currentTimeMillis() - start) + " ms");
				System.out.println("Run grammar of size " + g.size);
				String result = run(g, true);
				System.out.println("\t" + result);
				pw.println(result);
				pw.flush();
				i++;
			}
		}
		pw.close();
	}
	
	private static void runExperiment2() throws IOException {
		// Show median filter time and worst-case filter time for
		// terminal tree filter
		
		PrintWriter pw = new PrintWriter(new FileWriter(new File("terminal_tree_filter_stats_tmp.csv")));
		
		String header = "|g|,|t|,|p|,worst_case,|s|_worst,median,|s|_median";
		pw.println(header);
		
		File dir = new File("grammars/bad_grammars");
		File[] directoryListing = dir.listFiles();
		int i = 0;
		if (directoryListing != null) {
			for (File child : directoryListing) {
				double start =System.currentTimeMillis();
				System.out.println("~~~~~ grammar " + i + " ~~~~~");
				Grammar g = new Grammar(child.getAbsolutePath(), true, false);
				System.out.println("Read grammar " + child.getName() + " in " + (System.currentTimeMillis() - start) + " ms");
				System.out.println("Run grammar of size " + g.size);
				String result = run(g, true);
				System.out.println("\t" + result);
				pw.println(result);
				pw.flush();
				i++;
			}
		}
		pw.close();
	}
	
	private static void runExperiment1() throws IOException {
		// Show median filter time and worst-case filter time for
		// b-filter
		
		PrintWriter pw = new PrintWriter(new FileWriter(new File("basic_filter_stats_tmp.csv")));
		
		String header = "|g|,|t|,|p|,worst_case,|s|_worst,median,|s|_median";
		pw.println(header);
		
		File dir = new File("grammars/bad_grammars");
		File[] directoryListing = dir.listFiles();
		int i = 0;
		if (directoryListing != null) {
			for (File child : directoryListing) {
				double start =System.currentTimeMillis();
				System.out.println("~~~~~ grammar " + i + " ~~~~~");
				Grammar g = new Grammar(child.getAbsolutePath(), false, false);
				System.out.println("Read grammar " + child.getName() + " in " + (System.currentTimeMillis() - start) + " ms");
				System.out.println("Run grammar of size " + g.size);
				String result = run(g, false);
				System.out.println("\t" + result);
				pw.println(result);
				pw.flush();
				i++;
			}
		}
		pw.close();
	}

	private static String run(Grammar g, boolean tree) {
		System.out.println("Start experiment");
		String[] allStringsInLanguage = getLanguage(g);

		int ITERS = 10;
		int g_size = g.size;
		int t_size = g.terminals.size();
		int p_size = g.filterableProductions.size() + g.nonterminalsOnly.size();
		double worstCase = 0;
		double worstSize = 0;
		double median = 0;
		double medianSize = 0;
		

		System.out.println("\tRunning median case experiments");
		for(int i = 0; i < ITERS; i++) {
			String randomElementInLanguage = getRandomElement(allStringsInLanguage);
			double start = System.currentTimeMillis();
			g.filter(randomElementInLanguage, tree);
			double runTime = System.currentTimeMillis()-start;
			median += runTime;
			medianSize += randomElementInLanguage.length();
		}
		median /= ITERS;
		medianSize /= ITERS;
		
		System.out.println("\tRunning worst case experiments");
		for(int i = 0; i < ITERS; i++) {
			double start = System.currentTimeMillis();
			g.filter(allStringsInLanguage[0], tree);
			double runTime = System.currentTimeMillis()-start;
			worstCase += runTime;
			worstSize += allStringsInLanguage[0].length();
		}
		worstCase /= ITERS;
		worstSize /= ITERS;
		
		return String.format("%d,%d,%d,%f,%f,%f,%f", g_size, t_size, p_size, worstCase, worstSize,
				median, medianSize);
	}

	private static String getRandomElement(String[] allStringsInLanguage) {
		int rnd = new Random().nextInt(allStringsInLanguage.length);
	    return allStringsInLanguage[rnd];
	}

	private static String[] getLanguage(Grammar g) {
		LinkedList<String> stringSet = new LinkedList<>();
		for(Production p : g.filterableProductions) {
			stringSet.add(String.join(" ", p.rhs));
		}
		stringSet.sort(new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return -1*Integer.compare(arg0.length(), arg1.length());
			}
		});
		
		Object[] gfg = stringSet.toArray();
		String[] str = Arrays.copyOf(gfg, 
                gfg.length, 
                String[].class);
		return str;
	}
}
