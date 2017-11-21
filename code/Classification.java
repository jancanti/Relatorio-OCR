import java.util.List;

public interface Classification {
	CharacterImage analyze(CharacterImage character);
	List<CharacterImage> getCharacters();
}