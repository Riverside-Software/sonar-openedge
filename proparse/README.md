### Changements de classes

TokenList implements TokenStream
  ==> implements TokenSource
  Entièrement migré
  
PostLexer implements TokenStream
  ==> implements TokenSource
  
MultiChannelTokenList implements TokenSource : nouvelle classe remplaçant le TokenStreamHiddenTokenFilter dans DoParse, et masquant le préprocesseur 

ProTokenFactory remplace NodeFactory

ProToken extends CommonHiddenStreamToken
  ==> ProToken extends CommonToken

ProEvalSupport ==> à déplacer entièrement dans PreproEval

StringFuncs ==> entièrement migré
