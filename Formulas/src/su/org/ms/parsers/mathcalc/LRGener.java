package su.org.ms.parsers.mathcalc;

import su.org.ms.utils.Pair;
import su.org.ms.parsers.common.ParserException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 15.09.2007
 * Time: 18:24:56
 */
public class LRGener
{

	LRGener(String[] args)
	{

	}

	/**
	 * @param s - запись в талице состояний в формате [r,s]n
	 * @return - получить номер из записи в таблице actions
	 */
	public static int getActionNumber(String s)
	{
		String snumber = s.substring(1);
		return Integer.parseInt(snumber);
	}

	public static void main(String[] args) throws ParserException
	{

//		Pair<String[], TableForm.LRRuleToken>[] rules = generateRulesByStringArray(new String[]{
//				"SH,S",
//				"S,C,C",
//				"C,c,C",
//				"C,d"
//		});
//		String tststr="d,c,c,c,c,c,d,$";
//		String[] symbols=new String[]{"S","C","c","d"};


//		Pair<String[], TableForm.LRRuleToken>[] rules = generateRulesByStringArray(new String[]{
//				"EH,E",
//				"E,E,+,T",
//				"E,T",
//				"T,T,*,F",
//				"T,F",
//				"F,(,E,)",
//				"F,id"
//		});
//		String tststr="id,+,id,*,id,$";
//		String[] symbols=new String[]{"E","T","F","(","id","+","*",")"};

//		Pair<String[], TableForm.LRRuleToken>[] rules = generateRulesByStringArray(new String[]{
//				"SH,S",
//				"S,L,=,R",
//				"S,R",
//				"L,*,R",
//				"L,id",
//				"R,L",
//		});
//		String tststr="id,=,*,*,*,*,id,$";
//		String[] symbols=new String[]{"S","L","R","*","id","="};
/*
		new String[]
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
		});
 */
		Pair<String[], IToken>[] rules = GrammarUtils.generateTstRulesByStringArray(

		new String[]
		{
				"EHH,EH1",

				"EH1,EH1,?,EH1,:,EH",
				"EH1,EH",


				"EH,EH,op_c,E",
				"EH,E",

				"E,E,op_a,T",
				"E,T",
				"A,A,sp,EH1",
				"A,EH1",
				"T,P",
				"T,T,op_m,P",
				"P,i",
				"P,n",
				"P,F",
				"F,f,)",
				"F,f,A,)",
				"P,(,EH1,)",
//Унарные операции
				"P,op_a,i",
				"P,op_a,n",
				"P,op_a,(,EH1,)",
				"P,op_a,F"

		});
//		for (int i = 0; i < rules.length; i++)
//			for (int j = 0; j < rules[i].first.length; j++)
//			{
//				String symbol = rules[i].first[j];
//				if (symbol.equals("sp"))
//					rules[i].first[j]=",";
//			}

 		String tststr="i,op_a,op_a,i,op_m,f,op_a,n,sp,n,sp,i,),$";
// 		String tststr="i,op_a,i,op_c,i,op_c,n,?,i,op_c,n,?,n,:,n,:,n,op_a,i,$";

//		LinkedList<String> allsymbols = new LinkedList<String>();
//		for (String symbol : symbols)
//			allsymbols.add(symbol);

		HashMap<Integer, HashMap<String, Integer>> fgoto = new HashMap<Integer, HashMap<String, Integer>>();
		HashMap<Integer, Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>>> tables=new HashMap<Integer, Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>>>();
		HashMap<String, HashSet<String>> slhs = new HashMap<String, HashSet<String>>();

		LRGener lrgener=new LRGener(args);
//		for (int i = 0; i < rules.length; i++)
//		{
//			Pair<String[], TableForm.LRRuleToken> rule = rules[i];
//			for (int j = 0; j < rules[i].first.length; j++)
//			{
//				if (!rules[i].first[j].equals(((String[])TableForm.rules[i].first)[j]))
//					System.out.println("s = " + rules[i].first[j]+" i:"+i);
//			}
//		}
		HashSet<String> hsterminals = new HashSet<String>();
		LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>> kernel = lrgener.generateKernel(rules, true, fgoto,tables,slhs,hsterminals,null,null);
		lrgener.checkConflicts(tables);


//		HashMap<Pair<Integer,Pair<Integer,Integer>>,HashSet<Pair<Integer,Pair<Integer,Integer>>>> propogation=getpropagationLA(fgoto,kernel,rules,slhs,hsterminals);
//		propagationLA(kernel,propogation);
//		modifyTables(rules,kernel,tables);
		lrgener.tsttranslation(GrammarUtils.generateTstTokensByString(tststr),rules,tables);

		System.out.println("");
	}

