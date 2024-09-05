import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import templetes.*;

// To start
// cd src; javac -d ../build CAP1.java; java -cp ../build CAP1

// Vecchio file: https://informatica.i-learn.unito.it/pluginfile.php/346322/mod_resource/content/2/LFT_Lab2223L1.pdf
// Nuovo file:   https://informatica.i-learn.unito.it/pluginfile.php/385502/mod_resource/content/2/LFT_Lab2324L1.pdf

public class CAP1 {

    /* Esercizio: 1.2
    // Testo:  Progettare e implementare un DFA che riconosca il linguaggio degli identificatori in un linguaggio 
    //         in stile Java: un identificatore e una sequenza non vuota di lettere, numeri, ed il ` simbolo di _
    //         (underscore) che non comincia con un numero e che non puo essere composto solo ` dal simbolo _.
    //
    //         Compilare e testare il suo funzionamento su un insieme significativo di esempi.
    //              Es accettate:     "x", "flag1", "x2y2", "x_1", "lft_lab", "_temp", "x_1_y_2", "x__", "__5"
    //              Es non accettate: "5", "221B", "123", "9 to 5", " "
    //
    // Risoluzione grafo: il grafo su carta.
    // Risoluzione tabella: 
    //      Stato   Input   Next
    //    ->q0      =num    q3
    //    ->q0      !=num   q1
    //      q1      all     q2*
    //      q2*     all     q2*
    //      q3      all     q3 
    */
    private static boolean javaIDSDFA(String input) {
        Predicate<Character> inAlphabet = ch ->
            Character.isLetter(ch) || 
            Character.isDigit(ch)  || 
            ch == '_';

        // Main loop to read each char
        int state = 0;
        int index = 0;
        while (state >= 0 && index < input.length()) {
            final char ch = input.charAt(index++);

            // Check char in DFA alphabet (Σ)
            if (!inAlphabet.test(ch)) 
                state = -1;

            // Transiction function (δ)
            switch (state) {
                case 0: {
                    if (Character.isDigit(ch)) state = 3;
                    else                       state = 2;
                    break;
                }
                case 1: {
                    if (Character.isLetter(ch) || ch == '_') state = 2;
                    break;
                }
                case 2: {
                    state = 2;
                    break;
                }
                case 3: {
                    state = 3;
                    break;
                }
            }
        }

        // If last DFA state in set of accept state (F) return true 
        return state == 2;
    }
    private static boolean javaIDSDFATest() {
        String[] accepted  = { "x", "flag1", "x2y2", "x_1", "lft_lab", "_temp", "x_1_y_2", "x__", "__5" };
        String[] naccepted = { "5", "221B", "123", "9_to_5", " " };

        boolean a = Arrays
            .stream(accepted)
            .map(CAP1::javaIDSDFA)
            .reduce(true, (acc, val) -> acc && val);    // all true

        boolean b = Arrays
            .stream(naccepted)
            .map(CAP1::javaIDSDFA)
            .reduce(true, (acc, val) -> acc && !val);   // all false

        return a && b;
    }

