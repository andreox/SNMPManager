import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.OID;

import java.util.Vector;
import java.util.concurrent.*;
import java.io.IOException;
import java.lang.Thread;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.snmp4j.smi.OID;
import org.snmp4j.mp.SnmpConstants ;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;

public class GETThread implements Runnable {

		private UdpAddress address ;
		private OID oid ;
		private boolean stop_running ;
		private CommunityTarget target ;
		private Snmp s ;
		private PDU pdu ;
		
		GETThread( UdpAddress address, OID oid ) throws IOException {
			
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
    		pdu.add(new VariableBinding(this.oid));

        	//s.listen() ;
        	//manager.sendRequest( s , manager.createGetPdu(systime), target ) ;
		}
		
		public void run()  {
			
			while ( !stop_running ) {
				
				try {
					
					s.listen();
					ResponseEvent responseEvent = s.send(pdu, target);
					PDU response = responseEvent.getResponse();
					//System.out.println("I'm here - Thread Get") ;
					
					if (response == null) {
						
						System.out.println("TimeOut...");
					}
					
					else {
						if (response.getErrorStatus() == PDU.noError) {
							
							Vector<? extends VariableBinding> vbs = response.getVariableBindings();
							System.out.println("GET RESULT from IP " +address+" : ") ;
							
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
			System.out.println("TRAP Thread stopped me");	
		}
		
		public void stop_run() { this.stop_running = true ; }
}
