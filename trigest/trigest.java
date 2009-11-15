package trigest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class trigest {
	byte[] signature=new byte[1024];

	public byte[] getSignature() {
		return signature;
	}

	public trigest(File FileIn) throws Exception {
		//System.out.println("This is a File - " + FileIn.getName());
		BufferedReader input =  new BufferedReader(new FileReader(FileIn));
		CreateSignature(input);
	}

	public trigest(String StringIn) throws Exception{
		//System.out.println("This is a String - " + StringIn);
		BufferedReader input =  new BufferedReader(new StringReader(StringIn));
		CreateSignature(input);
	}

	private void CreateSignature(BufferedReader bReader) throws Exception {
		Map tripletHash = new LinkedHashMap();
		String line = null; 
		byte[] dArraytemp=new byte[1024]; 

		//Open the Input file and read all the bit positions and the triplets
		URL ur = this.getClass().getResource("/triplet_frequency_2");

		InputStreamReader a = new InputStreamReader(this.getClass().getResourceAsStream("/triplet_frequency_2"));
		BufferedReader triRead1 = new BufferedReader(a);
		//BufferedReader triRead =  new BufferedReader(new FileReader(new File(ur.toURI())));
		while (( line = triRead1.readLine()) != null){
			//System.out.println(line);
			//Construct the hash
			tripletHash.put(line.substring(0, 3) , new Integer( 0 ) );
		}
		//System.out.println("Finished Creating Hash");


		while (( line = bReader.readLine()) != null){
			//System.out.println(line + "\n");
			for(int i=0; i<line.length()-2;i++){
				//System.out.println(line.substring(i, i + 3));
				if (line.substring(i,i+3).indexOf(" ") == -1){
					if (tripletHash.containsKey(line.substring(i, i + 3)))
						tripletHash.put( line.substring(i, i + 3) , new Integer( 1 ) );
					else
						tripletHash.put( "..." , new Integer( 1 ) );
				}
			}
		}
		bReader.close();

		//System.out.println( "Now Hash entries:" );
		int shiftby=8;
		byte bytevalue=0;
		int dArrayIndex=0;
		for ( Iterator it = tripletHash.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry e = (Map.Entry) it.next();
			shiftby--;
			byte[] value=e.getValue().toString().getBytes();
			value[0]-=48;
			bytevalue^=(value[0]<<shiftby);
			if (shiftby==0){
				dArraytemp[dArrayIndex]=bytevalue;
				dArrayIndex++;
				shiftby=8;
				bytevalue=0;
			}
			//System.out.println( e.getKey() + " is " + e.getValue() + " Byte value is " + bytevalue  + " shift by "+ shiftby + " value 0 is " + (value[0]<<shiftby));

		}
		//printbary(dArraytemp);
		System.arraycopy( dArraytemp , 0, signature, 0, 1024 );
		//printbary(dArray);

	}

	public static void main(String[] args) throws Exception {
		trigest d1=new trigest("aaa");
		printbary(d1.getSignature());

		trigest d2=new trigest(new File("1.txt"));
		printbary(d2.getSignature());


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

}