    /* Esercizio: 1.3
    // Testo:  Progettare e implementare un DFA che riconosca il linguaggio di stringhe che contengono
    //         un numero di matricola seguito (subito) da un cognome, dove la combinazione di matricola e cognome 
    //         corrisponde a studenti del turno 2 o del turno 3 del laboratorio di Linguaggi Formali e Traduttori.
    //
    //         Si ricorda le regole per suddivisione di studenti in turni:
    //          • Turno T1: cognomi la cui iniziale e compresa tra A e K, e il numero di matricola ` e dispari; `
    //          • Turno T2: cognomi la cui iniziale e compresa tra A e K, e il numero di matricola ` e pari; `
    //          • Turno T3: cognomi la cui iniziale e compresa tra L e Z, e il numero di matricola ` e dispari; `
    //          • Turno T4: cognomi la cui iniziale e compresa tra L e Z, e il numero di matricola ` e pari. `
    // 
    //         Esempi:
    //              accettate:     "123456Bianchi", "654321Rossi", "2Bianchi", "122B"
    //              non accettate: "654321Bianchi", "123456Rossi", "654322", "Rossi"
    //          
    //         Precisazioni:
    //              Nel contesto di questo esercizio, un numero di matricola non ha un numero prestabilito di cifre 
    //              (ma deve essere composto di almeno una cifra).
    //              Un cognome corrisponde a una sequenza di lettere, e deve essere composto di almeno una lettera.
    //
    // Risoluzione grafo: il grafo su carta.
    // Risoluzione tabella: 
    //      Stato   Input   Next
    //    ->q0      disp    q1
    //    ->q0      pari    q2
    //    ->q0      lett    q4'
    //      ------------------
    //      q1      pari    q2
    //      q1      disp    q1
    //      q1      {A-K}   q3*
    //      q1      !{A-K}  q4'
    //      ------------------
    //      q2      pari    q2
    //      q2      disp    q1
    //      q2      {L-Z}   q3*
    //      q2      !{L-Z}  q4'
    //      ------------------
    //      q3*     lett   q3*
    //      q3*     num    q4'
    //      ------------------
    //      q4'     all    q4'
    // 
    // Input:
    //      pari = {0, 2, 4, 6, 8} 
    //      disp = {1, 3, 5, 7, 9}
    //      num  = {pari, disp}
    //      lett = {tutte lettere min e maiusc}
    // 
    // Promemoria: Ero arrivato alla soluzione due ore fa ma avevo scasmbiato il test della lettera della matricola di q1 e q2, 
    //             questo è chiaramnte colpa del persimo design di codice (imperativo) e della mia DSA (è non vuole dire data structures and algorithms)
    //             in realta il codice degli automi non dovrebbe essere scitto da umani ma generalizato con input tabella di stati e tranzizioni
    //             cosi da allegerire il compito del programmatore, inoltr il codice di DFA e altri automi penso che sia sempre uguale.
    //             Pero non so se i prof possano accetare una cosa del genere, e soprattutto non saprei come farlo in java (lettura da file, matrice, insiemi ecc)
    */
    private static boolean isInRange(char ch, char start, char end) {
        return ch >= start && ch <= end;
    }
    private static boolean badgeNumberDFA(String input) {
        Predicate<Character> inAlphabet = ch ->
            Character.isLetter(ch) || Character.isDigit(ch);

        // Main loop to read each char
        int state = 0;
        int index = 0;
        while (state >= 0 && index < input.length()) {
            final char ch = input.charAt(index++);

            // Check char in DFA alphabet (Σ)
            if (!inAlphabet.test(ch)) 
                state = -1;

            // Transiction function (δ)
            switch (state) {
                case 0: {
                    if      (Character.isLetter(ch)) state = 4;
                    else if ((ch - '0') % 2 == 0)    state = 2;
                    else                             state = 1;
                    break;
                }
                case 1: {
                    if (Character.isLetter(ch)) {
                        
                        if (isInRange(ch, 'L', 'Z')) state = 3;
                        else                         state = 4; 
                    }
                    else if ((ch - '0') % 2 == 0)  state = 2;
                    else                           state = 1;
                    break;
                }
                case 2: {
                    if (Character.isLetter(ch)) {
                        if (isInRange(ch, 'A', 'K'))  state = 3;
                        else                         state = 4; 
                    }
                    else if ((ch - '0') % 2 == 0)  state = 2;
                    else                           state = 1;
                    break;
                }
                case 3: {
                    if (Character.isLetter(ch)) state = 3;
                    else                        state = 4; 
                    break;
                }
                case 4: {
                    state = 4;
                    break;
                }
            }
        }

        // If last DFA state in set of accept state (F) return true 
        return state == 3;
    }
    private static boolean badgeNumberDFATest() {
        String[] accepted  = { "123456Bianchi", "654321Rossi", "2Bianchi", "122B" };
        String[] naccepted = { "654321Bianchi", "123456Rossi", "654322", "Rossi" };

        boolean a = Arrays
            .stream(accepted)
            .map(CAP1::badgeNumberDFA)
            .reduce(true, (acc, val) -> acc && val);    // all true);

        boolean b = Arrays
            .stream(naccepted)
            .map(CAP1::badgeNumberDFA)
            .reduce(true, (acc, val) -> acc && !val);   // all false

        return a && b;
    }

