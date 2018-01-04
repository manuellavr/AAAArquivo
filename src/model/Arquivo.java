package model;

import java.io.Serializable;

public class Arquivo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5413208203796385940L;
	
	private String nomeDoArquivo;
	private byte[] conteudo;
	
	public Arquivo(String nome, byte[] dados){
		this.nomeDoArquivo = nome;
		this.conteudo = dados;
	}
	
	
	
	public String getNomeDoArquivo() {
		return nomeDoArquivo;
	}
	public void setNomeDoArquivo(String nomeDoArquivo) {
		this.nomeDoArquivo = nomeDoArquivo;
	}
	public byte[] getConteudo() {
		return conteudo;
	}
	public void setConteudo(byte[] conteudo) {
		this.conteudo = conteudo;
	}
}
