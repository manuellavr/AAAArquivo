package view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import controller.ClienteController;

public class JanelaBusca extends JFrame {

	private JPanel contentPane;
	private JTextField textBusca;
	private JTable table;
	static ClienteController controller;
	private JTextField textEndereco;
	String endRegistro;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JanelaBusca frame = new JanelaBusca(controller);
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
	public JanelaBusca(final ClienteController controller) {
		
		JanelaBusca.controller = controller;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 490, 336);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(248, 248, 255));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setTitle("AAAAAaaaAAArquivo! - Busca");
		
		textBusca = new JTextField();
		textBusca.setBounds(21, 21, 333, 20);
		contentPane.add(textBusca);
		textBusca.setColumns(10);
		setLocationRelativeTo(null);
		
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(21, 57, 432, 197);
		contentPane.add(scrollPane);
		
		table = new JTable();
		table.setBorder(new LineBorder(new Color(0, 0, 0)));
		table.setBackground(new Color(245, 245, 245));
		table.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Nome do Arquivo", "Peers", 
			}
		));
		
		String[] colunas = {"Nome do Arquivo", "Peers"};
		
		 final DefaultTableModel model = new DefaultTableModel(){
		
			private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int row, int column){
			return false;
		}
	};
	
	model.setColumnIdentifiers(colunas);
	
	model.setRowCount(0);
		
		table.setBounds(21, 64, 432, 181);
		
		JButton btnBuscar = new JButton("Buscar");
		btnBuscar.setForeground(Color.WHITE);
		btnBuscar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				model.setRowCount(0);
				String nomeDoArquivo = textBusca.getText();
				
				ArrayList<String> IPs = controller.buscarArquivo(nomeDoArquivo);
				
				if(IPs != null && !IPs.isEmpty()){
					for(String IP : IPs){
						System.out.println("Endereco: " + IP);
					}
					
					System.out.println("Tamanho da lista de IPs: "+IPs.size());
					
					model.setRowCount(0);
						for(String endereco : IPs){
							
							String end[] = endereco.split(";");
							
									endRegistro = end[0];
									String[] p = new String[2];
									p[0] = nomeDoArquivo;
									p[1] = end[1];
									model.addRow(p);
								}

				}else
				{
					JOptionPane.showMessageDialog(null, "Nenhum arquivo com esse nome foi encontrado!");
				}

				table = new JTable(model);

				table.setShowGrid(false);
				table.getColumnModel().getColumn(0).setPreferredWidth(101);
				scrollPane.setViewportView(table);
			}
				
		});
		btnBuscar.setBackground(new Color(173, 216, 230));
		btnBuscar.setBounds(364, 20, 89, 23);
		contentPane.add(btnBuscar);
		
		textEndereco = new JTextField();
		textEndereco.setBounds(144, 265, 183, 20);
		contentPane.add(textEndereco);
		textEndereco.setColumns(10);
		
		JLabel lblEndereoDoPeer = new JLabel("Endere\u00E7o do Peer ");
		lblEndereoDoPeer.setBounds(21, 268, 116, 14);
		contentPane.add(lblEndereoDoPeer);
		
		JButton btnDownload = new JButton("Download");
		btnDownload.setForeground(Color.WHITE);
		btnDownload.setBackground(SystemColor.activeCaption);
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					controller.baixarArquivo(endRegistro, textEndereco.getText(), textBusca.getText());
				}finally{
					JOptionPane.showMessageDialog(null, "O download foi realizado com sucesso!");
				}
				
			}
		});
		btnDownload.setBounds(347, 264, 106, 23);
		contentPane.add(btnDownload);
		
		
	}
}
