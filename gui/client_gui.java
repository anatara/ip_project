package gui;



import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.String.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Toolkit;
import packetformat.*;
import trigest.trigest;
import general.*;

public class client_gui extends JFrame implements  ActionListener, Runnable  
{
	public JLabel resolver, local,filepath,dnsserv, pwd,resolver1, dnsserv1, dnsserv2,page_display, Username_Reg, Pwd_Reg, Email_Reg, Pub_file, Pub_file_size, Pub_abstract,Pub_file_label ;
	public JTextField localtxt,urltxt,filearea, pwdtxt,localtxt_home,Username_Reg_txt, Pwd_Reg_txt,Email_Reg_txt;
	public JTextArea[] sresult_dis=new JTextArea[10];
	public JTextArea Pub_abstract_area;
	public JButton exit,submit,screen2, search_home, exit_home,publish_home, previous_page, next_page, Register, Register_login, Pub_ok, unPublish, Remove_files, cancel_unpublish;
	public JButton[] sresult_but=new JButton[10];
	public JFrame frame, frame1, Reg_frame, Pub_frame,unPublish_Frame;
	public JList list;
	public DefaultListModel listModel;
	Map<String, String> fileHash = new LinkedHashMap<String, String>();

	public InetAddress myAddr,servAddr;
	public search_result[] sresult=new search_result[100];
	public int sresults=0,sresults_sh=0;
	public int download_reply=0;
	public String myname;
	public File publish_file;
	String[] farray;
	int present_file_count;

	public Properties prop=new Properties();
	public Thread thread;
	public genfunc gfns = new genfunc();
	DatagramSocket pocket;


	public client_gui() throws Exception
	{
		super();
		prop.load(this.getClass().getResourceAsStream("/Resolver.Properties"));
		getDesign();
		load_file_hash();

		myinet4addr getmyaddr = new myinet4addr();
		myAddr = getmyaddr.getMy4Ia();

		servAddr= InetAddress.getByAddress(gfns.getIpAsArrayOfByte(prop.getProperty("Server")));

		System.out.println("Using my IP address " + myAddr + " and Server IP address" + servAddr);


		open_udp_port();
		thread=new Thread(this);
		thread.start();

	}

	private void load_file_hash() throws Exception {

		BufferedReader triRead1 = new BufferedReader(new FileReader("SHA_Path"));
		String line = null; 

		while (( line = triRead1.readLine()) != null){
			fileHash.put(line.substring(0, 40) , line.substring(40));
		}
		triRead1.close();
		//a.close();

	}

	public void open_udp_port() {

		try{
			int servPort = Integer.parseInt(prop.getProperty("Client_UDP_Port"));

			pocket= new DatagramSocket(servPort);
			//Creating a packet to hold the incoming UDP packet
			DatagramPacket packet=new DatagramPacket(new byte[256],256);
		}
		catch(IOException ioe){
			System.out.println("Error:"+ioe);
		}

	}

	public void run()
	{
		DatagramPacket packet=new DatagramPacket(new byte[256],256);
		while(true){
			try {
				if (pocket.isClosed()){break;}
				pocket.receive(packet);  
				System.out.println("packet received");
				process_pocket(packet);
				if (pocket.isClosed()){break;}
			}
			catch (IOException e) {
				System.out.println(e);} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		//pocket.close();
	}		

	private void process_pocket(DatagramPacket packet) throws Exception {
		pack udp_pack = new pack(packet.getData(),packet.getLength());

		switch(udp_pack.getPkttype()){

		case 81:
			if (login_reply(udp_pack)) {

				client_keepAlive cka = new client_keepAlive(this);

				open_frame1();
				new Thread(cka).start();
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Wrong Password","Password Check", JOptionPane.INFORMATION_MESSAGE);
			}
			break;
		case 21:
		{
			System.out.println("Download request received ");
			send_dload_reply(udp_pack);
		}
		break;	

		case 22:
		{
			download_reply=udp_pack.data[udp_pack.getPaylength()-1];
			System.out.println("Download request succesfull with reply " + download_reply);
		}
		break;	

		case 6:
		{
			if (udp_pack.getData()[udp_pack.getData().length-1]==1){
				JOptionPane.showMessageDialog(null, "Registered","New User Registration", JOptionPane.INFORMATION_MESSAGE);
				Reg_frame.setVisible(false);
				frame.setVisible(true);
			}
			else {
				JOptionPane.showMessageDialog(null, "Registration failed - User Name already exists","New User Registration", JOptionPane.INFORMATION_MESSAGE);
				Username_Reg_txt.setText(" ");
				Pwd_Reg_txt.setText("");
				Email_Reg_txt.setText(" ");
			}
		}
		break;


		case 99:
		{
			System.out.println("Some error in login");
			JOptionPane.showMessageDialog(null, "Please Login again","Login timed out", JOptionPane.INFORMATION_MESSAGE);
		}
		break;	
		}

	}

	private void send_dload_reply(pack udpPack) throws Exception {
		gfns.printbary(udpPack.getData());
		byte res =1;

		if (fileHash.containsKey(gfns.ByteArraytohexString(udpPack.getData()))){
			res=2;
		}

		//InetAddress myAddr=InetAddress.getLocalHost();
		int servPort = Integer.parseInt(prop.getProperty("Client_UDP_Port"));
		InetAddress cliAddr = udpPack.getIP();
		int cliPort = udpPack.getPort_no();
		byte [] payload=new byte[]{00, 01, res};
		pack udp_pack = new pack( (byte)22, (int)1234, (byte)16, myAddr,servPort,payload.length,payload);

		DatagramPacket pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,cliAddr,cliPort);
		pocket.send(pack);
	}

