package su.org.ms.parsers.mathcalc;

import su.org.ms.parsers.common.LexException;
import su.org.ms.parsers.common.CalcException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.ByteArrayInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: VLADM
 * Date: 06.05.2006
 * Time: 10:57:36
 */
public class LexReader
{
//Читает строку и возвращает набор лексем.

//	public static final String DEF_CODEPAGE="WINDOWS-1251";

	final static byte spaces[] = {' ', '\t', '\n', 0x0D}; //Пробльные литералы
	final static byte addLiterals[] = {'_'};  //Дополнительные оитералы которые могут встречаться в имени
	final static byte addInternalOnceLiterals[] = {'.'}; //Дополнительные литералы которые могут встречаться
	final static byte servLiterals[] = {'(', ')', '*', '/', '+', '-', ',','$',':','?','>','=','<','!'};
	public static HashSet<Integer> spacesH = new HashSet<Integer>();
	// (имя парметра может начинаться с подчеркивания и за ним должна идти буква)
	public static HashSet<Integer> addLiteralsH = new HashSet<Integer>();
	//внутри имени или занчения
	public static HashSet<Integer> addInternalOnceH = new HashSet<Integer>();
	public static HashSet<Integer> servLiteralsH = new HashSet<Integer>();

//	private static final String EOF = "EOF";

	public LexReader(String arg[])
	{
		for (byte space : spaces)
			spacesH.add(new Integer(space));

		for (byte addLiteral : addLiterals)
			addLiteralsH.add(new Integer(addLiteral));

		for (byte addInternalOnceLiteral : addInternalOnceLiterals)
			addInternalOnceH.add(new Integer(addInternalOnceLiteral));

		for (byte servLiteral : servLiterals)
			servLiteralsH.add(new Integer(servLiteral));
	}

	public static String generateStringByTokens(LinkedList<LexReader.LexemToken> tokens)
	{
		StringBuffer retVal=new StringBuffer();
		for (LexemToken token : tokens)
			retVal.append("$").append(token.getToken());
		return retVal.toString();
	}

	public static void main(String[] args) throws LexException, IOException
	{

//		byte[] bt1="???".getBytes();
//		byte[] bt2="???".getBytes("UTF-8");

////		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bt1));
//		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bt2));
//		byte bt=dis.readByte();
//		char chr=dis.readChar();
//		byte[] bytes1 = new byte[]{(byte) ((chr & 0xFF00) >> 8), (byte) (chr & 0xFF)};
//		System.out.println(new String(bytes1,"UTF-8"));

//		System.out.println(dis.available());
//		StringBuffer buf=new StringBuffer();
//		buf.append(chr);
//		System.out.println("XXX:"+buf.toString());

		LexReader lr = new LexReader(args);
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream("(f(_a12,a13)*12.3-23+_b*c /  _asdf/c)?12:33".getBytes()));

//		DataInputStream dis = new DataInputStream(new ByteArrayInputStream("12.3-23+_b*c /  _asdf/012.".getBytes()));

		LinkedList<String> list = new LinkedList<String>();
		lr.generateTokens(list, dis, new int[1]);
		LinkedList<LexemToken> tokens = lr.toLexicalList(list,null);