    // Esercizio: 1.4
    // Testo: Progettare e implementare un DFA che riconosca il linguaggio delle costanti nu-
    //        meriche in virgola mobile utilizzando la notazione scientifica dove il simbolo e indica la funzio-
    //        ne esponenziale con base 10. L’alfabeto del DFA contiene i seguenti elementi: le cifre numeriche
    //        0, 1, . . . , 9, il segno . (punto) che precede una eventuale parte decimale, i segni + (pi `u) e - (meno)
    //        per indicare positivit `a o negativit `a, e il simbolo e.
    //        Le stringhe accettate devono seguire le solite regole per la scrittura delle costanti numeriche.
    //        In particolare, una costante numerica consiste di due segmenti, il secondo dei quali `e opzionale:
    //        il primo segmento `e una sequenza di cifre numeriche che (1) pu `o essere preceduta da un segno
    //        + o meno -, (2) pu `o essere seguita da un segno punto ., che a sua volta deve essere seguito da
    //        una sequenza non vuota di cifre numeriche; il secondo segmento inizia con il simbolo e, che a
    //        sua volta `e seguito da una sequenza di cifre numeriche che soddisfa i punti (1) e (2) scritti per il
    //        primo segmento. Si nota che, sia nel primo segmento, sia in un eventuale secondo segmento, un
    //        segno punto . non deve essere preceduto per forza da una cifra numerica
    // 
    // Esempi di stringhe accettate:     "123", "123.5", ".567", "+7.5", "-.7", "67e10", "1e-2","-.7e2", "1e2.3"
    // Esempi di stringhe non accettate: ".", "e3", "123.", "+e6", "1.2.3", "4e5e6", "++3"
    //
    // Risoluzione grafo: il grafo su carta.
    private static DFA<String> floatPointNumbersDFA() {
        Set<String> Q = new HashSet<>(Arrays.asList("q0", "q1", "q2", "q3", "q4", "q5", "r0", "r1", "r2", "r3", "r4", "r5"));
        Set<String> F = new HashSet<>(Arrays.asList("q2", "q4", "r2", "r4"));

        List<Predicate<Character>> alphabet = Arrays.asList(
            Character::isDigit,
            c -> c == '.' || c == 'e' || c == '+' || c == '-'
        );

        Map<Entry<String, Predicate<Character>>, String> tf = new HashMap<>();
        tf.put(Map.entry("q0", c -> c == '+' || c == '-'), "q1");
        tf.put(Map.entry("q0", Character::isDigit), "q2");
        tf.put(Map.entry("q0", c -> c == '.'), "q3");
        tf.put(Map.entry("q0", c -> c == 'e'), "q5");
        
        tf.put(Map.entry("q1", Character::isDigit), "q2");
        tf.put(Map.entry("q1", c -> c == '.'), "q3");
        tf.put(Map.entry("q1", c -> c == 'e' || c == '+' || c == '-'), "q5");

        tf.put(Map.entry("q2", c -> c == '.'), "q3");
        tf.put(Map.entry("q2", c -> c == 'e'), "r0");
        tf.put(Map.entry("q2", Character::isDigit), "q2");
        tf.put(Map.entry("q2", c -> c != '.' && c != 'e' && !Character.isDigit(c)), "q5");

        tf.put(Map.entry("q3", Character::isDigit), "q4");
        tf.put(Map.entry("q3", c -> !Character.isDigit(c)), "q5");

        tf.put(Map.entry("q4", Character::isDigit), "q4");
        tf.put(Map.entry("q4", c -> c == 'e'), "r0");
        
        tf.put(Map.entry("q4", c -> c != 'e' && !Character.isDigit(c)), "q5");


        // Nota: Questa è la stessa stuttura di sopra che solo se incontra un ulteriore e non accetta
        //       come soluzione è veramente brutta, se avessi implementato un PDA deterministico avrei
        //       fatto push nella pila di e ed il comportamento delle transizioni sarebbe cambiato.
        tf.put(Map.entry("r0", c -> c == '+' || c == '-'), "r1");
        tf.put(Map.entry("r0", Character::isDigit), "r2");
        tf.put(Map.entry("r0", c -> c == '.'), "r3");
        tf.put(Map.entry("r0", c -> c == 'e'), "q5");
        
        tf.put(Map.entry("r1", Character::isDigit), "r2");
        tf.put(Map.entry("r1", c -> c == '.'), "r3");
        tf.put(Map.entry("r1", c -> c == 'e' || c == '+' || c == '-'), "q5");

        tf.put(Map.entry("r2", c -> c == '.'), "r3");
        tf.put(Map.entry("r2", Character::isDigit), "r2");
        tf.put(Map.entry("r2", c -> c != '.' &&  !Character.isDigit(c)), "q5");

        tf.put(Map.entry("r3", Character::isDigit), "r4");
        tf.put(Map.entry("r3", c -> !Character.isDigit(c)), "q5");

        tf.put(Map.entry("r4", Character::isDigit), "r4");
        tf.put(Map.entry("r4", c -> !Character.isDigit(c)), "q5");

        return new DFA<>(Q, F, "q0", alphabet, tf);
    }
    private static boolean floatPointNumbersDFATest() { 
        String[] accepted  = { "123", "123.5", ".567", "+7.5", "-.7", "67e10", "1e-2","-.7e2", "1e2.3" };
        String[] naccepted = { ".", "e3", "123.", "+e6", "1.2.3", "4e5e6", "++3" };
        
        DFA<String> dfa = floatPointNumbersDFA();
        boolean a = Arrays
            .stream(accepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && val);    // all true

        boolean b = Arrays
            .stream(naccepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && !val);   // all false

        return true;
    }

