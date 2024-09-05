package parser.full;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import constructs.Tag;
import constructs.Token;
import constructs.Word;
import lexer.Lexer;

/* To start
// On:        cd src
// Compilare: javac -d ../build parser/full/ExamLanguageParser.java
// Eseguire : java -cp ../build parser.full.ExamLanguageParser
//
// Nota:      Per utilizzare i package in java stavo quasi per piangere, 
//            questo è piu per me che come documentazione vera :( .*/

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

public class ExamLanguageParser extends Parser {

    public ExamLanguageParser(Lexer lexer) {
        super(lexer);
    }

    // Productions methods ...

    // Prog section

    @Override
    protected void start() {         
        switch(look.tag) {
            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.FOR:
            case Tag.IF:
            case '{' :     // Token.lpg.tag
                statlist();
                break;
            default: 
                error("Unexpected token in start");
        }
        match(Tag.EOF);
    }

    // Statements section

    private void statlist() {
        switch(look.tag) {
            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.FOR:
            case Tag.IF:
            case '{':     //  Token.lpg.tag
                stat();
                statlistp();
                break;
            default: 
                error("Unexpected token in statlist");
        }
    }

    private void statlistp() {
        switch(look.tag) {
            case ';':               // Token.semicolon.tag
                match(look.tag);    
                stat();
                statlistp();
                break;
            case '}':               //  Token.rpg.tag
            case Tag.EOF: 
                break;
            default: 
                error("Unexpected token in statlistp");
        }
    }

    private void stat() {
        switch(look.tag) {
            case Tag.ASSIGN:
                match(look.tag);
                assignlist();
                break;
            case Tag.PRINT:
                match(look.tag);
                match(Token.lpt.tag);     // '('
                exprlist();
                match(Token.rpt.tag);     //  ')'
                break;
            case Tag.READ:
                match(look.tag);
                match(Token.lpt.tag);     // '('
                idlist();
                match(Token.rpt.tag);     //  ')'
                break;
            case Tag.FOR:
                match(look.tag);
                match(Token.lpt.tag);     // '('
                bfor();
                match(Token.rpt.tag);     //  ')'
                match(Word.dotok.tag);    // 'do'
                stat();
                break;
            case Tag.IF:
                match(look.tag);
                match(Token.lpt.tag);  // '('
                bexpr();
                match(Token.rpt.tag);  //  ')'
                stat();
                pelse();
                break;
            case '{':                    //  Token.lpg.tag
                match(look.tag);         // '{'
                statlist();
                match(Token.rpg.tag);    // '}'
                break;
            default: 
                error("Unexpected token in stat");
        }
    }
    
    private void pelse() {
        switch(look.tag) {
            case Tag.ELSE:                  // 'else'
                match(look.tag);    
                stat();
                match(Tag.END);
                break;
            case Tag.END:                   // 'end'
                match(look.tag);
                break;
            default: 
                error("Unexpected token in pelse");
        }
    }

    private void bfor() {
        switch(look.tag) {
            case Tag.ID:                      // else
                match(Tag.ID);
                match(Tag.INIT);                // :=
                expr();
                match(Token.semicolon.tag);     // ';'
                bexpr();
                break;
            case Tag.RELOP:                     // RELOP
                bexpr();
                break;
            default: 
                error("Unexpected token in bfor");
        }
    }
    
    // Assigns section

    private void assignlist() {
        switch(look.tag) {
            case '[':                       // Token.lpq.tag
                match(Token.lpq.tag);       // '['
                expr();             
                match(Tag.TO);              // to
                idlist();
                match(Token.rpq.tag);       // ']'
                assignlistp();
                break;
            default: 
                error("Unexpected token in assignlist");
        }
    }

    private void assignlistp() {
        switch(look.tag) {
            case '[':                       //  Token.lpq.tag
                match(Token.lpq.tag);       // '['
                expr();             
                match(Tag.TO);              // to
                idlist();
                match(Token.rpq.tag);       // ']'
                assignlistp();
                break;
            case '}':                       //  Token.rpg.tag
            case ';':                       // Token.comma.tag
            case Tag.ELSE:                  
            case Tag.END:
            case Tag.EOF: 
                break;
            default: 
                error("Unexpected token in assignlistp");
        }
    }

    //  IDlist section

    private void idlist() {
        switch(look.tag) {
            case Tag.ID:               
                match(look.tag);              
                idlistp();
                break;
            default: 
                error("Unexpected token in idlist");
        }
    }

    private void idlistp() {
        switch(look.tag) {
            case ',':                       // Token.comma.tag
                match(Token.comma.tag);          
                match(Tag.ID);              
                idlistp();
                break;
            case ')':              // Token.rpt.tag
            case ']':              // Token.rpq.tag
                break;
            default: 
                error("Unexpected token in idlistp");
        }
    }

    // Expressions section

    private void bexpr() {
        switch(look.tag) {
            case Tag.RELOP:               
                match(look.tag); 
                expr();
                expr();             
                break;
            default: 
                error("Unexpected token in bexpr");
        }
    }

    private void expr() {
        switch(look.tag) {
            case '+':                   // Token.plus.tag
            case '*':                   // Token.mult.tag   
                match(look.tag);
                match(Token.lpt.tag);       // '('
                exprlist();
                match(Token.rpt.tag);       // ')'           
                break;
            case '-':             // Token.minus.tag
            case '/':             // Token.div.tag    
                match(look.tag); 
                expr();
                expr();            
                break;
            case Tag.NUM:
            case Tag.ID:
                match(look.tag);
                break;
            default: 
                error("Unexpected token in expr");
        }
    }

    private void exprlist() {
        switch(look.tag) {
            case '+':       // Token.plus.tag
            case '*':       // Token.mult.tag
            case '-':       // Token.minus.tag
            case '/':       // Token.div.tag
            case Tag.NUM:
            case Tag.ID:               
                expr();
                exprlistp();         
                break;
            default: 
                error("Unexpected token in exprlist");
        }
    }

    private void exprlistp() {
        switch(look.tag) {
            case ',':               // Token.comma.tag
                match(look.tag);
                expr();
                exprlistp();     
                break;
            case ')':                //  Token.rpt.tag
                break;
            default: 
                error("Unexpected token in exprlistp");
        }
    }

    public static void main(String[] args) {
        try {
            // Lexer
            String path = "../test/Input.lft";
            BufferedReader input = new BufferedReader(new FileReader(path));    
            Lexer l = new Lexer(input);

            // Parser
            Parser parser = new ExamLanguageParser(l);   
            parser.parse();

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
