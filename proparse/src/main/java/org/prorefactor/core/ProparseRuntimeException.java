package org.prorefactor.core;

public class ProparseRuntimeException extends RuntimeException {
  private static final long serialVersionUID = -1350324743265891607L;

  public ProparseRuntimeException(String message) {
    super(message);
  }

  public ProparseRuntimeException(Throwable cause) {
    super(cause);
  }
}
