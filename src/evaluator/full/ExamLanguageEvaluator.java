package evaluator.full;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import constructs.*;
import lexer.Lexer;
import parser.full.Parser;
import evaluator.full.utils.*;

/* To start
// On:        cd src
// Compilare: javac -d  ../build evaluator/full/ExamLanguageEvaluator.java;
// Eseguire : java -cp  ../build evaluator.full.ExamLanguageEvaluator;
// Jasmin   : java -jar ../jasmin.jar ../test/Output.j; 
// Move     : move Output.class ../build -force;
// ExeJasmin: java -cp  ../build Output

// Nota:      Per utilizzare i package in java stavo quasi per piangere, 
//            questo è piu per me che come documentazione vera :( .*/

/* 
javac -d  ../build evaluator/full/ExamLanguageEvaluator.java;
java -cp  ../build evaluator.full.ExamLanguageEvaluator;
java -jar ../jasmin.jar ../test/Output.j; 
move Output.class ../build -force;
java -cp  ../build Output
*/

// Risorse utili:
// https://informatica.i-learn.unito.it/pluginfile.php/392156/mod_resource/content/1/jvm.pdf
// https://informatica.i-learn.unito.it/pluginfile.php/392184/mod_resource/content/1/lft-vv-lezioni-202324L8.pdf
// https://informatica.i-learn.unito.it/pluginfile.php/392138/mod_resource/content/1/LFT_Lab2324L8.pdf

// TODO: 5.2

public class ExamLanguageEvaluator extends Parser {
    SymbolTable st     = new SymbolTable();         // Tabella del simboli, <name, address>
    CodeGenerator code = new CodeGenerator();       // Oggeto generatore di codice
    int addressCount   = 0;                         // ...

    public ExamLanguageEvaluator(Lexer lexer) {
        super(lexer);
    }

    enum Contex {READ, PRINT, ASSIGN, EXPR, FOR, IF }

    // Productions methods ...

    // Prog section

