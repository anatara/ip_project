import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;
import java.lang.*;


import sun.net.www.http.KeepAliveCache;
import trigest.*;
import gui.*;
import packetformat.*;
import database.*;
import general.*;

class clientSocket implements Runnable{

	private Socket socket;
	public Properties prop=new Properties();
	public genfunc gfns = new genfunc();
	public Connection con;
	public Statement stmt;
	public PreparedStatement pstmt;
	public ResultSet rs;
	public server c_server;


	clientSocket(Socket socket, server c_server){
		this.socket=socket;
		this.c_server=c_server;

	}

	public void run()  {
		byte[] buffer=new byte[16];
		byte[] payload;
		byte[] tcp_packet;
		pack tcp_pack;
		try {
			prop.load(this.getClass().getResourceAsStream("/Resolver.Properties"));
			InputStream in= socket.getInputStream();
			OutputStream out= socket.getOutputStream();

			in.read(buffer, 0, buffer.length);

			System.out.println("Printing the buffer");
			gfns.printbary(buffer);

			tcp_pack=new pack(buffer,buffer.length);
			payload=new byte[tcp_pack.getPaylength()];
			in.read(payload,0,payload.length);

			tcp_packet=new byte[buffer.length + payload.length];
			System.arraycopy(buffer, 0, tcp_packet, 0, buffer.length);
			System.arraycopy(payload,0, tcp_packet, buffer.length, payload.length);

			tcp_pack=new pack(tcp_packet,tcp_packet.length);

			byte[] usrname = new byte[gfns.convBaryInt(new byte[]{tcp_pack.data[0],tcp_pack.data[1]})];
			System.arraycopy(tcp_pack.data, 2, usrname, 0, usrname.length);
			//			System.out.println ("adfadsfdfadsf this is the uise name " + new String(usrname));

			System.out.println("New tcp connection");
			if (! valid_active_user(tcp_pack,new String(usrname))){
				System.out.println("User not currently active. Please login");
				//login_error_udp(tcp_pack.IP);
				c_server.send_reply(tcp_pack, (byte)99, (byte) 0);
				System.out.println("sent a UPD packet");
				out.write(0);
			}
			else if(tcp_pack.getPkttype() == 3){
				//upload_file_digest(tcp_pack);
				out.write((byte)upload_file_digest(tcp_pack));
			}

			else if(tcp_pack.getPkttype() == 4){

				text_searched(out,tcp_pack); 
				//				out.write(text_searched(tcp_pack));
				System.out.println("Key search request received");
				//				out.write("Results".getBytes());

			}
			else {
				System.out.println("User not currently active. Please login");
				//login_error_udp(tcp_pack.IP);
				//c_server.send_reply(tcp_pack, (byte)99, (byte) 0);
			}
			out.close();
			in.close();
			//socket.close();	

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	public void text_searched(OutputStream out,pack tcp_pack) throws SQLException, Exception {

		System.out.println("In the text searched");

		BitSet text_search_bits=new BitSet(8192);
		BitSet filedigest_bits=new BitSet(8192);


		int index=0,rec_count=0;
		int lengthF=0;

		//The last 1024 is the signature of the searched string 
		byte[] text_searched=new byte[1024];
		//gfns.printbary(tcp_pack.getData());

		System.arraycopy(tcp_pack.getData(), tcp_pack.getData().length-1024, text_searched, 0, 1024);
		text_search_bits=gfns.fromByteArray(text_searched);

		//gfns.printbary(text_searched);

		con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
		String sql = "SELECT * from  FILETABLE WHERE ACTIVE_STATUS = ? ORDER BY DOWNLOADS DESC";
		pstmt = con.prepareStatement(sql);

		pstmt.setInt(1, 1);	
		rs = pstmt.executeQuery();

		while(rs.next()){


			byte [] temp_holder=new byte[256];
			filedigest_bits=gfns.fromByteArray(gfns.hexStringToByteArray(rs.getString("FILEDIGEST"))); 
			filedigest_bits.and(text_search_bits);
			if(filedigest_bits.equals(text_search_bits)){
				//System.out.println("a hit");


				index=0;
				System.arraycopy(gfns.getIpAsArrayOfByte(rs.getString("IPADDR")),0,temp_holder,index,4);
				index+=4;				


				System.arraycopy(gfns.hexStringToByteArray(rs.getString("MSGDST")),0,temp_holder,index,20);
				index+=20;


				System.arraycopy(gfns.convIntBary(rs.getInt("FILESIZE")), 0, temp_holder, index, 4);
				index+=4;

				System.arraycopy(gfns.convIntBary_2(rs.getInt("DOWNLOADS")), 0, temp_holder, index, 2);
				index+=2;

				lengthF=rs.getString("FILENAME").length();
				System.arraycopy(gfns.convIntBary_2(lengthF),0,temp_holder,index,2);
				index+=2;

				System.arraycopy(rs.getString("FILENAME").getBytes(), 0, temp_holder,index,rs.getString("FILENAME").getBytes().length);
				index+=rs.getString("FILENAME").getBytes().length;

				lengthF=rs.getString("ABSTRACT").length();
				System.arraycopy(gfns.convIntBary_2(lengthF),0,temp_holder,index,2);
				index+=2;

				System.arraycopy(rs.getString("ABSTRACT").getBytes(), 0, temp_holder,index,rs.getString("ABSTRACT").getBytes().length);
				index+=rs.getString("ABSTRACT").getBytes().length;

				lengthF=rs.getString("USER").length();
				System.arraycopy(gfns.convIntBary_2(lengthF), 0, temp_holder, index, 2);
				index+=2;


				System.arraycopy(rs.getString("USER").getBytes(), 0, temp_holder,index,rs.getString("USER").getBytes().length);
				index+=rs.getString("USER").getBytes().length;



				out.write(gfns.convIntBary_2(index));
				out.write(temp_holder,0,index);
				rec_count++;		
			}
		}
		out.write(gfns.convIntBary_2(0));

	}

	private boolean valid_active_user(pack tcp_pack, String usrname) throws SQLException {
		int Result=0;


		con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
		String sql = "SELECT ACTIVE_STATUS from  IPTABLE where IPADDR=? AND HOST=?";
		pstmt = con.prepareStatement(sql);

		pstmt.setString(1, tcp_pack.getIP().getHostAddress());	//Setting the status as active
		pstmt.setString(2, usrname);	
		rs = pstmt.executeQuery();
		while(rs.next()){
			Result = Integer.parseInt(rs.getString("ACTIVE_STATUS"));
			System.out.println("User active Status checked" + Result);
			break;
		}			

		if(Result != 0)
			return true;
		else
			return false;	
	}

	public int upload_file_digest(pack tcp_pack) {

		byte[] lengthF=new byte[2];
		int aIndex=0,result=0;


		System.arraycopy(tcp_pack.getData(),0,lengthF,0,2);
		byte[]  usrname=new byte[gfns.convBaryInt(lengthF)];
		System.arraycopy(tcp_pack.getData(),2,usrname,0,gfns.convBaryInt(lengthF));
		aIndex+=(2+gfns.convBaryInt(lengthF));

		System.arraycopy(tcp_pack.getData(),aIndex,lengthF,0,2);
		byte[] filename=new byte[gfns.convBaryInt(lengthF)];
		System.arraycopy(tcp_pack.getData(),aIndex+2,filename,0,gfns.convBaryInt(lengthF));
		aIndex+=(2+gfns.convBaryInt(lengthF));

		System.arraycopy(tcp_pack.getData(),aIndex,lengthF,0,2);
		byte[] abtract=new byte[gfns.convBaryInt(lengthF)];
		System.arraycopy(tcp_pack.getData(),aIndex+2,abtract,0,gfns.convBaryInt(lengthF));
		aIndex+=(2+gfns.convBaryInt(lengthF));

		byte[] filesize=new byte[4];
		System.arraycopy(tcp_pack.getData(),aIndex,filesize,0,4);
		aIndex+=4;

		byte[] filedigest=new byte[1024];
		System.arraycopy(tcp_pack.getData(),aIndex,filedigest,0,1024);
		aIndex+=1024;

		byte[] filemd=new byte[20];
		System.arraycopy(tcp_pack.getData(),aIndex,filemd,0,20);
		aIndex+=20;


		try{

			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
			String sql = "INSERT INTO FILETABLE (IPADDR, USER, ACTIVE_STATUS, FILENAME, FILESIZE, FILEDIGEST, MSGDST,ABSTRACT,DOWNLOADS) VALUES(?,?,?,?,?,?,?,?,?)";
			pstmt = con.prepareStatement(sql);
			pstmt.clearParameters();


			pstmt.setString(1,tcp_pack.getIP().getHostAddress());
			pstmt.setString(2,new String(usrname));
			pstmt.setInt(3,1);
			pstmt.setString(4,new String(filename));
			pstmt.setInt(5,gfns.convBaryInt(filesize));
			String hexString_fd=gfns.ByteArraytohexString(filedigest);
			pstmt.setString(6,hexString_fd);
			String hexString_md=gfns.ByteArraytohexString(filemd);
			pstmt.setString(7,hexString_md);
			pstmt.setString(8, new String(abtract));
			pstmt.setInt(9, 0);

			result=pstmt.executeUpdate();
			if(result==1){
				System.out.println("Insertion successful");
			}
			else{
				System.out.println("Insertion failed");
			}

		}

		catch (SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}
		return result;

	}

}



public class server  implements Runnable{

	public Properties prop=new Properties();
	public genfunc gfns = new genfunc();
	public DatagramSocket pocket;
	public Connection con;
	public Statement stmt;
	public PreparedStatement pstmt;
	public ResultSet rs;
	public InetAddress myAddr;
	public Map<String, Integer> user_status_hash = new LinkedHashMap<String, Integer>();


	public void run() {


		stmt=null;
		pstmt=null;
		rs=null;


		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch(Exception ex){
			System.out.println("Problem with registering the driver" + ex);
		}

		try{
			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
		}

		catch (SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}

		try {
			prop.load(this.getClass().getResourceAsStream("/Resolver.Properties"));

			myinet4addr getmyaddr = new myinet4addr();
			myAddr = getmyaddr.getMy4Ia();
			System.out.println("Using my IP address " + myAddr );

			open_udp_port();


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		



	}

	//Open a UDP pocket and wait for ever
	public void open_udp_port() throws Exception {

		try{
			int servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));

			pocket= new DatagramSocket(servPort);

			//Creating a packet to hold the incoming UDP packet
			DatagramPacket packet=new DatagramPacket(new byte[256],256);

			while(true){
				try {
					pocket.receive(packet);  
					//System.out.println("packet received");
					process_pocket(packet);
					if (1==0){break;}
				}
				catch (IOException e) {
					System.out.println(e);}
			}
			pocket.close();
		}
		catch(IOException ioe){
			System.out.println("Error:"+ioe);
		}	

	}

	private void process_pocket(DatagramPacket packet) throws Exception {

		pack udp_pack = new pack(packet.getData(),packet.getLength());
		byte result;

		//		gfns.printbary(udp_pack.data);
		//		System.out.println("packet length is " + packet.getLength());
		switch(udp_pack.getPkttype()){

		case 1:

			result = validate_login(udp_pack)[0];
			if ( result != (byte)255) {
				System.out.println("Login is success");
				update_status_host_table(udp_pack,1);
			}
			else {
				System.out.println("Login is failure");
			}
			send_reply(udp_pack,(byte)81,result);

			break;

		case 2:
			update_status_host_table(udp_pack,0);
			break;			

		case 3:
			//System.out.println("received 3");
			update_status_host_table(udp_pack,1);
			break;	

		case 5:

			// Registering New User

			register_user(udp_pack);
			break;

		case 51:
			remove_file_entry(udp_pack);
			break;		

		case 31:
			update_dload_count(udp_pack);
			break;			
		}

	}


	private void remove_file_entry(pack udpPack) {


		byte[] lenghtF=new byte[2];
		int aIndex=0;

System.out.println("Going to remove file entry");
		System.arraycopy(udpPack.data,0,lenghtF,0,2);
		byte[]  usrname=new byte[gfns.convBaryInt(lenghtF)];
		System.arraycopy(udpPack.data,2,usrname,0,gfns.convBaryInt(lenghtF));
		aIndex+=2+gfns.convBaryInt(lenghtF);

		System.arraycopy(udpPack.data,aIndex,lenghtF,0,2);
		byte[]  md=new byte[gfns.convBaryInt(lenghtF)];
		System.arraycopy(udpPack.data,aIndex+2,md,0,gfns.convBaryInt(lenghtF));
		aIndex+=2+gfns.convBaryInt(lenghtF);

		stmt=null;
		pstmt=null;
		rs=null;

		try{
			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
			String sql = " DELETE from FILETABLE where USER = ? AND MSGDST = ?";
			pstmt = con.prepareStatement(sql);

			pstmt.setString(1, new String(usrname) );	//Setting the status as active
			pstmt.setString(2, gfns.ByteArraytohexString(md) );
			pstmt.executeUpdate();

		}

		catch (SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}

	}




	private void update_dload_count(pack udp_pack) {
		stmt=null;
		pstmt=null;
		rs=null;

		byte[] lenghtF=new byte[2];
		int index=0;

		System.arraycopy(udp_pack.data,0,lenghtF,0,2);
		byte[]  usrname=new byte[gfns.convBaryInt(lenghtF)];
		System.arraycopy(udp_pack.data,2,usrname,0,gfns.convBaryInt(lenghtF));
		index=2+gfns.convBaryInt(lenghtF);

		System.arraycopy(udp_pack.data,index,lenghtF,0,2);
		byte[]  md=new byte[gfns.convBaryInt(lenghtF)];
		System.arraycopy(udp_pack.data,2+index,md,0,gfns.convBaryInt(lenghtF));

		try{
			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");

			String sql = "UPDATE FILETABLE SET DOWNLOADS = DOWNLOADS + 1 WHERE USER = ? AND MSGDST = ?";


			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, new String(usrname));//Setting the ip address
			pstmt.setString(2, gfns.ByteArraytohexString(md));//Setting the ip address

			pstmt.executeUpdate();

		}

		catch (SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}



	}


