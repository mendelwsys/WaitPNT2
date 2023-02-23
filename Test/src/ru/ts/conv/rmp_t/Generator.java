package ru.ts.conv.rmp_t;

/**
 * Генератор описателя хранилища и главного хранилища
 */
public class Generator
{

	protected String tagName;
	protected Class cl;
	protected String storclassName;

	public Generator(String tagName,Class generclass)
	{
		this.tagName=tagName;
		this.cl = generclass;
		storclassName=cl.getName();
	}

	public Generator(String tagName,String storclassName)
	{
		this.tagName=tagName;
		this.storclassName=storclassName;
	}

	public String getTag()
	{
		return tagName;
	}

	public Class getCl()
	{
		return cl;
	}

	public void setCl(Class cl)
	{
		this.cl = cl;
	}

	public String getClassName()
	{
		return storclassName;
	}

	public void setClassName(String storclassName)
	{
		this.storclassName = storclassName;
	}

}
