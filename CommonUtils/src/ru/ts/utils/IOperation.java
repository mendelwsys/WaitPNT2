package ru.ts.utils;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.awt.*;

public class IOperation
{
	/**
	 * Получить содержимое файла по его имени и классу для загрузки
	 * @param cl - класс с помощью которого открывается ресурс
	 * @param resname - имя ресурса
	 * @param codepage -
	 * @return - строка 0 содержимое файла
	 * @throws IOException -
	 */
	static public String getTxtContentByResFile(Class cl,String resname,String codepage)
			throws IOException
	{
		InputStream stream = cl.getResourceAsStream(resname);
		BufferedReader br = new BufferedReader(new InputStreamReader(stream,codepage));
		StringBuffer rv=new StringBuffer();
		String rl=null;
		while ((rl=br.readLine())!=null)
			rv.append("\n").append(rl);

		rv.append("\n");
		return rv.toString();
	}

	static public void showError(Exception e, String title)
	{
		ByteArrayOutputStream baos;
		PrintStream ps = new PrintStream(baos = new ByteArrayOutputStream());
		e.printStackTrace(ps);
		ps.flush();
		ps.close();
		JOptionPane.showMessageDialog(null, new String(baos.toByteArray()), title, JOptionPane.ERROR_MESSAGE);
	}

	static public DataOutputStream getOutPutStreamFromFile(JFrame frame,String Header,String ext) throws FileNotFoundException
	{
		FileDialog fld = new FileDialog(frame, Header, FileDialog.SAVE);
		fld.setVisible(true);
		String fullfilename = fld.getDirectory() + "\\" + fld.getFile();
		if (fld.getDirectory()==null || fld.getFile()==null || fullfilename==null || fullfilename.length()==0)
			return null;
		File fl = new File(fullfilename);
		return new DataOutputStream(new FileOutputStream(fl));
	}

	static public DataInputStream getInPutStreamFromFile(JFrame frame,String Header,String ext) throws FileNotFoundException
	{
		FileDialog fld = new FileDialog(frame, Header, FileDialog.LOAD);
		fld.setVisible(true);
		String fullfilename = fld.getDirectory() + "\\" + fld.getFile();
		if (fld.getDirectory()==null || fld.getFile()==null || fullfilename==null || fullfilename.length()==0)
			return null;
		File fl = new File(fullfilename);
		return new DataInputStream(new FileInputStream(fl));
	}

	static public File getDirPath(Dialog dialog,String header,String dirs,javax.swing.filechooser.FileFilter filter)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(dirs));
		chooser.setDialogTitle(header);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (filter!=null)
			chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(dialog);
		if(returnVal == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		return null;
	}

	/**
	 * get file full path by dialog
	 * @param frame - main frame of application
	 * @param Title  - dialog title
	 *@param Header - header of dialog
	 * @param ext - extention filter
	 * @param openlocation - open dialog on location @return full path of choosed file or null if canceled
	 * @return - file if it was chosen or null if not
	 */
	static public File getFilePath(Component frame, String Title, final String Header, final String ext, String openlocation)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(Title);
		chooser.setFileFilter(new javax.swing.filechooser.FileFilter()
		{
			public boolean accept(File f)
			{
				return !f.isFile() || f.getName().endsWith(ext);
			}

			/**
			 * The description of this filter. For example: "JPG and GIF Images"
			 * @see javax.swing.filechooser.FileView#getName
			 */
			public String getDescription()
			{
				return Header;
			}
		}
		);
		if (openlocation != null)
		{
			File file = new File(openlocation);
			if (file.exists())
				chooser.setSelectedFile(file);
		}
		int returnVal = chooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		return null;
	}
}