	public void register_user(pack udpPack) throws Exception
	{
		byte[] lenghtF=new byte[2];
		int aIndex=0, result=0;


		System.arraycopy(udpPack.data,0,lenghtF,0,2);
		byte[]  usrname=new byte[gfns.convBaryInt(lenghtF)];
		System.arraycopy(udpPack.data,2,usrname,0,gfns.convBaryInt(lenghtF));
		aIndex+=2+gfns.convBaryInt(lenghtF);

		System.out.println("User: " + lenghtF);
		gfns.printbary(usrname);

		System.arraycopy(udpPack.data,aIndex,lenghtF,0,2);
		byte[]  passwd=new byte[gfns.convBaryInt(lenghtF)];
		System.arraycopy(udpPack.data,aIndex+2,passwd,0,gfns.convBaryInt(lenghtF));
		aIndex+=2+gfns.convBaryInt(lenghtF);

		System.out.println("Pwd: " + passwd);
		gfns.printbary(passwd);

		System.arraycopy(udpPack.data,aIndex,lenghtF,0,2);
		byte[]  email=new byte[gfns.convBaryInt(lenghtF)];
		System.arraycopy(udpPack.data,aIndex+2,email,0,gfns.convBaryInt(lenghtF));
		aIndex+=2+gfns.convBaryInt(lenghtF);		

		stmt=null;
		pstmt=null;
		rs=null;

		try{

			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
			String sql = "INSERT INTO IPTABLE (IPADDR, HOST, PASSWORD, ACTIVE_STATUS, EMAIL) VALUES(?,?,?,?,?)";
			pstmt = con.prepareStatement(sql);
			pstmt.clearParameters();
			pstmt.setString(1,udpPack.getIP().getHostAddress());
			pstmt.setString(2,new String(usrname));
			pstmt.setString(3,gfns.ByteArraytohexString(passwd));
			pstmt.setInt(4,0);
			pstmt.setString(5,new String(email));
			result=pstmt.executeUpdate();
			if(result==1){
				System.out.println("Insertion successful");
			}
			else{
				System.out.println("Insertion failed");
			}

			send_reply(udpPack, (byte)6, (byte) result);

		}

		catch (SQLException ex){
			send_reply(udpPack, (byte)6, (byte) 0);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}		

	}










