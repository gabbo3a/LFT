package lexer;

import java.io.*; 
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;
import constructs.*;
import templetes.*;

/* To start
// 
// Compilare: javac -d ../build lexer/Lexer.java
// Eseguire : java -cp ../build lexer.Lexer
//
// Nota:      Per utilizzare i package in java stavo quasi per piangere, 
//            questo è piu per me che come documentazione vera :( .*/

/* Explanation: Questa è la mia implementazioen del lexer per LFT, ho modificato molto il codice 
 *              di partenza dato in laboratorio poiche il codice era troppo legato al dati del problema.
 *              La struttura è la soluzione adottata dovrebbe essere abbastanza chiara e spiegata bene 
 *              localmente nei pezzi di codice. In linea di principio la classe è divisa in dati e metodi
 *              ovvimente i dati potrebbere diventare dei paramentri il che aumenta durevolezza del codice.
 *              Inoltre viene utilizzata una classe DFA implementata in precedenza che implementa gli automi
 *              deterministici. Utilizzo della classe DFA non sarebbe necessario poiche si potrebbero anche 
 *              utilizzare le REGEX di fatto compatibili ed equivalenti come approcci. Infatti per i token/word
 *              che descivono una keyword ho utilizzato il wrap di una equals in una lambda, che tramite 
 *              interfaccia funzoinale FA è un approcio equavalente.
*/

public class Lexer {
    List<Character> separators = Arrays.asList(' ', '\t', '\n', '\r');        // Separator characters
    private BufferedReader input;                                             // Object points to file
    private int line = 1;                                                     // Count of lines (Era static ma non ha senso)
    private char peek = ' ';                                                  // Last char readed (peeking is caching)
    
    private record TokenAndPatternType(Token token, FA pattern) {};
    // Dont keyword
    private static final TokenAndPatternType[] tokensWithoutAttribute = {           
        new TokenAndPatternType(Token.lpt,       i -> i.equals("(")),
        new TokenAndPatternType(Token.rpt,       i -> i.equals(")")), 
        new TokenAndPatternType(Token.lpq,       i -> i.equals("[")),
        new TokenAndPatternType(Token.rpq,       i -> i.equals("]")),
        new TokenAndPatternType(Token.lpg,       i -> i.equals("{")),
        new TokenAndPatternType(Token.rpg,       i -> i.equals("}")),
        new TokenAndPatternType(Token.plus,      i -> i.equals("+")),
        new TokenAndPatternType(Token.minus,     i -> i.equals("-")),
        new TokenAndPatternType(Token.mult,      i -> i.equals("*")),
        new TokenAndPatternType(Token.div,       i -> i.equals("/")),
        new TokenAndPatternType(Token.semicolon, i -> i.equals(";")),
        new TokenAndPatternType(Token.comma,     i -> i.equals(",")),
        new TokenAndPatternType(Token.not,       i -> i.equals("!"))
    };
    // Keyword
    private static final TokenAndPatternType[] tokensWithAttribute = {              
        new TokenAndPatternType(Word.assign,   i -> i.equals("assign")),
        new TokenAndPatternType(Word.to,       i -> i.equals("to")),
        new TokenAndPatternType(Word.iftok,    i -> i.equals("if")),
        new TokenAndPatternType(Word.elsetok,  i -> i.equals("else")),
        new TokenAndPatternType(Word.dotok,    i -> i.equals("do")),
        new TokenAndPatternType(Word.fortok,   i -> i.equals("for")),
        new TokenAndPatternType(Word.begin,    i -> i.equals("begin")),
        new TokenAndPatternType(Word.end,      i -> i.equals("end")),
        new TokenAndPatternType(Word.print,    i -> i.equals("print")),
        new TokenAndPatternType(Word.read,     i -> i.equals("read")),
        new TokenAndPatternType(Word.truetok,  i -> i.equals("true")),
        new TokenAndPatternType(Word.falsetok, i -> i.equals("false")),
        new TokenAndPatternType(Word.init,     i -> i.equals(":=")),
        new TokenAndPatternType(Word.or,       i -> i.equals("||")),
        new TokenAndPatternType(Word.and,      i -> i.equals("&&")),
        new TokenAndPatternType(Word.lt,       i -> i.equals("<")),
        new TokenAndPatternType(Word.gt,       i -> i.equals(">")),
        new TokenAndPatternType(Word.eq,       i -> i.equals("==")),
        new TokenAndPatternType(Word.le,       i -> i.equals("<=")),
        new TokenAndPatternType(Word.ne,       i -> i.equals("<>")),
        new TokenAndPatternType(Word.ge,       i -> i.equals(">=")),
    };

