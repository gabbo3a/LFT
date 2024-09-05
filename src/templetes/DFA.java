package templetes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Spiegazione: Progettare DFA è molto divertente, ma doverli implementare in java è molto tedioso e ripetitivo.
//              Ovviamente la implementazione in java o un qualsiasi linguaggi di programmazione nonostante sia noiasa
//              come pratica rimane fondamentale per verificare il corretto funzionamento del modello e per l'utilizzo 
//              effetivo. Percio l'idea iniziale erta quella di programmare un classe/modulo DFA che tramite dei parametri 
//              descrittivi del modello (tabella di tranzizione in DS) crei effetivo codice java. I vantaggi di un aproccio
//              del genere sono molteplici dalla legibilita alla riutilizzabilita del codice implementato.
//
// Struttura:   Un DFA come visto in teoria non è altro di un modello matematico che descrive il calcolo piu o meno specifio,
//              il principo base è l'interazione tra stati del automa ogni stato puo transizionare in altro dato un certo input
//              appartenete al alfabeto e qualora alla fine del "input" (singolo o esteso) si trovasse in uno stato finale allora
//              input sarebber effetivamente accetato.
//
// Struttura formale: Linguaggio matematico un DFA è espresso dalla 5-tupla, (Q, Σ, ∂: QxΣ->Q, qs∈Q, F⊆Q) il cui significato porta 
//                    alla completa determinazione del DFA. In ordine inseme degli stati, alfabeto del input, funzione di transizione,
//                    (puo essere anche estesa a stringhe in modo intuitivo) stato iniziale, insieme degli stati finali.
//
// Struttura codice: Il codice è molto intuitiva, è sato impelmentato il minimo idispensabile per avere una classe funzionate evitando comodita 
//                   come stati di diversi tipi (string, char, ecc) [tramite generics], alias e equals (anche se dovrebe essere òa base :]).
//                   Per poter capire bene come utilizzare la classe è presente un esempio funzoinante al fondo del file.

public record DFA<T> (
    Set<T> Q,
    Set<T> F,
    T start,
    List<Predicate<Character>> alphabet,
    Map<Entry<T, Predicate<Character>>, T> table 
) implements FA {

    // Checks construction
    public DFA {
        if (Q == null)     
            throw new IllegalArgumentException("Q cannot be null");
        if (F == null)     
            throw new IllegalArgumentException("F cannot be null");
        if (alphabet == null)     
            throw new IllegalArgumentException("alphabet cannot be null");
        if (Q.size() == 0)     
            throw new IllegalArgumentException("Q cannot be empty");
        if (!Q.containsAll(F)) 
            throw new IllegalArgumentException("F must be subset of Q");
        if (!Q.contains(start)) 
            throw new IllegalArgumentException("Start state must be contained in Q");
        
        // Extract Tuples keys and values to check domain and codomain
        Set<T> inState = table
            .keySet()
            .stream()
            .map(tuple -> tuple.getKey())
            .collect(Collectors.toSet());
        Set<T> outState = table
            .values()
            .stream()
            .collect(Collectors.toSet());
        Set<T> merged = Stream
            .concat(inState.stream(), outState.stream())
            .collect(Collectors.toSet());
        
        for (T q : merged) {
            if (!Q.contains(q)) 
                throw new IllegalArgumentException("Q does not contain state: " + q);
        }
        // Input there is no way to control it here
    }

    // Method to check input in alphabet
    private boolean inputInAlphabet(String input) {
        // Se tutti il char di input rispettano almeno 
        // un predicato allora sono parte del linguaggio 
        // Fuzionamento si basa su un any (almeno uno) 
        // e all (tutti) base.
        return input
            .chars()
            .allMatch(charInt -> alphabet
                .stream()
                .anyMatch(predicate -> predicate
                    .test((char) charInt)
                )
        );
    }

    // Method to check if string is accept of DFA
    public boolean scan(String input) {
        if (!inputInAlphabet(input))
            return false;
            // throw new IllegalArgumentException("Input containm dont valid chars");

        // Parse each char with DFA
        T state = this.start;
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            
            // System.out.print("-> " + state + " ");
            
            // Search next state on input table (transactional table)
            Set<Entry<T, Predicate<Character>>> rows = table.keySet();
            for (Entry<T, Predicate<Character>> row : rows) {
                T require                = row.getKey();
                Predicate<Character> set = row.getValue();
            
                // Test if is next state entry
                if (require.equals(state) && set.test(ch)) {
                    state = table.get(row);
                    break;
                }
            }
        }
        
        // Check if state is contains in accept states
        return F.contains(state);
    }
    
    @Override
    public String toString() {
        return String.format("""
            Q:      %s
            F:      %s
            q0:     %d
            a:      read source code
            TTable: read source code
        """, Q, F, start);
    }
}