	private boolean login_reply(pack udpPack) {

		boolean blnResult = false;

		if (udpPack.data[udpPack.getPaylength()-1] == (byte)1 )
			blnResult=true;

		return blnResult;
	}


	public void getDesign()
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

		Register_login=new JButton("New User");
		Register_login.setBounds(250,400,100,20);
		panel.add(Register_login);


		frame.add(panel);

		frame.setSize(500,500);

		Toolkit toolkit = getToolkit();
		Dimension size = toolkit.getScreenSize();
		frame.setLocation(size.width/4 - getWidth()/4, size.height/4 - getHeight()/4);

		frame.setVisible(true);
		submit.addActionListener(this);
		exit.addActionListener(this);
		screen2.addActionListener(this);
		Register_login.addActionListener(this);

		//second frame

		frame1=new JFrame("New Frame");
		frame1.setLayout(new GridLayout(1,2));

		JPanel panel1=new JPanel();
		panel1.setLayout(null);
		panel1.setBorder(BorderFactory.createTitledBorder("New Panel"));
		resolver1 = new JLabel("Enter the text to be searched:");
		resolver1.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
		resolver1.setBounds(70,80,100,20);
		panel1.add(resolver1);

		//Adding to second frame

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

		previous_page=new JButton("Previous");
		previous_page.setBounds(50,570,100,20);
		panel1.add(previous_page);
		previous_page.setEnabled(false);

		page_display = new JLabel("");
		page_display.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
		page_display.setBounds(180,570,400,20);
		panel1.add(page_display);
		page_display.setEnabled(true);

		next_page=new JButton("Next");
		next_page.setBounds(450,570,75,20);
		panel1.add(next_page);
		next_page.setEnabled(false);

		// for providing search results
		for (int i = 0; i < 10; i++) {
			sresult_dis[i]=new JTextArea();
			sresult_dis[i].setBounds(50,150+i*40,400,30);
			panel1.add(sresult_dis[i]);

			sresult_but[i]=new JButton("Download");
			sresult_but[i].setBounds(500,150+i*40,100,20);
			panel1.add(sresult_but[i]);
			sresult_but[i].setEnabled(false);
		}


		publish_home=new JButton("Publish");
		publish_home.setBounds(200,620,75,20);
		panel1.add(publish_home);

		exit_home=new JButton("Exit");
		exit_home.setBounds(300,620,75,20);
		panel1.add(exit_home);



		unPublish = new JButton("UnPublish");
		unPublish.setBounds(500,620,125,20);
		panel1.add(unPublish);

		// Registration Frame

		Reg_frame = new JFrame("Registration Screen");
		Reg_frame.setLayout(new GridLayout(1,2));
		JPanel panel2 = new JPanel();
		panel2.setLayout(null);
		panel2.setBorder(BorderFactory.createTitledBorder("Registration Panel"));

		Username_Reg = new JLabel("User Name:");
		Username_Reg.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
		Username_Reg.setBounds(70,80,100,20);
		panel2.add(Username_Reg);

		Username_Reg_txt=new JTextField(30);
		Username_Reg_txt.setBounds(210,80,150,20);
		panel2.add(Username_Reg_txt);

		Pwd_Reg = new JLabel("Password:");
		Pwd_Reg.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
		Pwd_Reg.setBounds(70,140,100,20);
		panel2.add(Pwd_Reg);

		Pwd_Reg_txt=new JPasswordField(15);
		Pwd_Reg_txt.setBounds(210,140,150,20);
		panel2.add(Pwd_Reg_txt);

		Email_Reg = new JLabel("Email ID:");
		Email_Reg.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
		Email_Reg.setBounds(70,200,100,20);
		panel2.add(Email_Reg);

		Email_Reg_txt=new JTextField(30);
		Email_Reg_txt.setBounds(210,200,150,20);
		panel2.add(Email_Reg_txt);

