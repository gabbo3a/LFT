package constructs;

/* Explanation: Token class represent a singol char token with the name and 
 *              number of token is equals for comodoty. This class is a parent 
 *              of each token type in our lexer. (static assets)
 * 
 * Note:  	    For each type of token it is possible with a little effort create 
 * 				a general lexer for any language.
*/

public class Token {
    public final int tag;	 // Simple integer to uniquely identify a token 

	// Buider & toString & ...
    public Token(int t)      { tag = t; }	
    public String toString() { return "<" + tag + ">"; }

	// Static assets to singol char tokens 
    public static final Token
		not       = new Token('!'),
		lpt       = new Token('('),
		rpt       = new Token(')'),
		lpq       = new Token('['),
		rpq       = new Token(']'),
		lpg       = new Token('{'),
		rpg       = new Token('}'),
		plus      = new Token('+'),
		minus     = new Token('-'),
		mult      = new Token('*'),
		div       = new Token('/'),
		semicolon = new Token(';'),
		comma     = new Token(',');
}