package com.logfarm.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger 
{
	public static boolean isUsed = false;
	public static final Logger singleton = new Logger();
	
	private File file;
	private FileWriter fileWriter;
	private BufferedWriter writer;
	
	public void enable()
	{
		try 
		{
			file = new File("log.txt");
			file.createNewFile();
			fileWriter = new FileWriter(file);
			writer = new BufferedWriter(fileWriter);
			System.out.println("logger ready");
			isUsed = true;
		} catch (IOException e) 
		{
			System.out.println("An error occurred when creating the log-file.");
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try 
		{
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void log(String line)
	{
		try 
		{
			writer.write(line);
			writer.newLine();
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