	/**
	 * Генератор ядра LR состояний
	 *
	 * @param rules -правила грамматики
	 * @param islr0 - флаг показывает генерировать LR(0) состояния или LR(1)
	 * @param fGoto - функции переходов формат номер состояния в возвращаемом списке -> <Символ ->Номер состояния в возвращаемом списке>
	 * @param tables LR(k) таблицы для заполнения формат номер состояния пара (столбец таблицы действий, столбец таблицы переходов)//TODO посмотреть заполнение для LR(0)
	 * столбец таблицы действий = (терминальный символ ->множество действий (если грамматика LR(1) одно действие)
	 * столбец таблицы переходов = (не терминальный символ ->номер перехода)
	 * @param slhs - мно-во выводимых слева символов передается для заполнения
	 * @param hsterminals - мно-во терминальных символов для заполнения
	 * @param hsnonterminals -
	 * @param symbols -
	 * @return ядро
	 * @throws ParserException - исключение возникает когда не возможно сгенерировать LR(k) грамматику
	 */
	public LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>> generateKernel(Pair<String[],
			IToken>[] rules,
			boolean islr0,
			HashMap<Integer, HashMap<String, Integer>> fGoto,
			HashMap<Integer, Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>>> tables,
			HashMap<String, HashSet<String>> slhs,
			Collection<String> hsterminals,
			Collection<String> hsnonterminals,
			Collection<String> symbols
	) throws ParserException
	{
		boolean crscall = (hsnonterminals == null || hsterminals == null || hsnonterminals.size()==0 || hsterminals.size()==0);

		if (hsnonterminals==null)
			hsnonterminals = new HashSet<String>();
		if (hsterminals==null)
			hsterminals = new HashSet<String>();

		if (crscall)
			GrammarUtils.createSymbolsSet(rules, hsnonterminals, hsterminals);



		slhs= GrammarUtils.generateFirst(rules, hsnonterminals, hsterminals,slhs);

		HashMap<String, HashSet<String>> follow=null;
		if (islr0)
			follow = GrammarUtils.generateFollow(rules, slhs,hsterminals);

		if (symbols==null)
		{
			symbols = new HashSet<String>();
			symbols.addAll(hsterminals);
			symbols.addAll(hsnonterminals);
		}

		LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>> lkernel = new LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>>();
		LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>> rkernel = new LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>>();

		HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> st0 = new HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>();

		HashSet<String> cntx = new HashSet<String>();
		cntx.add("$");
		st0.add(new Pair<Pair<Integer, Integer>, HashSet<String>>(new Pair<Integer, Integer>(0, 0), cntx));
		rkernel.add(st0);
		lkernel.add(st0);

		int nfromstate = 0;
		do
		{
			for (; nfromstate < rkernel.size(); nfromstate++)
			{
				HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> subkernel = rkernel.get(nfromstate);
				HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> rclosure = GrammarUtils.closure(rules, subkernel, slhs,hsterminals);
				//Генерируем ядра и сравнивем с теми ядрами которые у нас уже есть
				for (String symbol : symbols)
				{
					HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> newState = generateState(rules, rclosure, symbol);
					if (newState != null && newState.size() > 0)
					{
						int ntostate = 0;
						for (; ntostate < lkernel.size(); ntostate++)
						{
							HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> state = lkernel.get(ntostate);

							if (eqState(state, newState, islr0)) //проверка на то что у нас состояния одинаковы
								break;
						}

						if (ntostate == lkernel.size())
							lkernel.add(newState);

						HashMap<String, Integer> statesmap = getfGotoVal(fGoto, nfromstate);
						if (statesmap.get(symbol) != null && statesmap.get(symbol) != ntostate)
							throw new ParserException("Can't generate LR tables");
						statesmap.put(symbol, ntostate);

						Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>> tblrow = getRowsFromTables(tables, nfromstate);

						if (hsterminals.contains(symbol))
						{//Заполняем перенос для правила nfromstate по символу symbol в таблице ACTIONS
							HashSet<String> actions = getActionCell(tblrow,symbol);
							actions.add("s"+String.valueOf(ntostate));
						}
						else
							tblrow.second.put(symbol,ntostate); //Заполняем таблицу переходов в таблице GOTO (Уже проверено при формировании функции переходов)
					}
				}

				for (Pair<Pair<Integer, Integer>, HashSet<String>> state : subkernel) //Для ядра состояний генерируем команды свертки
				{
					Pair<Integer, Integer> rulesstate=state.first;
					if (rules[rulesstate.first].first.length<=rulesstate.second+1)
					{
						//Генерируем свертку для правила, поскольку точка разбора в правиле находится в конце
						Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>> tblrow = getRowsFromTables(tables, nfromstate);
						HashSet<String> ctxx = null;//new HashSet<String>();
//						ctxx.add("$");  //TODO проверить еще раз для случая LR(0)
						if (islr0)
						{
//							HashSet<String> l
							ctxx =follow.get(rules[rulesstate.first].first[0]);
							if (ctxx ==null || ctxx.size()==0) //А здесь может и не быть тоже по построению
							{
								ctxx = new HashSet<String>();
								ctxx.add("$");
							}
//							if (lctx!=null && lctx.size()>0)
//									ctxx=lctx;
						}
						else
						{
//							HashSet<String> l
							ctxx =state.second; //Получить контекст правила (здесь он всегда есть по построению)
//							if (lctx!=null && lctx.size()>0)
//									ctxx=lctx;
						}
						for (String tblsymbol : ctxx)
						{//Для всех символов контекста правила заполнить ACTION сверткой
							HashSet<String> actions = getActionCell(tblrow, tblsymbol);
							if (rulesstate.first==0 && tblsymbol.equals("$"))
								actions.add("acc");
							else
								actions.add("r"+String.valueOf(rulesstate.first));
						}

					}
				}

			}
			if (rkernel.size() < lkernel.size())
			{
				rkernel = lkernel;
				lkernel = new LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>>(rkernel);
			}
			else
				break;
		}
		while (true);
		return rkernel;
	}

