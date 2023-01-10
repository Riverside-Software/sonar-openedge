&scoped-define ERR_GENERAL_EMPTYKEY "ERR_GENERAL_EMPTYKEYERR_GENERAL_EMPTYKEY"
class rssw.testclass:
  method public void foo():
    AddError("ttCrsMod_Prod.nr_mod_prod":U , "ERR_GENERAL_EMPTYKEY":U, subst(GetTranslation({&ERR_GENERAL_EMPTYKEY}), "Moduleproduct") ).
  end method.

  method public char getTranslation(prm as char):
    return prm.
  end method.

  @Generated.
  method public void test1(prm1 as char):

    return.
  end method.

  @rssw.lang.Generated.
  method public void test2(prm1 as char):

    return.
  end method.

  method public Object test3():
    return ?.
  end method.

end class.
