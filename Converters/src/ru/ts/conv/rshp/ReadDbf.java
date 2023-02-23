package ru.ts.conv.rshp;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

/**
 *
 *
 */
public class ReadDbf
{
	public static void main(String args[])
	{

		try
		{

			// create a DBFReader object
			//
			InputStream inputStream = new FileInputStream("D:\\dbf\\rrspnt_200.dbf"); // take dbf file as program argument
			DBFReader reader = new DBFReader(inputStream);
			reader.setCharactersetName("WINDOWS-1251");
			// get the field count if you want for some reasons like the following
			//
			int numberOfFields = reader.getFieldCount();

			// use this count to fetch all field information
			// if required
			//
			for (int i = 0; i < numberOfFields; i++)
			{

				DBFField field = reader.getField(i);

				// do something with it if you want
				// refer the JavaDoc API reference for more details
				//
				System.out.println(field.getName());
			}

			System.out.println("\n\n\n");
			// Now, lets us start reading the rows
			//
			Object[] rowObjects;


			int j=0;
			while ((rowObjects = reader.nextRecord()) != null)
			{

				for (int i = 0; i < rowObjects.length; i++)
				{
					System.out.println(rowObjects[i]);
				}
				j++;
				System.out.println("\n\n\n");
			}

			// By now, we have itereated through all of the rows

			inputStream.close();
			System.out.println("cntJ = " + j);
		}
		catch (DBFException e)
		{

			System.out.println(e.getMessage());
		}
		catch (IOException e)
		{

			System.out.println(e.getMessage());
		}
	}
}  
