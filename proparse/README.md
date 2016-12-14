### Changements de classes

TokenList implements TokenStream
  ==> implements TokenSource
PostLexer implements TokenStream
  ==> implements TokenSource
MultiChannelTokenList implements TokenSource : nouvelle classe remplaçant le TokenStreamHiddenTokenFilter dans DoParse, et masquant le préprocesseur 

ProTokenFactory remplace NodeFactory

ProToken extends CommonHiddenStreamToken
  ==> ProToken implements WritableToken (voir si ne devrait pas étendre CommonToken)

ProEvalSupport ==> à déplacer entièrement dans PreproEval

