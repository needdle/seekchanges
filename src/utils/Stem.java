package utils;

public class Stem {
	private static PorterStemmer stemmer = new PorterStemmer();

	public static String stem(String word) {
		stemmer.reset();
		stemmer.stem(word);
		return stemmer.toString();

	}
	public static void main(String []args){
		for(String str: Splitter.splitNatureLanguage("this is a good day today !{!?]")){
			System.out.println(str);
		}
	}
}
