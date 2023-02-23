package su.org.ms.parsers.mathcalc;

import su.org.ms.parsers.common.ParserException;
import su.org.ms.parsers.common.CalcException;
import su.org.ms.parsers.mathcalc.tst.TestToken;
import su.org.ms.utils.Pair;

import java.util.Stack;
import java.util.LinkedList;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 05.08.2007
 * Time: 16:57:24
 */
public class PrecGramm
{


	//The Grammar (uppercase is nonterminals lowercase is terminal)
	public static final Pair[] rules =
			{
					new Pair<String[], IToken>(new String[]{"EH", "E"}, new GrammarUtils.RuleToken()),
					new Pair<String[], IToken>(new String[]{"F", "f", ")"}, new GrammarUtils.RuleToken()
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
					new Pair<String[], IToken>(new String[]{"F", "f", "AH", ")"}, new GrammarUtils.RuleToken()
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
					new Pair<String[], IToken>(new String[]{"AH", "A"}, new GrammarUtils.RuleToken()),
					new Pair<String[], IToken>(new String[]{"A", "A", "sp", "EH"}, new GrammarUtils.RuleToken()
					{
						protected LinkedList<IToken> setTokens(LinkedList<IToken> args, GrammarUtils.RuleToken token) throws ParserException
						{
							if (args.size() == 3 && args.get(0) instanceof GrammarUtils.CalcToken)
							{
								GrammarUtils.CalcToken iToken = (GrammarUtils.CalcToken) args.get(0);
								LinkedList<IToken> rargs = new LinkedList<IToken>(iToken.args);
//						rargs.add(args.get(1)); //NOT Include comma
								rargs.add(args.get(2));
								return rargs;
							}
							throw new ParserException("Can't apply rule with number:" + this.nrule);
						}
					}),

					new Pair<String[], IToken>(new String[]{"A", "EH"}, new GrammarUtils.RuleToken()
					{
						protected LinkedList<IToken> setTokens(LinkedList<IToken> args, GrammarUtils.RuleToken token) throws ParserException
						{
							return args;
						}
					}),


					new Pair<String[], IToken>(new String[]{"E", "E", "op_a", "TH"}, new GrammarUtils.RuleToken()
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
					new Pair<String[], IToken>(new String[]{"E", "TH"}, new GrammarUtils.RuleToken()),
					new Pair<String[], IToken>(new String[]{"TH", "T"}, new GrammarUtils.RuleToken()),
					new Pair<String[], IToken>(new String[]{"T", "P"}, new GrammarUtils.RuleToken()),
					new Pair<String[], IToken>(new String[]{"T", "T", "op_m", "P"}, new GrammarUtils.RuleToken()
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
					new Pair<String[], IToken>(new String[]{"P", "i"}, new GrammarUtils.RuleToken()),
					new Pair<String[], IToken>(new String[]{"P", "n"}, new GrammarUtils.RuleToken()),
					new Pair<String[], IToken>(new String[]{"P", "F"}, new GrammarUtils.RuleToken()
					{
						protected LinkedList<IToken> setTokens(LinkedList<IToken> args, GrammarUtils.RuleToken token) throws ParserException
						{
							return args;
						}
					}),

					new Pair<String[], IToken>(new String[]{"P", "(", "EH", ")"}, new GrammarUtils.RuleToken()
					{
						public double getValue(LinkedList<IToken> args) throws CalcException
						{
							if (args.size() != 3)
								throw new CalcException("Can't calc argument for rule" + nrule);
							return args.get(1).getValue();
						}
					}),

			};
	private static final String[] teststr = new String[]{"(", "i", "op_a", "i", ")", "op_m", "(", "i", "op_a", "n", ")"};//"sdf+12.1";

	static
	{
		for (int i = 0; i < rules.length; i++)
		{
			Pair<String[], IToken> rule = rules[i];
			((GrammarUtils.RuleToken) rule.second).initRuleToken(rule.first, i);
		}
	}

	private PrecGener tableform;//Grammar builder for math calculate

	public PrecGramm(String[] args) throws ParserException
	{
		tableform = new PrecGener(args,rules);
	}

	public static void main(String[] args) throws ParserException
	{
		Stack<IToken> stack = new Stack<IToken>();
		for (int i = teststr.length - 1; i >= 0; i--)
			stack.push(new TestToken(teststr[i]));

		IToken result = new PrecGramm(args).translate(stack);

		{
			try
			{
				System.out.println("result.getValue() = " + result.getValue());
			}
			catch (CalcException e)
			{
				throw new ParserException(e);
			}
		}

//		for (Integer rule : rules)
//			System.out.println("rule = " + rule);
	}

	private Pair<String, Integer> getRootRule(LinkedList<IToken> storsmbl) throws ParserException
	{
		for (int i = 0; i < rules.length; i++)
		{
			Pair<String[], IToken> rulepair = rules[i];
			String[] rule = rulepair.first;
			if (rule.length - 1 == storsmbl.size())
			{
				int j = 1;
				for (; j < rule.length; j++)
				{
					if (!rule[j].equals(storsmbl.get(j - 1).getToken()))
						break;
				}
				if (j == rule.length)
					return new Pair<String, Integer>(rule[0], i);
			}
		}

		String arg = "";
		for (int i = 0; i < storsmbl.size(); i++)
			arg += " " + storsmbl.get(i).getToken();
		throw new ParserException("Can't find rule with argumnet:" + arg);
	}

	private IToken parserStep(Stack<IToken> inputStack, Stack<IToken> storageStack, Stack<Integer> ruleNumbers) throws ParserException
	{
		HashSet<String> attitude = null;
		String storsmbl = "ERROR";
		String insymbol = "ERROR";

		if (inputStack.size() == 0)
		{

			if (storageStack.size() > 0)
			{
				storsmbl = storageStack.get(storageStack.size() - 1).getToken();
				if (storageStack.size() == 2 && storsmbl.equals(PrecGener.BEGSYMBL))
				{
					IToken root = storageStack.pop();
					storsmbl = storageStack.get(storageStack.size() - 1).getToken();
					if (storsmbl.equals(PrecGener.MARKER))
						storageStack.pop();
					return root;
				}
				else if (tableform.inRSH_E(storsmbl))
				{
					attitude = new HashSet<String>();
					attitude.add(PrecGener.MRE);
				}
				else
					return new PrecGener.Marker();
			}
			else
				return new PrecGener.Marker();
		}
		else
		{
			storsmbl = storageStack.get(storageStack.size() - 1).getToken();
			insymbol = inputStack.get(inputStack.size() - 1).getToken();
			attitude = tableform.getPredAttitude(storsmbl, insymbol);
		}


		if (attitude == null)
			throw new ParserException("Parse error of expretion: (" + storsmbl + " EPTY " + insymbol + ")");
		else
		{
			attitude = new HashSet<String>(attitude);
			if (attitude.size() > 1)
			{
				if (storageStack.size() >= 2 && storageStack.get(storageStack.size() - 1).getToken().equals(PrecGener.BEGSYMBL) && storageStack.get(storageStack.size() - 2).getToken().equals("("))
				{
					attitude.clear();
					attitude.add(PrecGener.EQL);
				}
				else
					attitude.remove(PrecGener.EQL);
				if (attitude.size() != 1)
					throw new ParserException("Parse error of expretion: (" + storsmbl + " EPTY or MORE then ONE " + insymbol + ")");
//				System.out.println("Table has ambiguous paramters");   //TODO анализ стека однако, на предмет точного определения какой параметр надо задать
			}

			if (attitude.contains(PrecGener.EQL))
				storageStack.push(inputStack.pop());
			else if (attitude.contains(PrecGener.LSS))
			{
				storageStack.push(new PrecGener.Marker());
				storageStack.push(inputStack.pop());
			}
			else if (attitude.contains(PrecGener.MRE))
			{
				IToken popsymbol;
				LinkedList<IToken> rule_arg = new LinkedList<IToken>();
				while (!((popsymbol = storageStack.pop()).getToken().equals(PrecGener.MARKER)))
					rule_arg.addFirst(popsymbol);

				Pair<String, Integer> ruleroot = getRootRule(rule_arg);

				Pair<String[], IToken> rule = rules[ruleroot.second];

				inputStack.push(new GrammarUtils.CalcToken(rule.second.getToken(), rule.second.getNrule(), rule_arg, rules));

				ruleNumbers.push(ruleroot.second);

				if (storageStack.size() == 0)
				{
					storageStack.push(new PrecGener.Marker());
					storageStack.push(inputStack.pop());
				}
			}
			else
				throw new ParserException("Unknown attitude: " + attitude);
		}

		return null;
	}

	public IToken translate(Stack<IToken> inputStack) throws ParserException
	{
		Stack<IToken> storageStack = new Stack<IToken>();
		Stack<Integer> retruleNumbers = new Stack<Integer>();

		storageStack.push(new PrecGener.Marker());
		storageStack.push(inputStack.pop());

		IToken result = null;
		while ((result = parserStep(inputStack, storageStack, retruleNumbers)) == null) ;

		if (storageStack.size() > 0)
		{
			String exString = "";
			for (IToken s : storageStack)
				exString += " " + s.getToken();
			throw new ParserException("Error state in parser machine Stack content:" + exString);
		}
		return result;
	}

}
