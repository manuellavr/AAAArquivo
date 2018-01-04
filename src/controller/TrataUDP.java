package controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import util.Protocolo;

/**
 * Classe que implementa os métodos de gerenciamento de mensagens UDP
 * @author Manuella Vieira
 *
 */
public class TrataUDP implements Runnable{
	
	DatagramPacket pacote;
	public static ServidorController servidor;
	int codigo = 0;
	ByteArrayInputStream in;
	ObjectInputStream is;
	DatagramSocket socketUDP;
	
	
	public TrataUDP(DatagramPacket pacote, ServidorController servidor){
		this.pacote = pacote;
		TrataUDP.servidor = servidor;
	}
	
	
	@Override
	public void run() {
		
		  try {
			socketUDP = new DatagramSocket();
		} catch (SocketException e2) {
			e2.printStackTrace();
		}
		  byte[] dados = pacote.getData();
		    
		  	in = new ByteArrayInputStream(dados);
		    
			try {
			 	is = new ObjectInputStream(in);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			
		    try {
		    	codigo = is.readInt();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		    System.out.println(codigo);
		    
		    switch(codigo){
		    	case Protocolo.QUERY_SUPERNO:
		    		this.respondeSuperno();
		    		break;
		    	case Protocolo.QUERY_ARQUIVO:
		    		this.enviaArquivos();
		    		break;
		    	case Protocolo.DELETA_ARQUIVO:
		    		this.deletaArquivos();
		    		break;
		    }
		    
		  try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Recebe o nome do arquivo a ser deletado e confere se este está na lista de nós conhecidos pelo supernó que recebeu a requisição. 
	 * Caso esteja, retorna a lista dos peers que possuem aquele arquivo para que ele possa ser deletado.
	 */
	private void deletaArquivos() {
		InetAddress endNo = pacote.getAddress();
		int portaNo = pacote.getPort();
		ArrayList<String> IPs = null;
		String nomeArquivo = null;
		
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = null;
			try {
				os = new ObjectOutputStream(outputStream);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			try {
				nomeArquivo = (String) is.readObject();
				System.out.println("Buscando "+nomeArquivo+" para deletar");
			} catch (ClassNotFoundException | IOException e2) {
				e2.printStackTrace();
			}
			
			if(servidor.mapaArquivos.containsKey(nomeArquivo)){
				
				IPs = servidor.mapaArquivos.get(nomeArquivo);
				
				ArrayList<String> enderecos = new ArrayList<String>();
				
				enderecos.addAll(IPs);
				
				try {
					os.writeInt(Protocolo.ARQUIVO_ENCONTRADO);
					os.writeObject(enderecos);
					os.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

				byte[] data =  outputStream.toByteArray();
				
				DatagramPacket out = new DatagramPacket(data,data.length,endNo, portaNo);
				
				try {
					socketUDP.send(out);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				servidor.mapaArquivos.remove(nomeArquivo);
				
				try {
					os.reset();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}


	private void enviaArquivos() {

		InetAddress endNo = pacote.getAddress();
		int portaNo = pacote.getPort();
		ArrayList<String> IPs = null;
		String nomeArquivo = null;
		
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = null;
			try {
				os = new ObjectOutputStream(outputStream);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			try {
				nomeArquivo = (String) is.readObject();
				System.out.println("Buscando" + nomeArquivo);
			} catch (ClassNotFoundException | IOException e2) {
				e2.printStackTrace();
			}
			
			if(servidor.mapaArquivos.containsKey(nomeArquivo)){
				
				IPs = servidor.mapaArquivos.get(nomeArquivo);
				
				ArrayList<String> enderecos = new ArrayList<String>();
				
				enderecos.addAll(IPs);
				
				try {
					os.writeInt(Protocolo.ARQUIVO_ENCONTRADO);
					os.writeObject(enderecos);
					os.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

				byte[] data =  outputStream.toByteArray();
				
				DatagramPacket out = new DatagramPacket(data,data.length,endNo, portaNo);
				
				try {
					socketUDP.send(out);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					os.reset();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
}
		

	public void respondeSuperno(){
		InetAddress endNo = pacote.getAddress();
		int portaNo = pacote.getPort();
				
		if(ServidorController.numFolhas < ServidorController.MAX_FOLHAS){
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = null;
			try {
				os = new ObjectOutputStream(outputStream);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			try {
				os.writeObject(servidor.meuIP+":"+servidor.porta);
				os.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			byte[] data =  outputStream.toByteArray();
			
			DatagramPacket out = new DatagramPacket(data,data.length,endNo, portaNo);
			
			try {
				socketUDP.send(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				os.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
		