package main.tool;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    private static final String indent = "    ";

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Get      : Expr object, Token name",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Set      : Expr object, Token name, Expr value",
                "Super    : Token keyword, Token method",
                "This     : Token keyword",
                "Unary    : Token operator, Expr right",
                "Variable : Token name"));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> methods",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body"));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types)
            throws FileNotFoundException, UnsupportedEncodingException {
        Path path = Path.of(outputDir, baseName + ".java");
        PrintWriter writer = new PrintWriter(path.toFile(), "UTF-8");

        writer.println("package main.jlox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // the AST classes
        for (String type : types) {
            writer.println();

            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println(indent + "abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println(indent + "interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(
                    indent + indent + "R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase()
                            + ");");
        }

        writer.println(indent + "}");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println(indent + "static class " + className + " extends " + baseName + " {");

        // Constructor
        writer.println(indent + indent + className + "(" + fieldList + ") {");

        // Store parameters in fields
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println(indent + indent + indent + "this." + name + " = " + name + ";");
        }

        // Finish constructor
        writer.println(indent + indent + "}");

        // Visitor pattern
        writer.println();
        writer.println(indent + indent + "@Override");
        writer.println(indent + indent + "<R> R accept(Visitor<R> visitor) {");
        writer.println(indent + indent + indent + "return visitor.visit" + className + baseName + "(this);");
        writer.println(indent + indent + "}");

        // Fields
        writer.println();
        for (String field : fields) {
            writer.println(indent + indent + "final " + field + ";");
        }

        writer.println(indent + "}");
    }
}