    // Esercizio: 1.5  
    // Testo: Progettare e implementare un DFA con alfabeto {/, *, a}che riconosca il linguag-
    //        gio di "commenti" delimitati da /* (all’inizio) e */ (alla fine): cio`e l’automa deve accettare le
    //        stringhe che contengono almeno 4 // aratteri che iniziano con /*, che finiscono con */, e che con-
    //        tengono una sola occorrenza della sequenza */, quella finale (dove l’asterisco della sequenza */
    //        non deve essere in comune con quello della sequenza /* all’inizio).
    // 
    // Esempi di stringhe accettate:     "/****/", "/*a*a*/", "/*a/**/", "/**a///a/a**/", "/**/", "/*/*/""
    // Esempi di stringhe non accettate: "/*/", "/**/***/"
    //
    // Risoluzione grafo: il grafo su carta.
    // Risoluzione tabella: 
    //      Stato   Input   Next
    //    ->q0      /       q1
    //    ->q0      rest    q5
    //      ------------------
    //      q1      *      q2
    //      q1      rest   q5
    //      ------------------
    //      q2      *      q3
    //      q2      rest   q2
    //      ------------------
    //      q3      a       q2
    //      q3      /       q4*
    //      q3      *       q3
    //      ------------------ 
    //      q4*     all     q5
    //      ------------------
    //      q5      all     q5
    //      ------------------  
    private static DFA<Integer> javaCommentsDFA() {
        Set<Integer> Q = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        Set<Integer> F = new HashSet<>(Arrays.asList(4));

        List<Predicate<Character>> alphabet = Arrays.asList(
            c -> c == '/' || c == '*' || c == 'a'
        );

        Map<Entry<Integer, Predicate<Character>>, Integer> tf = new HashMap<>();
        tf.put(Map.entry(0, c -> c == '/'), 1);
        tf.put(Map.entry(0, c -> c != '/'), 5);

        tf.put(Map.entry(1, c -> c == '*'),  2);
        tf.put(Map.entry(1, c -> c != '*' ), 5);

        tf.put(Map.entry(2, c -> c == '*'), 3);
        tf.put(Map.entry(2, c -> c != '*'), 2);

        tf.put(Map.entry(3, c -> c == 'a'), 2);
        tf.put(Map.entry(3, c -> c == '/'), 4);
        tf.put(Map.entry(3, c -> c == '*'), 3);

        tf.put(Map.entry(4, c -> true), 5);
        tf.put(Map.entry(5, c -> true), 5);

        return new DFA<>(Q, F, 0, alphabet, tf);
    }
    private static boolean javaCommentsDFATest() {
        String[] accepted  = { "/****/", "/*a*a*/", "/*a/**/", "/**a///a/a**/", "/**/", "/*/*/" };
        String[] naccepted = { "/*/", "/**/***/" };
        
        DFA<Integer> dfa = javaCommentsDFA();
        boolean a = Arrays
            .stream(accepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && val);    // all true

        boolean b = Arrays
            .stream(naccepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && !val);   // all false

        return a && b;
    }

