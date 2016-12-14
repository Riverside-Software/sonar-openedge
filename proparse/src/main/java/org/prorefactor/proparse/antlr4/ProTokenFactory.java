package org.prorefactor.proparse.antlr4;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;

public class ProTokenFactory implements TokenFactory<ProToken> {
  private final TokenSource source;

  public ProTokenFactory(TokenSource source) {
    this.source = source;
  }

  @Override
  public ProToken create(Pair<TokenSource, CharStream> source, int type, String text, int channel, int start, int stop,
      int line, int charPositionInLine) {
    ProToken t = new ProToken(type, text, line, charPositionInLine);
    t.setTokenSource(this.source);
    if (text != null) {
      t.setText(text);
    }
    t.setChannel(channel);

    return t;
  }

  @Override
  public ProToken create(int type, String text) {
    ProToken token = new ProToken(type, text);
    token.setTokenSource(source);

    return token;
  }

}
