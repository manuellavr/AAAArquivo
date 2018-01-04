package util;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import model.Arquivo;

/**
 * Interface RMI que define os métodos remotos a serem implementados pelos peers
 * @author Manuella Vieira
 *
 */
public interface ICompartilha extends Remote{
	
	public ArrayList<String> buscaArquivo(String nomeDoArquivo) throws RemoteException;
	
	public boolean removeArquivo(String nomeDoArquivo) throws RemoteException;
	
	public Arquivo armazenaArquivo(String nomeDoArquivo)throws RemoteException;
	
	public void adicionarArquivo(String nomeDoArquivo, String endereco) throws RemoteException;
	
	public void adicionarNo() throws RemoteException;
	
	public boolean removeArquivoNo(String nomeDoArquivo) throws RemoteException;
	
}
 