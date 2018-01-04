package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import model.Arquivo;
import util.ICompartilha;


public class ClienteController {
	
	static ICompartilha superno, peer;
	static DatagramSocket socket;
	static String meuIP;
	static ServidorController servidor;
	
	public ClienteController(){
		this.meuIP = this.pegaIPLocal();
		this.criaPastaVazia();
			try {
				servidor = new ServidorController();
			} catch (RemoteException e) {
				e.printStackTrace();
			}finally{
			this.conectaSuperno();
		}
	}
	
	
	/**
	 * Recebe um objeto que implementa a interface RMI e irá funcionar como supernó
	 */
	public static void conectaSuperno(){
		
		
		String endereco[] = servidor.registrySuperno.split(":"); 
			
		Registry registry = null;
		try {
			registry = LocateRegistry.getRegistry(endereco[0], Integer.parseInt(endereco[1]));
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		try {
			superno = (ICompartilha) registry.lookup("rmi://"+endereco[0]+"/"+endereco[1]);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		try {
				superno.adicionarNo();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Chama o método remoto do peer para receber um objeto Arquivo (array de bytes + nome do arquivo). 
	 * Salva uma cópia do arquivo escolhido na pasta de Arquivos Baixados. 
	 * @param nomeDoArquivo
	 */
	public void baixarArquivo(String endRegistro, String endPeer, String nomeDoArquivo){

		try {
			
			System.out.println(endRegistro);
			
			String end[] = endRegistro.split(":");
			String endP[] = endPeer.split(":");
			
			Registry registro = LocateRegistry.getRegistry(end[0], Integer.parseInt(end[1]));
			
			peer = (ICompartilha) registro.lookup("rmi://"+endP[0]+"/"+endP[1]);
			
			String separador = System.getProperty("file.separator")+System.getProperty("file.separator");
			if(servidor.isUnix()){
				separador = System.getProperty("file.separator");
			}
			
			Arquivo temp = peer.armazenaArquivo(nomeDoArquivo);
			File novoArquivo = new File(System.getProperty("user.dir")+separador+"ArquivosBaixados"+separador+temp.getNomeDoArquivo());
			FileOutputStream outstream = new FileOutputStream(novoArquivo);
			
			byte buffer[] = temp.getConteudo();
			
			outstream.write(buffer);
			outstream.flush();
			outstream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Salva o arquivo na pasta de arquivos disponíveis pra upload, salva o nome do arquivo na lista de arquivos disponíveis e 
	 * chama um método do supernó remotamente para que este adicione os dados do arquivo + endereço do peer que o possui em seu hashmap.
	 * @param arquivo
	 */
	public void uploadArquivo(File arquivo){
		/*
		 * Copia um arquivo pra pasta Arquivos do programa e o disponibiliza pra download. Chama o método remoto do supernó pra adicionar o 
		 * arquivo ao hashmap dele
		 */
		
		copiaArquivo(arquivo);
		
		servidor.meusArquivos.add(arquivo.getName());
		System.out.println(arquivo.getName());
		try {
			superno.adicionarArquivo(arquivo.getName(), servidor.enderecoRegistry+";"+meuIP+":"+servidor.porta);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Copia arquivo para pasta de arquivos disponíveis pra upload em uma mesma máquina
	 * @param arquivo
	 */
	public static void copiaArquivo(File arquivo){

		
		String separador = System.getProperty("file.separator")+System.getProperty("file.separator");
		if(servidor.isUnix()){
			separador = System.getProperty("file.separator");
		}
		
		Path origem = arquivo.toPath();
		Path destino = Paths.get(System.getProperty("user.dir")+separador+"ArquivosUpload"+separador+arquivo.getName());

		try {
			Files.copy(origem, destino);
		} catch (IOException e) {
			System.out.println("Este arquivo já está disponível!");
		}
	}
	
	
	/**
	 * Cria pastas para armazenar os arquivos disponíveis pra download e arquivos baixados
	 */
	public static void criaPastaVazia(){

			File pasta = new File("ArquivosUpload");
			File pasta2 = new File("ArquivosBaixados");
			
		    if (!pasta.exists()){   
		     boolean result = pasta.mkdir();    
		     if(result){  
		    	 System.out.println("A pasta foi criada!");
		     }
		    }
		    
		    if(!pasta2.exists()){
		    	pasta2.mkdir();
		    }
	}
	
	/**
	 * Chama o método remotamente para buscar um arquivo no supernó 
	 * @param nomeDoArquivo
	 * @return
	 */
	public ArrayList<String> buscarArquivo(String nomeDoArquivo){
		
		ArrayList<String> IPs = null;
		
		try {
			IPs = superno.buscaArquivo(nomeDoArquivo);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		System.out.println("Tamanho da lista no cliente: "+IPs.size());
		
		return IPs;
		
	}

	
	/**
	 * Pega o IP da máquina em que o programa está rodando
	 * @return
	 */
	public String pegaIPLocal(){
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean removeArquivo(String nomeDoArquivo){
		try {
			return superno.removeArquivo(nomeDoArquivo);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

}
