package com.progress.xref;

import java.util.List;

public class EmptyCrossReference extends CrossReference {
  public EmptyCrossReference() {
    this.source = List.of();
  }
}
