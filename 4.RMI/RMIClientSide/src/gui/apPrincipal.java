package gui;


import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import rmi.Client;
import javax.swing.border.TitledBorder;
import javax.swing.JSeparator;

public class apPrincipal {

	private JFrame frame;
	private JButton btnConfirmar;
	private JPanel panelEndereco;
	private JPanel panelServico;
	private JFormattedTextField txtIP;
	private JFormattedTextField txtPorta;
	private JFormattedTextField txtServico;
	private JLabel label;
	private JLabel lblIP;
	private JLabel lblNomeDoServico;
	private Client oClient;
	private String sEndServidor;
	private String sPorta;
	private String sServico;
	private String sRetorno;
	private JTextField txtResposta;
	private JLabel lblMensagemDoServidor;
	private JLabel lblPorta;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					apPrincipal window = new apPrincipal();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public apPrincipal() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		oClient = new Client();
		
		frame = new JFrame();
		frame.setTitle("Configuração do RMI");
		frame.setBounds(100, 100, 457, 310);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		panelEndereco = new JPanel();
		panelEndereco.setBorder(new TitledBorder(null, "Endere\u00E7o do Servidor", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelEndereco.setBounds(27, 12, 384, 74);
		frame.getContentPane().add(panelEndereco);
		panelEndereco.setLayout(null);

		txtIP = new JFormattedTextField();
		txtIP.setToolTipText("Digite o IP do servidor");
		txtIP.setBounds(12, 43, 170, 19);
		panelEndereco.add(txtIP);

		txtPorta = new JFormattedTextField();
		txtPorta.setToolTipText("Porta");
		txtPorta.setBounds(194, 43, 87, 19);
		panelEndereco.add(txtPorta);

		label = new JLabel(":");
		label.setBounds(184, 43, 19, 15);
		panelEndereco.add(label);

		lblIP = new JLabel("IP");
		lblIP.setBounds(12, 25, 170, 15);
		panelEndereco.add(lblIP);
		
		lblPorta = new JLabel("Porta");
		lblPorta.setBounds(194, 25, 87, 15);
		panelEndereco.add(lblPorta);

		panelServico = new JPanel();
		panelServico.setBorder(new TitledBorder(null, "Nome do servi\u00E7o:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelServico.setLayout(null);
		panelServico.setBounds(27, 98, 384, 74);
		frame.getContentPane().add(panelServico);

		txtServico = new JFormattedTextField();
		txtServico.setToolTipText("(ex: HelloServer)");
		txtServico.setBounds(12, 43, 170, 19);
		panelServico.add(txtServico);

		lblNomeDoServico = new JLabel("(ex: teste)");
		lblNomeDoServico.setBounds(12, 24, 191, 15);
		panelServico.add(lblNomeDoServico);

		btnConfirmar = new JButton("Confirmar");
		btnConfirmar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sEndServidor = txtIP.getText();
				sPorta = txtPorta.getText();
				sServico = txtServico.getText();
				
				oClient.setEndServidor(sEndServidor + ":" + sPorta);
				oClient.setsServico(sServico);
				try {
					sRetorno = oClient.Conecta();
				} catch (RemoteException | MalformedURLException | NotBoundException e1) {
					e1.printStackTrace();
				}
				txtResposta.setText(sRetorno);
			}
		});
		btnConfirmar.setBounds(147, 184, 171, 25);
		frame.getContentPane().add(btnConfirmar);
		
		txtResposta = new JTextField();
		txtResposta.setBounds(191, 242, 220, 19);
		frame.getContentPane().add(txtResposta);
		txtResposta.setColumns(10);
		
		lblMensagemDoServidor = new JLabel("Mensagem do servidor:");
		lblMensagemDoServidor.setBounds(12, 244, 191, 15);
		frame.getContentPane().add(lblMensagemDoServidor);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(12, 221, 417, 2);
		frame.getContentPane().add(separator);
	}
}
