package su.org.ms.parsers.mathcalc;

import su.org.ms.parsers.common.ParserException;
import su.org.ms.parsers.common.CalcException;
import su.org.ms.utils.Pair;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 05.08.2007
 * Time: 22:47:16
 *
 */
public class PrecGener
{


	public static final String MARKER = "MR";
	public static final String BEGSYMBL = "EH";
	public static final String MRE = ">";
	public static final String EQL = "=";
	public static final String LSS = "<";
	//The fields whitch set in the first phase
	private HashSet<String>[][] prectable;
	private String[] symbolsset;
	private String[] terminalset;
	private String[] nonterminalset;
	private String[] RHS_BEGSYMBL;

	public PrecGener(String[] args,Pair<String[], IToken>[] rules)
			throws ParserException
	{
		HashSet<String> hsterminals = new HashSet<String>();
		HashSet<String> hsnonterminals = new HashSet<String>();
		GrammarUtils.createSymbolsSet(rules,hsnonterminals, hsterminals);

		nonterminalset = hsnonterminals.toArray(new String[0]);
		terminalset = hsterminals.toArray(new String[0]);

		symbolsset = new String[nonterminalset.length + terminalset.length];
		for (int i = 0; i < nonterminalset.length; i++)
			symbolsset[i] = nonterminalset[i];

		for (int i = 0; i < terminalset.length; i++)
			symbolsset[i + nonterminalset.length] = terminalset[i];


		LinkedList<String> terminals = new LinkedList<String>(hsterminals);
		LinkedList<String> nonterminals = new LinkedList<String>(hsnonterminals);

		prectable = fillTableByGrammar(rules, nonterminals, terminals);
	}

	public static int getSymbolIndex(String symbol, LinkedList<String> nonterminals, LinkedList<String> terminals) throws ParserException
	{
		for (int i = 0; i < nonterminals.size(); i++)
		{
			if (symbol.equals(nonterminals.get(i)))
				return i;
		}

		for (int i = 0; i < terminals.size(); i++)
		{
			if (symbol.equals(terminals.get(i)))
				return i + nonterminals.size();
		}
		throw new ParserException("Unknown symbol:" + symbol);
	}

	public static String getSymbolByIndex(int index, LinkedList<String> nonterminals, LinkedList<String> terminals) throws ParserException
	{
		if (index < nonterminals.size())
			return nonterminals.get(index);
		else
			return terminals.get(index - nonterminals.size());
	}

	public static void main(String[] args) throws ParserException
	{

		String[] grammar=new String[]
		{
				"EHH,EH",

				"EH,EH,op_c,E",
				"EH,E",

				"E,E,op_a,T",
				"E,T",
				"A,A,sp,EH",
				"A,EH",
				"T,P",
				"T,T,op_m,P",
				"P,i",
				"P,n",
				"P,F",
				"F,f,)",
				"F,f,A,)",
				"P,(,EH,)"
		};
		Pair<String[], IToken>[] rules = GrammarUtils.generateTstRulesByStringArray(grammar);

		new PrecGener(args,rules).testPrint();
	}

	public HashSet<String> getPredAttitude(String storsmbl, String insymbol) throws ParserException
	{
		int storix = getIndexBySymbol(storsmbl);
		int inix = getIndexBySymbol(insymbol);
		return prectable[storix][inix];
	}

	public int getIndexBySymbol(String symbol) throws ParserException
	{
		LinkedList<String> nonterminals = new LinkedList<String>();
		for (int i = 0; i < nonterminalset.length; i++)
			nonterminals.add(nonterminalset[i]);

		LinkedList<String> terminals = new LinkedList<String>();
		for (int i = 0; i < terminalset.length; i++)
			terminals.add(terminalset[i]);

		return getSymbolIndex(symbol, nonterminals, terminals);
	}

	public boolean inRSH_E(String symbol)
	{
		for (int i = 0; i < RHS_BEGSYMBL.length; i++)
			if (RHS_BEGSYMBL[i].equals(symbol))
				return true;
		return false;
	}

