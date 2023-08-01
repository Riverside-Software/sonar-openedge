package org.prorefactor.proparse;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.prorefactor.proparse.antlr4.Proparse;

public class ErrorDetectionListener extends BaseErrorListener {
  private final List<Interval> errors = new ArrayList<>();
  private final List<String> errCode = new ArrayList<>();

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
      String msg, RecognitionException caught) {
    Proparse proparse = (Proparse) recognizer;

    if (caught instanceof NoViableAltException) {
      NoViableAltException nvae = (NoViableAltException) caught;
      int startIndex = nvae.getStartToken().getTokenIndex();
      int endIndex = nvae.getOffendingToken().getTokenIndex();
      errors.add(new Interval(startIndex, endIndex));
      errCode.add(proparse.getTokenStream().getText(new Interval(startIndex, endIndex)));
    } else if (caught instanceof InputMismatchException) {
      InputMismatchException ime = (InputMismatchException) caught;
      errors.add(ime.getCtx().getSourceInterval());
      errCode.add(proparse.getTokenStream().getText(ime.getCtx().getSourceInterval()));
    } else if (caught == null) {
      int index = ((Token) offendingSymbol).getTokenIndex();
      errors.add(new Interval(index, index));
      errCode.add(proparse.getTokenStream().getText(new Interval(index, index)));
    }
  }

  public List<Interval> getErrors() {
    return errors;
  }

  public List<String> getErrCode() {
    return errCode;
  }
}
