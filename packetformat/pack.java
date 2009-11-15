package packetformat;

import java.net.InetAddress;

public class pack{
	
	public byte [] packet;
	
	public byte pkttype;
	public byte ID[]=new byte[6];
	public byte TTL;
	public byte IP[]=new byte[4];
	public byte port_no[]=new byte[2];
	public byte reqrep;
	
	
	public pack(byte pkttype, int iD, byte tTL, InetAddress iP, int portNo, byte[] data) {
		super();
		clean_all();
		
		this.pkttype = pkttype;
		ID = new byte[] {(byte)(iD >>> 24),(byte)(iD >>> 16),(byte)(iD >>> 8),(byte)iD};
		TTL = tTL;
		IP = iP.getAddress();
		port_no = new byte[] {(byte)(portNo >>> 8),(byte)portNo};		

		byte [] temp=new byte[]{this.pkttype,ID[0],ID[1],ID[2],ID[3],TTL,IP[0],IP[1],IP[2],IP[3],port_no[0],port_no[1]};

		packet = new byte[temp.length + data.length];
		System.arraycopy(temp, 0, packet, 0, temp.length);
		System.arraycopy(data, 0, packet, temp.length, data.length);
		
		
	}
	
	
	public byte[] getPacket() {
		return packet;
	}


	private void clean_all() {
		// TODO Auto-generated method stub
		pkttype=0;
		packet=new byte[]{0};
		ID=new byte[]{0};
		IP=new byte[]{0};
		TTL=0;
		port_no=new byte[]{0};
		reqrep=0;
	}


	public String reqsetpkt(byte []p,byte[] id,byte[] T,byte[] ip,byte[] no)
	{
//		this.pkttype = p;
//		this.ID = id;
//		this.TTL = T ;
//		this.IP = ip;
//		this.port_no = no;
//
//		String s_pkt_type=new String(pkttype);
//		String s_pkt_ID=new String(ID);
//		String s_pkt_TTL=new String(TTL);
//		String s_pkt_IP=new String(IP);
//		String s_pkt_portno=new String(port_no);
//
//		return(s_pkt_type + s_pkt_ID + s_pkt_TTL + s_pkt_IP + s_pkt_portno);
//
//		/*StringBuffer s = new StringBuffer();
//     		s.append(pkttype);
//     		s.append(ID);
//     		s.append(TTL);
//     		s.append(IP);
//     		s.append(port_no);
//       		return s.toString();*/
		return "hi";
	}
	public String rplysetpkt(byte []p1,byte[] id1,byte[] T1,byte[] ip1,byte[] no1)
	{
//		this.pkttype1 = p1;
//		this.ID1 = id1;
//		this.TTL1 = T1 ;
//		this.IP = ip1;
//		this.port_no = no1;
//
//		StringBuffer sb = new StringBuffer();
//		sb.append(pkttype1);
//		sb.append(ID1);
//		sb.append(TTL1);
//		sb.append(IP);
//		sb.append(port_no);
//		sb.append(pkttype);
//		sb.append(ID);
//		sb.append(TTL);
//		return sb.toString();
		return "hi";
	}
	public void reqbkpkt(byte[]req) 
	{		
//		String str = new String(req);
//		String packettype = str.substring(0,1);
//		String identification = str.substring(1, 7);
//		String ttl = str.substring(7, 8);
//		String ip = str.substring(8, 12);
//		String portno = str.substring(12, 14);
//		pkttype = packettype.getBytes();
//		ID = identification.getBytes();
//		TTL = ttl.getBytes();
//		IP = ip.getBytes();
//		port_no = portno.getBytes();


	}
	public void rplybkpkt(byte[]rply) 
	{
//		String str1 = rply.toString();
//		String packettype1 = str1.substring(1,2);
//		String identification1 = str1.substring(2, 8);
//		String ttl1 = str1.substring(8, 9);
//		String ip = str1.substring(9, 13);
//		String portno = str1.substring(13, 15);
//		String packettype = str1.substring(15, 16);
//		String identification = str1.substring(16, 22);
//		String ttl = str1.substring( 22,23);
//
//		pkttype1 = packettype1.getBytes();
//		ID1 = identification1.getBytes();
//		TTL1 = ttl1.getBytes();
//		IP = ip.getBytes();
//		port_no = portno.getBytes();
//		pkttype = packettype.getBytes();
//		ID = identification.getBytes();
//		TTL = ttl.getBytes();   	
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
	
	public static void main(String args[]) 
	{  
	}
}