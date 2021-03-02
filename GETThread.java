import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.OID;

import java.util.Vector;
import java.util.concurrent.*;
import java.io.IOException;
import java.lang.Thread;
import java.util.ArrayList ;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.smi.OctetString;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.mp.SnmpConstants ;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.event.ResponseEvent;

public class GETThread implements Runnable {

		private UdpAddress address ;
		private ArrayList<OID> oid ;
		private boolean stop_running ;
		private CommunityTarget target ;
		private Snmp s ;
		private PDU pdu ;
		
		GETThread( UdpAddress address, ArrayList<OID> oid ) throws IOException {
			
			this.address = address ;
			this.oid = oid ;
			stop_running = false ;
			
       		target = new CommunityTarget();
        	target.setCommunity(new OctetString("public"));
        	target.setVersion(SnmpConstants.version2c);
        	target.setAddress(this.address);
        	target.setTimeout(3000);        //3s
        	target.setRetries(1);
        	s = new Snmp(new DefaultUdpTransportMapping() ) ;
        	
    		pdu = new PDU();
    		pdu.setType(PDU.GET);
    		for ( int i = 0 ; i < oid.size() ; i++ ) 
    			pdu.add(new VariableBinding(this.oid.get(i)));

        	//s.listen() ;
        	//manager.sendRequest( s , manager.createGetPdu(systime), target ) ;
		}
		
		public void run()  {
			
			while ( !stop_running ) {
				
				try {
					
					s.listen();
					ResponseEvent responseEvent = s.send(pdu, target);
					PDU response = responseEvent.getResponse();
					System.out.println("I'm here - Thread Get ADDRESS "+address) ;
					
					if (response == null) {
						
						System.out.println("TimeOut...");
					}
					
					else {
						if (response.getErrorStatus() == PDU.noError) {
							
							Vector<? extends VariableBinding> vbs = response.getVariableBindings();
							System.out.println("GET RESULT "+address+"  : ") ;
							
							for (VariableBinding vb : vbs) {
								
								System.out.println(vb + " ," + vb.getVariable().getSyntaxString());
							}
						}
						
						else {
							
							System.out.println("Error(ThreadGet) :" + response.getErrorStatusText());
							
						}
					}
					
					Thread.sleep(10000);

				}
				
				catch( Exception e ) {
					
					e.printStackTrace();
				}
				
			}
			System.out.println("GET Thread Stopped running") ;
		}
		
		public void stop_run() { this.stop_running = true ; }
}
