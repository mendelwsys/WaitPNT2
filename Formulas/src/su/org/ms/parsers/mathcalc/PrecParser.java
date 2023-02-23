package su.org.ms.parsers.mathcalc;

import su.org.ms.parsers.common.CalcException;
import su.org.ms.parsers.common.ParserException;
import su.org.ms.parsers.common.LexException;

import java.util.Stack;
import java.util.LinkedList;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 11.08.2007
 * Time: 20:55:02
 */
public class PrecParser
{
	private LexReader lr;
	private PrecGramm gr;

//TODO закомменчено lobanov
// 	private IGetFormulaByName getFormulaByName;

    protected PrecParser(String[] args) throws ParserException
	{
//TODO закомменчено lobanov
//		this.getFormulaByName = getFormulaByName;
		lr = new LexReader(args);
		gr = new PrecGramm(args);
	}

//TODO добавлен параметр getFormulaByName (c) lobanov
	public synchronized double calculate(String expression, IGetFormulaByName getFormulaByName)
		throws LexException, IOException, ParserException, CalcException
	{
		int indexQM=-1;
		if ((indexQM=expression.indexOf('?'))>0) //? не может быть первым символом в вычислении выражений
		{
			if (expression.indexOf(':')>indexQM)
			{
				//Вычисляем главное выражение
				String[] main_tail=expression.split("[?]");
				String[] tail=main_tail[1].split(":");
				IToken result = precalculate(main_tail[0], getFormulaByName);
				if (result.getValue()!=0) //Вычисляем первое выражение
					result = precalculate(tail[0], getFormulaByName);
				else//Второе
					result = precalculate(tail[1], getFormulaByName);
				return result.getValue();
			}
			else
				throw new ParserException("colon mark (:) is absent or has no appropriate place in string");
		}
		else
		{
			IToken result = precalculate(expression, getFormulaByName);
			return result.getValue();
		}
	}

//TODO добавлен параметр getFormulaByName (c) lobanov
	protected IToken precalculate(String expression, IGetFormulaByName getFormulaByName)
			throws LexException, IOException, ParserException, CalcException
	{
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(expression.getBytes()));

		LinkedList<String> list = new LinkedList<String>();
		lr.generateTokens(list, dis, new int[1]);
		LinkedList<LexReader.LexemToken> tokens = lr.toLexicalList(list,null);

//		String code=LexReader.generateStringByTokens(tokens);
//		System.out.println("code = " + code);

		Stack<IToken> stack = new Stack<IToken>();
		for (int i = tokens.size() - 1; i >= 0; i--)
		{
			LexReader.LexemToken lexemToken = tokens.get(i);
			if (lexemToken.getToken().equals("i"))
			{
				String parName = lexemToken.getRepresent();
				String formula = null;
				try
				{
					formula = getFormulaByName.getFormulaByName(parName);
				}
				catch (ParserException e)
				{
					e.printStackTrace();
                }
				if (formula == null)
					try
					{
						formula = String.valueOf(MathInvoker.getConstant(parName));
					}
					catch (Exception e)
					{
						throw new ParserException("Unknown symbol:" + parName);
					}
//TODO добавлен параметр getFormulaByName (c) lobanov
				lexemToken.setRepresent(String.valueOf(calculate(formula, getFormulaByName)));
			}
			stack.push(lexemToken);
		}

        return gr.translate(stack);
	}

//	private static PrecParser parser=null;
////TODO закомменчено lobanov
//	public static synchronized PrecParser createParser(String[] args)//, IGetFormulaByName getFormulaByName)
//			throws ParserException
//	{
////TODO закомменчено lobanov
//		if (parser==null)
//			parser = new PrecParser(args);//, getFormulaByName);
//		return parser;
//	}

}