		dnsserv2 = new JLabel("Register New User");
		dnsserv2.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,18));
		dnsserv2.setBounds(100,30,300,50);
		panel2.add(dnsserv2);

		Register=new JButton("Register");
		Register.setBounds(200,300,100,20);
		panel2.add(Register);
		Register.addActionListener(this);

		Reg_frame.add(panel2);
		Reg_frame.setSize(500,500);
		Reg_frame.setVisible(false);		


		// For Showing Published Data

		Pub_frame = new JFrame("Publish File");
		Pub_frame.setLayout(new GridLayout(1,2));
		JPanel Pub_panel = new JPanel();
		Pub_panel.setLayout(null);
		Pub_panel.setBorder(BorderFactory.createTitledBorder("Publish File"));

		//	Pub_file = new JLabel("File Name:");
		//	Pub_file.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
		//	Pub_file.setBounds(70,80,100,20);
		//	Pub_panel.add(Pub_file);

		Pub_file_size = new JLabel("...");
		Pub_file_size.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
		Pub_file_size.setBounds(70,80,500,20);
		Pub_panel.add(Pub_file_size);


		Pub_file_label=new JLabel(" ");
		Pub_file_label.setBounds(70,110,450,20);
		Pub_panel.add(Pub_file_label);

		Pub_abstract = new JLabel("Abstract:");
		Pub_abstract.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
		Pub_abstract.setBounds(70,140,100,20);
		Pub_panel.add(Pub_abstract);

		Pub_abstract_area = new JTextArea();
		Pub_abstract_area.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));
		Pub_abstract_area.setBounds(70,160,360,300);
		Pub_panel.add(Pub_abstract_area);

		Pub_ok=new JButton("Ok");
		Pub_ok.setBounds(360,480,70,20);
		Pub_panel.add(Pub_ok);
		Pub_ok.addActionListener(this);

		Pub_frame.add(Pub_panel);
		Pub_frame.setSize(600,600);
		Pub_frame.setVisible(false);



		//UnPublish Frame
		unPublish_Frame = new JFrame("UnPublish");
		unPublish_Frame.setLayout(new GridLayout(1,2));		
		JPanel unPublish_panel = new JPanel();
		unPublish_panel.setLayout(null);
		unPublish_panel.setBorder(BorderFactory.createTitledBorder("UnPublish Files"));

		Remove_files = new JButton("Remove Files");
		Remove_files.setBounds(300,150,140,20);
		unPublish_panel.add(Remove_files);

		cancel_unpublish = new JButton("Cancel");
		cancel_unpublish.setBounds(300,250,140,20);
		unPublish_panel.add(cancel_unpublish);

		//		JCheckBox j1 = new JCheckBox();
		//		j1.setBounds(50, 100, 50, 50);
		//		unPublish_panel.add(j1);

		listModel = new DefaultListModel();

		list = new JList(listModel); 
		//list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		list.setBounds(50,50,200,500);


		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 80));
		unPublish_panel.add(list);

		unPublish_Frame.add(unPublish_panel);
		unPublish_Frame.setSize(600,600);
		unPublish_Frame.setVisible(false);

		Remove_files.addActionListener(this);
		cancel_unpublish.addActionListener(this);

		search_home.addActionListener(this);
		exit_home.addActionListener(this);
		publish_home.addActionListener(this);
		next_page.addActionListener(this);
		previous_page.addActionListener(this);
		unPublish.addActionListener(this);

		for (int i = 0; i < 10; i++) {
			sresult_but[i].addActionListener(this);
		}


		frame1.add(panel1);
		frame1.setSize(700,700);
		frame1.setVisible(false);
	}


	public  void actionPerformed(ActionEvent ae)
	{	
		if(ae.getSource()==exit)
		{
			//System.exit(0);
			try {
				client_exit();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		else if(ae.getSource() == screen2)
		{
			open_frame1();
		}

		else if(ae.getSource()==submit)
		{
			String username = localtxt.getText();
			String password = pwdtxt.getText();
			myname=username;
			try {
				send_login(username,password);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}

		else if(ae.getSource()== search_home)
		{
			String txtsearch=localtxt_home.getText();
			if(txtsearch.length()==0){
				JOptionPane.showMessageDialog(null, "Enter some text to search","Search", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			try {
				clean_results();
				send_txtsearch(txtsearch);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			show_results(1);

		}
		else if(ae.getSource() == next_page){
			show_results(1);
		}

		else if(ae.getSource() == previous_page){
			sresults_sh-=2;
			show_results(-1);
		}

		else if(ae.getSource() == publish_home){

			try {
				final JFileChooser jfc = new JFileChooser();
				jfc.showOpenDialog(this);
				if(jfc.getSelectedFile()!=null){
					//publishdata("test",jfc.getSelectedFile());
					publish_file=jfc.getSelectedFile();
					Pub_file_size.setText("File :   " + publish_file.getName() + "    (" + publish_file.length() + " bytes)");
					Pub_file_label.setText(publish_file.getPath());
					Pub_file_label.setFont(new Font("TimesNewRoman",Font.CENTER_BASELINE,12));

					//frame1.setVisible(false);
					Pub_frame.setVisible(true);
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//JOptionPane.showMessageDialog(null, "Published","Publish", JOptionPane.INFORMATION_MESSAGE);

		}


		else if(ae.getSource() == unPublish)
		{

			unPublish_Frame.setVisible(true);
			
			publish_home.setEnabled(false);

			listModel.removeAllElements();
			Iterator it = fileHash.keySet().iterator();
			farray = new String[fileHash.size()];
			File fname;
			present_file_count = 0;
			while ( it.hasNext()){
				farray[present_file_count]=(String) it.next();
				fname = new File (fileHash.get(farray[present_file_count]));
				listModel.addElement(fname.getName());
				present_file_count ++;
			}

		}

		else if(ae.getSource() == cancel_unpublish)
		{

			unPublish_Frame.setVisible(false);
			publish_home.setEnabled(true);
			//listModel.clear();



		}

		else if(ae.getSource() == Remove_files)
		{
			int list_sel_index = list.getSelectedIndex();
			//list.getSele
			System.out.println(list_sel_index);
			listModel.remove(list_sel_index);
			System.out.println("removing file hash of "+ farray[list_sel_index]);
			try {
				remove_file_hash(farray[list_sel_index]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			Iterator it = fileHash.keySet().iterator();
			farray = new String[fileHash.size()];
			File fname;
			present_file_count = 0;
			while ( it.hasNext()){
				farray[present_file_count]=(String) it.next();
				fname = new File (fileHash.get(farray[present_file_count]));
				present_file_count ++;
			}


		}

		else if(ae.getSource() == Pub_ok)
		{
			try{
				publishdata(localtxt.getText(),publish_file,Pub_abstract_area.getText());
				Pub_frame.setVisible(false);
				//		System.out.println("Abstract: "+ Pub_abstract_area.getText() );
				frame1.setVisible(true);
				Pub_abstract_area.setText("");
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


		else if(ae.getSource() == Register_login)
		{

			frame.setVisible(false);
			Reg_frame.setVisible(true);
			

		}

		else if(ae.getSource() == Register)
		{
			String new_username = Username_Reg_txt.getText();
			String new_password = Pwd_Reg_txt.getText();
			String new_email = Email_Reg_txt.getText();
			try {
				sendReg(new_username,new_password,new_email);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		else if(ae.getSource() == exit_home)
		{
			try {
				client_exit();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.exit(0);
		}

		for (int i = 0; i < 10; i++) {

			if(ae.getSource() == sresult_but[i])
				//JOptionPane.showMessageDialog(null, "Enter some text to search "+ sresults_sh,"Search", JOptionPane.INFORMATION_MESSAGE);
				download_file(i+((sresults_sh-1)*10));
		}


	}




	private void load_farray() {
		Iterator it = fileHash.keySet().iterator();
		farray = new String[fileHash.size()];
		File fname;
		present_file_count = 0;
		while ( it.hasNext()){
			farray[present_file_count]=(String) it.next();
			fname = new File (fileHash.get(farray[present_file_count]));
			listModel.addElement(fname.getName());
			present_file_count ++;
		}

	}

	// Registration
	public void sendReg(String user, String pwd, String email) throws Exception
	{
		//	InetAddress myAddr=InetAddress.getLocalHost();
		//	InetAddress servAddr=InetAddress.getLocalHost();

		int servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));
		int cliPort  = Integer.parseInt(prop.getProperty("Client_UDP_Port"));


		//User name & length
		byte[] b1=gfns.convIntBary_2(user.length());
		byte[] b2=user.getBytes();

		//Password and length
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] b4 = md.digest(pwd.getBytes());
		byte[] b3 = gfns.convIntBary_2(b4.length);

		System.out.println("PWD:");
		gfns.printbary(b4);
		//Email and length
		byte[] b5=gfns.convIntBary_2(email.length());
		byte[] b6=email.getBytes();


		byte[] payload=new byte[2*3 + b2.length + b4.length + b6.length];


		System.arraycopy(b1, 0, payload, 0, b1.length);
		System.arraycopy(b2, 0, payload, b1.length, b2.length);
		System.arraycopy(b3, 0, payload, b1.length+b2.length, b3.length);
		System.arraycopy(b4, 0, payload, b1.length+b2.length+b3.length, b4.length);
		System.arraycopy(b5, 0, payload, b1.length+b2.length+b3.length+b4.length, b5.length);
		System.arraycopy(b6, 0, payload, b1.length+b2.length+b3.length+b4.length+b5.length, b6.length);

		pack udp_pack_reg = new pack((byte)5,(int)1601,(byte)16,myAddr,cliPort,(int)payload.length,payload);
		DatagramPacket pack_reg = new DatagramPacket(udp_pack_reg.getPacket(),udp_pack_reg.getPacket().length,servAddr,servPort);					    

		pocket.send(pack_reg);

	}






	private void download_file(int i) {
		System.out.println("Staring download file from " + sresult[i].getResultCont());
		try {
			for (int j = 0; j < 10; j++) {
				send_dload_udp_request(i);
				Thread.currentThread();
				Thread.sleep(500);
				if(download_reply!=0) {break;}
			}
			System.out.println("Staring download file from " + download_reply);
			if (download_reply==1 || download_reply==0){
				System.out.println("Download failed -- file not found in server");
				JOptionPane.showMessageDialog(null, "ERROR : File not found on client","Download Failed", JOptionPane.INFORMATION_MESSAGE);
				sresult[i].dload_status=0;
				sresult_but[i-(sresults_sh-1)*10].setEnabled(false);
			}
			else if (download_reply==2){
				final JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.showSaveDialog(this);
				String folder_name="";
				if(jfc.getSelectedFile()!=null){
					folder_name=jfc.getSelectedFile().getAbsolutePath()+"/";
				}
				File file=new File(folder_name + sresult[i].getFilename());
				System.out.println("Trying Download -- file found in server, storing in path" + file.getAbsolutePath());
				if (establish_download(i,file)==1){
					JOptionPane.showMessageDialog(null, "File Succesfully Downloaded","Download Success", JOptionPane.INFORMATION_MESSAGE);
				}
				else {
					JOptionPane.showMessageDialog(null, "Error in File Download - Please try again","Download Failed", JOptionPane.INFORMATION_MESSAGE);
				
				}
				notify_bserver_dload(i,1);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private void notify_bserver_dload(int i, int j) throws Exception {
		int servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));
		int cliPort  = Integer.parseInt(prop.getProperty("Client_UDP_Port"));

		byte[] ba1 = gfns.convIntBary_2(sresult[i].getUser().length());
		byte[] ba2 = sresult[i].getUser().getBytes();


		byte[] ba4 = sresult[i].getMD();
		byte[] ba3 = gfns.convIntBary_2(ba4.length);

		byte[] payload = new byte[2*2 + ba2.length + ba4.length + 1];

		System.arraycopy(ba1, 0, payload, 0, ba1.length);
		System.arraycopy(ba2, 0, payload, ba1.length, ba2.length);
		System.arraycopy(ba3, 0, payload, ba1.length+ba2.length, ba3.length);
		System.arraycopy(ba4, 0, payload, ba1.length+ba2.length+ba3.length, ba4.length);
		payload[payload.length-1]=(byte)j;

		pack udp_pack = new pack( (byte)31, (int)1234, (byte)16, myAddr,cliPort,(int)payload.length,payload);

		DatagramPacket pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,servAddr,servPort);					    
		pocket.send(pack);

	}

	private int establish_download(int i, File file) throws Exception {	

		//InetAddress myAddr=InetAddress.getLocalHost();
		InetAddress servAddr=sresult[i].getIP();

		int servPort = Integer.parseInt(prop.getProperty("Client_TCP_Port"));

		Socket clientSocket = null;
		int cliPort = 0;
		try {
			clientSocket = new Socket(servAddr,servPort);

		cliPort  = clientSocket.getLocalPort();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		OutputStream os = clientSocket.getOutputStream();
		InputStream in = clientSocket.getInputStream();

		byte[] payload = sresult[i].MD;

		pack tcp_stream = new pack((byte)25,(int)5678,(byte)16,myAddr,cliPort,(int)payload.length,payload);

		System.out.println("Payload size:" + payload.length);
		if(clientSocket.isConnected()){
			os.write(tcp_stream.getPacket());
		}

		FileOutputStream prf= new FileOutputStream(file);

		byte[] buf_dload_file=new byte[sresult[i].filesize];
		in.read(buf_dload_file);
		prf.write(buf_dload_file);


		clientSocket.close();
		prf.close();
		os.close();
		in.close();

		return 1;



	}

	void send_dload_udp_request(int i) throws Exception{


		//InetAddress myAddr=InetAddress.getLocalHost();
		InetAddress servAddr=sresult[i].getIP();

		int servPort = Integer.parseInt(prop.getProperty("Client_UDP_Port"));
		int cliPort  = Integer.parseInt(prop.getProperty("Client_UDP_Port"));

		byte[] payload = sresult[i].MD;

		pack udp_pack = new pack( (byte)21, (int)1234, (byte)16, myAddr,cliPort,(int)payload.length,payload);

		DatagramPacket pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,servAddr,servPort);					    
		pocket.send(pack);


	}


	private void clean_results() {
		for (int i = 0; i < sresults; i++) {
			sresult[i]=null;
		}
		sresults=0;
		sresults_sh=0;
		previous_page.setEnabled(false);
		next_page.setEnabled(false);
		clean_page();
	}

	private void clean_page(){

		for (int i = 0; i < 10; i++) {
			sresult_dis[i].setText(new String(""));
			sresult_but[i].setEnabled(false);
		}
		page_display.setText("");


	}
	private void show_results(int direction) {
		clean_page(); 
		// TODO Auto-generated method stub
		if (sresults==-1){

			JOptionPane.showMessageDialog(null, "Sorry, No results found","Search", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		for (int i=0; (i+sresults_sh*10)<=sresults && i<10; i++) {
			sresult_dis[i].setText(sresult[i+sresults_sh*10].getResultCont());
			if (sresult[i+sresults_sh*10].getDloadStatus()!=0)
				sresult_but[i].setEnabled(true);
			//			else
			//				sresult_but[i].setEnabled(false);
		}
		sresults_sh++;

		previous_page.setEnabled(true);
		next_page.setEnabled(true);
		page_display.setEnabled(true);

		page_display.setText("Displaying " + sresults_sh + " page out of " + (sresults/10+1) +" pages");

		if (sresults_sh<=1){
			previous_page.setEnabled(false);

		}
		if (sresults_sh*10>sresults){
			next_page.setEnabled(false);

		}


	}

	private void client_exit() throws Exception {
		// TODO Auto-generated method stub
		//InetAddress myAddr=InetAddress.getLocalHost();
		//InetAddress servAddr=InetAddress.getLocalHost();

		int servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));
		int cliPort  = Integer.parseInt(prop.getProperty("Client_UDP_Port"));


		byte[] ba1= gfns.convIntBary_2(myname.length());
		byte[] ba2= myname.getBytes();

		byte[] payload = new byte[2 + ba2.length + 3];

		System.arraycopy(ba1, 0, payload, 0, ba1.length);
		System.arraycopy(ba2, 0, payload, ba1.length, ba2.length);




		//	byte[] payload=new byte[]{00, 01, 00};
		pack udp_pack = new pack( (byte)2, (int)1234, (byte)16, myAddr,cliPort,(int)payload.length,payload);

		DatagramPacket pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,servAddr,servPort);					    
		pocket.send(pack);
		pocket.close();
		System.exit(0);

	}

	private void send_txtsearch(String txtsearch) throws Exception {

		//InetAddress myAddr=InetAddress.getLocalHost();
		//InetAddress servAddr=InetAddress.getLocalHost();

		int servPort = Integer.parseInt(prop.getProperty("Server_TCP_Port"));
		//int cliPort  = Integer.parseInt(prop.getProperty("Client_TCP_Port"));

		Socket clientSocket=new Socket(servAddr,servPort);
		OutputStream out= clientSocket.getOutputStream();
		InputStream in = clientSocket.getInputStream();

		byte[] lengthF=new byte[2];
		byte[] temp_holder;
		int index=-1;

		trigest textdigest=new trigest(txtsearch);



		//send user name also 

		byte[] ba1= gfns.convIntBary_2(myname.length());
		byte[] ba2= myname.getBytes();

		byte[] ba3=textdigest.getSignature();

		byte[] payload = new byte[2 + ba2.length + ba3.length];

		System.arraycopy(ba1, 0, payload, 0, ba1.length);
		System.arraycopy(ba2, 0, payload, ba1.length, ba2.length);
		System.arraycopy(ba3, 0, payload, ba1.length+ba2.length, ba3.length);


		pack tcp_stream = new pack( (byte)4, (int)7890, (byte)16, myAddr,clientSocket.getPort(),payload.length,payload);

		if(clientSocket.isConnected()){

			gfns.printbary(tcp_stream.getPacket());
			out.write(tcp_stream.getPacket());
			System.out.println("Key search request sent");
		}

		in.read(lengthF);
		while(gfns.convBaryInt(lengthF)!=0){
			index++;
			temp_holder=new byte[gfns.convBaryInt(lengthF)];
			in.read(temp_holder);
			gfns.printbary(temp_holder);
			sresult[index]=new search_result(temp_holder);
			in.read(lengthF);
		}
		sresults=index;

		clientSocket.close();
		out.close();
		in.close();
	}

	public void open_frame1()
	{
		frame.setVisible(false);
		frame1.setVisible(true);
	}



	// Sending username and pwd to server
	public void send_login(String user,String pwd) throws Exception
	{

		//InetAddress myAddr=InetAddress.getLocalHost();
		//InetAddress servAddr=InetAddress.getLocalHost();

		int servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));
		int cliPort  = Integer.parseInt(prop.getProperty("Client_UDP_Port"));

		byte[] ba1= gfns.convIntBary_2(user.length());
		byte[] ba2= user.getBytes();

		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] ba4 = md.digest(pwd.getBytes());
		byte[] ba3 = gfns.convIntBary_2(ba4.length);

		byte[] payload = new byte[2*2 + ba2.length + ba4.length];

		System.arraycopy(ba1, 0, payload, 0, ba1.length);
		System.arraycopy(ba2, 0, payload, ba1.length, ba2.length);
		System.arraycopy(ba3, 0, payload, ba1.length+ba2.length, ba3.length);
		System.arraycopy(ba4, 0, payload, ba1.length+ba2.length+ba3.length, ba4.length);

		pack udp_pack = new pack( (byte)1, (int)1234, (byte)16, myAddr,cliPort,(int)payload.length,payload);

		DatagramPacket pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,servAddr,servPort);					    
		//		    DatagramSocket sock = new DatagramSocket(cliPort);
		pocket.send(pack);
		//		pocket.close();				

	}

	public String publishdata(String user,File file, String abtract ) throws Exception{

		//InetAddress myAddr=InetAddress.getLocalHost();
		//InetAddress servAddr=InetAddress.getLocalHost();

		if ( abtract == null) { abtract = new String("---");}

		int servPort = Integer.parseInt(prop.getProperty("Server_TCP_Port"));
		//int cliPort  = Integer.parseInt(prop.getProperty("Client_TCP_Port"));
		//int servPort=22886;
		//int cliPort=12487;


		Socket clientSocket=new Socket(servAddr,servPort);
		int cliPort  = clientSocket.getLocalPort();
		OutputStream os = clientSocket.getOutputStream();
		InputStream in = clientSocket.getInputStream();

		String filename=file.getName();
		trigest filedigest=new trigest(file);
		//		trigest textdigest=new trigest("aaa");

		//		gfns.printbary(textdigest.getSignature());
		//		gfns.printbary(filedigest.getSignature());
		//		System.out.println("text digest in HexString format" + gfns.ByteArraytohexString(textdigest.getSignature()));
		//		System.out.println("file digest in HexString format" + gfns.ByteArraytohexString(filedigest.getSignature()));

		//Username & length
		byte[] user_len=gfns.convIntBary_2(user.length());
		byte[] username=user.getBytes();

		//Filename & length
		byte[] fname_len=gfns.convIntBary_2(filename.length());
		byte[] fname=filename.getBytes();

		//		Abstract * length
		//String abtract="ABSTRACT";
		byte[] abtract_len=gfns.convIntBary_2(abtract.length());
		byte[] fabstract=abtract.getBytes();

		//Filesize 
		long filesize=file.length();
		byte[] fsize=new byte[]{(byte)(filesize >>> 24),(byte)(filesize >>> 16),(byte)(filesize >>> 8),(byte)filesize };

		//FileDigest
		byte[] fdigest=filedigest.getSignature();

		//Message Digest
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		FileInputStream unid = new FileInputStream(file);
		byte[] file_cont=new byte[unid.available()];
		unid.read(file_cont);
		byte[] mdigest = md.digest(file_cont);




		int index=0;
		byte[] payload=new byte[user_len.length + username.length +fname_len.length + fname.length + abtract_len.length + fabstract.length +fsize.length + fdigest.length + mdigest.length ];

		System.arraycopy(user_len,0,payload,0,2);
		index+=2;

		System.arraycopy(username,0,payload,index,username.length);
		index+=username.length;

		System.arraycopy(fname_len,0,payload,index,2);
		index+=2;

		System.arraycopy(fname,0,payload,index,fname.length);
		index+=fname.length;

		System.arraycopy(abtract_len,0,payload,index,2);
		index+=2;

		System.arraycopy(fabstract,0,payload,index, fabstract.length);
		index+=fabstract.length;

		System.arraycopy(fsize,0,payload,index,4);
		index+=4;

		System.arraycopy(fdigest,0,payload,index,fdigest.length);
		index+=fdigest.length;

		System.arraycopy(mdigest,0,payload,index,mdigest.length);
		index+=mdigest.length;

		gfns.printbary(payload);
		//gfns.printbary(payload);
		pack tcp_stream = new pack((byte)3,(int)5678,(byte)16,myAddr,cliPort,(int)payload.length,payload);

		System.out.println("Payload size:" + payload.length);
		if(clientSocket.isConnected()){
			os.write(tcp_stream.getPacket());
		}
		String file_sha1 = gfns.ByteArraytohexString(mdigest);

		if (in.read()==1){
			JOptionPane.showMessageDialog(null, "Publish successful","Publish", JOptionPane.INFORMATION_MESSAGE);
			add_file_hash(file, file_sha1);

		}
		else{
			JOptionPane.showMessageDialog(null, "Publish failed","Publish", JOptionPane.INFORMATION_MESSAGE);
		}
		clientSocket.close();
		os.close();

		return(file_sha1);
	}


	private void add_file_hash(File file, String file_sha1) {

		fileHash.put( file_sha1 , file.getAbsolutePath());

		try
		{
			FileWriter fw = new FileWriter(new File("SHA_Path"),true);
			fw.write(file_sha1);//appends the string to the file
			fw.write(file.getAbsolutePath());
			fw.write("\n");
			fw.close();
		}

		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage());
		} 

	}	

	private void remove_file_hash(String file_sha1) throws Exception {

		fileHash.remove(file_sha1);

		try
		{
			FileWriter fw = new FileWriter(new File("SHA_Path"));
			Iterator it = fileHash.keySet().iterator();
			File fname;
			String ts;
			while ( it.hasNext()){
				ts= (String) it.next();
				fw.write(ts);//appends the string to the file
				ts=fileHash.get(ts);
				fw.write(ts);
				fw.write("\n");
			}
			fw.close();
		}

		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage());
		} 
		
		
		
		
		int servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));
		int cliPort  = Integer.parseInt(prop.getProperty("Client_UDP_Port"));

		byte[] ba1= gfns.convIntBary_2(myname.length());
		byte[] ba2= myname.getBytes();


		byte[] ba4 = gfns.hexStringToByteArray(file_sha1);
		byte[] ba3 = gfns.convIntBary_2(ba4.length);

		byte[] payload = new byte[2*2 + ba2.length + ba4.length];

		System.arraycopy(ba1, 0, payload, 0, ba1.length);
		System.arraycopy(ba2, 0, payload, ba1.length, ba2.length);
		System.arraycopy(ba3, 0, payload, ba1.length+ba2.length, ba3.length);
		System.arraycopy(ba4, 0, payload, ba1.length+ba2.length+ba3.length, ba4.length);

		pack udp_pack = new pack( (byte)51, (int)1234, (byte)16, myAddr,cliPort,(int)payload.length,payload);

		DatagramPacket pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,servAddr,servPort);					    
		//		    DatagramSocket sock = new DatagramSocket(cliPort);
		pocket.send(pack);
		//		pocket.close();				
		
		
		

	}	



	public static void main(String args[]) throws Exception
	{	

		tcp_server tcp_server=new tcp_server();
		new Thread(tcp_server).start();
		client_gui t1=new client_gui();
	}

}

