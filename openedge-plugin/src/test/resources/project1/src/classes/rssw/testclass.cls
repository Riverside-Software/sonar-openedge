&scoped-define ERR_GENERAL_EMPTYKEY "ERR_GENERAL_EMPTYKEYERR_GENERAL_EMPTYKEY"
class rssw.testclass:
  method public void foo():
    AddError("ttCrsMod_Prod.nr_mod_prod":U , "ERR_GENERAL_EMPTYKEY":U, subst(GetTranslation({&ERR_GENERAL_EMPTYKEY}), "Moduleproduct") ).
  end method.

  method public char getTranslation(prm as char):
    return prm.
  end method.

end class.
