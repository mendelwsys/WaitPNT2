package su.org.ms.parsers.mathcalc;

import su.org.ms.utils.Pair;
import su.org.ms.parsers.common.CalcException;
import su.org.ms.parsers.common.ParserException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 23.09.2007
 * Time: 10:29:41
 *
 */
public class GrammarUtils
{
	public static String[] formLHS(String symbol, Pair<String[], IToken>[] grammar, Collection<String> nonterminals)
	{
		HashSet<String> lhs = new HashSet<String>();

		for (Pair<String[], IToken> aGrammar : grammar)
		{
			if (aGrammar.first[0].equals(symbol))
				lhs.add(aGrammar.first[1]);
		}
		int psz;
		do
		{
			psz = lhs.size();
			HashSet<String> l_lhs = new HashSet<String>(lhs);
			for (String l_lh : l_lhs)
			{
				if (nonterminals.contains(l_lh))
					for (Pair<String[], IToken> aGrammar : grammar)
						if (aGrammar.first[0].equals(l_lh))
							lhs.add(aGrammar.first[1]);
			}
		}
		while (psz != lhs.size());

		return lhs.toArray(new String[0]);
	}

	public static String[] formRHS(String symbol, Pair<String[], IToken>[] grammar, HashSet<String> nonterminals)
	{
		HashSet<String> rhs = new HashSet<String>();

		for (Pair<String[], IToken> aGrammar : grammar)
		{
			if (aGrammar.first[0].equals(symbol))
				rhs.add(aGrammar.first[aGrammar.first.length - 1]);
		}

		int psz;
		do
		{
			psz = rhs.size();
			HashSet<String> l_rhs = new HashSet<String>(rhs);
			for (String l_rh : l_rhs)
			{
				if (nonterminals.contains(l_rh))
					for (Pair<String[], IToken> aGrammar : grammar)
						if (aGrammar.first[0].equals(l_rh))
							rhs.add(aGrammar.first[aGrammar.first.length - 1]);
			}
		}
		while (psz != rhs.size());
		return rhs.toArray(new String[0]);
	}

	public static void createSymbolsSet(Pair<String[], IToken>[] rules,Collection<String> hsnonterminals, Collection<String> hsterminals)
	{
		 if (hsnonterminals.size()!=0) //Изоляция входной коллекции
		 	hsnonterminals=new HashSet<String>();

		if (hsterminals.size()!=0) //Изоляция входной коллекции
			hsterminals=new HashSet<String>();

		for (Pair<String[], IToken> rule : rules)
		{
			for (String symbol : rule.first)
				if (Character.isUpperCase(symbol.charAt(0)))
					hsnonterminals.add(symbol);
				else
					hsterminals.add(symbol);
		}
	}


	private static HashSet<String> getFromHashMap(HashMap<String, HashSet<String>> hm,String symbol)
	{
		HashSet<String> hashSet = hm.get(symbol);
		if (hashSet ==null)
		{
			hashSet = new HashSet<String>();
			hm.put(symbol, hashSet);
		}
		return hashSet;
	}

	public static HashMap<String, HashSet<String>> generateFollow(Pair<String[], IToken>[] rules,
																  HashMap<String, HashSet<String>> slhs,
																  Collection<String> hsterminals
		)
	{
		HashMap<String, HashSet<String>> follow= new HashMap<String, HashSet<String>>();

		HashSet<String> hs=getFromHashMap(follow,rules[0].first[0]);
		hs.add("$");//За начальной строкой идет символ конца строки
		//Проходим все правила и генерируем мно-ва для рядом стоящих символов
		for (Pair<String[], IToken> rule : rules)
		{
			for (int cnt=1;cnt<rule.first.length-1;cnt++)
			{
				hs=getFromHashMap(follow,rule.first[cnt]);
				String rulesymbol = rule.first[cnt + 1];
				HashSet<String> sl = slhs.get(rulesymbol);
				if (sl!=null)
					hs.addAll(sl);
				else if (hsterminals.contains(rulesymbol))
					hs.add(rulesymbol);
				else
					System.out.println("Hang symbol is:"+rulesymbol);
			}
		}

		boolean ismod;
		do
		{
			ismod=false;
//Для самых правых символов правила добавляем мно-ва их корней, пока мно-ва меняются
			for (Pair<String[], IToken> rule : rules)
			{
					hs=getFromHashMap(follow,rule.first[rule.first.length-1]);
					int bsz=hs.size();
					HashSet<String> rulefollow = follow.get(rule.first[0]);
					if (rulefollow!=null)
						hs.addAll(rulefollow); //Добавить то что следует за корнем правила
					ismod|=(bsz!=hs.size());
			}
		}
		while (ismod);
		return follow;
	}

