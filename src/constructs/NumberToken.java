package constructs;

/* Explanation: 
 * 
*/

public class NumberToken extends Token {
	public int lexeme;			// Lexeme value to word of language

	// Buider & toString & ...
	public NumberToken(int n) { super(Tag.NUM); lexeme = n; }
	public String toString()  { return "<" + tag + ", " + lexeme + ">"; }
}
