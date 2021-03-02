import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.* ;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.OID;

public class ManagerServerExecutor {

	private HashMap <String,String> OID_Text_Mapping ; //Stringa descrittiva associata ad OID
	private HashMap <String,String> OID_OIC_Mapping ;
	
	public static void main( String[] args ) throws IOException {
		
		ExecutorService executor = Executors.newFixedThreadPool(2) ;
		UdpAddress udp_get = new UdpAddress("10.169.1.204/161") ;
		OID oid_get = new OID("1.3.6.1.2.1.1.3.0") ; //sysUpTime
		GETThread get = new GETThread( udp_get,  oid_get) ;
		
		UdpAddress udp_trap = new UdpAddress("0.0.0.0/162") ;
		TRAPThread trap = new TRAPThread( udp_trap, get) ;
		
		executor.execute(get);
		executor.execute(trap);
		
	}
	
	public ManagerServerExecutor() {
		
		OID_OIC_Mapping = new HashMap() ;
		OID_Text_Mapping = new HashMap() ;
		
		String line = "" ;
		String splitBy = "," ;
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader("db.csv")) ;
			
			while ( ( line = br.readLine()) != null ) {
				
				String [] infos = line.split(splitBy) ;
				try {
				OID_OIC_Mapping.put(infos[0],infos[3]) ;
				OID_Text_Mapping.put(infos[0],infos[1]) ;	
				} catch( ArrayIndexOutOfBoundsException e ) {} 
			}

			System.out.println(OID_OIC_Mapping);
			
		} catch( IOException e ) {
			
			e.printStackTrace();
			
		}

	}
}