    // Esercizio: 1.6 
    // Testo: Modificare l’automa dell’esercizio precedente in modo che riconosca il linguaggio
    //        di stringhe (sull’alfabeto {/, *, a}) che contengono "commenti" delimitati da /* e */, ma con
    //        la possibilit `a di avere stringhe prima e dopo come specificato qui di seguito. L’idea `e che sia
    //        possibile avere eventualmente commenti (anche multipli) immersi in una sequenza di simboli
    //        dell’alfabeto. Quindi l’unico vincolo `e che l’automa deve accettare le stringhe in cui un’occorren-
    //        za della sequenza /* deve essere seguita (anche non immediatamente) da un’occorrenza della
    //        sequenza */. Le stringhe del linguaggio possono non avere nessuna occorrenza della sequenza
    //        /* (caso della sequenza di simboli senza commenti). Implementare l’automa seguendo la costru-
    //        zione vista in Listing 1.
    // 
    // Esempi di stringhe accettate:     "aaa/****/aa", "aa/*a*a*/", "aaaa", "/****/", "/*aa*/", "*/a", "a/**/***a", "a/**/***/a", "a/**/aa/***/a
    // Esempi di stringhe non accettate: "aaa/*/aa", "a/**//***a", "aa/*aa"
    //
    // Risoluzione grafo: il grafo su carta.
    // Risoluzione tabella: 
    //      Stato   Input   Next
    //    ->q0*     /       q1
    //    ->q0*     rest    q0*
    //      ------------------
    //      q1*     *       q2
    //      q1*     rest    q0
    //      ------------------
    //      q2      *       q3
    //      q2      rest    q2
    //      ------------------
    //      q3      a       q2
    //      q3      /       q4*
    //      q3      *       q3
    //      ------------------ 
    //      q4*     /       q1*
    //      q4*     rest    q4*
    //      ------------------
    private static DFA<Integer> javaComments2DFA() {
        Set<Integer> Q = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        Set<Integer> F = new HashSet<>(Arrays.asList(0, 1, 4));

        List<Predicate<Character>> alphabet = Arrays.asList(
            c -> c == '/' || c == '*' || c == 'a'
        );

        Map<Entry<Integer, Predicate<Character>>, Integer> tf = new HashMap<>();
        tf.put(Map.entry(0, c -> c != '/'), 0);
        tf.put(Map.entry(0, c -> c == '/'), 1);
        
        tf.put(Map.entry(1, c -> c != '*'), 0);
        tf.put(Map.entry(1, c -> c == '*'), 2);
        
        tf.put(Map.entry(2, c -> c != '*'), 2);
        tf.put(Map.entry(2, c -> c == '*'), 3);
       
        tf.put(Map.entry(3, c -> c == 'a'), 2);
        tf.put(Map.entry(3, c -> c == '*'), 3);
        tf.put(Map.entry(3, c -> c == '/'), 4);

        tf.put(Map.entry(4, c -> c != '/'), 4);
        tf.put(Map.entry(4, c -> c == '/'), 1);
        
        return new DFA<>(Q, F, 0, alphabet, tf);
    }
    private static boolean javaComments2DFATest() {
        String[] accepted  = { "aaa/****/aa", "aa/*a*a*/", "aaaa", "/****/", "/*aa*/", "*/a", "a/**/***a", "a/**/***/a", "a/**/aa/***/a" };
        String[] naccepted = { "aaa/*/aa", "a/**//***a", "aa/*aa" };
        
        DFA<Integer> dfa = javaComments2DFA();
        boolean a = Arrays
            .stream(accepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && val);    // all true

        boolean b = Arrays
            .stream(naccepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && !val);   // all false

        return a && b;
    }

