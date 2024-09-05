package constructs;

/* Explanation: The tag class represents the number of words in the token, 
 *              conveniently divided by the word class. (static assets)
*/

public class Tag {
    public final static int
		EOF    = -1, 
		NUM    = 256, 
		ID     = 257, 
		RELOP  = 258,
		ASSIGN = 259, 
		TO     = 260, 
		IF     = 261, 
		ELSE   = 262, 
		DO     = 263, 
		FOR    = 264, 
		BEGIN  = 265, 
		END    = 266, 
		PRINT  = 267, 
		READ   = 268, 
		INIT   = 269, 
		OR     = 270, 
		AND    = 271,
		TTRUE  = 272, 
		TFALSE = 273;
}