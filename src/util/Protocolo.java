package util;

/**
 * Protocolo de comunicação para troca de mensagens multicast entre peers
 * @author Casa
 *
 */
public class Protocolo {
	
	public static final int QUERY_SUPERNO = 1; // Busca um supernó
	public static final int QUERY_ARQUIVO = 3; // Busca um arquivo
	public static final int ARQUIVO_ENCONTRADO = 4; // Indica que um arquivo foi encontrado
	public static final int DELETA_ARQUIVO = 5; // Busca arquivos a serem deletados
	
}
