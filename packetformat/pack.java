package packetformat;

import general.genfunc;

import java.net.InetAddress;

public class pack{

	public genfunc gfns = new genfunc();
	
	public byte [] packet;
	
	public byte pkttype;
	public byte ID[]=new byte[4];
	public byte TTL;
	public byte IP[]=new byte[4];
	public byte port_no[]=new byte[2];
	public byte reqrep;
	public byte [] data;
	public byte [] paylength=new byte[4];
	public pack(){
		
	}
	
	public pack(byte[] packet, int length){
		//gfns.printbary(packet);
		//System.out.println("packet lenth is " + length);		
		
		this.data=new byte[length-16];
		
		this.pkttype=packet[0];
		System.arraycopy(packet,  1, this.ID, 0, 4);
		this.TTL=packet[5];
		System.arraycopy(packet,  6, this.IP, 0, 4);
		System.arraycopy(packet, 10, this.port_no,0,2);
		System.arraycopy(packet, 12, this.paylength, 0, 4);
		System.arraycopy(packet, 16, this.data,0,data.length);
	}
	
	public pack(byte pkttype, int iD, byte tTL, InetAddress iP, int portNo, int paylen, byte[] data) {
		super();
		clean_all();
		
		this.pkttype = pkttype;
		ID = new byte[] {(byte)(iD >>> 24),(byte)(iD >>> 16),(byte)(iD >>> 8),(byte)iD};
		TTL = tTL;
		IP = iP.getAddress();
		port_no = new byte[] {(byte)(portNo >>> 8),(byte)portNo};
		paylength=new byte[] {(byte)(paylen >>> 24),(byte)(paylen >>> 16),(byte)(paylen >>> 8),(byte)paylen};

		byte [] temp=new byte[]{this.pkttype,ID[0],ID[1],ID[2],ID[3],TTL,IP[0],IP[1],IP[2],IP[3],port_no[0],port_no[1],paylength[0],paylength[1],paylength[2],paylength[3]};

		packet = new byte[temp.length + data.length];
		System.arraycopy(temp, 0, packet, 0, temp.length);
		System.arraycopy(data, 0, packet, temp.length, data.length);
		
		
	}
	
	
	public byte[] getPacket() {
		return packet;
	}
	
	public byte[] getheader(){
		byte[] temp = new byte [this.packet.length-this.data.length];
		System.arraycopy(this.packet, 0, temp, 0, temp.length);
		return temp;
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
		data=new byte[]{0};
		paylength=new byte[]{0};
	}


	
	public static void main(String args[]) 
	{  
	}


	public void putPacket(byte[] packet){
		
	}
	
	public int getID() {
		return gfns.convBaryInt(this.ID) ;
	}


	public void setID(byte[] id) {
		ID = id;
	}


	public InetAddress getIP() {
		InetAddress Addr=null;
		try{
			Addr=InetAddress.getByAddress(this.IP);
			
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
		
		return Addr;
	}


	public void setIP(byte[] ip) {
		IP = ip;
	}


	public int getPkttype() {
			
		return ((int)this.pkttype);
	}


	public void setPkttype(byte pkttype) {
		this.pkttype = pkttype;
	}


	public int getPort_no() {
		return(gfns.convBaryInt(port_no));
	}


	public void setPort_no(byte[] port_no) {
		this.port_no = port_no;
	}


	public int getTTL() {
		return ((int)this.TTL);
	}


	public void setTTL(byte ttl) {
		TTL = ttl;
	}


	public byte getReqrep() {
		return reqrep;
	}


	public void setReqrep(byte reqrep) {
		this.reqrep = reqrep;
	}
	

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
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

	public int getPaylength() {
		return (gfns.convBaryInt(this.paylength));
	}
	
	
	
}