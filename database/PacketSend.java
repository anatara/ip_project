package database;
import java.net.*;
import java.io.*;

import packetformat.*;

public class PacketSend {
	
	public static int DEFAULT_PORT=2000;
	public static int TTL_VALUE=255;
	

	public static final byte[] convIntBary_pkttype_TTL(int ival) {
        return new byte[] {(byte)ival};
}
	
	public static final int convBaryInt_pkttype_TTL(byte [] bary) {
        return  (bary[0]) ;
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
		
	try{
		DatagramSocket socket= new DatagramSocket();
		InetAddress localAddr=InetAddress.getLocalHost();
		String addr=localAddr.getHostAddress();
		
		byte[] pkt_type=convIntBary_pkttype_TTL(1);
		byte[] pkt_ID=convLongBary_pkt_ID(10000);
		byte[] pkt_TTL=convIntBary_pkttype_TTL(TTL_VALUE);
		byte[] pkt_IP=convStringBary_IP(addr);
		byte[] pkt_portno=convIntBary_portno(DEFAULT_PORT);
		
		String hostname="mani-laptop";
		
		System.out.println("Address:" + addr);
		
	

		InetAddress remoteAddr=InetAddress.getLocalHost();
		
		//String port_str=new String(port);
		//System.out.println("Port" + port_str);
		
			
		pack udp_pack = new pack();
		
			
		String header=udp_pack.reqsetpkt(pkt_type,pkt_ID,pkt_TTL,pkt_IP,pkt_portno );
		
		System.out.println("Header length:" + header.length());
		String data="HHHH-LAPTOP";
		String udp_packet= header + data;
		
		byte[] udp_pkt=udp_packet.getBytes();
		System.out.println("Total packet length :" + udp_pkt.length);
		
		ByteArrayOutputStream bout= new ByteArrayOutputStream();
		bout.write(udp_pkt);
		
					
		DatagramPacket packet = new DatagramPacket(udp_pkt,udp_pkt.length);
		packet.setAddress(remoteAddr);
		packet.setPort(DEFAULT_PORT);
		
		socket.send(packet);
		System.out.println("packet sent");
	}
	
	catch(UnknownHostException uhe){
			System.out.println("Cant find host");
	}
	
	catch(IOException ioe){
			System.out.println("Error-" + ioe);
	}
		

	}

}
