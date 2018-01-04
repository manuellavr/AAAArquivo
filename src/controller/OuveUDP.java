package controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

/**
 * Thread responsável por escutar mensagens UDP
 * @author Manuella Vieira
 *
 */
public class OuveUDP implements Runnable {
	
	public MulticastSocket socketUDP = null;
	public static DatagramPacket packet = null;
	public static ServidorController servidor = null;
	
	
	public OuveUDP(MulticastSocket socket, ServidorController servidor){
		this.socketUDP = socket;
		OuveUDP.servidor = servidor;
	}


	@Override
	public void run() {
			
		while(true){
			
		    byte[] buf = new byte[1204];
		    packet = new DatagramPacket(buf, buf.length);
		    try {
				socketUDP.receive(packet);
				System.out.println("Ta recebendo pelo menos?");
				TrataUDP gerenciador = new TrataUDP(packet, servidor);
				new Thread(gerenciador).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
	
		}	
		
	}
}
	
	
	
	
