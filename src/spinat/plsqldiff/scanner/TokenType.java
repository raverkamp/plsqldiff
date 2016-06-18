package spinat.plsqldiff.scanner;

public enum TokenType {

    // the types of tokens in PL/SQL
    // 
    
    EOLineComment, SString, MultiLineComment, QIdent, Ident, LParen, RParen,//
    Assign, Comma, Semi, Percent, Arrow, Equal, NEqual, LEqual, GEqual, //
    Greater, Less, Plus, Minus, DotDot, Dot, Div, Mul, StringAdd, //
    WhiteSpace, Int, Power, LabelStart, LabelEnd, Colon, TheEnd, Exclamation, //
    Float, QString,StrangeChar,
    Newline, 
    Schrott // = crap returnedd when unexpected end of file
}
