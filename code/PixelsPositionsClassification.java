import java.util.List;

public class PixelsPositionsClassification implements Classification {

	private List<CharacterImage> characters;
	private double averageProximity = 100d;
	private int averageQt = 0;

	public PixelsPositionsClassification(List<CharacterImage> characters) {
		this.characters = characters;
	}

	@Override
	public CharacterImage analyze(CharacterImage character) {
		int qt = 0;
		int qtCandidate = 0;
		CharacterImage bestCandidate = null;
		for (CharacterImage charAnalyze: characters) {
			qt++;
			charAnalyze.analisarProximidade2(character);
			if (bestCandidate == null || (bestCandidate.getProximidade() < charAnalyze.getProximidade())) {
				bestCandidate = charAnalyze;
				qtCandidate = qt;
			}

			if (bestCandidate.getProximidade() > averageProximity && qt > averageQt) {
				break;
			}
		}

		averageProximity = (averageProximity+bestCandidate.getProximidade())/2;
		averageQt = (averageQt+qtCandidate)/2;

		if (this.characters.remove(bestCandidate)) {
			this.characters.add(0,bestCandidate);
		}
		
		return bestCandidate;
	}

	@Override
	public List<CharacterImage> getCharacters() {
		return characters;
	}
}