    // Merge between tokensWithoutAttribute and tokensWithAttribute
    private static final TokenAndPatternType[] tokens = 
        Stream.concat(
            Arrays.stream(tokensWithoutAttribute), 
            Arrays.stream(tokensWithAttribute)
        ).toArray(TokenAndPatternType[]::new);
    
    // Builder
    public Lexer(BufferedReader input) {
        this.input = input;
    }

    public int getLine() { 
        return line; 
    }

    // Read next char and store on peek
    private void read() {
        try { 
            peek = (char) input.read(); // Read and store
            // System.out.println("peek: <" + peek + ">");
        } catch (IOException exc) {
            peek = (char) -1;           // Assign -1 to report error
        }
    }

    // Methods to skip comments

    // Read to consume a multi line comment
    private void skipMultilineComment() {
        // Search loop
        while (true) {
            
            // Check if comment is not close
            if (peek == (char) -1) 
                throw new RuntimeException("Multiline comment is not close on line " + line);

            // Check if comment is close
            if (peek == '*') {
                read();
                if (peek == '/') {
                    read();
                    break;
                }
            }

            // Next peek
            read(); 
        }
    }
    
    // Read to consume a singol line comment
    private void skipSingleLineComment() {
        while (peek != '\n' && peek != (char) -1) 
            read();
    }

    // DFA for language (Help: non so dove è meglio metterli :( )

    // Build identifier DFA
    private FA getIDSDFA() {
        Set<Integer> Q = new HashSet<>(Arrays.asList(0, 1, 2, 3));
        Set<Integer> F = new HashSet<>(Arrays.asList(2));
        List<Predicate<Character>> alphabet = Arrays.asList(
            Character::isDigit, 
            Character::isLetter, 
            c -> c == '_'
        );

        // Creazione delle entry e costruzione della mappa
        Map<Entry<Integer, Predicate<Character>>, Integer> tf = new HashMap<>();
         // Metter map of
        tf.put(Map.entry(0, Character::isDigit), 3);
        tf.put(Map.entry(0, Character::isLetter), 2);
        tf.put(Map.entry(0, c -> c == '_'), 1);
        tf.put(Map.entry(1, c -> c != '_'), 2);
        tf.put(Map.entry(2, c -> true), 2);
        tf.put(Map.entry(3, c -> true), 3);

        return new DFA<>(Q, F, 0, alphabet, tf);
    }

