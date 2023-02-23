package su.org.ms.parsers.mathcalc;

import su.org.ms.parsers.common.CalcException;
import su.org.ms.parsers.common.ParserException;
import su.org.ms.utils.Pair;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 22.09.2007
 * Time: 16:48:09
 *
 */
public class LRGramm
{

	public static Pair[] rawrules=
	{
			new Pair<String, IToken>("EHH,EH1",new LRRuleToken()),

			new Pair<String, IToken>("EH1,EH1,?,EH1,:,EH",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if (args.size() != 5)
						throw new CalcException("Can't calc argument for rule" + nrule);

					double val1 = args.get(0).getValue();
					if (val1!=0)
						return args.get(2).getValue();
					else
						return args.get(4).getValue();
				}
			}),
			new Pair<String, IToken>("EH1,EH",new LRRuleToken()),

			new Pair<String, IToken>("EH,EH,op_c,E",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if (args.size() != 3)
						throw new CalcException("Can't calc argument for rule" + nrule);

					double val1 = args.get(0).getValue();
					double val2 =  args.get(2).getValue();

					String operation = args.get(1).getRepresent();

					if (operation.equals("<="))
						return val1<=val2?1.0:0;
					if (operation.equals("<"))
						return val1<val2?1.0:0;
					if (operation.equals("=="))
						return val1==val2?1.0:0;
					if (operation.equals("!="))
						return val1!=val2?1.0:0;
					if (operation.equals(">="))
						return val1>=val2?1.0:0;
					if (operation.equals(">"))
						return val1>val2?1.0:0;
					throw new CalcException("unknown operation " + operation + " for rule" + nrule);

				}
			}),
			new Pair<String, IToken>("EH,E",new LRRuleToken()),

			new Pair<String, IToken>("E,E,op_a,T",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if (args.size() != 3)
						throw new CalcException("Can't calc argument for rule" + nrule);

					double val1 = args.get(0).getValue();
					double val2 = args.get(2).getValue();

					String operation = args.get(1).getRepresent();
					if (operation.equals("+"))
						return val1 + val2;
					if (operation.equals("-"))
						return val1 - val2;
					else
						throw new CalcException("unknown operation " + operation + " for rule" + nrule);
				}
			}),
			new Pair<String, IToken>("E,T",new LRRuleToken()),
			new Pair<String, IToken>("A,A,sp,EH1",new LRRuleToken()
			{
				protected LinkedList<IToken> setTokens(LinkedList<IToken> args, GrammarUtils.RuleToken token) throws ParserException
				{
					if (args.size() == 3 && args.get(0) instanceof GrammarUtils.CalcToken)
					{
						GrammarUtils.CalcToken iToken = (GrammarUtils.CalcToken) args.get(0);
						LinkedList<IToken> rargs = new LinkedList<IToken>(iToken.args);
						rargs.add(args.get(2));
						return rargs;
					}
					throw new ParserException("Can't apply rule with number:" + this.nrule);
				}
			}),
			new Pair<String, IToken>("A,EH1",new LRRuleToken()
			{

				protected LinkedList<IToken> setTokens(LinkedList<IToken> args, GrammarUtils.RuleToken token) throws ParserException
				{
					return args;
				}
			}),
			new Pair<String, IToken>("T,P",new LRRuleToken()),
			new Pair<String, IToken>("T,T,op_m,P",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if (args.size() != 3)
						throw new CalcException("Can't calc argument for rule" + nrule);

					double val1 = args.get(0).getValue();
					double val2 = args.get(2).getValue();

					String operation = args.get(1).getRepresent();
					if (operation.equals("*"))
						return val1 * val2;
					if (operation.equals("/"))
						return val1 / val2;
					else
						throw new CalcException("unknown operation " + operation + " for rule" + nrule);
				}
			}),
			new Pair<String, IToken>("P,i",new LRRuleToken()),
			new Pair<String, IToken>("P,n",new LRRuleToken()),
			new Pair<String, IToken>("P,F",new LRRuleToken()
			{
				protected LinkedList<IToken> setTokens(LinkedList<IToken> args, GrammarUtils.RuleToken token) throws ParserException
				{
					return args;
				}
			}),
			new Pair<String, IToken>("F,f,)",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if (args.size() != 2
							|| !args.get(0).isFunction())
						throw new CalcException("Can't calc argument for rule" + nrule);

					try
					{
						return MathInvoker.invoke(args.get(0).getRepresent());
					}
					catch (Exception e)
					{
						throw new CalcException(e);
					}
				}
			}),
			new Pair<String, IToken>("F,f,A,)",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if (args.size() != 3 || !(args.get(1) instanceof GrammarUtils.CalcToken)
							|| !args.get(0).isFunction())
						throw new CalcException("Can't calc argument for rule" + nrule);

					LinkedList<IToken> fargs = ((GrammarUtils.CalcToken) args.get(1)).args;

					Double[] dargs = new Double[fargs.size()];
					for (int i = 0; i < dargs.length; i++)
						dargs[i] = fargs.get(i).getValue();

					try
					{
						return MathInvoker.invoke(args.get(0).getRepresent(), dargs);
					}
					catch (Exception e)
					{
						throw new CalcException(e);
					}

				}
			}),
			new Pair<String, IToken>("P,(,EH1,)",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if (args.size() != 3)
						throw new CalcException("Can't calc argument for rule" + nrule);
					return args.get(1).getValue();
				}
			}),


			new Pair<String, IToken>("P,op_a,(,EH1,)",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if (args.size() != 4)
						throw new CalcException("Can't calc argument for rule" + nrule);
					if(args.get(0).getRepresent().equals("-"))
						return -args.get(2).getValue();
					else
						return args.get(2).getValue();
				}
			}),

			new Pair<String, IToken>("P,op_a,i",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if(args.get(0).getRepresent().equals("-"))
						return -args.get(1).getValue();
					else
						return args.get(1).getValue();
				}
			}),

			new Pair<String, IToken>("P,op_a,n",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if(args.get(0).getRepresent().equals("-"))
						return -args.get(1).getValue();
					else
						return args.get(1).getValue();
				}
			}),

			new Pair<String, IToken>("P,op_a,F",new LRRuleToken()
			{
				public double getValue(LinkedList<IToken> args) throws CalcException
				{
					if(args.get(0).getRepresent().equals("-"))
						return -args.get(1).getValue();
					else
						return args.get(1).getValue();
				}
			}),
	};
	public static Pair<String[], IToken>[] rules=GrammarUtils.generateRulesByStringArray(rawrules);
	HashMap<Integer, Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>>> lrtables;


	public LRGramm(String[] args)
			throws ParserException
	{
		LRGener gramGener=new LRGener(args); //Нам нужны таблицы а не генератор, поэтому объявляем как локальную переменную

		HashMap<Integer, HashMap<String, Integer>> fgoto = new HashMap<Integer, HashMap<String, Integer>>();
		lrtables =new HashMap<Integer, Pair<HashMap<String, HashSet<String>>, HashMap<String, Integer>>>();
		HashMap<String, HashSet<String>> slhs = new HashMap<String, HashSet<String>>();


		HashSet<String> hsterminals = new HashSet<String>();
//		LinkedList<HashSet<Pair<Pair<Integer, Integer>, HashSet<String>>>> kernel = //Ядро нам не нужно, так что не анализируем его
		gramGener.generateKernel(rules, true, fgoto, lrtables,slhs,hsterminals,null,null);//Генерация таблиц
		gramGener.checkConflicts(lrtables);//Проверка конфликтов в таблице
	}

	/**
	 * Построить дерево трансляции
	 * @param tokens - входное мно-во лексем
	 * @return - список состоящий из одного элемента который является деревом трансляции
	 * @throws ParserException -
	 */
	public LinkedList<IToken> translate(
			LinkedList<LexReader.LexemToken> tokens
			) throws ParserException
	{
		int currentstate=0;
		int infopos=0;

		LinkedList<String> tstack=new LinkedList<String>();
		tstack.add("0");

		LexReader.LexemToken currentsymbol=tokens.removeFirst();

		LinkedList<IToken> retVal=new LinkedList<IToken>();

		while (true)
		{
			HashSet<String> actionset= lrtables.get(currentstate).first.get(currentsymbol.getToken());
			if (actionset==null || actionset.size()==0)
				throw new ParserException("Parser exception at: "+currentsymbol.getRepresent()+ " Pos: "+infopos);
			if (actionset.size()>1)
				throw new ParserException("Kernel conflict at state: "+currentstate);

			String action=actionset.iterator().next();
			if (action.equals("acc"))
				break;
			int number= LRGener.getActionNumber(action);
			if (action.startsWith("s"))
			{//Обработка шифта
				//помещаем в стек символ и сосотояние (потом символ можно будет убрать)
				tstack.add(currentsymbol.getToken());
				tstack.add(String.valueOf(number));

				retVal.add(currentsymbol);

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


				//Производим удаление из стека аргументов правила
				LinkedList<IToken> rule_arg = new LinkedList<IToken>();
				argln=rules[number].first.length-1;
				while (argln>0)
				{
					rule_arg.addFirst(retVal.removeLast());
					argln--;
				}

				//И помещаем в стек его корень
				retVal.add(new GrammarUtils.CalcToken(rules[number].second.getToken(), rules[number].second.getNrule(), rule_arg,rules));

//				LRGener.dprintRule(rules,number,infopos);//Отладочно выводим правило


				//Помешаем в стек корень правила
				String ruleroot = rules[number].first[0];
	 //и следующие состояние которое определяется на основе состояния в стеке и корня правила по таблице GOTO
				Integer nextstate = lrtables.get(
						Integer.parseInt(tstack.getLast())).second.get(ruleroot);
				if (nextstate!=null)
					currentstate= nextstate;
				else
					throw new ParserException("Can't parse a string on:"+infopos+" current symbol is:"+ currentsymbol);
				tstack.add(ruleroot);
				tstack.add(String.valueOf(currentstate));
			}
		}
		return retVal;
	}

	public static class LRRuleToken extends GrammarUtils.RuleToken
	{
		public LRRuleToken()
		{

		}

		public LRRuleToken(String token, int nrule)
		{
			super(token,nrule);
		}

//		protected LinkedList<IToken> setTokens(LinkedList<IToken> args, GrammarUtils.RuleToken token) throws ParserException
//		{
//				return args;
//		}

	}



}
