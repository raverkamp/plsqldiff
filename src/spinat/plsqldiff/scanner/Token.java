package spinat.plsqldiff.scanner;

public final class Token {

    public final int col; //the column in the source code 
    public final int line; //the position where the ignored tokens before this token start *)
    public final int pos; //he absolute position in the string 
    public final String value; //the raw string , this value is interned !!
    public final String svalue; // the semantic value, values are normalized, this string is interned
    public final TokenType tokenType; // the token type, i.e. classification
    public final int no; // the number

    
    
    public Token(
            TokenType tokenType,
            int pos,
            int line,
            int col,
            String str,
            String svalue,
            int no) // the number
    {
        this.tokenType = tokenType;
        this.col = col;
        this.line = line;
        this.pos = pos;
        this.value = str.intern();
        this.svalue = svalue.intern();
        this.no = no;
    }

    public int getLineno() {
        return line;
    }

    public int getColno() {
        return col;
    }

    public boolean isRelevant() {
        return !(tokenType == TokenType.EOLineComment
                || tokenType == TokenType.MultiLineComment
                || tokenType == TokenType.WhiteSpace
                || tokenType == TokenType.Newline);
    }

    public boolean ignore() {
        return !isRelevant();
    }

    @Override
    public String toString() {
        return "<" + tokenType + " \"" + value + "\"" + ", (" + line + ", " + col + ")>";
    }
}