	public void update_status_host_table(pack udp_pack, int status) {

		stmt=null;
		pstmt=null;
		rs=null;

		InetAddress IPAddr=udp_pack.getIP();

		byte[] lenghtF=new byte[2];

		System.arraycopy(udp_pack.data,0,lenghtF,0,2);
		byte[]  usrname=new byte[gfns.convBaryInt(lenghtF)];
		System.arraycopy(udp_pack.data,2,usrname,0,gfns.convBaryInt(lenghtF));


		user_status_hash.put( new String(usrname), status);



		try{
			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");

			if(status == 1){
				String sql = "UPDATE IPTABLE SET ACTIVE_STATUS = ?, IPADDR = ? WHERE HOST = ?";


				pstmt = con.prepareStatement(sql);
				pstmt.setString(3, new String(usrname));//Setting the USERNAME
				pstmt.setString(2, IPAddr.getHostAddress());
				pstmt.setInt(1,status);	//Setting the status of F as active
				pstmt.executeUpdate();

				String sql1= "UPDATE FILETABLE SET ACTIVE_STATUS = ?, IPADDR = ?  WHERE USER = ?";
				pstmt=con.prepareStatement(sql1);
				pstmt.setString(2, IPAddr.getHostAddress());
				pstmt.setString(3, new String(usrname));//Setting the USERNAME
				pstmt.setInt(1,status);	//Setting the status of F as active
				pstmt.executeUpdate();



			}

			else{
				String sql = "UPDATE IPTABLE SET ACTIVE_STATUS = ? WHERE IPADDR = ? AND HOST = ?";
				String sql1 = "UPDATE FILETABLE SET ACTIVE_STATUS = ? WHERE IPADDR = ? AND USER = ?";

				pstmt = con.prepareStatement(sql);
				pstmt.setString(3,new String(usrname));
				pstmt.setString(2, IPAddr.getHostAddress());//Setting the ip address
				pstmt.setInt(1,status);	//Setting the status of F as active
				pstmt.executeUpdate();

				pstmt=con.prepareStatement(sql1);
				pstmt.setString(3,new String(usrname));
				pstmt.setString(2, IPAddr.getHostAddress());//Setting the ip address
				pstmt.setInt(1,status);	//Setting the status of F as active
				pstmt.executeUpdate();
			}

		}

		catch (SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}
	}


