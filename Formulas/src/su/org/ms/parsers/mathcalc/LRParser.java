package su.org.ms.parsers.mathcalc;

import su.org.ms.parsers.common.ParserException;
import su.org.ms.parsers.common.LexException;
import su.org.ms.parsers.common.CalcException;
import su.org.ms.utils.Pair;

import java.util.LinkedList;
import java.util.HashMap;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 23.09.2007
 * Time: 12:40:21
 *
 */
public class LRParser
{
	private LexReader lr; //Лексический анализатор
	private LRGramm grammar; //Синтаксический анализатор
	/**
	 * кеш деревьев разбора
	 */
	private HashMap<String, Pair<IToken, LinkedList<LexReader.LexemToken>>> cash =
			new HashMap<String, Pair<IToken, LinkedList<LexReader.LexemToken>>>();

	protected LRParser(String[] args) throws ParserException
	{
		lr= new LexReader(args);
		grammar= new LRGramm(args);
	}

	private String getHashCode(LinkedList<LexReader.LexemToken> tkns)
	{
		StringBuffer retVal=new StringBuffer();
		for (LexReader.LexemToken tkn : tkns)
		{
			String token = tkn.getToken();
			if (token.equals("n"))
				retVal.append("i").append("_");
			else
				retVal.append(token).append("_");
		}
		return retVal.toString();
	}

	/**
	 * !!!!Должно быть засинхронизовано!!! поскольку производится замена в дереве разбора
	 * реальными терминалами, а потом его вычисление
	 * @param hash -
	 * @param tkns -
	 * @return -
	 * @throws CalcException -
	 */
	private synchronized Double getfromcashe(String hash,LinkedList<LexReader.LexemToken> tkns) throws CalcException
	{
		Pair<IToken, LinkedList<LexReader.LexemToken>> precalc= cash.get(hash);
		if (precalc!=null)
		{
			int i=0;
			for (LexReader.LexemToken tkn : tkns)
			{
				precalc.second.get(i).replacebyLexem(tkn);
				i++;
			}
			return precalc.first.getValue();
		}
		return null;
	}

	private void preprocesstokens(LinkedList<LexReader.LexemToken> tkns,IGetValueByName valuebyname)
			throws ParserException
	{
		for (int i = 0; i < tkns.size(); i++)
		{
			LexReader.LexemToken lexemToken = tkns.get(i);
			if (lexemToken.getRepresent().equals("!") || lexemToken.getRepresent().equals("="))
			{
				if (tkns.size()<=i+1 || !tkns.get(i+1).getRepresent().equals("="))
					throw new ParserException("Unknown symbol \'"+lexemToken.getRepresent()+"\'");
				else
				{
					tkns.remove(i);
					tkns.remove(i);
					tkns.add(i,new LexReader.LexemToken(lexemToken.getRepresent()+"=",false,valuebyname));
					i--;
				}
			}
			else if (lexemToken.getRepresent().equals("<") || lexemToken.getRepresent().equals(">"))
				if (tkns.size()>i+1 && tkns.get(i+1).getRepresent().equals("="))
				{
					tkns.remove(i);
					tkns.remove(i);
					tkns.add(i,new LexReader.LexemToken(lexemToken.getRepresent()+"=",false,valuebyname));
					i--;
				}
		}
	}

	/**
	 * Вычислить формулу
	 * @param expression - собственно вормула
	 * @param getFormulaByName - интерфейс для получения формул по имени переменной
	 * @return результат вычичслений
	 * @throws LexException - исключение на этапе лексического анализа
	 * @throws IOException - исключение на этапе обработки строки
	 * @throws ParserException - исключение на этапе построения дерева разбора выражения
	 * @throws CalcException - исключение на этапе вычисление выражения
	 */
	public synchronized double calculate(String expression, IGetFormulaByName getFormulaByName)
			throws LexException, IOException, ParserException, CalcException
	{


		IGetValueByName valuebyname=new IGetValueByNameImpl(getFormulaByName);
		return calculate(expression, valuebyname);
	}

	/**
	 * Вычислить формулу
	 * @param expression - собственно вормула
	 * @param valuebyname - интерфейс для получения значений переменный по их именам
	 * @return результат вычичслений
	 * @throws LexException - исключение на этапе лексического анализа
	 * @throws IOException - исключение на этапе обработки строки
	 * @throws ParserException - исключение на этапе построения дерева разбора выражения
	 * @throws CalcException - исключение на этапе вычисление выражения
	 */

	public synchronized double calculate(
			String expression, IGetValueByName valuebyname)
			throws LexException, IOException, ParserException, CalcException
	{

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(expression.getBytes()));

		LinkedList<String> list = new LinkedList<String>();
		lr.generateTokens(list, dis, new int[1]);
		LinkedList<LexReader.LexemToken> tokens = lr.toLexicalList(list,valuebyname);

		preprocesstokens(tokens,valuebyname);
		tokens.add(new LexReader.LexemToken("$", false,valuebyname));

		String hash=getHashCode(tokens);
		Double dbl=getfromcashe(hash,tokens);
		if (dbl!=null)
			return dbl;

		LinkedList<IToken> tkns= grammar.translate(new LinkedList<LexReader.LexemToken>(tokens));
		if (tkns.size()>1 || tkns.size()==0)
			throw  new ParserException("Internal parser Error");

		IToken retVal = tkns.get(0);

		cash.put(hash,new Pair<IToken,LinkedList<LexReader.LexemToken>>(retVal,tokens));

		return retVal.getValue();
	}

	/**
	 * Сброс кеша деревьев разбора
	 */
	public void clearCash()
	{
		cash.clear();
	}

	public class IGetValueByNameImpl implements IGetValueByName
	{
		private IGetFormulaByName formulaByName;
		public IGetValueByNameImpl(IGetFormulaByName formulaByName)
		{
			this.formulaByName = formulaByName;
		}
		public double getValueByName(String parName) throws CalcException
		{
			try
			{
				String formula = null;
				try
				{
					formula = formulaByName.getFormulaByName(parName);
				}
				catch (ParserException e)
				{
//					e.printStackTrace();
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

				return  calculate(formula, this);
			}
			catch (CalcException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw  new CalcException(e);
			}
		}
	}


}
