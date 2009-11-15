package gui;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.lang.String.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Toolkit;
import packetformat.*;

public class client_gui extends JFrame implements ActionListener, Runnable  
{
	public JLabel resolver, local,filepath,dnsserv, pwd,resolver1, dnsserv1 ;
	public JTextField localtxt,urltxt,filearea, pwdtxt,localtxt_home, localtxt_home_1, localtxt_home_2, localtxt_home_3, localtxt_home_4, localtxt_home_5, localtxt_home_6, localtxt_home_7, localtxt_home_8, localtxt_home_9, localtxt_home_10;
	public JButton exit,submit,screen2, search_home, exit_home, download_1, download_2, download_3, download_4, download_5, download_6, download_7, download_8, download_9, download_10;
	public JTextArea txt;
	public JTextArea txt1;
	String h[] = {"0","1","2","3","4","5","6","7","8","9"};
	public Vector recvect;
	public JScrollPane sp,sp1;
	JFileChooser chooser;
	int filepoint=0,leng,selected;
	File file1;
	public String urlval,rnam,type,surl,purl,encrypteddata,check,surlrecv,durlrecv;
	public int i=0;
	Vector des;	
	public Vector srcvect = new Vector();
	public JFrame frame, frame1;
	public String prevnod,recattk,filedata,recdecode,currnam, recurl, ssurl, nam, spwd;
	public ServerSocket sersocket;
	public Socket socket,receive;
	public RandomAccessFile random;
	public Properties prop=new Properties();
	public Thread t;
	public PublicKey publickey;
	public byte digest[],gensign[];

	

	public client_gui() throws IOException
	{
		super();
		prop.load(this.getClass().getResourceAsStream("/Resolver.Properties"));
		System.out.println("Inside Resover cons.....");
		getDesign();
		t=new Thread(this);
		t.start();
	}

	public void run()
	{
		replyReceive();
	}

	public void getDesign()
	{
		try
		{
			String inf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			try
			{
				UIManager.setLookAndFeel(inf);
			}
			catch(Exception e)
			{

			}
			System.out.println("Inside getDesign::");
			frame=new JFrame("USER LOGIN SCREEN");
			frame.setLayout(new GridLayout(1,2));

			JPanel panel=new JPanel();
			panel.setLayout(null);
			panel.setBorder(BorderFactory.createTitledBorder("USER LOGIN"));
			resolver = new JLabel("User Name:");
			resolver.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
			resolver.setBounds(70,80,100,20);
			panel.add(resolver);

			dnsserv = new JLabel("LOG IN");
			dnsserv.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,14)); 
			dnsserv.setBounds(200,30,180,20);
			panel.add(dnsserv);

			localtxt=new JTextField(30);
			//localtxt.setFont(new Font("Tw Cen MT",Font.CENTER_BASELINE,14));
			localtxt.setBounds(210,80,150,20);
			panel.add(localtxt);

			pwd=new JLabel("Password");
			pwd.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
			pwd.setBounds(70,150,100,20);
			panel.add(pwd);

			pwdtxt=new JPasswordField(15);
			pwdtxt.setBounds(210,150,150,20);
			panel.add(pwdtxt);

			submit=new JButton("Submit");
			submit.setBounds(100,220,75,20);
			panel.add(submit);

			exit=new JButton("Exit");
			exit.setBounds(200,220,75,20);
			panel.add(exit);

			screen2=new JButton("Screen2");
			screen2.setBounds(300,300,75,20);
			panel.add(screen2);

			frame.add(panel);




			frame.setSize(500,500);

			Toolkit toolkit = getToolkit();
			Dimension size = toolkit.getScreenSize();
			frame.setLocation(size.width/2 - getWidth()/2, 
					size.height/2 - getHeight()/2);



			frame.setVisible(true);
			submit.addActionListener(this);
			exit.addActionListener(this);
			screen2.addActionListener(this);

			//				second frame

			frame1=new JFrame("New Frame");
			frame1.setLayout(new GridLayout(1,2));