    /* Esercizio: 1.6  [Scorso anno ho scambiato i pdf, non guardare]
    // Testo: Progettare e implementare un DFA con alfabeto {a, b} che riconosca il linguaggio
    // delle stringhe tali che a occorre almeno una volta in una delle ultime tre posizioni della stringa.
    // Il DFA deve accettare anche stringhe che contengono meno di tre simboli (ma almeno uno dei
    // simboli deve essere a).
    // 
    // Esempi di stringhe accettate: "abb", "bbaba", "baaaaaaa", "aaaaaaa", "a", "ba", "bba", "aa", "bbbababab"
    // Esempi di stringhe non accettate: "abbbbbb", "bbabbbbbbbb", "b"
    //
    // Risoluzione grafo: il grafo su carta.
    // Risoluzione tabella: 
    //      Stato   Input   Next
    //    ->q0      a       q1*
    //    ->q0      b       q0
    //      ------------------
    //      q1*     a|b     q2*
    //      ------------------
    //      q2*     a|b     q3*
    //      ------------------
    //      q3*     a       q1*
    //      q3*     b       q0
    //      ------------------ 
    */
    private static DFA<Integer> last3aDFA() {
        Set<Integer> Q = new HashSet<>(Arrays.asList(0, 1, 2, 3));
        Set<Integer> F = new HashSet<>(Arrays.asList(1, 2, 3));

        List<Predicate<Character>> alphabet = Arrays.asList(
            c -> c == 'a' || c == 'b'
        );

        Map<Entry<Integer, Predicate<Character>>, Integer> tf = new HashMap<>();
        tf.put(Map.entry(0, c -> c == 'b'), 0);
        tf.put(Map.entry(0, c -> c == 'a'), 1);
 
        tf.put(Map.entry(1, c -> c == 'a' || c == 'b'), 2);
        tf.put(Map.entry(2, c -> c == 'a' || c == 'b'), 3);
        tf.put(Map.entry(3, c -> c == 'a'), 1);
        tf.put(Map.entry(3, c -> c == 'b'), 0);

        return new DFA<>(Q, F, 0, alphabet, tf);
    }
    private static boolean last3aDFATest() {
        String[] accepted  = { "abb", "bbaba", "baaaaaaa", "aaaaaaa", "a", "ba", "bba", "aa", "bbbababab" };
        String[] naccepted = { "abbbbbb", "bbabbbbbbbb", "b" };

        DFA<Integer> dfa = last3aDFA();
        boolean a = Arrays
            .stream(accepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && val);    // all true

        boolean b = Arrays
            .stream(naccepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && !val);   // all false

        return a && b;
    }

    public static void main(String[] args) {
        Boolean[] test = {
            javaIDSDFATest(),
            badgeNumberDFATest(),
            // last3aDFATest(),
            floatPointNumbersDFATest(),
            javaCommentsDFATest(),
            javaComments2DFATest(),
        };

        Arrays
            .stream(test)
            .forEach(System.out::println);
    }
}
