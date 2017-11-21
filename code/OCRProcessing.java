import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;

public class OCRProcessing {

	private static final String BASE_DIR = "test-resource";

	private JTextArea txtConsole;
	private Classification chassification;

	public OCRProcessing() {
		try {
			this.chassification = new PixelsPositionsClassification(loadData());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public OCRProcessing(JTextArea txtConsole) {
		this();
		this.txtConsole = txtConsole;
	}

	public void learm(String letras, BufferedImage image) throws IOException, ClassNotFoundException {
		letras = letras.replaceAll(" ", "");

		CharacterProcessing processing = new CharacterProcessing();
		List<CharacterImage> characters = processing.discover(image);

		for (int index = 0; index < characters.size(); index++) {
			characters.get(index).setCharacter(letras.charAt(index));;
		}

		save(characters);
	}

	public String recogner(BufferedImage image) throws FileNotFoundException, ClassNotFoundException, IOException {
		CharacterProcessing processing = new CharacterProcessing();
		List<CharacterImage> characters = processing.discover(image);
		StringBuilder txt = new StringBuilder();

		CharacterImage previus = null;
		for (CharacterImage character: characters) {
			//ImageIO.write(character.newImage(), "png", new File(BASE_DIR+"/result-processing/"+Instant.now().toEpochMilli()+".png"));
			CharacterImage candidate = chassification.analyze(character);

			if (processing.isBlankSpace(previus, character)) {
				txt.append(" ");
			}

			txt.append(candidate.getCharacter());

			if (character.isLineBreak()) {
				txt.append("\n");
			}

			print(String.format("Caractere: %s, aprox: %s\n", candidate.getCharacter(), candidate.getProximidade()));
			previus = character;
		}

		print(String.format("O resultado e: %s\n", txt.toString()));

		return txt.toString();
	}

	private void save(List<CharacterImage> characters) throws IOException, ClassNotFoundException {
		File fileData = new File(BASE_DIR+"/data.ser");

		if (!fileData.getParentFile().exists()) {
			fileData.getParentFile().mkdirs();
		}

		chassification.getCharacters().addAll(characters);
		FileOutputStream fos = new FileOutputStream(fileData);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(chassification.getCharacters());
		oos.close();
		fos.close();
	}

	private List<CharacterImage> loadData() throws FileNotFoundException, IOException, ClassNotFoundException {
		File fileData = new File(BASE_DIR+"/data.ser");

		List<CharacterImage> characters = null;

		if (fileData.exists()) {
			FileInputStream fis = new FileInputStream(fileData);
			ObjectInputStream ois = new ObjectInputStream(fis);
			characters = (List<CharacterImage>) ois.readObject();
			fis.close();
			ois.close();
		}
		else {
			characters = new ArrayList<CharacterImage>();
		}

		return characters;
	}

	private void print(String text) {
		if (txtConsole == null) {
			//System.out.print(text);
		}
		else {
			txtConsole.append(text);
		}
	}
}