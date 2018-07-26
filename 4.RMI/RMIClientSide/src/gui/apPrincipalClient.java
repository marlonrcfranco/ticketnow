package gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import rmi.Client;

public class apPrincipalClient {
	public static JFrame frmConfiguraoRmi;
	private Client oClient;
	private String sRetorno;
	private String sEndServidor;
	private String sServico = "Validador";
	private boolean erro = true;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new apPrincipalClient();
					frmConfiguraoRmi.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	public apPrincipalClient() throws InvocationTargetException, InterruptedException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		oClient = new Client();
		frmConfiguraoRmi = new JFrame();
		frmConfiguraoRmi.setTitle("Configuração RMI");
		frmConfiguraoRmi.setBounds(100, 100, 377, 247);
		frmConfiguraoRmi.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmConfiguraoRmi.getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Endere\u00E7o do servidor:", TitledBorder.LEADING, TitledBorder.TOP,
				null, null));
		panel.setBounds(23, 12, 329, 157);
		frmConfiguraoRmi.getContentPane().add(panel);
		panel.setLayout(null);

		JLabel label = new JLabel(":");
		label.setBounds(131, 38, 11, 15);
		panel.add(label);

		JLabel lblIp = new JLabel("IP");
		lblIp.setVerticalAlignment(SwingConstants.BOTTOM);
		lblIp.setBounds(12, 23, 114, 15);
		panel.add(lblIp);

		JFormattedTextField txtIP = new JFormattedTextField();
		txtIP.setToolTipText("IP ou localhost");
		txtIP.setBounds(12, 36, 114, 19);
		panel.add(txtIP);

		JLabel lblPorta = new JLabel("Porta");
		lblPorta.setVerticalAlignment(SwingConstants.BOTTOM);
		lblPorta.setBounds(138, 19, 72, 19);
		panel.add(lblPorta);

		JFormattedTextField txtPorta = new JFormattedTextField();
		txtPorta.setToolTipText("Somente números.");
		txtPorta.setBounds(141, 36, 77, 19);
		panel.add(txtPorta);

		JLabel lblCheck;
		lblCheck = new JLabel("");
		lblCheck.setBounds(287, 57, 43, 35);
		panel.add(lblCheck);

		JLabel lblIcon = new JLabel("");
		lblIcon.setIcon(new ImageIcon(apPrincipalClient.class.getResource("/resources/icons/globe-icon.png")));
		lblIcon.setBounds(238, 12, 70, 70);
		panel.add(lblIcon);

		JLabel lblRetorno;
		lblRetorno = new JLabel("");
		lblRetorno.setFont(new Font("Dialog", Font.PLAIN, 10));
		lblRetorno.setBounds(12, 105, 310, 40);
		panel.add(lblRetorno);

		JButton btnTestarConexo = new JButton("Testar conexão");
		btnTestarConexo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sEndServidor = txtIP.getText() + ":" + txtPorta.getText();
				oClient.setEndServidor(sEndServidor);
				oClient.setsServico(sServico);
				sRetorno = oClient.Conecta();
				if (sRetorno.equalsIgnoreCase("ERRO")) {
					erro = true;
					lblRetorno.setText("ERRO: não foi possível se conectar à este servidor.");
					lblCheck.setIcon(
							new ImageIcon(apPrincipalClient.class.getResource("/resources/icons/check-red.png")));
				} else {
					erro = false;
					lblRetorno.setText("Conexão bem sucedida!");
					lblCheck.setIcon(
							new ImageIcon(apPrincipalClient.class.getResource("/resources/icons/check-green.png")));
					lblCheck.setVisible(true);
				}
			}
		});
		btnTestarConexo.setIcon(null);
		btnTestarConexo.setBounds(34, 68, 151, 25);
		panel.add(btnTestarConexo);

		JButton btnConfirmar = new JButton("Confirmar");
		btnConfirmar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (erro) {
					JOptionPane.showMessageDialog(null, "Informe um IP e Porta válidos antes de prosseguir.", "Atenção",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					frmConfiguraoRmi.dispatchEvent(new WindowEvent(frmConfiguraoRmi, WindowEvent.WINDOW_CLOSING));
				}
			}
		});
		btnConfirmar.setBounds(111, 181, 151, 25);
		frmConfiguraoRmi.getContentPane().add(btnConfirmar);

	}

	public String getsEndServidor() {
		return sEndServidor;
	}

	public String getsServico() {
		return sServico;
	}

	public void setsServico(String sServico) {
		this.sServico = sServico;
	}

}