			JPanel panel1=new JPanel();
			panel1.setLayout(null);
			panel1.setBorder(BorderFactory.createTitledBorder("New Panel"));
			resolver1 = new JLabel("Enter the text to be searched:");
			resolver1.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
			resolver1.setBounds(70,80,100,20);
			panel1.add(resolver1);

			//				Adding to second frame


			dnsserv1 = new JLabel("Home Screen");
			dnsserv1.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,18));
			dnsserv1.setBounds(200,30,180,20);
			panel1.add(dnsserv1);

			localtxt_home=new JTextField(40);
			localtxt_home.setBounds(210,80,150,20);
			panel1.add(localtxt_home);

			search_home=new JButton("Search");
			search_home.setBounds(400,80,75,20);
			panel1.add(search_home);

			// for providing search results
			localtxt_home_1=new JTextField(40);
			localtxt_home_1.setBounds(210,150,150,20);
			panel1.add(localtxt_home_1);

			localtxt_home_2=new JTextField(40);
			localtxt_home_2.setBounds(210,180,150,20);
			panel1.add(localtxt_home_2);

			localtxt_home_3=new JTextField(40);
			localtxt_home_3.setBounds(210,210,150,20);
			panel1.add(localtxt_home_3);

			localtxt_home_4=new JTextField(40);
			localtxt_home_4.setBounds(210,240,150,20);
			panel1.add(localtxt_home_4);

			localtxt_home_5=new JTextField(40);
			localtxt_home_5.setBounds(210,270,150,20);
			panel1.add(localtxt_home_5);

			localtxt_home_6=new JTextField(40);
			localtxt_home_6.setBounds(210,300,150,20);
			panel1.add(localtxt_home_6);

			localtxt_home_7=new JTextField(40);
			localtxt_home_7.setBounds(210,330,150,20);
			panel1.add(localtxt_home_7);

			localtxt_home_8=new JTextField(40);
			localtxt_home_8.setBounds(210,360,150,20);
			panel1.add(localtxt_home_8);

			localtxt_home_9=new JTextField(40);
			localtxt_home_9.setBounds(210,390,150,20);
			panel1.add(localtxt_home_9);

			localtxt_home_10=new JTextField(40);
			localtxt_home_10.setBounds(210,420,150,20);
			panel1.add(localtxt_home_10);

			download_1=new JButton("Download");
			download_1.setBounds(500,150,100,20);
			panel1.add(download_1);

			download_2=new JButton("Download");
			download_2.setBounds(500,180,100,20);
			panel1.add(download_2);

			download_3=new JButton("Download");
			download_3.setBounds(500,210,100,20);
			panel1.add(download_3);

			download_4=new JButton("Download");
			download_4.setBounds(500,240,100,20);
			panel1.add(download_4);

			download_5=new JButton("Download");
			download_5.setBounds(500,270,100,20);
			panel1.add(download_5);

			download_6=new JButton("Download");
			download_6.setBounds(500,300,100,20);
			panel1.add(download_6);

			download_7=new JButton("Download");
			download_7.setBounds(500,330,100,20);
			panel1.add(download_7);

			download_8=new JButton("Download");
			download_8.setBounds(500,360,100,20);
			panel1.add(download_8);

			download_9=new JButton("Download");
			download_9.setBounds(500,390,100,20);
			panel1.add(download_9);

			download_10=new JButton("Download");
			download_10.setBounds(500,420,100,20);
			panel1.add(download_10);

			exit_home=new JButton("Exit");
			exit_home.setBounds(200,620,75,20);
			panel1.add(exit_home);

			frame1.add(panel1);

			frame1.setSize(700,700);
			frame1.setVisible(false);

			currnam=InetAddress.getLocalHost().getHostName();
			System.out.println(" " + currnam);

		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource()==exit)
		{
			System.exit(0);
		}

		else if(ae.getSource() == screen2)
		{
			open_frame1();
		}

		else if(ae.getSource()==submit)
		{
			String username = localtxt.getText();
			String password = pwdtxt.getText();

			sendurl(username,password);			
		}

		else if(ae.getSource()== search_home)
		{
			JOptionPane.showMessageDialog(null, "Inside Search","Search", JOptionPane.INFORMATION_MESSAGE);
		}

		else if(ae.getSource() == exit_home)
		{
			System.exit(0);
		}


	}

	// Sending username and pwd to server
	public void sendurl(String user,String pwd)
	{
		try
		{
			InetAddress myAddr=InetAddress.getLocalHost();
			InetAddress servAddr=InetAddress.getLocalHost();
			
			int servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));
			int cliPort  = Integer.parseInt(prop.getProperty("Client_UDP_Port"));
			
			byte[] ba1= convIntBary(user.length());
			byte[] ba2= user.getBytes();
			byte[] ba3= convIntBary(pwd.length());
			byte[] ba4= pwd.getBytes();

			byte[] payload = new byte[2*2 + ba2.length + ba4.length];
			
			System.arraycopy(ba1, 0, payload, 0, ba1.length);
			System.arraycopy(ba2, 0, payload, ba1.length, ba2.length);
			System.arraycopy(ba3, 0, payload, ba1.length+ba2.length, ba3.length);
			System.arraycopy(ba4, 0, payload, ba1.length+ba2.length+ba3.length, ba4.length);

			pack udp_pack = new pack( (byte)1, (int)1234, (byte)16, myAddr,cliPort,payload);
			
		    DatagramPacket pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,servAddr,servPort);					    
		    DatagramSocket sock = new DatagramSocket(cliPort);
		    sock.send(pack);
		    sock.close();				
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}




	public void replyReceive()
	{
		System.out.println("Inside Reply Receive at Resolver(DNS Client)::::");
		try
		{
			sersocket= new ServerSocket(4444);
			while(true)
			{
				socket=sersocket.accept();
				prevnod=socket.getInetAddress().getHostName();
				System.out.println("previous node:"+prevnod);
				ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
				recurl=(String)ois.readObject();
				System.out.println("received path from DNS SERVER"+recurl);

				if(recurl.startsWith("url"))
				{
					des=new Vector();
					System.out.println("Inside Received URl::" + recurl);
					StringTokenizer st=new StringTokenizer(recurl,"$");
					st.nextToken();
					surlrecv=st.nextToken();
					ssurl=surlrecv;
					des.add(surlrecv);
					//System.out.println("MAP2 Contains Val And source url"+map2);
					durlrecv=st.nextToken();
					des.add(durlrecv);
					srcvect.add(des);
					System.out.println("srul vect val::::"+srcvect);
					///des.removeAllElements();
					System.out.println("srul vect val***********"+srcvect);
					nam=st.nextToken();
					System.out.println("Cleared Vector..." + nam);
					spwd=st.nextToken();

					chkpwd(spwd);

				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void chkpwd(String password)
	{
		System.out.println("Inside PWD check");
		if(password.equals("1"))
		{
			JOptionPane.showMessageDialog(null, "Correct Password","Password Check", JOptionPane.INFORMATION_MESSAGE);
			open_frame1();
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Enter the Correct Password","Wrong Password", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void open_frame1()
	{
		frame.setVisible(false);
		frame1.setVisible(true);
		search_home.addActionListener(this);
		exit_home.addActionListener(this);
	}

	public static final byte[] convIntBary(int ival) {
		return new byte[] {(byte)(ival >>> 8),(byte)ival};
	}

	public static final void printbary(byte[] bary) {
		for (int i = 0; i < bary.length; i++) {
			int t= bary[i];
			String temp = "0000";
			temp=temp.concat(Integer.toHexString(t));
			temp=temp.toUpperCase();
			System.out.printf("%c%c",temp.charAt(temp.length()-2),temp.charAt(temp.length()-1));
		}
		System.out.println("");
	}
	
	public static void main(String args[]) throws IOException
	{
		client_gui t1=new client_gui();
		System.out.println(" " + t1.currnam);
	}

}


