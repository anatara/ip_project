import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

import trigest.*;
import gui.*;
import packetformat.*;
import database.*;
import general.*;

public class server {

	public Properties prop=new Properties();
	public genfunc gfns = new genfunc();
	public DatagramSocket pocket;


	public server() throws Exception {
		prop.load(this.getClass().getResourceAsStream("/Resolver.Properties"));
		open_udp_port();
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
					System.out.println("packet received");
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
				//update_status_host_table()
			}
			else {
				System.out.println("Login is failure");
			}
			send_reply(udp_pack,(byte)81,result);

			break;
		}

	}

	private void send_reply(pack reqPack, byte code, byte res) throws Exception {
		switch (code) {
		case (byte)81 :
			if (res!=(byte)255) {res=(byte)1;}
			else res=(byte)0;

		InetAddress myAddr=InetAddress.getLocalHost();

		int servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));

		InetAddress cliAddr = reqPack.getIP();
		int cliPort = reqPack.getPort_no();
		pack udp_pack = new pack( (byte)81, (int)1234, (byte)16, myAddr,servPort,new byte[]{00, 01, res});

		DatagramPacket pack = new DatagramPacket(udp_pack.getPacket(),udp_pack.getPacket().length,cliAddr,cliPort);
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

		//gfns.printbary(usrname);
		//gfns.printbary(passwd);

		Connection con=null;
		Statement stmt=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch(Exception ex){
			System.out.println("Problem with registering the driver" + ex);
		}

		try{
			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");

			//System.out.println("\nConnection successfully established with MySQL");


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
		finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) { } // ignore

				rs = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { } // ignore

				stmt = null;
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException sqlEx) { } // ignore

				pstmt = null;
			}


		}
		if (blnResult) return  usrname;

		return new byte[]{(byte)255};
	}

	public static void main(String[] args) throws Exception {
		server s1=new server();
	}	
}
