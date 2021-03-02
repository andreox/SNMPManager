import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.HashMap ;
import java.io.BufferedReader;  
import java.io.FileReader;  
import java.util.concurrent.*;

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

public class TRAPThread implements Runnable, CommandResponder {
	
	private UdpAddress address ;
	private boolean stop_running ;
	private AbstractTransportMapping transport ;
	private ThreadPool threadPool ;
	private MessageDispatcher mDispatcher ;
	private CommunityTarget target ;
	private GETThread gt ;
	private Snmp s ;
	
	TRAPThread( UdpAddress address, GETThread gt ) throws IOException {
		
		this.gt = gt ;
		this.address = address ;
		stop_running = false ;
		transport = new DefaultUdpTransportMapping( this.address ) ;
	    threadPool = ThreadPool.create("DispatcherPool", 10); //(Definisco 10 thread) The ThreadPool provides a pool of a fixed number of threads that are capable to execute tasks that implement the Runnable interface concurrently. The ThreadPool blocks when all threads are busy with tasks and an additional task is added.
	    mDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl()); //prevede anche un servizio di invio messaggi SNMP
	    
	    mDispatcher.addMessageProcessingModel(new MPv1()) ;
	    mDispatcher.addMessageProcessingModel(new MPv2c()) ;
	    
		// add all security protocols
		SecurityProtocols.getInstance().addDefaultProtocols();
		SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

		// Create Target
		target = new CommunityTarget(); //da approfondire
		target.setCommunity(new OctetString("public"));

		s = new Snmp( mDispatcher, transport) ;
		s.addCommandResponder(this);

	}
	
	public void run() {

		try {
			transport.listen() ;
		}	
		catch( IOException e ) {
			e.printStackTrace() ;
		}	
		while ( !stop_running ) {
			
			
		}
	}
	
	public synchronized void processPdu( CommandResponderEvent cmdRespEvent) { //qui ricevo PDU delle Trap, logica da gestire
		
		System.out.println("==== TRAP BEGIN ====") ;
		PDU pdu = cmdRespEvent.getPDU();
		String ipAddressSender = "" ;
		if (pdu != null ) {

				ipAddressSender = cmdRespEvent.getPeerAddress().toString();
				//System.out.println("IP Trap Sender : " + ipAddressSender) ;
				 //System.out.println("errorStatus " + String.valueOf(pdu.getErrorStatus()));
			  // System.out.println("errorIndex "+ String.valueOf(pdu.getErrorIndex()));
		       //System.out.println("requestID " +String.valueOf(pdu.getRequestID()));
		       //System.out.println("snmpVersion " + String.valueOf(PDU.TRAP));
		       //System.out.println("communityString " + new String(cmdRespEvent.getSecurityName()));
		}
		
		 Vector<? extends VariableBinding> varBinds = pdu.getVariableBindings();
		    if (varBinds != null && !varBinds.isEmpty()) {
		      Iterator<? extends VariableBinding> varIter = varBinds.iterator();
		      String oid_str ;
		      
		      while (varIter.hasNext()) {
		        VariableBinding vb = varIter.next();
		        oid_str = vb.getVariable().toString() ; //vb.getOid().toString(); utile per esempi, ma serve OID effettivo
		        oid_str = oid_str.replaceAll("\\\\", "");
				
		        String syntaxstr = vb.getVariable().getSyntaxString();
		        int syntax = vb.getVariable().getSyntax();
		        System.out.println( "OID: " + oid_str); //mi serve per generare gli allarmi
		        System.out.println("Value: " +vb.getVariable());	        
		        System.out.println("syntaxstring: " + syntaxstr );
		        System.out.println("syntax: " + syntax);
		        //System.out.println("------");
		       	System.out.println("Stop Get Thread") ;
		       	gt.stop_run() ;	
		        /*if ( OID_OIC_Mapping.containsKey(oid_str) ) { //se è un segnale rilevante, istanzia messaggio diagnostica ed invialo
		        
				
		        	diagnostic.add(new DiagnosticMessage(ipAddressSender, oid_str, OID_OIC_Mapping.get(oid_str) )) ;
		        	//send alarm to DWH, maybe write diagnostic info in a csv or txt file for hystorical save
		        	System.out.println("\n\nInteresting TRAP received") ;
		        	System.out.println("IP Address : "+diagnostic.lastElement().getIP_Address_Agent()) ;
		        	System.out.println("OID : "+diagnostic.lastElement().getOID()) ;
		        	System.out.println("OIC associated : "+diagnostic.lastElement().getOIC()) ;
		        	System.out.println("Problem : "+OID_Text_Mapping.get(oid_str));
		        }*/
			
		      }
		      
		    }
		    
		System.out.println("==== TRAP END ===");
		System.out.println("");


	} 

	
	public void stop_run() { this.stop_running = true ;}
}

