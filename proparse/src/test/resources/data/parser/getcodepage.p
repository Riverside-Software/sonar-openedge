// FunctionKeywordTokenFilter test

DEFINE VARIABLE xxx AS LONGCHAR.

MESSAGE GET-CODEPAGE(xxx). // No conversion
MESSAGE GET-CODEPAGES(xxx). // Converted to GET-CODEPAGE
MESSAGE GET-CODEPAGE. // No conversion
MESSAGE GET-CODEPAGES. // No conversion
