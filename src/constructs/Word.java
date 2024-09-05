package constructs;

/* Explanation: Word class represent the multiple chars token on our lexer, 
 * 				This class inherits the characteristics of a token which is 
 * 				why it is its subclass. (static assets)
*/

public class Word extends Token {
    public String lexeme = "";  // Lexeme value to word of language

	// Buider & toString & ...
    public Word(int tag, String s) { super(tag); lexeme = s; }
    public String toString()       { return "<" + tag + ", " + lexeme + ">"; }

	// Static assets to world tokens 
    public static final Word
		assign   = new Word(Tag.ASSIGN, "assign"),
		to       = new Word(Tag.TO, 	"to"),
		iftok    = new Word(Tag.IF,     "if"),
		elsetok  = new Word(Tag.ELSE,   "else"),
		dotok    = new Word(Tag.DO,     "do"),
		fortok   = new Word(Tag.FOR,    "for"),
		begin    = new Word(Tag.BEGIN,  "begin"),
		end      = new Word(Tag.END,    "end"),
		print    = new Word(Tag.PRINT,  "print"),
		read     = new Word(Tag.READ,   "read"),
		truetok  = new Word(Tag.TTRUE,  "true"),
		falsetok = new Word(Tag.TFALSE, "false"),
		init     = new Word(Tag.INIT,   ":="),
		or       = new Word(Tag.OR,     "||"),
		and      = new Word(Tag.AND,    "&&"),
		lt       = new Word(Tag.RELOP,  "<"),
		gt       = new Word(Tag.RELOP,  ">"),
		eq       = new Word(Tag.RELOP,  "=="),
		le       = new Word(Tag.RELOP,  "<="),
		ne       = new Word(Tag.RELOP,  "<>"),
		ge       = new Word(Tag.RELOP,  ">=");
}