	private HashMap<String, Integer> getfGotoVal(HashMap<Integer, HashMap<String, Integer>> fgoto, int nfromstate)
	{
		HashMap<String, Integer> statesmap = fgoto.get(nfromstate);
		if (statesmap == null)
		{
			statesmap = new HashMap<String, Integer>();
			fgoto.put(nfromstate, statesmap);
		}
		return statesmap;
	}

	private HashSet<String> getActionCell(Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>> tblstring, String tblsymbol)
	{
		HashSet<String> action=tblstring.first.get(tblsymbol);
		if (action==null)
		{
			action = new HashSet<String>();
			tblstring.first.put(tblsymbol,action);
		}
		return action;
	}

	private Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>> getRowsFromTables(HashMap<Integer, Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>>> tables, int nfromstate)
	{
		Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>> tblstring=tables.get(nfromstate);
		if (tblstring==null)
		{
			tblstring =
					new Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>>(new HashMap<String, HashSet<String>>(),new HashMap<String, Integer>()); //TODO Может NULL сделать?
			tables.put(nfromstate,tblstring);
		}
		return tblstring;
	}

	/**
	 * Сгенерировать LR(0) состояние
	 *
	 * @param rules		- правила
	 * @param stateclosure - замкнутое состояние из которого генерируется состояние
	 * @param symbol	   - символ с помощью которого генерируется состояние
	 * @return ядро сгенерированного сотояния
	 */
	HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> generateState(Pair<String[], IToken>[] rules, HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> stateclosure, String symbol)
	{
		HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> retVal = new HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>();
		for (Pair<Pair<Integer, Integer>, HashSet<String>> pair1 : stateclosure)
		{
			Pair<Integer, Integer> pair = pair1.first;
			String[] rule = rules[pair.first].first;
			if (pair.second + 1 < rule.length && symbol.equals(rule[pair.second + 1]))
			{
				HashSet<String> lhs = new HashSet<String>(pair1.second);//TODO Возможно  можно только перекопировать ссылок
				retVal.add(new Pair<Pair<Integer, Integer>, HashSet<String>>
						(new Pair<Integer, Integer>(pair.first, pair.second + 1), lhs));
			}
		}
		return retVal;
	}

