package evaluator.full.utils;

import java.util.*;

// Explanation: Per tenere traccia degli identificatori occorre predisporre una tabella dei simboli.

public class SymbolTable {
    Map<String, Integer> OffsetMap = new HashMap<>();

    public void insert(String s, int address) {
        String errorMsg = "Reference to a memory location already occupied by another variable";

        if (!OffsetMap.containsValue(address)) OffsetMap.put(s, address);
        else                                   throw new IllegalArgumentException(errorMsg);
    }

    public int lookupAddress(String s) {
        if (OffsetMap.containsKey(s)) return OffsetMap.get(s);
        else                          return -1;
    }
}
