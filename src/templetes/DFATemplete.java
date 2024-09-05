package templetes;

// Descrizione: Templete di base per implementare DFA, implementazione è molto imperativa 
//              utilizza un switch case ed if per implementare transizioni da stati.
//              Sicuramente seisto modi migliori per generalizzare un DFA memorizzando le tabelle 
//              di transizione e/o usando la OOP (classi, interfaccie ecc..) pero sembra non essere 
//              impoertante ora.

// Funzionemento DFA: Un DFA o automa a stai finiti deterministico ; un modello matematico di calcolo 
//                    dove il modello si puo trovre solo un numero finito di stati in cui puo transizionare
//                    tramite una apposita funzoine di transizione tranite un simbolo in input appartenete 
//                    al suo allfabeto. La funzione di transione nei DFA restituire uno stato che sara il 
//                    prossimo in cui il modello si dovra spostare. puo essere stesa a piu du un carattere, 
//                    concatenazione di molti (funzoine di transizione estesa).

// Rapresentazione: Tutti i tipi di automi posso essere rapresentati mediante tabelle di tranzione o grafi (grafico)
//                  anche se equivalenti ai fini di progettazione risulta piu utili disegnarli, invece le tabelle sono
//                  molto utili per "generalizzare" (presuppongo).

// Esempio: Nel seguinte templete implementato un DFA che acceta solo stringhe composte 0, 1 che contegono almeno tre 0 copsecutivi.

// Descrizione formale del DFA
//      Stati     Q = {q0, q1, q2, q3}
//      Alfabeto  Σ = {1, 0} 
//      TFunz     Implementato con switch
//      SInziale  q0
//      SFinali   F = {q3}

class DFATemplete {

    // Functions to read char to char and call DFA to change state 
    public static boolean scan(String s) {
	    int state = 0;      // DFA state (∈ Q)    
        int i     = 0;      // String index

        // Iter each string element
        while(state >= 0 && i < s.length()) {
            final char ch = s.charAt(i++);
            
            switch (state) {
                case 0:
                    if      (ch == '0') state = 1;
                    else if (ch == '1') state = 0;
                    else                state = -1;
                break;

                case 1:
                    if      (ch == '0') state = 2;
                    else if (ch == '1') state = 0;
                    else                state = -1;
                break;

                case 2:
                    if      (ch == '0') state = 3;
                    else if (ch == '1') state = 0;
                    else                state = -1;
                break;

                case 3:
                    if (ch == '0' || ch == '1') state = 3;
                    else                        state = -1;
                break;
                
                // State n...
            }
        }
	    return state == 3;
    }

    public static void main(String[] args) {
	    System.out.println(scan(args[0]) ? "OK" : "NOPE");
    }
}