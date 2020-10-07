package com.crschnick.pdx_unlimiter.eu4.parser;

import com.crschnick.pdx_unlimiter.eu4.format.Namespace;
import com.crschnick.pdx_unlimiter.eu4.parser.GamedataParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Eu4NormalParser extends GamedataParser {

    public static final byte[] MAGIC = new byte[]{ 0x45, 0x55, 0x34, 0x74, 0x78, 0x74};

    public Eu4NormalParser() {
        super(MAGIC, Namespace.EMPTY);
    }

    @Override
    public List<GamedataParser.Token> tokenize(InputStream stream) throws IOException {
        StreamTokenizer t = new StreamTokenizer(stream);
        t.resetSyntax();
        t.commentChar('#');
        t.eolIsSignificant(false);
        t.lowerCaseMode(false);
        t.wordChars('.', '.');
        t.wordChars('0', '9');
        t.wordChars('a', 'z');
        t.wordChars('A', 'Z');
        t.wordChars('_', '_');
        t.wordChars('-', '-');
        t.wordChars(128, Integer.MAX_VALUE);
        t.whitespaceChars('\t', '\t');
        t.whitespaceChars(' ', ' ');
        t.whitespaceChars('\n', '\n');
        t.whitespaceChars('\r', '\r');
        t.quoteChar('"');

        List<GamedataParser.Token> tokens = new ArrayList<>();
        out: while (true) {
            int token = t.nextToken();
            switch (token) {
                case StreamTokenizer.TT_EOF:
                    break out;
                case StreamTokenizer.TT_EOL:
                    break;
                case StreamTokenizer.TT_NUMBER:
                    tokens.add(new ValueToken<Integer>((int) t.nval));
                    break;
                case StreamTokenizer.TT_WORD:
                    if (Pattern.matches("-?[0-9]+", t.sval)) {
                        tokens.add(new ValueToken<Long>(Long.parseLong(t.sval)));
                    } else if (Pattern.matches("([0-9]*)\\.([0-9]*)", t.sval)) {
                        tokens.add(new ValueToken<Double>(Double.valueOf(t.sval)));
                    } else if (t.sval.equals("yes")) {
                        tokens.add(new ValueToken<Boolean>(true));
                    } else if (t.sval.equals("no")) {
                        tokens.add(new ValueToken<Boolean>(false));
                    } else {
                        tokens.add(new ValueToken<String>(t.sval));
                    }
                    break;
                default:
                    if (token == '=') {
                        tokens.add(new EqualsToken());
                    }
                    if (token == '{') {
                        tokens.add(new OpenGroupToken());
                    }
                    if (token == '}') {
                        tokens.add(new CloseGroupToken());
                    }
                    if (token == '"') {
                        String s = t.sval;
                        tokens.add(new ValueToken<String>(s));
                    }
            }
        }
        return tokens;
    }
}