	public void send_reply(pack reqPack, byte code, byte res) throws Exception {
		//InetAddress myAddr;
		int servPort, cliPort;
		pack udp_pack;
		DatagramPacket pack;
		byte[] payload;

		switch (code) {
		case (byte)81 :
			if (res!=(byte)255) {res=(byte)1;}
			else res=(byte)0;

		//myAddr=InetAddress.getLocalHost();

		servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));

		InetAddress cliAddr = reqPack.getIP();
		cliPort = reqPack.getPort_no();
		payload=new byte[]{00, 01, res};
		udp_pack = new pack( (byte)81, (int)1234, (byte)16, myAddr,servPort,payload.length,payload);

		pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,cliAddr,cliPort);
		pocket.send(pack);
		break;

		case (byte)6 :

			//myAddr=InetAddress.getLocalHost();

			servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));

		cliAddr = reqPack.getIP();
		cliPort = Integer.parseInt(prop.getProperty("Client_UDP_Port"));
		payload=new byte[]{00, 01, res};
		udp_pack = new pack( (byte)6, (int)1234, (byte)16, myAddr,servPort,payload.length,payload);

		pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,cliAddr,cliPort);
		pocket.send(pack);
		break;


		case (byte)99 :

			//myAddr=InetAddress.getLocalHost();

			servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));

		cliAddr = reqPack.getIP();
		cliPort = Integer.parseInt(prop.getProperty("Client_UDP_Port"));
		payload=new byte[]{00, 01, res};
		udp_pack = new pack( (byte)99, (int)1234, (byte)16, myAddr,servPort,payload.length,payload);

		pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,cliAddr,cliPort);
		pocket.send(pack);
		break;

		}


	}

	private byte[] validate_login(pack udpPack) {

		boolean blnResult = false;

		byte[] lenghtF=new byte[2];
		int aIndex=0;


		System.arraycopy(udpPack.data,0,lenghtF,0,2);
		byte[]  usrname=new byte[gfns.convBaryInt(lenghtF)];
		System.arraycopy(udpPack.data,2,usrname,0,gfns.convBaryInt(lenghtF));
		aIndex+=2+gfns.convBaryInt(lenghtF);

		System.arraycopy(udpPack.data,aIndex,lenghtF,0,2);
		byte[]  passwd=new byte[gfns.convBaryInt(lenghtF)];
		System.arraycopy(udpPack.data,aIndex+2,passwd,0,gfns.convBaryInt(lenghtF));
		aIndex+=2+gfns.convBaryInt(lenghtF);

		stmt=null;
		pstmt=null;
		rs=null;

		try{
			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
			String sql = "SELECT PASSWORD from  IPTABLE where HOST=?";
			pstmt = con.prepareStatement(sql);

			pstmt.setString(1, new String(usrname) );	//Setting the status as active
			rs = pstmt.executeQuery();
			while(rs.next()){
				blnResult = Arrays.equals(passwd,gfns.hexStringToByteArray(rs.getString(1)));
				break;
			}			

		}

		catch (SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}

		if (blnResult) return  usrname;

		return new byte[]{(byte)255};
	}




	public static void main(String[] args) throws Exception {
		server s1=new server();
		server_keepAlive ska = new server_keepAlive(s1);
		tcp_server t1=new tcp_server(s1);
		new Thread(ska).start();
		new Thread(t1).start();
		new Thread(s1).start();

	}
}