    // Build integer number DFA
    private static FA getIntegerNumberDFA() {
        Set<Integer> Q = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4));
        Set<Integer> F = new HashSet<>(Arrays.asList(1, 2));
        List<Predicate<Character>> alphabet = Arrays.asList(
            Character::isDigit
        );

        Map<Entry<Integer, Predicate<Character>>, Integer> tf = new HashMap<>();
        // Metter map of
        tf.put(Map.entry(0, c -> c == '0'), 1);
        tf.put(Map.entry(0, c -> c > '0' && c <= '9'), 2); 
        tf.put(Map.entry(1, c -> true), 4);
        tf.put(Map.entry(2, c -> true), 2);

        return new DFA<>(Q, F, 0, alphabet, tf);
    }

    // Read possibile token and return corrispondet token if correct
    private Token recognize(String token) {
        FA IDS_DFA = getIDSDFA();
        FA INT_DFA = getIntegerNumberDFA();
        
        // Use a DFA set/array to check if it is a valid string (lexeme),
        // if a string is not valid return null token. 
        for (TokenAndPatternType tuple : tokens) {
            if (tuple.pattern.scan(token))
                return tuple.token;
        }

        // Check id and number
            // Nota: aggiunta di containsOnlyDigits, isZeroFillNumber fatto nel panico mentre facevo il parser
            //       (prima era presente solo hasStartWithZero) quindi per me tutto cio che inizia con uno zero
            //       era un numero zerofill :( (mi merito un po la bocciatura).
        Predicate<String> hasStartWithZero   = t -> !t.isEmpty() && t.charAt(0) == '0' && t.length() > 1;
        Predicate<String> containsOnlyDigits = t -> t.chars().allMatch(Character::isDigit);
        Predicate<String> isZeroFillNumber   = t -> hasStartWithZero.test(t) && containsOnlyDigits.test(t);
        if      (IDS_DFA.scan(token))           return new Word(Tag.ID, token);
        else if (INT_DFA.scan(token))           return new NumberToken(Integer.parseInt(token));
        else if (isZeroFillNumber.test(token))  throw new RuntimeException("Zero fill not supported on line " + line);
        else                                    return null;
    }

    // Reads character by character and greedy searches for the largest possible token
    public Token scan() {
        String token = "";          // Token buffer to recognized
        Token before = null;        // Caching last recognized
        
        // Separators skip loop
        while (separators.contains(peek) || peek == '/') {
            while (separators.contains(peek)) {
                if (peek == '\n') line++;   // Add line
                read();
            }

            // Comments management (skips)
            if (peek == '/') {
                read();
                if      (peek == '/') skipSingleLineComment(); // Check singol line
                else if (peek == '*') skipMultilineComment (); // Check multi line
                else                  return recognize("/");  // If it doesn't evaluate
            }
        }
        
        // Tokenazer loop
        while (true) {
            // Separator chars and EOF management 
            if (separators.contains(peek) || peek == (char) Tag.EOF) {
                before = recognize(token);  // Set last token recognized
                if (peek == '\n') line++;   // Add line

                break;
            }

            // Other chars management 
            token += peek;                      // Build a possibile token
            Token after = recognize(token);     // Save token to implements heuristics
            
            /*  Nota: questa soluzione da per scontato che i token a carttere singolo hannno priorita sugli altri
            //        è che non vengo utilizzati in nessun altro costrutto del linguaggio (identificatori, numeri, altre word).
            //        Cosi facendo se una sequaza di char inizia con un token a sigolo carattere verra preso e tokenizzato,
            //        invece se una sequenza di char a multiplo carattere esce fuori improvisamente dal linguaggio (else -> else*,
            //        oppure name*) significa che bisogna restituire il token antecedente al char di "troppo". 
            //        Se le premesse non vengo rispettante il codice non funziona piu, ma per stessa conferma della prof questo dovrebbe
            //        essere un requisito di buon design dell'inguaggio. In alternativa servirebbero altri vincoli come garanrire i 
            //        separatori.*/

            /* Nota: per renmderlo piu carino si potrebbe fare una API intermendia per Word e NumberToken, 
            //       per far si che ogni tipo di costrutto con atributo non debba essere enumeriato nella exp
            //       per due non è necessario ma se ne trovo un altra ti ordino di farlo me del futuro,
            //       lo stesso si applica sopra.*/
            Predicate<Token> isTokenWithAttribute = t -> (t instanceof Word || t instanceof NumberToken);

            // If increment a word goes outside the language then return
            if (after == null && isTokenWithAttribute.test(before)) 
                return before;

            // Find a sequance to start with dont keyword token
            boolean isAfterTokenWithoutAttribute = (
                after instanceof Token  && 
                !isTokenWithAttribute.test(after)
            );
            if (before == null && isAfterTokenWithoutAttribute) {
                read();       // Point to the next to analyze
                return after;
            }   
            
            // Turn of before, next char
            before = after;
            read();
        }
        
        return !token.isEmpty() ? before : new Token(Tag.EOF);
    }
    
    public static void main(String[] args) {
        try {
            String path = "../test/Input.lft";
            BufferedReader input = new BufferedReader(new FileReader(path));
            Lexer l = new Lexer(input);

            Token token;
            do {
                token = l.scan();
                System.out.println(token);
            } while (token != null && token.tag != Tag.EOF);

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}