	/**
	 * @param st1   -
	 * @param st2   -
	 * @param islr0 -
	 * @return true если st1==st2
	 */
	private boolean eqState(HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> st1, HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> st2, boolean islr0)
	{
//		fls:
//		if (st1.size()==st2.size())
//		{

		if (islr0 && st1.size() == st2.size())
		{
			HashSet<Pair<Integer, Integer>> st11 = new HashSet<Pair<Integer, Integer>>();
			HashSet<Pair<Integer, Integer>> st22 = new HashSet<Pair<Integer, Integer>>();
			for (Pair<Pair<Integer, Integer>, HashSet<String>> pair : st1)
				st11.add(pair.first);
			for (Pair<Pair<Integer, Integer>, HashSet<String>> pair : st2)
				st22.add(pair.first);
			return st11.equals(st22);
		}
		else
			return st1.equals(st2);
//			for (Pair<Pair<Integer,Integer>,HashSet<String>> pair : st1)
//			{
//				if (!st2.contains(pair))
//					break fls;
//			}
//			return true;
//		}
//		return false;
	}

	public void tsttranslation(
			LinkedList<String> tokens,
			Pair<String[],IToken>[] rules,
			HashMap<Integer, Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>>> tables) throws ParserException
	{
		int currentstate=0;
		int infopos=0;

		LinkedList<String> tstack=new LinkedList<String>();
		tstack.add("0");

		String currentsymbol=tokens.removeFirst();


		while (true)
		{
			HashSet<String> actionset= tables.get(currentstate).first.get(currentsymbol);
			if (actionset==null || actionset.size()==0)
				throw new ParserException("Parser exception at: "+currentsymbol+ " Pos: "+infopos);
			if (actionset.size()>1)
				throw new ParserException("Kernel conflict at state: "+currentstate);

			String action=actionset.iterator().next();
			if (action.equals("acc"))
				break;
			int number=getActionNumber(action);
			if (action.startsWith("s"))
			{//Обработка шифта
				//помещаем в стек символ и сосотояние (потом символ можно будет убрать)
				tstack.add(currentsymbol);
				tstack.add(String.valueOf(number));
				currentsymbol=tokens.removeFirst();
				currentstate=number;
				infopos++;
			}
			else
			{//Обработка свертки
				//Находим длинну аргумента правила
				int argln=rules[number].first.length-1;
				//Cнимаем со стека удвоенную длинну аргумента
				argln*=2;
				while (argln>0)
				{
					tstack.removeLast();
					argln--;
				}
				GrammarUtils.dprintRule(rules,number,infopos);//Отладочно выводим правило
	//Помешаем в стек корень правила
				String ruleroot = rules[number].first[0];
	 //и следующие состояние которое определяется на основе состояния в стеке и корня правила по таблице GOTO
				Integer nextstate = tables.get(
						Integer.parseInt(tstack.getLast())).second.get(ruleroot);
				if (nextstate!=null)
					currentstate= nextstate;
				else
					throw new ParserException("Can't parse a string on:"+infopos+" current symbol is:"+ currentsymbol);
				tstack.add(ruleroot);
				tstack.add(String.valueOf(currentstate));
			}
		}
	}

