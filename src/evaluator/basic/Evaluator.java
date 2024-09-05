package evaluator.basic;

import java.io.*;
import constructs.*;
import lexer.*;

/* To start
//
// Compilare: javac -d ../build evaluator/basic/Evaluator.java
// Eseguire:  java -cp ../build evaluator.basic.Evaluator
//
// Nota:      Per utilizzare i package in java stavo quasi per piangere, 
//            questo è piu per me che come documentazione vera :( .*/

/* Explanation: il valutatore utilizza la ricorsione del parser per poter fare la sintesi dell albero costruito, 
//              naturalmente lo fa durante al controllo del parser per ferificare se la sequenza di token sia appartenete
//              al linguaggio.
*/

/* Soluzione: il design della soluzioni non è il massimo, secondo me sarebbe meglio far produrre albero al parser con la ricorsione
//            e poi utilizzare un modulo per poterlo risolvero/valutare. Potrebbe rendere il valutatore e il parser piu modulari e leggibili,
//            dividendo le responsabilita.
*/

public class Evaluator {
    private Lexer lexer;            // Lexer object to get tokens
    private Token look;             // Lookahead for predict with LL(1) parser

    public Evaluator(Lexer l) {
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
        int expr_val = 0;

        switch (look.tag) {
            case '(':               // Token.lpt.tag
            case Tag.NUM:           // NUM
                expr_val = expr();
                break;
            case Tag.EOF:
                break;
            default:  
                error("Unexpected token in start");
        }
        match(Tag.EOF);

        System.out.println(expr_val);
    }

    private int expr() {
        int term_val = 0, exprp_val = 0;

        switch (look.tag) {
            case '(':               // Token.lpt.tag
            case Tag.NUM:           // NUM
                term_val  = term();
                exprp_val = exprp(term_val);
                break;
            default:  
                error("Unexpected token in expr");
        }

        return exprp_val;
    }

    private int exprp(int exprp_i) {
        int term_val = 0, exprp_val = 0;

        switch (look.tag) {
            case '+':
                match(look.tag);
                term_val = term(); 
                exprp_val = exprp(exprp_i + term_val);
                break;
            case '-':
                match(look.tag);
                term_val = term(); 
                exprp_val = exprp(exprp_i - term_val);
                break;
            case ')':
            case Tag.EOF:
                exprp_val = exprp_i;
                break;
            default:  
                error("Unexpected token in exprp");
        }

        return exprp_val;
    }

    private int term() {
        int termp_i = 0 , termp_val = 0;

        switch (look.tag) {
            case '(':               // Token.lpt.tag
            case Tag.NUM:           // NUM
                termp_i   = fact();
                termp_val = termp(termp_i);
                break;
            case Tag.EOF:
                break;
            default:  
                error("Unexpected token in term");
        }

        return termp_val;
    }

    private int termp(int termp_i) {
        int termp1_val = 0, termp_val = 0;

        switch (look.tag) {
            case '*':
                match(look.tag);
                termp1_val = termp_i * fact();
                termp_val = termp(termp1_val);
                break;
            case '/':
                match(look.tag);
                termp1_val = termp_i / fact();
                termp_val = termp(termp1_val);
                break;
            case ')':
            case '+':
            case '-':
            case Tag.EOF:
                termp_val = termp_i;
                break;
            default:  
                error("Unexpected token in termp");
        }

        return termp_val;
    }

    private int fact() {
        int fact_val = 0;
        
        switch (look.tag) {
            case '(':
                match('(');
                fact_val = expr();
                match(')');
                break;
            case Tag.NUM:   // Tag.NUM
                NumberToken NUM = (NumberToken) look;
                fact_val = NUM.lexeme;
                match(Tag.NUM);
                break;
            default:  
                error("Unexpected token in fact");
        }
        
        return fact_val;
    }

    public static void main(String[] args) {
        try {
            // Lexer
            String path = "../test/Input.lft";
            BufferedReader input = new BufferedReader(new FileReader(path));    
            Lexer l = new Lexer(input);

            // Parser
            Evaluator parser = new Evaluator(l);
            parser.start();
            System.out.println("Input OK");

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}