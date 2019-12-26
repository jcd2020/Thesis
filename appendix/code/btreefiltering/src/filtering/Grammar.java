package filtering;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Grammar {
	static class Production {
		String lhs;
		String[] rhs;
		HashSet<String> terminals;
		String deepestSymbol;
		
		public Production(String lhs, String[] rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
			this.terminals = new HashSet<>();
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof Production) {
				Production p = (Production)o;
				return lhs.equals(p.lhs) && Arrays.equals(rhs, p.rhs);
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(lhs, rhs);
		}
		
		@Override
		public String toString() {
			return lhs + "->" + String.join(" ", rhs);
		}
	}
	
	HashSet<Production> filterableProductions;
	HashSet<Production> nonterminalsOnly;
	HashSet<String> terminals;
	HashSet<Production> activeRules;
	HashSet<String> vocabulary;
	HashSet<String> nonterminals;
	int size = 0;
	Tree btree;
	EarlyTerminatingTree btree2;

	HashMap<String, Integer> sym2level;
	
	public Grammar(String fileName, boolean buildTree, boolean earlyTerminatingTree) throws IOException {
		System.out.println("Start loading grammar " + fileName);
		filterableProductions = new HashSet<>();
		terminals = new HashSet<>();
		vocabulary = new HashSet<>();
		nonterminals = new HashSet<>();
		nonterminalsOnly = new HashSet<>();
		List<String> indexing = new LinkedList<>();
		sym2level = new HashMap<String, Integer>();
		BufferedReader bf = new BufferedReader(new FileReader(new File(fileName)));
		while(bf.ready()) {
			String[] rule = bf.readLine().split("->");
			String lhs = rule[0].intern();
			nonterminals.add(lhs);
			String[] rhs = rule[1].split(" ");
			size += rhs.length;
			for(String token : rhs) {
				if(!vocabulary.contains(token.intern())) {
					indexing.add(token);
					vocabulary.add(token);
					sym2level.put(token, sym2level.size());
				}
			}
			filterableProductions.add(new Production(lhs, rhs));
		}
		bf.close();
		Iterator<Production> iter = filterableProductions.iterator();
		for(String sym : vocabulary) {
			if(!nonterminals.contains(sym)) {
				terminals.add(sym);
			}
		}
		while(iter.hasNext()) {
			Production p = iter.next();
			boolean hasTerminal = false;
			for(String symbol : p.rhs) {
				if(terminals.contains(symbol)) {
					p.terminals.add(symbol);
					hasTerminal = true;
				}
			}
			if(!hasTerminal) {
				nonterminalsOnly.add(p);
				iter.remove();
			}
		}
		
		if(buildTree) {
			System.out.println("\tbuilding tree");
			if(earlyTerminatingTree) {
				for(Production p : filterableProductions) {
					int max = -1;
					String maxSym = null;
					for(String sym : p.rhs) {
						if(sym2level.get(sym) > max) {
							maxSym = sym;
							max = sym2level.get(sym);
						}
					}
					p.deepestSymbol = maxSym;
				}
				this.buildTree(indexing, true);
			} else {
				this.buildTree(indexing, false);
			}
		}
	}
	
	public void terminalTreeFilter(String input) {
		activeRules = new HashSet<>();
		String[] tokens = input.split(" ");
		HashSet<String> terminalsInInput = new HashSet<>();
		
		for(String token : tokens) {
			if(terminals.contains(token)) {
				terminalsInInput.add(token);
			}
		}
		
		Object[] res = getFilteredTerminals(terminalsInInput);
		HashSet<String> filteredTerminals = (HashSet<String>) res[0];
		int deepestLevel = (int) res[1];
		
		if(btree != null)
			Tree.filter(btree, activeRules, filteredTerminals, deepestLevel);
		else
			EarlyTerminatingTree.filter(btree2, activeRules, filteredTerminals, deepestLevel);
		
		for(Production p : nonterminalsOnly) {
			activeRules.add(p);
		}
	}
	
	public void buildTree(List<String> indexing, boolean earlyTerminating) {
		if(earlyTerminating)
			this.btree2 = EarlyTerminatingTree.buildTree(indexing, this.filterableProductions);
		else
			this.btree = Tree.buildTree(indexing, this.filterableProductions);
	}
	
	public void bFilter(String input) {
		activeRules = new HashSet<>();
		String[] tokens = input.split(" ");
		HashSet<String> terminalsInInput = new HashSet<>();
		
		for(String token : tokens) {
			if(terminals.contains(token)) {
				terminalsInInput.add(token);
			}
		}
		
		Object[] res = getFilteredTerminals(terminalsInInput);
		HashSet<String> filteredTerminals = (HashSet<String>) res[0];
		int deepestLevel = (int) res[1];
		
		for(Production p : filterableProductions) {
			boolean ruledOut = false;
			for(String terminal : p.terminals) {
				if(filteredTerminals.contains(terminal)) {
					ruledOut = true;
				}
			}
			if(!ruledOut) {
				activeRules.add(p);
			}
		}
		
		for(Production p : nonterminalsOnly) {
			activeRules.add(p);
		}
	}

	private Object[] getFilteredTerminals(HashSet<String> terminalsInInput) {
		HashSet<String> filteredTerminals = new HashSet<>();
		
		Integer max = 0;
		for(String terminal : terminals) {
			if(!terminalsInInput.contains(terminal)) {
				filteredTerminals.add(terminal);
				if(sym2level.get(terminal) > max) {
					max = sym2level.get(terminal);
				}
			}
		}
		 
		return new Object[] {filteredTerminals, max};
	}

	public void filter(String input, boolean tree) {
		if(tree) {
			terminalTreeFilter(input);
		} else {
			bFilter(input);
		}
	}
	
}