class ExampleDFA {
    // Esercizio: 1.2
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
    //    ->q0      '_'     q1
    //    ->q0      lett    q2*
    //      q1      all     q2*
    //      q2*     all     q2*
    //      q3      all     q3 

    // Esempio con numeri
    private static DFA<Integer> javaIDSDFA() {
        Set<Integer> Q = new HashSet<>(Arrays.asList(0, 1, 2, 3));
        Set<Integer> F = new HashSet<>(Arrays.asList(2));
        List<Predicate<Character>> alphabet = Arrays.asList(
            Character::isDigit, 
            Character::isLetter, 
            c -> c == '_'
        );

        // Creazione delle entry e costruzione della mappa
        Map<Entry<Integer, Predicate<Character>>, Integer> tf = new HashMap<>();
        // Mettere Map of
        tf.put(Map.entry(0, Character::isDigit), 3);
        tf.put(Map.entry(0, Character::isLetter), 2);
        tf.put(Map.entry(0, c -> c == '_'), 1);
        tf.put(Map.entry(1, c -> c != '_'), 2);
        tf.put(Map.entry(2, c -> true), 2);
        tf.put(Map.entry(3, c -> true), 3);

        return new DFA<>(Q, F, 0, alphabet, tf);
    }
    
    private static boolean javaIDSDFATest() {
        String[] accepted  = { "x", "flag1", "x2y2", "x_1", "lft_lab", "_temp", "x_1_y_2", "x__", "__5" };
        String[] naccepted = { "5", "221B", "123", "9_to_5", "_" };

        DFA<Integer> dfa = javaIDSDFA();
        boolean a = Arrays
            .stream(accepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && val);    // all true);

        boolean b = Arrays
            .stream(naccepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && !val);   // all false

        return a && b;
    }

    // Esempio con stringe utilissimo per per NFA --> DFA
    private static DFA<String> javaIDSDFAS() {
        Set<String> Q = new HashSet<>(Arrays.asList("q0", "q1", "q2", "q3"));
        Set<String> F = new HashSet<>(Arrays.asList("q2"));
        List<Predicate<Character>> alphabet = Arrays.asList(
            Character::isDigit, 
            Character::isLetter, 
            c -> c == '_'
        );

        Map<Entry<String, Predicate<Character>>, String> tf = new HashMap<>();
        tf.put(Map.entry("q0", Character::isDigit), "q3");
        tf.put(Map.entry("q0", Character::isLetter), "q2");
        tf.put(Map.entry("q0", c -> c == '_'), "q1");
        tf.put(Map.entry("q1", c -> c != '_'), "q2");
        tf.put(Map.entry("q1", c -> true), "q2");
        tf.put(Map.entry("q2", c -> true), "q2");
        tf.put(Map.entry("q3", c -> true), "q3");

        return new DFA<>(Q, F, "q0", alphabet, tf);
    }
    
    private static boolean javaIDSDFATestS() {
        String[] accepted  = { "x", "flag1", "x2y2", "x_1", "lft_lab", "_temp", "x_1_y_2", "x__", "__5" };
        String[] naccepted = { "5", "221B", "123", "9_to_5", "_" };

        DFA<String> dfa = javaIDSDFAS();
        boolean a = Arrays
            .stream(accepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && val);

        boolean b = Arrays
            .stream(naccepted)
            .map(dfa::scan)
            .reduce(true, (acc, val) -> acc && !val);

        return a && b;
    }
    
    public static void main(String[] args) {
        System.out.println(javaIDSDFATest() ? "GOOD" : "BAD");
        System.out.println(javaIDSDFATestS() ? "GOOD" : "BAD");
    }
}
