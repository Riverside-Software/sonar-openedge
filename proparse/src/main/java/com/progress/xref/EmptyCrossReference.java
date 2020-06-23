package com.progress.xref;

import com.google.common.collect.ImmutableList;

public class EmptyCrossReference extends CrossReference {
  public EmptyCrossReference() {
    this.source = ImmutableList.<Source> of();
  }
}
