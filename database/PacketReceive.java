package database;
import java.net.*;
import java.io.*;
import java.util.Properties;

import packetformat.pack;

public class PacketReceive {

	public static Properties prop=new Properties();
	
	public static int convbarylen_int(byte[] bary ){
		return( ((bary[0] & 0xFF) << 8) + (bary[1] & 0xFF));
	}

	public static pack receive_request() {
		
		pack udp_pack=null;
		
		try{
			//int servPort = Integer.parseInt(prop.getProperty("Server_UDP_Port"));
			//int cliPort  = Integer.parseInt(prop.getProperty("Client_UDP_Port"));
			
			int servPort=22886;
			//Creating a socket
			DatagramSocket socket= new DatagramSocket(servPort);
			
			//Creating a packet to hold the incoming UDP packet
			DatagramPacket packet=new DatagramPacket(new byte[256],256);
			socket.receive(packet);
									
			System.out.println("packet received");
			
			udp_pack=new pack();
			ByteArrayInputStream bin=new ByteArrayInputStream(packet.getData());
						
			byte[] pkt_rxd=new byte[packet.getLength()];
			bin.read(pkt_rxd);
			printbary(pkt_rxd);
			System.out.println("packet lenth is " + packet.getLength());
			udp_pack.putPacket(pkt_rxd);
			
		 
								
			socket.close();
		}
		catch(IOException ioe){
			System.out.println("Error:"+ioe);
		}
		
		return udp_pack;
			
	}
	
	public static void process_packet(pack udp_pack){
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch(Exception ex){
			System.out.println("Problem with registering the driver" + ex);
		}
		
		
		db obj=new db();
		
		switch(udp_pack.getPkttype()){
		
		case 1:
			String username,password;
			byte[] payload=new byte[udp_pack.getData().length];
			byte[] usrname;
			byte[] usrname_len=new byte[2];
			byte[] passwd;
			byte[] passwd_len=new byte[2];
			
			System.arraycopy(payload,0,usrname_len,0,2);
			usrname=new byte[convbarylen_int(usrname_len)];
			
			System.arraycopy(payload,2,usrname,0,convbarylen_int(usrname_len));
			
			System.arraycopy(payload,convbarylen_int(usrname_len)+2, passwd_len,0,2);
			passwd=new byte[convbarylen_int(passwd_len)];
			System.arraycopy(payload,convbarylen_int(usrname_len)+4,passwd,0,convbarylen_int(passwd_len));
			
			username=new String(usrname);
			password=new String(passwd);
			System.out.println("Username : " + username);			
			//obj.add_user(username,password,udp_pack.getIP());
			if(obj.add_user(username,udp_pack.getIP().toString()) == true){
				System.out.println("User succeesfully registered");
			}
			
			else{ 
				System.out.println("Registration failure");
			}
		}
		
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
	
	
	
	public static void main(String[] args) {
		
		System.out.println("Binding to the local port");
		
		process_packet(receive_request());
	}
}
		