class search_result
{


	public genfunc gfns = new genfunc();


	public byte ID[]=new byte[4];
	public byte IP[]=new byte[4];
	public byte filename[];
	public byte abtract[];
	public byte user[];
	public byte MD[]=new byte[20];
	public int filesize;
	public int downloads;
	public int dload_status=1;

	public search_result(byte[] content) {
		super();
		byte[] temp;
		int index=0,size;

		System.arraycopy(content, index, IP, 0, IP.length);
		index+=4;				

		System.arraycopy(content, index, MD, 0, MD.length);
		index+=20;

		temp=new byte[4];
		System.arraycopy(content, index, temp, 0, temp.length);
		index+=4;		
		filesize=gfns.convBaryInt(temp);

		temp=new byte[2];
		System.arraycopy(content, index, temp, 0, temp.length);
		index+=2;		
		downloads=gfns.convBaryInt(temp);


		temp=new byte[2];
		System.arraycopy(content, index, temp, 0, temp.length);
		index+=2;		
		size=gfns.convBaryInt(temp);

		filename=new byte[size];
		System.arraycopy(content, index, filename, 0, size);
		index+=size;

		temp=new byte[2];
		System.arraycopy(content, index, temp, 0, temp.length);
		index+=2;		
		size=gfns.convBaryInt(temp);

		abtract=new byte[size];
		System.arraycopy(content, index, abtract, 0, size);
		index+=size;

		temp=new byte[2];
		System.arraycopy(content, index, temp, 0, temp.length);
		index+=2;		
		size=gfns.convBaryInt(temp);

		user=new byte[size];
		System.arraycopy(content, index, user, 0, size);
		index+=size;



	}

