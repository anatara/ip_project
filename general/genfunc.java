package general;

public class genfunc {

	public byte[] convIntBary_2(int ival) {
		return new byte[] {(byte)(ival >>> 8),(byte)ival};
	}

	public int convBaryInt(byte [] bary) {

		if (bary.length==4){
			return (int) (bary[0] << 24)+ ((bary[1] & 0xFF) << 16) + ((bary[2] & 0xFF) << 8) + (bary[3] & 0xFF);
		}
		else if (bary.length==2){
			return (int) ((bary[0] & 0xFF) << 8) + (bary[1] & 0xFF);
		}

		return 0;
	}

	public void printbary(byte[] bary) {
		for (int i = 0; i < bary.length; i++) {
			int t= bary[i];
			String temp = "0000";
			temp=temp.concat(Integer.toHexString(t));
			temp=temp.toUpperCase();
			System.out.printf("%c%c",temp.charAt(temp.length()-2),temp.charAt(temp.length()-1));
		}
		System.out.println("");
	}

	public byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

}