		for (IToken token : tokens)
			System.out.println("token = " + token.getToken());

//		for (int i = 0; i < list.size(); i++)
//		{
//			String s = (String) list.get(i);
//			System.out.println("t[" + i + "] = " + s);
//		}
	}

	public LinkedList<LexemToken> toLexicalList(LinkedList<String> lex,IGetValueByName getValueByName)
	{
		LinkedList<LexemToken> retList = new LinkedList<LexemToken>();
		for (String strlex : lex)
			retList.add(new LexemToken(strlex, false,getValueByName));

//Insert function token to parsing string
		for (int i = 0; i < retList.size() - 1; i++)
		{
			LexemToken lexemToken1 = retList.get(i);
			IToken lexemToken2 = retList.get(i + 1);
			if (lexemToken1.getToken().equals("i") && lexemToken2.getToken().equals("("))
			{
				lexemToken1.setFunction(true);
				retList.remove(i + 1);
			}
		}
		return retList;
	}

	private boolean isAllowedNameLiteral(byte ch, String curToken)
	{
		boolean isal = ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z') || ('0' <= ch && ch <= '9')
				|| addLiteralsH.contains(new Integer(ch)) || addInternalOnceH.contains(new Integer(ch));

		if (isal && addInternalOnceH.contains(new Integer(ch)))
		{
			Iterator iterator = addInternalOnceH.iterator();
			while (iterator.hasNext())
			{
				byte val = ((Integer) iterator.next()).byteValue();
				isal = (curToken.indexOf((char) val) == -1);
				if (!isal)
					break;
			}
		}

		if (isal)
		{
			byte[] bytes = curToken.getBytes();
			byte bt = bytes[bytes.length - 1];
			if (addInternalOnceH.contains(new Integer(bt)))
				isal = !('0' <= ch && ch <= '9');
		}
		return isal;
	}

	private boolean isAllowedBeginNameLiteral(byte ch)
	{
		return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z')
				|| (addLiteralsH.contains(new Integer(ch)));
	}

	private boolean isAllowedValueLiteral(byte ch, String curToken)
	{
		boolean isal = (('0' <= ch && ch <= '9')
				|| (addInternalOnceH.contains(new Integer(ch))));

		if (isal && (addInternalOnceH.contains(new Integer(ch))))
			isal = (curToken.indexOf(new Integer(ch).intValue()) == -1);
		return isal;
	}

	private boolean isAllowedBeginValueLiteral(byte ch)
	{
		return (('0' <= ch && ch <= '9'));
	}

	private boolean isAllowedOperation(byte ch)
	{
		return (servLiteralsH.contains(new Integer(ch)));
	}

	public void generateTokens(LinkedList<String> tokenList, DataInputStream dis, int[] currentPos) throws LexException, IOException
	{
		try
		{
			StringBuffer token = new StringBuffer();
			for (; ;)
			{

				byte ch = dis.readByte();
				currentPos[0]++;

				if (spacesH.contains(new Integer(ch)))
				{
					if (ch == 0x0D && dis.readByte() != '\n')
					{
						currentPos[0]++;
						throw new LexException("Unexpected Symbol:" + new String(new byte[]{ch}) + " Pos:" + currentPos);
					}
				}
				else if (isAllowedBeginNameLiteral(ch))
				{
					token.append((char) ch);
					generateNameToken(tokenList, dis, token, currentPos);
					token.setLength(0);
				}
				else if (isAllowedBeginValueLiteral(ch))
				{
					token.append((char) ch);
					generateValueToken(tokenList, dis, token, currentPos);
					token.setLength(0);
				}
				else if (isAllowedOperation(ch))
				{
					byte[] bt = {ch};
					tokenList.add(new String(bt));
					token.setLength(0);
				}
				else
				{
					byte[] bt = {ch};
					String symbol = new String(bt);
					System.out.println(symbol);
					throw new LexException("Unexpected symbol:" + symbol + " Pos:" + currentPos);
				}
			}
		}
		catch (EOFException e)
		{
			// tokenList.add(EOF); //Нет необходимость добавлять в список лексем конец файла,
			// окончание вычисление производится по исчерпанию списка
		}
	}

	public void generateNameToken(List<String> tokenList, DataInputStream dis,
								  StringBuffer token, int[] currentPos) throws LexException, IOException
	{
		try
		{
			for (; ;)
			{
				byte ch = dis.readByte();
				currentPos[0]++;
				if (isAllowedOperation(ch) || spacesH.contains(new Integer(ch)))
				{
					tokenList.add(token.toString());
					if (!spacesH.contains(new Integer(ch)))
					{
						byte[] bt = {ch};
						tokenList.add(new String(bt));
					}
					break;
				}
				else if (isAllowedNameLiteral(ch, token.toString()))
					token.append((char) ch);
				else
				{
					byte[] bt = {ch};
					String symbol = new String(bt);
					System.out.println(symbol);
					throw new LexException("Unexpected symbol:" + symbol + " Pos:" + currentPos);
				}
			}
		}
		catch (EOFException e)
		{
			tokenList.add(token.toString());
			throw new EOFException();
		}
	}

	public void generateValueToken(List<String> tokenList, DataInputStream dis,
								   StringBuffer token, int[] currentPos) throws LexException, IOException
	{
		try
		{
			for (; ;)
			{
				byte ch = dis.readByte();
				currentPos[0]++;
				if (isAllowedOperation(ch) || spacesH.contains(new Integer(ch)))
				{
					tokenList.add(token.toString());
					if (!spacesH.contains(new Integer(ch)))
					{
						byte[] bt = {ch};
						tokenList.add(new String(bt));
					}
					break;
				}
				else if (isAllowedValueLiteral(ch, token.toString()))
					token.append((char) ch);
				else
				{
					byte[] bt = {ch};
					String symbol = new String(bt);
					System.out.println(symbol);
					throw new LexException("Unexpected symbol:" + symbol + " Pos:" + currentPos);
				}
			}
		}
		catch (EOFException e)
		{
			tokenList.add(token.toString());
			throw new EOFException();
		}

	}

	public static class LexemToken implements IToken
	{
		private String represent;
		private boolean function;
		private IGetValueByName valueByName;

		LexemToken(String represent, boolean function,IGetValueByName getValueByName)
		{

			this.represent = represent;
			this.function = function;
			this.valueByName = getValueByName;
		}

		public void replacebyLexem(LexemToken lexem)
		{
			represent=lexem.represent;
			function=lexem.function;
			valueByName=lexem.valueByName;
		}

		public String getRepresent()
		{
			return represent;
		}

		public void setRepresent(String represent)
		{
			this.represent = represent;
		}

		public boolean isFunction()
		{
			return function;
		}

		public void setFunction(boolean function)
		{
			this.function = function;
		}

		public String getToken()
		{
			if (function)
				return "f";
			try
			{
				Double.parseDouble(represent);
				return "n";
			}
			catch (NumberFormatException e)
			{
				if (represent.equals("+") || represent.equals("-"))
					return "op_a";
				if (represent.equals("*") || represent.equals("/"))
					return "op_m";

				if (
						represent.equals("==") || represent.equals("!=") ||
						represent.equals("<") || represent.equals(">") ||
						represent.equals("<=") || represent.equals(">=")
					)
					return "op_c";

				if (represent.equals(","))
					return "sp";

				if (servLiteralsH.contains(new Integer(represent.toCharArray()[0])))
					return represent;
				return "i";
			}
		}

		public double getValue() throws CalcException
		{
			try
			{
				return Double.parseDouble(represent);
			}
			catch (NumberFormatException e)
			{
				if (getToken().equals("i") && valueByName !=null)
					return valueByName.getValueByName(getRepresent());
				throw new CalcException(e);
			}
		}

		public int getNrule()
		{
			return -1;
		}
	}
}