    @Override
    protected void start() {
        int inner_next = code.newLabel();

        switch(look.tag) {
            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.FOR:
            case Tag.IF:
            case '{' :     // Token.lpg.tag
                statlist(inner_next);
                match(Tag.EOF); 
                break;
            default: 
                error("Unexpected token in start");
        }
      
        // Generazione codice
        try {
            code.emitLabel(inner_next);
            code.toJasmin();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    // Statements section

    private void statlist(int next) {
        int inner_next = code.newLabel();

        switch(look.tag) {
            case Tag.ASSIGN:    
            case Tag.PRINT:
            case Tag.READ:
            case Tag.FOR:
            case Tag.IF:
            case '{':                //  Token.lpg.tag
                stat(inner_next);
                code.emitLabel(inner_next);
                statlistp(next);
                break;
            default: 
                error("Unexpected token in statlist");
        }
    }

    private void statlistp(int next) {
        int inner_next = code.newLabel();

        switch(look.tag) {
            case ';':               // Token.semicolon.tag
                match(look.tag);    
                stat(inner_next);
                code.emitLabel(inner_next);
                statlistp(next);
                break;
            case '}':               //  Token.rpg.tag
            case Tag.EOF:
                code.emit(OpCode.GOto, next); 
                break;
            default: 
                error("Unexpected token in statlistp");
        }
        
    }
    
    private void stat(int next) {
        switch(look.tag) {
            case Tag.ASSIGN:
                match(look.tag);
                assignlist();
                code.emit(OpCode.GOto, next); 
                break;
            case Tag.PRINT:
                match(look.tag);
                match(Token.lpt.tag);     // '(' Tag.lpt
                exprlist(Contex.PRINT, OpCode.empty);
                match(Token.rpt.tag);     //  ')'
                code.emit(OpCode.GOto, next); 
                break;
            case Tag.READ:
                match(look.tag);
                match(Token.lpt.tag);     // '('
                idlist(Contex.READ);
                match(Token.rpt.tag);     //  ')'
                code.emit(OpCode.GOto, next); 
                break;
            case Tag.FOR: {
                int ltrue = code.newLabel();
                int lcond = code.newLabel();

                match(look.tag);
                match(Token.lpt.tag);     // '('
                bfor(ltrue, next, lcond);

                match(Token.rpt.tag);     //  ')'
                match(Word.dotok.tag);    // 'do'

                code.emitLabel(ltrue);
                stat(lcond);
                break;
            }
            case Tag.IF: {
                int ltrue = code.newLabel();
                int lfalse = code.newLabel();

                match(look.tag);
                match(Token.lpt.tag);  // '('
                bexpr(ltrue, lfalse);
                match(Token.rpt.tag);  //  ')'

                code.emitLabel(ltrue);
                stat(next);
                pelse(lfalse, next);
                break;
            }
            case '{':                    //  Token.lpg.tag
                match(look.tag);         // '{'
                statlist(next); 
                match(Token.rpg.tag);    // '}'
                break;
            default: 
                error("Unexpected token in stat");
        }
    }
    
    private void pelse(int lfalse, int next) {
        code.emitLabel(lfalse);

        switch(look.tag) {
            case Tag.ELSE: { // 'else'
                match(look.tag);
                stat(next);
                match(Tag.END);
                break;
            }
            case Tag.END:   // 'end'
                match(look.tag);
                code.emit(OpCode.GOto, next);
                break;
            default: 
                error("Unexpected token in pelse");
        }
    }
    
    private void bfor(int ltrue, int lfalse, int lcond) {
        switch(look.tag) {
            case Tag.ID:                        // else
               Word tmp = (Word) look;

                match(Tag.ID);
                match(Tag.INIT);                // :=
                
                int id_address = st.lookupAddress(tmp.lexeme);
                if (id_address == -1) {
                    id_address = addressCount;
                    st.insert(tmp.lexeme, addressCount++);
                }
                expr();
                match(Token.semicolon.tag);     // ';'
                code.emit(OpCode.istore, id_address);
                
                code.emitLabel(lcond);
                bexpr(ltrue, lfalse);
                break;
            case Tag.AND:
            case Tag.OR:
            case '!':
            case Tag.RELOP:                     
                code.emitLabel(lcond);
                bexpr(ltrue, lfalse);
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
                idlist(Contex.ASSIGN);
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
                idlist(Contex.ASSIGN);
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

    private void idlist(Contex contex) {
        switch(look.tag) {
            case Tag.ID:
                Word tmp = (Word) look;
                if      (contex == Contex.ASSIGN) code.emit(OpCode.dup);
                else if (contex == Contex.READ)   code.emit(OpCode.invokestatic, 0);
                
                // Get address or build address
                int id_address = st.lookupAddress(tmp.lexeme);
                if (id_address == -1) {
                    id_address = addressCount;
                    st.insert(tmp.lexeme, addressCount++);
                }
                code.emit(OpCode.istore, id_address);
                
                match(look.tag);              
                idlistp(contex);
                break;
            default: 
                error("Unexpected token in idlist");
        }
    }

    private void idlistp(Contex contex) {
        switch(look.tag) {
            case ',':                       // Token.comma.tag
                match(Token.comma.tag); 
                
                Word tmp = (Word) look;
                if      (contex == Contex.ASSIGN) code.emit(OpCode.dup);
                else if (contex == Contex.READ)   code.emit(OpCode.invokestatic, 0);
                
                // Get address or build address
                int id_address = st.lookupAddress(tmp.lexeme);
                if (id_address == -1) {
                    id_address = addressCount;
                    st.insert(tmp.lexeme, addressCount++);
                }
                code.emit(OpCode.istore, id_address);
                         
                match(Tag.ID);              
                idlistp(contex);
                break;
            case ')':              // Token.rpt.tag
            case ']':              // Token.rpq.tag
                // Se sei in assign ultomo dup deve essere rimosso
                if (contex == Contex.ASSIGN) code.emit(OpCode.pop);
                break;
            default: 
                error("Unexpected token in idlistp");
        }
    }

    // Expressions section
    private void bexpr(int ltrue, int lfalse) {
        switch(look.tag) {
            case Tag.AND: {
                match(look.tag);

                int nbexprl = code.newLabel();
                bexpr(nbexprl, lfalse);
                code.emitLabel(nbexprl);
                bexpr(ltrue, lfalse);
                break;
            }
            case Tag.OR: {
                match(look.tag);

                int nbexprl = code.newLabel();
                bexpr(ltrue, nbexprl);
                code.emitLabel(nbexprl);
                bexpr(ltrue, lfalse);
                code.emit(OpCode.GOto, lfalse);
                break;
            }
            case '!':
                match(look.tag);
                bexpr(lfalse, ltrue);
                break;
            case Tag.RELOP: {
                Word tmp = (Word) look;

                match(look.tag); 
                expr();
                expr();
                
                // Nota:
                if      (tmp == Word.lt) code.emit(OpCode.if_icmplt, ltrue);
                else if (tmp == Word.gt) code.emit(OpCode.if_icmpgt, ltrue);
                else if (tmp == Word.eq) code.emit(OpCode.if_icmpeq, ltrue);
                else if (tmp == Word.le) code.emit(OpCode.if_icmple, ltrue);
                else if (tmp == Word.ne) code.emit(OpCode.if_icmpne, ltrue);
                else if (tmp == Word.ge) code.emit(OpCode.if_icmpge, ltrue);
                code.emit(OpCode.GOto, lfalse);

                break;
            }
            case Tag.TTRUE:
            case Tag.TFALSE: {
                int ljump = 
                    look.tag == Tag.TTRUE ? 
                    ltrue                 : 
                    lfalse;

                code.emit(OpCode.GOto, ljump);
                match(look.tag);
                break;
            }
            default: 
                error("Unexpected token in bexpr");
        }
    }

    private void expr() {
        switch(look.tag) {
            case '+':  
            case '*': { 
                OpCode op = (
                    (char) look.tag == '+' ? 
                    OpCode.iadd            : 
                    OpCode.imul
                );

                match(look.tag);
                match(Token.lpt.tag);       // '('
                exprlist(Contex.EXPR, op);
                match(Token.rpt.tag);       // ')'
                break;
            }
            case '-':               // Token.minus.tag
            case '/': {             // Token.div.tag    
                OpCode op = (
                    (char)look.tag == '-' ? 
                    OpCode.isub        : 
                    OpCode.idiv
                );

                match(look.tag); 
                expr();
                expr();
                code.emit(op); 
                break;
            }
            case Tag.NUM: {
                NumberToken tmp = (NumberToken) look;
                code.emit(OpCode.ldc, tmp.lexeme);

                match(look.tag);
                break;
            }
            case Tag.ID: {
                Word tmp = (Word) look;
                int id_address = st.lookupAddress(tmp.lexeme);
                if (id_address == -1)
                    error("Symbol not found"); 
                code.emit(OpCode.iload, id_address);

                match(look.tag);
                break;
            }
            default: 
                error("Unexpected token in expr");
        }
    }
    
    private void exprlist(Contex contex, OpCode op) {    
        switch(look.tag) {
            case '+':       // Token.plus.tag
            case '*':       // Token.mult.tag
            case '-':       // Token.minus.tag
            case '/':       // Token.div.tag
            case Tag.NUM:
            case Tag.ID:               
                expr();
                if (contex == Contex.PRINT) 
                    code.emit(OpCode.invokestatic, 1);
                
                exprlistp(contex, op);         
                break;
            default: 
                error("Unexpected token in exprlist");
        }
    }

    private void exprlistp(Contex contex, OpCode op) {
        switch(look.tag) {
            case ',':               // Token.comma.tag
                match(look.tag);
                expr();

                if      (contex == Contex.PRINT) code.emit(OpCode.invokestatic, 1);
                else if (contex == Contex.EXPR)  code.emit(op);
                    
                exprlistp(contex, op);         
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
            Parser Evaluator = new ExamLanguageEvaluator(l);   
            Evaluator.parse();

            input.close();
        } catch (IOException e) {
            System.out.println(e);
            // e.printStackTrace();
        }
    }
}
