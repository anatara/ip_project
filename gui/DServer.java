package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;


public class DServer implements Runnable
{

	public ServerSocket sersocket,sersocket1;
	public Socket receive;
	public Properties prop=new Properties();
	public int s=0;
	public String ssuser,snam;
	public String suser,spwd,nam;
	public String recurl,prevnod,currnam;
	public Vector srcvect = new Vector();
	public Thread t,t1;
	Vector des;

	public DServer()
	{
		super();

		System.out.println("Inside Resover cons.....");
		t=new Thread(this);
		t.start();

	}
	
	public void run()
	{
		receiveurl();
	}

	public void receiveurl()
	{
		System.out.println("waiting for Connection inside getRequest");
		try
		{
			sersocket= new ServerSocket(7777);

			while(true)
			{
				receive=sersocket.accept();
				prevnod=receive.getInetAddress().getHostName();
				System.out.println("Inside Receive URL::"+prevnod);
				ObjectInputStream ois=new ObjectInputStream(receive.getInputStream());
				recurl=(String)ois.readObject();

				if(recurl.startsWith("url"))
				{
					des=new Vector();
					System.out.println("Inside Received URl::" + recurl);
					StringTokenizer st=new StringTokenizer(recurl,"$");
					st.nextToken();
					suser=st.nextToken();
					ssuser=suser;
					des.add(suser);
					spwd=st.nextToken();
					des.add(spwd);
					srcvect.add(des);
					System.out.println("srul vect val::::"+srcvect);
					nam=st.nextToken();
					System.out.println("Cleared Vector..." + nam);
					//snam=nam;
					checkpassword(suser,spwd,nam);

				}

			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void checkpassword(String surl,String durl,String nam)
	{		
		System.out.println("Inside PWD check");
		if (surl.equals("admin" ))
		{
			if(durl.equals("admin"))
			{
				s = 1;
			}
			else
			{
				s = 0;
			}
		}
		else
		{
			s = 0;
		}
		sendreply(nam);

	}


	public void sendreply(String name)
	{
		ObjectOutputStream oos1=null;
		String sysnam="";
		System.out.println("INSIDE REPLY SEND TO USER LOGIN::"+name);
		try
		{
			FileInputStream fis=new FileInputStream("Resolver.Properties");
			prop.load(fis);
			sysnam=prop.getProperty("Resolver");
			System.out.println("Resolver:"+sysnam);
			Socket socket1=new Socket(sysnam,4444);
			oos1=new ObjectOutputStream(socket1.getOutputStream());
			String currnam=InetAddress.getLocalHost().getHostName();
			oos1.writeObject("url"+"$"+suser+"$"+spwd+"$"+currnam+"$"+s);

		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
	}


	public static void main(String args[])
	{
		DServer dnsserv=new DServer();
	}
}