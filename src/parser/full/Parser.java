package parser.full;

import constructs.Token;
import constructs.Tag;
import lexer.Lexer;

// Explanation: Implementazione di un generico parser, carino ma l'implementazzione 
//              migliore dovrebbe essere grammatica in input senza dover ricompilare.
//              in realta si potrebbe prendere in input e chiamare javac da codice per
//              renderlo "dinamico" pero penso sia meta programmazione (un po come 
//              quando da php si stampa su html per li è interpretato è non è proprio
//              la stessa cosa) :).

public abstract class Parser {
    protected Lexer lexer;            // Lexer object to get tokens
    protected Token look;             // Lookahead for predict with LL(1) parser

    public Parser(Lexer l) {
        lexer = l;      // Init lexer
        move();         // Init first look token
    }

    // Goes to the next token // or peek
    protected void move() {
        look = lexer.scan();
        System.out.println("token = " + look);
    }

    // Method to consume current token and point to next
    protected void match(int t) {
        if (look.tag != t)                                 
            error("Syntax error");

        if (look.tag != Tag.EOF)        
            move();
    }

    // Method to start parse token seq
    public void parse() {
        try {
            start();
            System.out.println("Input OK");
        } catch (RuntimeException e) {
            System.out.println(e);
        }
    }

    // Method for first symbol
    protected abstract void start();

    // Method to generate an error with message
    protected void error(String msg) {
	    throw new RuntimeException("Near line " + lexer.getLine() + ": " + msg);
    }

    // Implementazione err prof (carino uso obj)     
    /* protected SyntaxError error(){
        return new SyntaxError("ERROR: "+ w +", pos "+i);
    }*/
}