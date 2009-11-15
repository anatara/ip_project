package database;
import java.net.*;
import java.io.*;

import packetformat.pack;

public class PacketReceive {

	
	public static final int convBaryInt(byte [] bary) {
        return (bary[0] << 24)+ ((bary[1] & 0xFF) << 16) + ((bary[2] & 0xFF) << 8) + (bary[3] & 0xFF);
}

	public static final byte[] convIntBary(int ival) {
        return new byte[] { (byte)(ival >>> 24),(byte)(ival >>> 16),(byte)(ival >>> 8),(byte)ival};
}
	
	public static final byte[] convIntBary_pkttype_TTL(int ival) {
        return new byte[] {(byte)ival};
}
	
	public static final int convBaryInt_pkttype_TTL(byte [] bary) {
        return (bary[0]) ;
    }
	
	public static final byte[] convLongBary_pkt_ID(long ival) {
        return new byte[] {(byte)(ival >>> 48),(byte)(ival >>> 32),(byte)(ival >>> 24),(byte)(ival >>> 16),(byte)(ival >>> 8),(byte)ival};
}
	
	public static final long convBaryLong_pkt_ID(byte[] bary) {
        return  ((bary[0]<<48) + ((bary[1] & 0xFF) << 32) + ((bary[2] & 0xFF) << 24) + ((bary[3] & 0xFF) << 16) + ((bary[4] & 0xFF) << 8) + (bary[5] & 0xFF));
}
	public static final byte[] convIntBary_portno(int ival){
		return new byte[]{(byte)(ival>>>8),(byte)(ival)};
	}
	
	public static final int convBaryInt_portno(byte[] bary){
		return(((bary[0] & 0xFF) << 8) + (bary[1] & 0xFF));
	}
	
	public static final byte[] convStringBary_IP(String IPAddr){
		
	Integer firstbyte =Integer.parseInt(IPAddr.substring(0,IPAddr.indexOf('.')));
	IPAddr=IPAddr.substring((IPAddr.indexOf('.')) + 1);
	Integer secondbyte= Integer.parseInt(IPAddr.substring(0,IPAddr.indexOf('.')));
	IPAddr=IPAddr.substring((IPAddr.indexOf('.')) + 1);
	Integer thirdbyte= Integer.parseInt(IPAddr.substring(0,IPAddr.indexOf('.')));
	IPAddr=IPAddr.substring((IPAddr.indexOf('.')) + 1);
	Integer fourthbyte=Integer.parseInt(IPAddr);
	
	
	return new byte[]{firstbyte.byteValue(),secondbyte.byteValue(),thirdbyte.byteValue(),fourthbyte.byteValue()};
		
}
	
	public static final String convBaryString_IP(byte[] bary){
		
		
		
		Integer firstbyte=new Integer((int)bary[0] & 0x000000FF);
		
		Integer secondbyte=new Integer((int)bary[1] & 0x000000FF);
		Integer thirdbyte=new Integer((int)bary[2] & 0x000000FF);
		Integer fourthbyte=new Integer((int)bary[3] & 0x000000FF);
	
		return(firstbyte.toString() + "." + secondbyte.toString() + "." + thirdbyte.toString() + "." + fourthbyte.toString());
						
	}
	public static void main(String[] args) {
		
		System.out.println("Binding to the local port");
		
		try{
			
			//Creating a socket
			DatagramSocket socket= new DatagramSocket(2000);
			System.out.println("Bound to the local port:" + socket.getLocalPort());
			
			//Creating a packet to hold the incoming UDP packet
			DatagramPacket packet=new DatagramPacket(new byte[256],256);
			socket.receive(packet);
			
			System.out.println("packet received");
			
			
			ByteArrayInputStream bin=new ByteArrayInputStream(packet.getData());
			
			byte[] pkt_rxd=new byte[256];
			bin.read(pkt_rxd);
			
			String pkt=new String(pkt_rxd);
			String header=pkt.substring(0,14);
			String data=pkt.substring(14);
			
			
			pack udp_header=new pack();
			udp_header.reqbkpkt(header.getBytes());
			
			
			//Retrieving the individual fields from the packet header
			
			int pkt_type = convBaryInt_pkttype_TTL(udp_header.pkttype);
			long ID=convBaryLong_pkt_ID(udp_header.ID);
			int TTL=convBaryInt_pkttype_TTL(udp_header.TTL);
			String ip_addr=convBaryString_IP(udp_header.IP);
			int port_no=convBaryInt_portno(udp_header.port_no);
			
			
			System.out.println("Data:" + data);
			String hostname=data.substring(0,16);
			System.out.println("packet type:" + pkt_type);
			
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			}
			catch(Exception ex){
				System.out.println("Problem with registering the driver" + ex);
			}
			
			
			
			db obj=new db();
			
			boolean add_status=obj.add_user(hostname,ip_addr);
			
			if(add_status == true){
				System.out.println("Insertion Success");
			}
			
			else{
				System.out.println("Insertion failed");
			}
			
			obj.update_status(ip_addr);
			socket.close();
		}
		catch(IOException ioe){
			System.out.println("Error:"+ioe);
		}
			
			
		

	}

}
