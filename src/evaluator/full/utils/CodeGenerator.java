package evaluator.full.utils;

import java.util.LinkedList;
import java.io.*;

// Explanation: Ha lo scopo di memorizzare in una struttura apposita la lista delle istruzioni
//             (come oggetti di tipo Instruction) generate durante la parsificazione.


public class CodeGenerator {
    LinkedList <Instruction> instructions = new LinkedList<>();
    int label = 0;

    // Methods to add instructions

    public void emit(OpCode opCode) {
        instructions.add(new Instruction(opCode));
    }

    public void emit(OpCode opCode, int operand) {
        instructions.add(new Instruction(opCode, operand));
    }

    public void emitLabel(int operand) {
        emit(OpCode.label, operand);
    }

    public int newLabel() {
        return label++;
    }

    // Build jasmin document
    public void toJasmin() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter("../test/Output.j"));
        
        // Build document
        String temp = "";
        
        temp += header;                                 // Add header
        while(instructions.size() > 0) {                // Adds instruction by instruction
            Instruction tmp = instructions.remove();
            temp += tmp.toJasmin();
        }
        temp += footer;                                 // Add footer

        // Print and close resource
        out.println(temp);
        out.flush();
        out.close();
    }

    // Constant header and footer values
        // Nota: si potrebbero utilizzare i blocchi di testo di java """..."""
 
    private static final String header = 
        ".class public Output \n"
        + ".super java/lang/Object\n"
        + "\n"

        + ".method public <init>()V\n"
        + " aload_0\n"
        + " invokenonvirtual java/lang/Object/<init>()V\n"
        + " return\n"
        + ".end method\n"
        + "\n"

        + ".method public static print(I)V\n"
        + " .limit stack 2\n"
        + " getstatic java/lang/System/out Ljava/io/PrintStream;\n"
        + " iload_0 \n"
        + " invokestatic java/lang/Integer/toString(I)Ljava/lang/String;\n"
        + " invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n"
        + " return\n"
        + ".end method\n"
        + "\n"

        + ".method public static read()I\n"        
        + " .limit stack 3\n"        
        + " new java/util/Scanner\n"        
        + " dup\n"        
        + " getstatic java/lang/System/in Ljava/io/InputStream;\n"        
        + " invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V\n"        
        + " invokevirtual java/util/Scanner/next()Ljava/lang/String;\n"        
        + " invokestatic java/lang/Integer.parseInt(Ljava/lang/String;)I\n"        
        + " ireturn\n"        
        + ".end method\n"

        + "\n"
        + ".method public static run()V\n"
        + " .limit stack 1024\n"
        + " .limit locals 256\n";
    
    private static final String footer = 
        " return\n"
        + ".end method\n"
        + "\n"
        
        + ".method public static main([Ljava/lang/String;)V\n"
        + " invokestatic Output/run()V\n"
        + " return\n"
        + ".end method\n";
}