	public HashSet<String>[][] fillTableByGrammar(Pair<String[], IToken>[] grammar, LinkedList<String> nonterminals, LinkedList<String> terminals) throws ParserException
	{

		int sz = nonterminals.size() + terminals.size();
		HashSet<String>[][] retVal = new HashSet[sz][sz];

		HashMap<String, String[]> lhs = new HashMap<String, String[]>();
		HashMap<String, String[]> rhs = new HashMap<String, String[]>();
		HashSet<String> notermset = new HashSet<String>(nonterminals);
		for (String nonterminal : nonterminals)
		{
			lhs.put(nonterminal, GrammarUtils.formLHS(nonterminal, grammar, notermset));
			rhs.put(nonterminal, GrammarUtils.formRHS(nonterminal, grammar, notermset));

		}

		RHS_BEGSYMBL = rhs.get(BEGSYMBL);

		for (Pair<String[], IToken> rule : grammar)
		{
			for (int i = 1; i < rule.first.length - 1; i++)
			{
				String s1 = rule.first[i];
				int index1 = getSymbolIndex(s1, nonterminals, terminals);
				String s2 = rule.first[i + 1];
				int index2 = getSymbolIndex(s2, nonterminals, terminals);
				if (retVal[index1][index2] == null)
					retVal[index1][index2] = new HashSet<String>();

				retVal[index1][index2].add(EQL);

				String[] slhs = lhs.get(s2);
				if (slhs != null)
					for (String slh : slhs)
					{
						int index22 = getSymbolIndex(slh, nonterminals, terminals);
						if (retVal[index1][index22] == null)
							retVal[index1][index22] = new HashSet<String>();

						retVal[index1][index22].add(LSS);
					}

				String[] srhs = rhs.get(s1);
				if (srhs != null)
					for (String srh : srhs)
					{
						int index11 = getSymbolIndex(srh, nonterminals, terminals);
						if (retVal[index11][index2] == null)
							retVal[index11][index2] = new HashSet<String>();
						retVal[index11][index2].add(MRE);

						if (slhs != null)
							for (String slh : slhs)
							{
								int index22 = getSymbolIndex(slh, nonterminals, terminals);
								if (retVal[index11][index22] == null)
									retVal[index11][index22] = new HashSet<String>();
								retVal[index11][index22].add(MRE);
							}
					}
			}
		}

		return retVal;
	}

	private void testPrint() throws ParserException
	{
		LinkedList<String> nonterminals = new LinkedList<String>();
		LinkedList<String> terminals=new LinkedList<String>();

		System.out.print("  ");
		for (String nonterminal : nonterminalset)
		{
			System.out.print(nonterminal+" ");
			nonterminals.add(nonterminal);

		}
		for (String terminal : terminalset)
		{
			System.out.print(terminal+" ");
			terminals.add(terminal);
		}
		System.out.println();


		for (int i = 0; i < prectable.length; i++)
		{
			String s1 = getSymbolByIndex(i, nonterminals, terminals);
			System.out.print(s1 +" ");

			HashSet<String>[] atts = prectable[i];
			for (int j = 0; j < atts.length; j++)
			{
				String s2 = getSymbolByIndex(j, nonterminals, terminals);
				HashSet<String> att = atts[j];
				if (att==null)
					System.out.print(" ETY");
				else
				{
					String attrstr="";
					for (String s : att)
						if (attrstr.length()==0)
							attrstr=s;
						else
							attrstr+="/"+s;

					System.out.print(" " + attrstr);
				}
			}
			System.out.println();
		}

	}

	public HashSet<String>[][] getPrectable()
	{
		return prectable;
	}

	public String[] getSymbolsset()
	{
		return symbolsset;
	}

	public String[] getTerminalset()
	{
		return terminalset;
	}

	public String[] getNonterminalset()
	{
		return nonterminalset;
	}

	public String[] getRHS_BEGSYMBL()
	{
		return RHS_BEGSYMBL;
	}

	public static class Marker implements IToken
	{

		public String getRepresent()
		{
			return null;
		}

		public boolean isFunction()
		{
			return false;
		}

		public String getToken()
		{
			return MARKER;
		}

		public double getValue() throws CalcException
		{
			throw new CalcException("Marker has no value");
		}

		public int getNrule()
		{
			return -1;
		}
	}
}

//		for (String nonterminal : nonterminals)
//		{
//			String[] lhs=formRHS(nonterminal,rules,hsnonterminals);
//			System.out.print("for " + nonterminal+" : {");
//			for (String lh : lhs)
//			{
//				System.out.print(" " + lh);
//			}
//			System.out.println(" }");
//		}
