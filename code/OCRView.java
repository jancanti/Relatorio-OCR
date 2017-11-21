import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import br.com.deployxtech.ocr.OCRProcessing;

public class OCRView extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private JButton btnTest = new JButton("Testar");
	private JButton btnLearm = new JButton("Treinar");
	private JPanel pnlControl = new JPanel();

	private JLabel lblImage = new JLabel();
	private JComboBox<String> cmbFonts = new JComboBox<>();
	private JButton btnSearchImage = new JButton("Selecionar...");
	private JPanel pnlImage = new JPanel();
	private JTextArea txtResult = new JTextArea();
	private JTextArea txtConsole = new JTextArea();
	private JPanel pnlProcessing = new JPanel(new BorderLayout(10,10));

	private JFileChooser fc = new JFileChooser(".");

	private OCRProcessing processing = new OCRProcessing(txtConsole);

	private BufferedImage image;

	public OCRView() {
		setTitle("Reconhecimento de Caracteres");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		init();
	}

	private void init() {
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font[] fonts = e.getAllFonts();
		for (Font font: fonts) {
			cmbFonts.addItem(font.getFontName());
		}

		setLayout(new BorderLayout(10,10));
		pnlControl.add(btnSearchImage);
		pnlControl.add(btnTest);
		pnlControl.add(btnLearm);
		pnlControl.add(cmbFonts);
		pnlControl.setPreferredSize(new Dimension(800, 60));
		getContentPane().add(pnlControl, BorderLayout.NORTH);
		pnlProcessing.setLayout(new GridLayout(2, 1));
		JScrollPane imageScroll = new JScrollPane(lblImage);
		imageScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
		pnlProcessing.add(imageScroll, BorderLayout.NORTH);
		pnlProcessing.add(txtResult, BorderLayout.CENTER);
		getContentPane().add(pnlProcessing, BorderLayout.CENTER);
		txtConsole.setEditable(false);
		JScrollPane consoleScroll = new JScrollPane(txtConsole);
		consoleScroll.setPreferredSize(new Dimension(800, 300));
		getContentPane().add(consoleScroll, BorderLayout.SOUTH);

		btnSearchImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
							"Image files", ImageIO.getReaderFileSuffixes());
					fc.setFileFilter(imageFilter);
					fc.setAcceptAllFileFilterUsed(false);
					int returnVal = fc.showOpenDialog(OCRView.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File fileImage = fc.getSelectedFile();
						image = ImageIO.read(fileImage);
						lblImage.setIcon(new ImageIcon(image));

						Pattern pattern = Pattern.compile("(\\()(.*?)(\\))");
						Matcher matcher = pattern.matcher(fileImage.getName());
						if (matcher.find()) {
							String letras = matcher.group(2);
							txtResult.setText(letras);
						}
						else {
							txtResult.setText("");
						}
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		btnTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (image == null) {
						JOptionPane.showMessageDialog(OCRView.this, "E preciso selecionar uma imagem.");
					}
					else if (!txtResult.getText().equals("")) {
						JOptionPane.showMessageDialog(OCRView.this, "E preciso apagar a caixa de texto para realizar o teste.");
					}
					else {
						String result = processing.recogner(image);
						txtResult.setText(result);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		btnLearm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (image == null) {
						JOptionPane.showMessageDialog(OCRView.this, "E preciso selecionar uma imagem.");
					}
					else if (txtResult.getText().equals("")) {
						JOptionPane.showMessageDialog(OCRView.this, "E preciso escrever as letras exatamente como esta na imagem.");
					}
					else {
						processing.learm(txtResult.getText(), image);
						txtResult.setText("");
						lblImage.setIcon(null);
						JOptionPane.showMessageDialog(OCRView.this, "Letras aprendidas...");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		cmbFonts.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ .,!?-_ 0123456789";	
				Font font = fonts[cmbFonts.getSelectedIndex()];
				font = new Font(font.getFontName(), Font.PLAIN, 40);

				Rectangle2D rectagleText = font.getStringBounds(text, new FontRenderContext(new AffineTransform(), true, true));

				int width = new Double(rectagleText.getWidth()).intValue()+20, height = new Double(rectagleText.getHeight()).intValue()+15;


				BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

				Graphics2D g2d = (Graphics2D) newImage.getGraphics();
				g2d.setColor(Color.white);
				g2d.fillRect(0, 0, width, height);
				g2d.setColor(Color.black);
				g2d.setFont(font);
				g2d.drawString(text, 10, height-15);
				g2d.dispose();

				lblImage.setIcon(new ImageIcon(newImage));
				txtResult.setText(text.replace(" ", ""));
				image = newImage;
			}
		});;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OCRView view = new OCRView();
		view.setSize(400, 600);
		view.setLocationByPlatform(true);
		view.setVisible(true);
	}

}