	public static LinkedList<String> generateTstTokensByString(String strrule)
	{
		String[] tokens=strrule.split(",");
		LinkedList<String> retVal=new LinkedList<String>();
		for (String token : tokens)
			retVal.add(token);
		return retVal;
	}

	public static Pair<String[], IToken>[] generateTstRulesByStringArray(String[] strrules)
	{
		Pair<String[], IToken>[] retVal = new Pair[strrules.length];
		for (int i = 0; i < strrules.length; i++)
		{
			String strrule = strrules[i];
			retVal[i] = new Pair<String[], IToken>(strrule.split(","), new GrammarUtils.RuleToken());
		}
		return retVal;
	}

	public static void dprintRule(Pair<String[],IToken>[] rules,int i,int infopos)
	{
		int arglen=rules[i].first.length;
		System.out.print("Pos:"+infopos+" "+rules[i].first[0]+"->");
		for (int j = 1; j < arglen; j++)
			System.out.print(rules[i].first[j]+" ");
		System.out.println();
	}

	//Семантика пара<пара <номер правила, позиция точки разбора в правиле>,мно-во терминальных символов праваого контекста>
	/**
	 * @param rules  - правила грамматики
	 * @param kernel - начальное ядро
	 * @param slhs   - левое терминальное мно-во
	 * @param hsterminals - мно-во терминалов
	 * @return -
	 * @throws su.org.ms.parsers.common.ParserException -
	 */
	static public HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> closure(Pair<String[], IToken>[] rules,
											HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> kernel,
											HashMap<String, HashSet<String>> slhs,
											Collection<String> hsterminals
											) throws ParserException
	{


		HashMap<Pair<Integer, Integer>, HashSet<String>> rclosure = new HashMap<Pair<Integer, Integer>, HashSet<String>>();
//		HashMap<Pair<Integer, Integer>, HashSet<String>> lrclosure = new HashMap<Pair<Integer, Integer>, HashSet<String>>();

		for (Pair<Pair<Integer, Integer>, HashSet<String>> pair : kernel)
		{
			rclosure.put(pair.first,pair.second);
//			lrclosure.put(pair.first,pair.second);
		}

		do
		{
			boolean ischanged=false;
			Set<Pair<Integer, Integer>> states=new HashSet<Pair<Integer, Integer>>(rclosure.keySet());
			for (Pair<Integer, Integer> state : states)
			{

				//Получить символ перед точкой
				String curentsymbol = null;
				String[] rule = rules[state.first].first;
				if (state.second + 1 < rule.length)
				{
					curentsymbol = rule[state.second + 1];

					//Вычислить допустимый контекст
					HashSet<String> lhscontext = null;
					if (state.second + 2 < rule.length)
					{
						String rulesymbol = rule[state.second + 2];
						lhscontext = slhs.get(rulesymbol);
						if (lhscontext==null && hsterminals.contains(rulesymbol))
						{
							lhscontext = new HashSet<String>();
							lhscontext.add(rulesymbol);
						}
						else if (lhscontext==null)
							throw new ParserException("Hang symbol in rule n:"+state.first+" symbol:"+rulesymbol);
					}
					else
						lhscontext = new HashSet<String>(rclosure.get(state));

					for (int i = 0; i < rules.length; i++)
						if (rules[i].first[0].equals(curentsymbol))
						{
							Pair<Integer, Integer> key = new Pair<Integer, Integer>(i, 0);
							HashSet<String> lhscontext1=rclosure.get(key);
							if (lhscontext1!=null)
							{
								int bf=lhscontext1.size();
								lhscontext1.addAll(lhscontext);
								ischanged=ischanged || (bf!=lhscontext1.size());
							}
							else
							{
								rclosure.put(key, lhscontext);
								ischanged=true;
							}
						}
				}
			}

			if (!ischanged)
				break;

//			{
//				rclosure = lrclosure;
//				lrclosure = new HashMap<Pair<Integer, Integer>, HashSet<String>>(rclosure);
//			}
//			else
		}
		while (true);


		HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> retVal= new HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>();
		Set<Pair<Integer, Integer>> states=rclosure.keySet();
		for (Pair<Integer, Integer> state : states)
			retVal.add(new Pair<Pair<Integer, Integer>, HashSet<String>>(state,rclosure.get(state)));

		return retVal;
	}