	private HashSet<Pair<Integer,Pair<Integer,Integer>>> getpropagationFromTable(
			HashMap<Pair<Integer,Pair<Integer,Integer>>,HashSet<Pair<Integer,Pair<Integer,Integer>>>> tpropgation,
			Pair<Integer,Pair<Integer,Integer>> key) //пара <номер состояния,правило>
	{
		HashSet<Pair<Integer,Pair<Integer,Integer>>> retVal=tpropgation.get(key);
		if (retVal==null)
		{
			retVal=new HashSet<Pair<Integer,Pair<Integer,Integer>>>();
			tpropgation.put(key,retVal);
		}
		return retVal;
	}

	//Второй компонент элементов передаваемого ядра сбрасывается, кроме нулевого состояния
	//после исполнения второй компонент элементов передаваемого ядра содержит свободно генерируемый контекст
	//Из состояник,правило состояния -> <В состояние, правило для из состояния>
	public  HashMap<Pair<Integer,Pair<Integer,Integer>>,HashSet<Pair<Integer,Pair<Integer,Integer>>>> getpropagationLA
			(
				HashMap<Integer, HashMap<String, Integer>> fgoto,
				LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>> kernel,
				Pair<String[], IToken>[] rules,
				HashMap<String, HashSet<String>> slhs,
				HashSet<String> hsterminals

			) throws ParserException
	{

		for (HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> states : kernel)
			for (Pair<Pair<Integer, Integer>, HashSet<String>> state : states)
				if (state.second!=null)
					state.second=new HashSet<String>();

		HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> states = kernel.get(0);
		for (Pair<Pair<Integer, Integer>, HashSet<String>> state : states)
		{
			state.second=new HashSet<String>();
			state.second.add("$");
		}

		HashMap<Pair<Integer,Pair<Integer,Integer>>,HashSet<Pair<Integer,Pair<Integer,Integer>>>> retVal=new HashMap<Pair<Integer,Pair<Integer,Integer>>,HashSet<Pair<Integer,Pair<Integer,Integer>>>>();

		for (int stnum = 0; stnum < kernel.size(); stnum++)
		{
			HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> subkernel = kernel.get(stnum);
			HashSet<String> nulcontext=new HashSet<String>();//Нам необходимо знать присутсвует ли нулевой контекст элементе состояния
			nulcontext.add("#");

			for (Pair<Pair<Integer, Integer>, HashSet<String>> state1 :subkernel) //по всем состояниям исходного ядра
			{
				HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> statekernel = new HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>();
				statekernel.add(new Pair<Pair<Integer, Integer>, HashSet<String>>(state1.first,nulcontext)); //создаем ядро которое состоит из одного состояния исходного ядра
				HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> statesclosure =
						GrammarUtils.closure(rules, statekernel, slhs,hsterminals); //замыкаем его что бы понят какие предпосмотры распространяются


				for (Pair<Pair<Integer, Integer>, HashSet<String>> state2 : statesclosure) //по всем состояниям замыкания
				{
					String[] rule = rules[state2.first.first].first;

					if (rule.length>state2.first.second+1)
					{
						String symbol= rule[state2.first.second+1];
						if (state2.second.contains("#"))
						{
							//Записываем что распространяем контекст
							HashSet<Pair<Integer,Pair<Integer,Integer>>> hs =
									getpropagationFromTable(
											retVal,
											new  Pair<Integer,Pair<Integer,Integer>>(stnum,state1.first));

							Integer stto = fgoto.get(stnum).get(symbol);
							if (stto!=null)
								hs.add(new Pair<Integer,Pair<Integer,Integer>>(stto,state2.first));//Добавляем распространение
						}

						if (state2.second.size()>1 || (state2.second.size()==1 && !state2.second.contains("#")))
						{

						//Добавляем свободно генерируемые контексты в ядро (для состояния fgoto(current,symbol))
							Integer stnto = fgoto.get(stnum).get(symbol);
							if (stnto!=null)
							{
								HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> tokernel = kernel.get(stnto);//Ядро в которое переходит по symbol
								for (Pair<Pair<Integer, Integer>, HashSet<String>> tostate : tokernel)
								{
									if (tostate.second==null)
										tostate.second= new HashSet<String>();
									tostate.second.addAll(state2.second);
									tostate.second.remove("#");
								}
							}
						}
					}
				}
			}
		}
		return retVal;
	}