	public String getResultCont() {
		//String tstring="File Name : " + this.getFilename() + " Size : " + filesize + " User : " +  this.getUser() + " Dloads : " + downloads + "\n" + this.getAbtract();
		String tstring=this.getFilename() + " (" + filesize + " bytes, " + this.downloads + " downloads) by : " +  this.getUser() + "\n" + this.getAbtract();
		//System.out.println(tstring);

		return new String(tstring);
	}

	public String getAbtract() {
		return new String(abtract);
	}

	public int getDownloads() {
		return downloads;
	}

	public int getDloadStatus() {
		return dload_status;
	}

	public String getFilename() {
		return new String(filename);
	}

	public int getFilesize() {
		return filesize;
	}

	public genfunc getGfns() {
		return gfns;
	}

	public byte[] getID() {
		return ID;
	}

	public InetAddress getIP() {
		try {
			return InetAddress.getByAddress(IP);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public byte[] getMD() {
		return MD;
	}

	public String getUser() {
		return new String(user);
	}




}









class tcp_server implements Runnable{

	public Properties prop=new Properties();
	//	server c_server;

	public tcp_server() throws IOException{
		prop.load(this.getClass().getResourceAsStream("/Resolver.Properties"));
	}
	public void run(){
		try {
			System.out.println("Opening TCP Port");
			open_tcp_port();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void open_tcp_port() throws IOException {

		int servPort = Integer.parseInt(prop.getProperty("Client_TCP_Port"));

		ServerSocket server=new ServerSocket(servPort);
		clientSocket newclient;
		while(true){

			try{
				newclient= new clientSocket(server.accept());
				Thread t=new Thread(newclient);
				System.out.println("Creating a new thread for this req");
				t.start();

			}
			catch(IOException e){
				System.out.println("Server Accept failed" + e);
			}

		}



	}
}









class clientSocket implements Runnable{

	private Socket socket;
	public Properties prop=new Properties();
	public genfunc gfns = new genfunc();
	public Connection con;
	public Statement stmt;
	public PreparedStatement pstmt;
	public ResultSet rs;

	clientSocket(Socket socket){
		this.socket=socket;
	}

	public void run()  {
		byte[] buffer=new byte[16];
		byte[] payload;
		pack tcp_pack;
		Map<String, String> fileHash = new LinkedHashMap<String, String>();

		try {		
			BufferedReader triRead1 = new BufferedReader(new FileReader("SHA_Path"));
			String line = null; 

			while (( line = triRead1.readLine()) != null){
				fileHash.put(line.substring(0, 40) , line.substring(40));
			}
			triRead1.close();	

			prop.load(this.getClass().getResourceAsStream("/Resolver.Properties"));
			InputStream in= socket.getInputStream();
			OutputStream out= socket.getOutputStream();

			in.read(buffer, 0, buffer.length);

			System.out.println("Printing the buffer");
			gfns.printbary(buffer);

			tcp_pack=new pack(buffer,buffer.length);
			payload=new byte[tcp_pack.getPaylength()];
			in.read(payload,0,payload.length);

			System.out.println("Hit is " + fileHash.get(gfns.ByteArraytohexString(payload)));
			File file=new File(fileHash.get(gfns.ByteArraytohexString(payload)));
			FileInputStream hit_file= new FileInputStream(file);
			byte[] buff=new byte[(int)file.length()];
			hit_file.read(buff);
			out.write(buff);

			out.close();
			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}


class client_keepAlive implements Runnable{
	public client_gui cgi;
	public genfunc gfns = new genfunc();

	client_keepAlive(client_gui cgi){
		this.cgi=cgi;
	}

	public void run(){
		while(true){
			try {
				Thread.currentThread();
				Thread.sleep(140000);
				send_hello_pack();
				String usrname=cgi.myname;
				//cgi.send
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void send_hello_pack() throws Exception {

		int servPort = Integer.parseInt(cgi.prop.getProperty("Server_UDP_Port"));
		int cliPort = Integer.parseInt(cgi.prop.getProperty("Client_UDP_Port"));

		byte[] ba1= gfns.convIntBary_2(cgi.myname.length());
		byte[] ba2= cgi.myname.getBytes();

		byte[] payload = new byte[2+ ba2.length];

		System.arraycopy(ba1, 0, payload, 0, ba1.length);
		System.arraycopy(ba2, 0, payload, ba1.length, ba2.length);

		pack udp_pack = new pack( (byte)3, (int)1234, (byte)16, cgi.myAddr,cliPort,payload.length,payload);

		DatagramPacket pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,cgi.servAddr,servPort);
		cgi.pocket.send(pack);
	}



}
