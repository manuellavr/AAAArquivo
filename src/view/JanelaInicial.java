package view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controller.ClienteController;
import controller.ServidorController;

public class JanelaInicial extends JFrame {

	private JPanel contentPane;
	ClienteController controller;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JanelaInicial frame = new JanelaInicial();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public JanelaInicial() {
		
		controller = new ClienteController();
		setTitle("AAAAAaaaAAArquivo!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(248, 248, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setLocationRelativeTo(null);
		
		JLabel lblArquivo = new JLabel("AAAaaAaAAArquivo!\r\n");
		lblArquivo.setForeground(new Color(70, 130, 180));
		lblArquivo.setFont(new Font("Tahoma", Font.PLAIN, 23));
		lblArquivo.setBounds(110, 11, 302, 58);
		contentPane.add(lblArquivo);
		
		JButton btnAdicionar = new JButton("Adicionar arquivo");
		btnAdicionar.setForeground(new Color(255, 255, 240));
		btnAdicionar.setBackground(new Color(70, 130, 180));
		btnAdicionar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser seletorDeArquivo = new JFileChooser();
				int retorno = seletorDeArquivo.showOpenDialog(null);
				
				if (retorno == JFileChooser.APPROVE_OPTION) { 
					File arquivo = seletorDeArquivo.getSelectedFile(); // Recebe o arquivo selecionado pelo usuÃ¡rio
					try {
						controller.uploadArquivo(arquivo);
						// aqui vai chamar o método do cliente, que vai chamar o método do servidor, pra adicionar o arquivo na lista
					}finally{

						JOptionPane avisoConclusao = new JOptionPane();
						avisoConclusao.showMessageDialog(null, "Seu arquivo está disponível para download!");
						}
					}
				}
			});
		btnAdicionar.setBounds(114, 80, 208, 23);
		contentPane.add(btnAdicionar);
		
		JButton btnBaixar = new JButton("Baixar Arquivo");
		btnBaixar.setForeground(new Color(255, 255, 240));
		btnBaixar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new JanelaBusca(controller).setVisible(true);;
			}
		});
		btnBaixar.setBackground(new Color(70, 130, 180));
		btnBaixar.setBounds(114, 132, 208, 23);
		contentPane.add(btnBaixar);
		
		JButton btnRemover = new JButton("Remover Arquivo\r\n");
		btnRemover.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String input = JOptionPane.showInputDialog("Insira o nome do arquivo que deseja remover");
				if(controller.removeArquivo(input)){
					JOptionPane.showMessageDialog(null, "Arquivo deletado");
				}
				
			}
		});
		btnRemover.setForeground(new Color(255, 255, 240));
		btnRemover.setBackground(new Color(70, 130, 180));
		btnRemover.setBounds(114, 183, 208, 23);
		contentPane.add(btnRemover);
	}
}