	public void propagationLA
			(
				LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>> kernel,
				HashMap<Pair<Integer,Pair<Integer,Integer>>,HashSet<Pair<Integer,Pair<Integer,Integer>>>> propsheme
			)
	{
		boolean ischanged;
		do
		{
			ischanged=false;
			for (int stnum = 0; stnum < kernel.size(); stnum++)
			{
				HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> kernelstate = kernel.get(stnum); //получить ядро состояния

				for (Pair<Pair<Integer, Integer>, HashSet<String>> fromstate : kernelstate) //Для каждого состояния из ядра
				{
					HashSet<Pair<Integer,Pair<Integer,Integer>>> propagationset=propsheme.get(new Pair<Integer,Pair<Integer,Integer>>(stnum,fromstate.first));
					//Получить мо-во состояний куда оно распространяется мно-во пар <>
					if (propagationset!=null)
					{
						for (Pair<Integer,Pair<Integer,Integer>> propstate : propagationset)
						{
							HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> tostates = kernel.get(propstate.first);
							//получить набор состояний из которых выбрать те куда распространяются контексты
							for (Pair<Pair<Integer, Integer>, HashSet<String>> tostate : tostates)
							{
								if (tostate.first.first==propstate.second.first && tostate.first.second==propstate.second.second+1)
								{//выьираем для распространения только состояния, которые записаны в propagationset.second, т.е. записаны в таблице для распространения
									int bf=tostate.second.size();
									tostate.second.addAll(fromstate.second);
									ischanged=ischanged||(bf!=tostate.second.size());
								}
							}
						}
					}
				}
			}
		} while(ischanged);
	}

	public void modifyTables
			(
				Pair<String[], IToken>[] rules,
				LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>> kernel,
				HashMap<Integer, Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>>> tables
			)
	{
//Удалить свертки из таблиц
		Set<Integer> stnums=tables.keySet();
		for (Integer stnum : stnums)
		{
			HashMap<String, HashSet<String>> tactions=tables.get(stnum).first;
			Set<String> symbols=tactions.keySet();
			for (String symbol : symbols)
			{
				HashSet<String> actions= tactions.get(symbol);
				HashSet<String> newactions= new HashSet<String>();
				for (String action : actions)
					if (action.startsWith("s"))
						newactions.add(action);
				actions.clear();
				actions.addAll(newactions);
			}
		}

//поместить свертки исходя из ядра
		for (int nstate = 0; nstate < kernel.size(); nstate++)
		{
			HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>> states = kernel.get(nstate);

			for (Pair<Pair<Integer, Integer>, HashSet<String>> state : states)
			{
				if (rules[state.first.first].first.length<=state.first.second+1)
				{//Точка находится в конце проставить свертки по правилу
					HashSet<String> actsymbols=state.second;//символы при которых осущесвлять свертку
					HashMap<String, HashSet<String>> actionsrow = tables.get(nstate).first;//получить кортеж для данного состояния
					for (String actsymbol : actsymbols)//для каждого символа из кортежа
					{
						if (state.first.first!=0)
							actionsrow.get(actsymbol).add("r"+state.first.first); //прописать свертку по правилу
						else
							actionsrow.get(actsymbol).add("acc");
					}
				}
			}
		}
	}

	public void checkConflicts(HashMap<Integer, Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>>> tables)
	{
		Set<Integer> states=tables.keySet();
		for (Integer state : states)
		{
			HashMap<String, HashSet<String>> actionrow = tables.get(state).first;
			Set<String> sstr=actionrow.keySet();
			for (String token : sstr)
			{
				HashSet<String> actionsset = actionrow.get(token);
				if (actionsset.size()>1)
				{
					System.out.println("Conflict detected in state:"+state+" for token: \'"+token+"\' is:");
					for (String action : actionsset)
						System.out.print(action+",");
					System.out.println();
				}
			}
		}
	}

}
