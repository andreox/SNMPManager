import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List; 
import java.util.ArrayList ;
import java.util.concurrent.* ;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.OID;

public class ManagerServerExecutor {

	private HashMap <String,String> OID_Text_Mapping ; //Stringa descrittiva associata ad OID
	private HashMap <String,String> OID_OIC_Mapping ;
	private HashMap <UdpAddress, ArrayList<OID>> IP_OID_Get_Mapping ;
	
	
	public static void main( String[] args ) throws IOException {
		
		ManagerServerExecutor mse = new ManagerServerExecutor() ;
		HashMap<UdpAddress, ArrayList<OID> > map = mse.getIP_OID_Get_Mapping();
		ArrayList<GETThread> list_of_get_threads = new ArrayList<GETThread>() ;
		int num_of_hosts_get = 0 ;
		
		for ( HashMap.Entry<UdpAddress, ArrayList<OID>> entry : map.entrySet() ) {
			
			GETThread get = new GETThread( entry.getKey(), entry.getValue()) ;
			list_of_get_threads.add(get) ;
			num_of_hosts_get++ ;
			
		}
		
		UdpAddress udp_trap = new UdpAddress("0.0.0.0/162") ;
		TRAPThread trap = new TRAPThread( udp_trap, list_of_get_threads.get(0)) ;
		
		ExecutorService executor = Executors.newFixedThreadPool(num_of_hosts_get+1) ; //numero di host per le GET e thread per ricevere TRAPS
		
		for ( int i = 0 ; i < num_of_hosts_get ; i++ ) {
			
			executor.execute(list_of_get_threads.get(i));
			
		}
		
		executor.execute(trap);
		
	}
	
	public ManagerServerExecutor() {
		
		OID_OIC_Mapping = new HashMap() ;
		OID_Text_Mapping = new HashMap() ;
		IP_OID_Get_Mapping = new HashMap() ;
		
		String line = "" ;
		String splitBy = "," ;
		String [] infos ;
		ArrayList<OID> OID_list = new ArrayList<OID>() ;
		
		try {
			
			//BufferedReader br = new BufferedReader(new FileReader("db.csv")) ;
			BufferedReader br2 = new BufferedReader(new FileReader("get.csv")) ;
			
			/*while ( ( line = br.readLine()) != null ) {
				
				infos = line.split(splitBy) ;
				try {
				OID_OIC_Mapping.put(infos[0],infos[3]) ;
				OID_Text_Mapping.put(infos[0],infos[1]) ;	
				} catch( ArrayIndexOutOfBoundsException e ) {} 
			}*/
			
			while ( ( line = br2.readLine()) != null ) {
				
				infos = line.split(splitBy) ;
				OID_list.clear();
				
				if ( infos.length > 1 ) { 
					for ( int i = 1 ; i < infos.length ; i++ ) {
					
						OID temp_oid = new OID(infos[i]) ;
						OID_list.add(temp_oid) ;
					}
					
				}
				UdpAddress temp = new UdpAddress(infos[0]) ;
				IP_OID_Get_Mapping.put(temp, OID_list) ;
			}
			
		} catch( IOException e ) {
			
			e.printStackTrace();
			
		}

	}
	
	public HashMap<UdpAddress, ArrayList<OID>> getIP_OID_Get_Mapping() {
		return IP_OID_Get_Mapping;
	}
	
	
}

