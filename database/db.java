package database;
import java.sql.*;
import java.io.*;
import java.util.*;

public class db {
		
	public static void retrieve_users(){
		Connection con=null;
		Statement stmt=null;
		ResultSet rs=null;
		
		try{
				con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
				
				System.out.println("\nConnection successfully established with MySQL");
				stmt=con.createStatement();
							
				
				if(stmt.execute("SELECT * from  HOST_TABLE")){
					rs = stmt.getResultSet();
					System.out.print("Host_Name ");
					System.out.println(" IP Address");
					while(rs.next()){
						System.out.print(rs.getString(1) + " ");
						System.out.println(rs.getString(2));
						
					}
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
		}
	}
	
public  boolean add_user(String hostname,String ip){
	
	Connection con=null;
	Statement stmt=null;
	ResultSet rs=null;
	PreparedStatement pstmt=null;
	boolean flag=false;
	
	try{
			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
			
			System.out.println("\nConnection successfully established with MySQL");
					
			System.out.println("Hostname:" + hostname);
			System.out.println("IP:" + ip);
			String sql = "INSERT INTO HOST_TABLE (HOSTNAME,IPADDR,ACTIVE_STATUS) VALUES(?,?,?)";
		
			pstmt = con.prepareStatement(sql);
						 
			 boolean status = false;
			 //byte[] Hash=null;
			 //byte[] RSA=null;
			 pstmt.clearParameters();
			 
			 pstmt.setString(1,hostname);
			 pstmt.setString(2,ip);
			 pstmt.setBoolean(3,status);
			 //pstmt.setBinaryStream(4,Hash,1024);
			 //pstmt.setBinaryStream(5, RSA, 1024)
			 
			 
			 
			 if((pstmt.executeUpdate()) != 0)
			 {
				 flag=true;
			 }
			 
			 else{
				 
				 flag=false;
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
	return flag;
	
} 

public static boolean validate_user(String hostname,String ip,byte[] rsa){
	
	Connection con=null;
	Statement stmt=null;
	ResultSet rs=null;
	boolean validate_flag=false;
	
	try{
			con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
			
			System.out.println("\nConnection successfully established with MySQL");
			stmt=con.createStatement();
						
			
			if(stmt.execute("SELECT * from  HOST_TABLE")){
				rs = stmt.getResultSet();
				
				while(rs.next()){
					if(hostname.equals(rs.getString(1)) && ip.equals(rs.getString(2))){
					
						validate_flag=Arrays.equals(rs.getBytes(4),rsa);
						//update_status(ip);
						break;
					
					}
											
				}
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
	}
		
	return validate_flag;
	
}

public void update_status(String ip){
	
	Connection con=null;
	Statement stmt=null;
	PreparedStatement pstmt=null;
	ResultSet rs=null;
	
	
	try{
		con=DriverManager.getConnection("jdbc:mysql://localhost/bootstrap?" + "user=root&password=mysqlpwd");
		
		//System.out.println("\nConnection successfully established with MySQL");
				
		
		String sql = "UPDATE HOST_TABLE SET ACTIVE_STATUS = ? WHERE IPADDR = ?";
		pstmt = con.prepareStatement(sql);
		
		pstmt.setString(2, ip);//Setting the ip address			 
		pstmt.setInt(1, 1);	//Setting the status as active
		pstmt.executeUpdate();
		
				
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
	
}

public static void main(String[] args) {
		
		//Registering the MySQLDriver
		
		/*try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch(Exception ex){
			System.out.println("Problem with registering the driver" + ex);
		}
		
				
		boolean add_status=add_user("ARUN-LAPTOP","192.150.10.5");
		
		if(add_status == true){
			System.out.println("Insertion Success");
		}
		
		else{
			System.out.println("Insertion failed");
		}
		
		
			
		retrieve_users();
	
		*/
	}
	
	

}
