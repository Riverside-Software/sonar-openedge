package org.prorefactor.core.unittest.util;

import org.prorefactor.core.schema.Schema;
import org.prorefactor.refactor.settings.IProgressSettings;
import org.prorefactor.refactor.settings.IProparseSettings;

import com.google.inject.AbstractModule;

public class UnitTestSports2000UnixModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(IProgressSettings.class).to(UnixProgressSettings.class);
    bind(IProparseSettings.class).to(UnitTestProparseSettings.class);
    bind(Schema.class).to(UnitTestSports2000Schema.class);
  }
}