class tcp_server implements Runnable{

	public Properties prop=new Properties();
	server c_server;

	public tcp_server(server s1) throws IOException{
		this.c_server = s1;
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

		int servPort = Integer.parseInt(prop.getProperty("Server_TCP_Port"));

		ServerSocket server=new ServerSocket(servPort);
		clientSocket newclient;
		while(true){

			try{
				newclient= new clientSocket(server.accept(),c_server);
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




class server_keepAlive implements Runnable{


	public Properties prop=new Properties();
	public genfunc gfns = new genfunc();
	public Connection con;
	public Statement stmt;
	public PreparedStatement pstmt;
	public ResultSet rs;
	public server k_server;

	server_keepAlive(server k_server){
		this.k_server=k_server;
	}

	public void run(){
		while(true){
			try {
				create_hash(k_server.user_status_hash);
				//System.out.println("start sleep");
				Thread.currentThread();
				Thread.sleep(300000);
				//System.out.println("end sleep");

				String usrname;
				Iterator it = k_server.user_status_hash.keySet().iterator();
				while(it.hasNext()) {
					usrname=(String) it.next();
					if (k_server.user_status_hash.get(usrname) == 0){
						System.out.println("checking usr name " + usrname + " status is " + k_server.user_status_hash.get(usrname));
						update_status(usrname, 0);
					}
				} 

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void update_status(String usrname, int i) {
		stmt=null;
		pstmt=null;
		rs=null;

		try{
			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");

			String sql = "UPDATE IPTABLE SET ACTIVE_STATUS = ? WHERE HOST = ?";
			String sql1 = "UPDATE FILETABLE SET ACTIVE_STATUS = ? WHERE USER = ?";

			pstmt = con.prepareStatement(sql);
			pstmt.setString(2,usrname);
			pstmt.setInt(1,0);	//Setting the status of F as active
			pstmt.executeUpdate();

			pstmt=con.prepareStatement(sql1);
			pstmt.setString(2,usrname);
			pstmt.setInt(1,0);	//Setting the status of F as active
			pstmt.executeUpdate();
		}

		catch (SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}

	}

	public void create_hash(Map<String, Integer> user_stat_hash) throws Exception {
		user_stat_hash.clear();
		con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
		String sql = "SELECT HOST from  IPTABLE where ACTIVE_STATUS=?";
		pstmt = con.prepareStatement(sql);

		pstmt.setInt(1, 1);	//Setting the status as active
		rs = pstmt.executeQuery();
		while(rs.next()){
			user_stat_hash.put(rs.getString("HOST"), 0);
		}
	}
}


