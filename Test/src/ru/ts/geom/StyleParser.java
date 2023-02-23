package ru.ts.geom;


import java.util.*;
import java.io.File;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;

public class StyleParser
{

	HashMap<String, CommonStyle> retStyles= new HashMap<String,CommonStyle>();

	public StyleParser()
	{
	}

	public static void main(String[] args) throws Exception
	{
		StyleParser parser = new StyleParser();

		HashMap<String,CommonStyle> retVal = parser.parse("D:\\Vlad\\JavaProj\\Styles\\polygon.xml");

		Set<String> keys = retVal.keySet();

		for (String key : keys)
		{
			System.out.println("key = " + key);
		}


	}

	public HashMap<String, CommonStyle> parse(String inputFile) throws Exception
	{
		StyleParser.SParser handler = new StyleParser.SParser();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(new File(inputFile), handler);
		return retStyles;
	}

	public class SParser
			extends DefaultHandler
	{

		boolean rulein=false;
		boolean typein=false;
		boolean wtype=false;
		boolean fillin=false;
		boolean fillcolin=false;
		LinkedList<String> types=new LinkedList<String>();
		String fillcolor;
		private boolean wfillcolorin=true;

		public SParser()
		{

		}


		public void startDocument()
				throws SAXException
		{

		}

		public void endDocument()
				throws SAXException
		{

		}

		public void startElement(String namespaceURI,
								 String sName, // def name (localName)
								 String qName, // qualified name
								 Attributes attrs)
				throws SAXException
		{

			String eName = sName; // element name

			if (fillcolin)
			{
				if (qName.equalsIgnoreCase("ogc:Literal"))
					wfillcolorin=true;

			}
			else
			if (typein)
			{
				if (qName.equalsIgnoreCase("ogc:Literal"))
					wtype=true;
			}
			else
			if (rulein)
			{

				if (fillin)
				{
					if (qName.equalsIgnoreCase("sld:CssParameter") && attrs.getValue(0).equals("fill"))
						fillcolin=true;
				}
				else
				if (qName.equalsIgnoreCase("ogc:PropertyName"))
					typein=true;
				else
				if (qName.equalsIgnoreCase("sld:Fill"))
					fillin=true;


			}
			else
			if (qName.equalsIgnoreCase("sld:Rule"))
			{
				rulein=true;
			}



//			if ("".equals(eName)) eName = qName; // namespaceAware = false

		}


		public void endElement(String namespaceURI,
							   String sName, // def name
							   String qName  // qualified name
		) throws SAXException
		{

			if (qName.equalsIgnoreCase("ogc:Literal") && wfillcolorin)
			{
				wfillcolorin=false;
				fillcolin=false;
			}


			if (qName.equalsIgnoreCase("ogc:Literal") && wtype)
			{
				wtype=false;
				typein=false;
			}

			if (qName.equalsIgnoreCase("sld:Rule"))
			{
				rulein=false;

				for (String type : types)
				{
					int color= (int) Long.parseLong(fillcolor.substring(1),16);
					retStyles.put(type,new CommonStyle(color,color));
				}
				types.clear();
			}

			if (qName.equalsIgnoreCase("sld:Fill"))
				fillin=false;

		}

		public void characters(char buf[], int offset, int len)
				throws SAXException
		{
			if (wtype)
				types.add(new String(buf,offset,len));
			if (wfillcolorin)
				fillcolor=new String(buf,offset,len);
		}
	}

}
