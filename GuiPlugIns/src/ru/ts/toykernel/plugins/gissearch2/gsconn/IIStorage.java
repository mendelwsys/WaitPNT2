package ru.ts.toykernel.plugins.gissearch2.gsconn;

import su.org.coder.utils.SysCoderEx;

import java.io.IOException;

public interface IIStorage
{ 

	int getCurpos() throws IOException, SysCoderEx; 

	String get(int i) throws IOException, SysCoderEx; 

	boolean hasMoreElements() throws IOException, SysCoderEx; 

	boolean hasPrevElements() throws IOException, SysCoderEx; 

	String nextElement() throws IOException, SysCoderEx; 

	int setposon(int pos) throws IOException, SysCoderEx; 

	String prevElement() throws IOException, SysCoderEx; 

	int size() throws IOException, SysCoderEx; 

}
