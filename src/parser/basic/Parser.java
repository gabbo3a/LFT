package parser.basic;

import java.io.*;
import constructs.*;
import lexer.*;

/* To start
//
// Compilare: javac -d ../build parser/basic/Parser.java
// Eseguire : java -cp ../build parser.basic.Parser
//
// Nota:      Per utilizzare i package in java stavo quasi per piangere, 
//            questo è piu per me che come documentazione vera :( .*/

// Calcoli: https://drive.google.com/file/d/1P9ViHvDLlbCQz8K_8ZNUr4NWdA0C3Y0Q/view?usp=sharing
// Video:   https://informatica.i-learn.unito.it/mod/url/view.php?id=228570

/* Explanation: il parser ricorsivo discendente LL(1) utilizza la ricorsione per cercare di generare un albero
//              tramite la "stringa" di input (sequnza di token), se cio non è possibile lancia un errore interrompoendo 
//              la costruzione dell albero.
//              Questo si puo afre in maniera deterministica poiche si utilizzano gli insiemi guida per cui ogni varaibile 
//              di testa di una produzione anche se ha N corpi tali corpi possono generare insiemi di simbili diversi eliminado 
//              la "ambiguta". 
*/

/* Soluzione: il design del codice è stato dato cosi, non mi piace tanto poiche ogni volta che bisogna cambiare linguaggio
//            si è costrutti a ricompilare, sarebbe meglio separarre i dati dal algortimo gestendo direttemante SET 
//            FIRST, FOLLOW e grammatiche (SET di Runnable) come input che essendo oggeti possono essere passati 
//            come parametri ecc, pero a qual punto si potrebbe/dovrebbe pure controllare la corretezza del input (la grammatica e
//            gli insemi FIRST e FOLLOW ) di ogni produzione, probalimente un sacco di grafi ecc.
//            Per non parlare del problema delle dichiarazioni se si definisce un Runnable che utilizza un altro Runnable deve essere
//            dichiarato prima o accesibile prima e se pero tutti devono riscrivre tutti nei loro corpi (caso un po limite), non saprei 
//            gestirlo.
//            Lavoro che probabilmente mi prenderebbe un sacco di tempo e mi farebbe piangere quindi nulla :). 
//            In alternativa si potrebbe fare un programma che stampo un programma e poi lo compila come questo che è sicuramente piu semplice
//            anche se la vedrei come una sconfitta.
*/

// Modifica template: avendo modificato la classe lexer incaspulando il BufferedReader, ho dovuto
//                    dodificare anche il perser nel costruttore e un attributo pbr.
//                    Cosi non si deve trascinare ditro come una zavorra :) .

/* Template metodi stupido stupido:
    switch (look.tag) {

        // FIRST SET ... 
        case ...:        
        case ...:
        ...
            match(look.tag);
            call produzioni ...
            break;
        Ripetere per tutte le produzioni se non si possono unire...

        // FOLLOW SET ...
        case ...:
        case ...:
        ...
            nomatch(); // Perche non è ancora stato costrutito albero fini a qual punto
            break;     // corrispodne a riscivere tale varibile in epsilon
        
        // !GUIDA SET
        default:  // Errore di quaesta prozione 
            error("Unexpected token in start"); 
            
    }
    match(...); // Se necessario carattere da cosumare sempre dopo
*/

/* Test slide: 
    // )2   --> start
    // 2+(  --> expr
    // 5+)  --> term
    // 1+2( --> termp
    // 1*+2 --> fact
    // 1+2) --> syntax error
*/ 

public class Parser {
    private Lexer lexer;            // Lexer object to get tokens
    private Token look;             // Lookahead for predict with LL(1) parser

    public Parser(Lexer l) {
        lexer = l;
        move();
    }

    // Goes to the next token
    void move() {
        look = lexer.scan();
        System.out.println("token = " + look);
    }

    // Method to generate an error with message
    void error(String msg) {
	    throw new Error("near line " + lexer.getLine() + ": " + msg);
    }

    // Method to consume current token and point to next
    void match(int t) {
        if (look.tag != t)                                 
            error("syntax error");

        if (look.tag != Tag.EOF)        
            move();
    }

    // Productions methods ...

    public void start() {
        switch (look.tag) {
            case '(':               // Token.lpt.tag
            case Tag.NUM:           // NUM
                expr();
                break;
            case Tag.EOF:
                break;
            default:  
                error("Unexpected token in start");
        }
        match(Tag.EOF);
    }

    private void expr() {
        switch (look.tag) {
            case '(':               // Token.lpt.tag
            case Tag.NUM:           // NUM
                term();
                exprp();
                break;
            // case Tag.EOF:
                // break;
            default:  
                error("Unexpected token in expr");
        }
    }

    private void exprp() {
        switch (look.tag) {
            case '+':
            case '-':
                match(look.tag);
                term(); 
                exprp();
                break;
            case ')':
            case Tag.EOF:
                break;
            default:  
                error("Unexpected token in exprp");
        }
    }

    private void term() {
        switch (look.tag) {
            case '(':               // Token.lpt.tag
            case Tag.NUM:           // NUM
                fact();
                termp();
                break;
            case Tag.EOF:
                break;
            default:  
                error("Unexpected token in term");
        }
    }

    private void termp() {
        switch (look.tag) {
            case '*':
            case '/':
                match(look.tag);
                fact();
                termp();
                break;
            case ')':
            case '+':
            case '-':
            case Tag.EOF:
                break;
            default:  
                error("Unexpected token in termp");
        }
    }

    private void fact() {
        // static final int lpt = Token.lpt.tag; ? 
        switch (look.tag) {
            case '(':
                match('(');
                expr();
                match(')');
                break;
            case Tag.NUM:   // Tag.NUM
                match(Tag.NUM);
                break;
            default:  
                error("Unexpected token in fact");
        }
        
    }

    public static void main(String[] args) {
        try {
            // Lexer
            String path = "../test/Input.lft";
            BufferedReader input = new BufferedReader(new FileReader(path));    
            Lexer l = new Lexer(input);

            // Parser
            Parser parser = new Parser(l);
            parser.start();
            System.out.println("Input OK");

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}