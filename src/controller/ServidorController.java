package controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.UUID;

import model.Arquivo;
import util.ICompartilha;
import util.Protocolo;

/**
 * Classe para controlar o lado servidor de um nó da rede
 * @author Manuella Vieira
 *
 */
public class ServidorController extends UnicastRemoteObject implements ICompartilha{

	public HashMap<String, ArrayList<String>> mapaArquivos = new HashMap<String, ArrayList<String>>(); // Mapeia um nome de arquivo a endereços de peers 
	public ArrayList<String> meusArquivos = new ArrayList<String>(); // Armazena os arquivos disponíveis pra download do próprio nó 
	public String meuIP;
	static Registry registry;
	String enderecoRegistry, registrySuperno;
	static int porta = 1099, numFolhas = 0;
	public String ID;
	public InetAddress grupoNo, grupoSuper;
	public MulticastSocket socketFolhas, socketSupernos; // Criar um grupo de multicast pra nós folha e outro pra supernós. 230.0.0.3 e 4, respectivamente
	Scanner in = new Scanner(System.in);
	DatagramSocket socketUDP;
	public String SO; // Sistema Operacional do usuário
	public static final int MAX_FOLHAS = 3; // Define o máximo de folhas que um supernó pode ter -1 (o próprio supernó)
	
	
	public ServidorController() throws RemoteException{
		super();
		// Pega o nome do sistema operacional onde o programa está rodando para definir os caminhos dos arquivos
		SO = System.getProperty("os.name").toLowerCase();
		
		this.meuIP = this.pegaIPLocal();
		ID = UUID.randomUUID().toString();

		System.out.println("Insira a porta na qual o servidor irá rodar: ");
		porta = in.nextInt();
		
		
		if(conectaSuperno()){
			String end[] = enderecoRegistry.split(":");
			
			if(end[0].equals(meuIP) && end[1].equals(porta)){

				registry = LocateRegistry.getRegistry(end[0], Integer.parseInt(end[1]));
				registry.rebind("rmi://"+meuIP+"/"+porta, this);
				
			}
			else{
				enderecoRegistry = meuIP+":"+porta;
				registry = LocateRegistry.createRegistry(porta);
				registry.rebind("rmi://"+meuIP+"/"+porta, this);
			}
			try {
				socketFolhas = new MulticastSocket(42252);
				grupoNo = InetAddress.getByName("230.0.0.3");
				socketFolhas.joinGroup(grupoNo);
				socketFolhas.setLoopbackMode(false);

				OuveUDP threadUDP = new OuveUDP(socketFolhas, this);
				new Thread(threadUDP).start();
		
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			// Se não existem supernós disponíveis, vira supernó e cria o servidor de registros
			try {
				socketSupernos = new MulticastSocket(44252);
				socketSupernos.joinGroup(grupoSuper);
				socketSupernos.setLoopbackMode(false);

				OuveUDP threadUDP = new OuveUDP(socketSupernos, this);
				new Thread(threadUDP).start();
		
			} catch (IOException e) {
				e.printStackTrace();
			}
			enderecoRegistry = meuIP + ":" + porta;
			registrySuperno = enderecoRegistry;
			registry = java.rmi.registry.LocateRegistry.createRegistry(porta);
			registry.rebind("rmi://"+meuIP+"/"+porta, this);
		}
	}
	
	
	public static void main(String[] args){
		try {
			new ServidorController();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Envia mensagem ao grupo de supernós pra saber se existem supernós ativos. Aguarda uma mensagem por 3 segundos. 
	 * Caso exista, retorna true, caso não, retorna false. 
	 * @return
	 */
	public boolean conectaSuperno(){
		
		boolean recebeu = false;
		byte[] buf = new byte[1024];
		DatagramPacket pacoteRecebido = null;
		
		
		try {
			socketUDP = new DatagramSocket();
			
			pacoteRecebido = new DatagramPacket(buf, buf.length);
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
		

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(outputStream);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		try {
			os.writeInt(Protocolo.QUERY_SUPERNO);
			os.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		byte[] data =  outputStream.toByteArray();
		
		try {
			grupoSuper = InetAddress.getByName("230.0.0.4");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		DatagramPacket out = new DatagramPacket(data,data.length,grupoSuper, 44252);
		try {
			socketUDP.send(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			os.reset();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {

			socketUDP.setSoTimeout(3000);
			
			 while(true){     
		            try {
		                socketUDP.receive(pacoteRecebido);
		                recebeu = true;

		                byte[] dados = pacoteRecebido.getData();
		    		    
		    		  	ByteArrayInputStream in = new ByteArrayInputStream(dados);
		    		  	ObjectInputStream is = null;
		    		  	
		    			try {
		    			 	 is = new ObjectInputStream(in);
		    			} catch (IOException e1) {
		    				e1.printStackTrace();
		    			}
		    			
		    			enderecoRegistry = (String) is.readObject();
		    			registrySuperno = enderecoRegistry;
		    			System.out.println(enderecoRegistry);
		    			is.close();
		                break;
		            }
		            catch (SocketTimeoutException e) {
		            	System.out.println("Nenhuma resposta recebida!");
		            	break;
		            } catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
		        }
			
			 return recebeu;
			 
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return recebeu;
	}
	
	
	/** Busca arquivo pelo nome na lista de arquivos em nós folhas conhecidos pelo supernó. Se não achar, manda msg pra outro
	 * supernó perguntando se os nós folha dele possuem o arquivo. 
	 * Quando achar, retorna a lista de peers que possuem o arquivo. 
	 * */
	@Override
	public ArrayList<String> buscaArquivo(String nomeDoArquivo) {
		
		ArrayList<String> enderecos = null;
		ArrayList<String> end = new ArrayList<String>();
		
		if(this.mapaArquivos.containsKey(nomeDoArquivo)){
			enderecos = this.mapaArquivos.get(nomeDoArquivo);
			
			end.addAll(enderecos);
			
		}else{
			
			
			byte[] buf = new byte[1024];
			DatagramPacket pacoteRecebido = null;
			
				
			pacoteRecebido = new DatagramPacket(buf, buf.length);

			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = null;
			try {
				os = new ObjectOutputStream(outputStream);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			try {
				os.writeInt(Protocolo.QUERY_ARQUIVO);
				os.writeObject(nomeDoArquivo);
				os.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			byte[] data =  outputStream.toByteArray();
			
			DatagramPacket out = new DatagramPacket(data,data.length,grupoSuper, 44252);
			try {
				socketUDP.send(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				os.reset();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

				try {
					socketUDP.setSoTimeout(3000);
				} catch (SocketException e2) {
					e2.printStackTrace();
				}
				
				 while(true){     
			            try {
			                socketUDP.receive(pacoteRecebido);

			                byte[] dados = pacoteRecebido.getData();
			    		    
			    		  	ByteArrayInputStream in = new ByteArrayInputStream(dados);
			    		  	ObjectInputStream is = null;
			    		  	
			    			try {
			    			 	 is = new ObjectInputStream(in);
			    			} catch (IOException e1) {
			    				e1.printStackTrace();
			    			}
			    			if(is.readInt() == Protocolo.ARQUIVO_ENCONTRADO){
			    				end = (ArrayList<String>) is.readObject();
			    			}
			                break;
			            }
			            catch (SocketTimeoutException e) {
			            	System.out.println("Arquivo Não Encontrado!");
			            	break;
			            } catch (IOException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
			        }
		}
		return end;
	}

	/**
	 * Procura o arquivo pelo nome na lista de nós folha que ele conhece. Se achar, chama um método de deletar o arquivo remotamente
	 */
	@Override
	public boolean removeArquivo(String nomeDoArquivo) {
	
		ArrayList<String> folhas = null;
		ArrayList<String> nos = new ArrayList<String>();
		
		
		if(this.mapaArquivos.containsKey(nomeDoArquivo)){
			folhas = mapaArquivos.get(nomeDoArquivo);
			nos.addAll(folhas);
			
			if(!nos.isEmpty() && !nos.equals(null)){

				for(String endereco : nos){
					
					System.out.println("Enderecos onde deletar: " + endereco);
					
					String endGeral[] = endereco.split(";");
					String endReg[] = endGeral[0].split(":");
					String endNo[] = endGeral[1].split(":");
					
					Registry registro = null; 
					
					try {
						registro = LocateRegistry.getRegistry(endReg[0],Integer.parseInt(endReg[1]));
					} catch (NumberFormatException | RemoteException e1) {
						e1.printStackTrace();
					}
					
					try {
						ICompartilha noFolha = (ICompartilha) registro.lookup("rmi://"+endNo[0]+"/"+endNo[1]);
						noFolha.removeArquivoNo(nomeDoArquivo);
					} catch (RemoteException | NotBoundException e) {
						e.printStackTrace();
					}
				}
				mapaArquivos.remove(nomeDoArquivo);
				return true;
			}
		}
		else{
			
			ArrayList<String> enderecos = null;
			
			byte[] buf = new byte[1024];
			DatagramPacket pacoteRecebido = null;
				
			pacoteRecebido = new DatagramPacket(buf, buf.length);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = null;
			try {
				os = new ObjectOutputStream(outputStream);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			try {
				os.writeInt(Protocolo.DELETA_ARQUIVO);
				os.writeObject(nomeDoArquivo);
				os.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			byte[] data =  outputStream.toByteArray();
			
			DatagramPacket out = new DatagramPacket(data,data.length,grupoSuper, 44252);
			try {
				socketUDP.send(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				os.reset();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

				try {
					socketUDP.setSoTimeout(3000);
				} catch (SocketException e2) {
					e2.printStackTrace();
				}
				
				 while(true){     
			            try {
				                socketUDP.receive(pacoteRecebido);
	
				                byte[] dados = pacoteRecebido.getData();
				    		    
				    		  	ByteArrayInputStream in = new ByteArrayInputStream(dados);
				    		  	ObjectInputStream is = null;
				    		  	
				    			try {
				    			 	 is = new ObjectInputStream(in);
				    			} catch (IOException e1) {
				    				e1.printStackTrace();
				    			}
				    			if(is.readInt() == Protocolo.ARQUIVO_ENCONTRADO){
				    				enderecos = (ArrayList<String>) is.readObject();
				    				 
						   				 Registry registro = null;
						   				 
						   				 if(!enderecos.equals(null) && !enderecos.isEmpty())
						   				 for(String end : enderecos){
			
					   							String endNo[] = end.split(";");
					   							String[] endFolha = endNo[1].split(":");
					   							String[] endReg = endNo[0].split(":");
					   							
					   							try {
								   					 registro = LocateRegistry.getRegistry(endReg[0], Integer.parseInt(endReg[1]));
								   				} catch (NumberFormatException | RemoteException e) {
								   					e.printStackTrace();
								   				}
					   							
					   							try {
					   								ICompartilha noFolha = (ICompartilha) registro.lookup("rmi://"+endFolha[0]+"/"+endFolha[1]);
					   								noFolha.removeArquivoNo(nomeDoArquivo);
					   							} catch (RemoteException | NotBoundException e) {
					   								e.printStackTrace();
					   							}
					   				 	}
					    			}
			    					return true;
			            		}
			            catch (SocketTimeoutException e) {
			            	System.out.println("Arquivo Não Encontrado!");
			            	break;
			            } catch (IOException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
				 }
			
			}
			return false;	
		}

	/**
	 * Método do peer pra obter um array de bytes de um arquivo. Retorna um objeto Arquivo que possui o array de bytes correspondente ao 
	 * conteúdo do arquivo e o nome do arquivo.
	 */
	@Override
	public Arquivo armazenaArquivo(String nomeDoArquivo) {

		String separador = null;
		
		if(isUnix()){
			separador = System.getProperty("file.separator");
		}else if(isWindows()){
			separador = System.getProperty("file.separator")+System.getProperty("file.separator");
		}
		
		File file = new File(System.getProperty("user.dir")+separador+"ArquivosUpload"+separador+nomeDoArquivo);
		
		byte buffer[] = new byte[(int)file.length()];
		
		BufferedInputStream input = null;
		try {
			input = new
			
			BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			input.read(buffer,0,buffer.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Arquivo arquivo = new Arquivo(nomeDoArquivo, buffer);
		
		return arquivo;
		
	}
	

	/**
	 * Quando um cliente (nó folha) disponibiliza um novo arquivo pra upload, adiciona a combinação endereço do servidor de 
	 * registros+endereço IP+porta arquivo no hashmap
	 */
	@Override
	public void adicionarArquivo(String nomeDoArquivo, String endereco) {
		
		ArrayList<String> enderecos = new ArrayList<String>();
		enderecos.add(endereco);
		if(!mapaArquivos.containsKey(nomeDoArquivo)){
			this.mapaArquivos.put(nomeDoArquivo, enderecos);
		}
		else{
			mapaArquivos.get(nomeDoArquivo).add(endereco);
		}
	}
	
	
	/**
	 * Pega o IP da máquina na qual o programa está rodando
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
	
	

	/**
	 * Deleta um arquivo específico da pasta de arquivos disponíveis pra upload 
	 */
	@Override
	public boolean removeArquivoNo(String nomeDoArquivo) throws RemoteException {
		
		String separador = null;
		
		if(isUnix()){
			separador = System.getProperty("file.separator");
		}else if(isWindows()){
			separador = System.getProperty("file.separator")+System.getProperty("file.separator");
		}
		
		File file = new File(System.getProperty("user.dir")+separador+"ArquivosUpload"+separador+nomeDoArquivo);
		if(file.delete()){
			System.out.println("O arquivo foi deletado");
			this.meusArquivos.remove(nomeDoArquivo);
		}else{
			System.out.println("ooops");
		}
		return false;
	}
	
	/**
	 * Checa se o sistema operacional sendo utilizado é unix
	 * @return
	 */
	public boolean isUnix(){
			return (SO.indexOf("nix") >= 0 || SO.indexOf("nux") >= 0 || SO.indexOf("aix") > 0 );
	}
	
	/**
	 * Checa se o sistema operacional sendo utilizado é windows
	 * @return
	 */
	public boolean isWindows(){
		return (SO.indexOf("win") >= 0);
	}


	/**
	 * Incrementa a quantidade de nós folha conectados ao supernó 
	 */
	@Override
	public void adicionarNo() {
		ServidorController.numFolhas++;
	}
	
}