	/**
	 * Возвратить выводимые слева символы для не терминалов
	 *
	 * @param rules		  - правила грамматики
	 * @param hsnonterminals - не терминалы
	 * @param hsterminals	- терминалы
	 * @param slhs -
	 * @return - отображение символ-><Мно-во терминальных символов выводимых слева из этого символа>
	 */
	public static HashMap<String, HashSet<String>> generateFirst(
			Pair<String[], IToken>[] rules,
			Collection<String> hsnonterminals,
			Collection<String> hsterminals,
			HashMap<String, HashSet<String>> slhs)
	{

		if (slhs==null)
			slhs = new HashMap<String, HashSet<String>>();


		for (String symbol : hsnonterminals)
		{
//Формируе LSH (мно-во которое выводимо из символа слева)
			String[] rhs = GrammarUtils.formLHS(symbol, rules, hsnonterminals);

//Вычленяем терминалы из мно-ва
			HashSet<String> terminalrhs = new HashSet<String>();
			for (String rh : rhs)
			{
				if (hsterminals.contains(rh))
					terminalrhs.add(rh);
			}
			slhs.put(symbol, terminalrhs);//Делаем отображение символ-><Мно-во терминальных символов выводимых слева>
		}

		return slhs;
	}

	public static Pair<String[], IToken>[] generateRulesByStringArray(Pair<String, IToken>[] strrules)
	{
		Pair<String[], IToken>[] retVal = new Pair[strrules.length];
		for (int i = 0; i < strrules.length; i++)
		{
			String strrule = strrules[i].first;
			retVal[i] = new Pair<String[], IToken>(strrule.split(","), strrules[i].second);
			((RuleToken)retVal[i].second).initRuleToken(retVal[i].first,i);
		}
		return retVal;
	}

	public static class RuleToken implements IToken
	{

		String token;
		int nrule;

		public RuleToken()
		{

		}


		public RuleToken(String token, int nrule)
		{
			this.token = token;
			this.nrule = nrule;
		}

		public int getNrule()
		{
			return nrule;
		}

		public void initRuleToken(String[] rootRule, int nrule)
		{
			this.token = rootRule[0];
			this.nrule = nrule;
		}

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
			return token;
		}

		public double getValue() throws CalcException
		{
			throw new CalcException("root rule has no value");
		}

		public double getValue(LinkedList<IToken> args) throws CalcException
		{
			if (args.size() == 1)
				return args.get(0).getValue();
			else
				throw new CalcException("root rule has no value");
		}

		protected LinkedList<IToken> setTokens(LinkedList<IToken> args, RuleToken token) throws ParserException
		{
			if (args.size() == 1 && args.get(0) instanceof CalcToken)
			{
				CalcToken iToken = (CalcToken) args.get(0);
				token.nrule = iToken.getNrule();
				return iToken.args;
			}
			else
				return args;
		}

	}

	public static class CalcToken extends RuleToken
	{
		protected LinkedList<IToken> args;
		private Pair<String[], IToken>[] rules;

		public CalcToken(String token, int nrule, LinkedList<IToken> args,Pair<String[], IToken>[] rules) throws ParserException
		{
			super(token, nrule);
			this.rules = rules;
			setTokens(args);
		}

		public void setTokens(LinkedList<IToken> args) throws ParserException
		{
			RuleToken rt = (RuleToken) rules[nrule].second;
			this.args = rt.setTokens(args, this);
		}

		public double getValue() throws CalcException
		{
			RuleToken rt = (RuleToken) rules[nrule].second;
			return rt.getValue(this.args);
		}
	}
}
