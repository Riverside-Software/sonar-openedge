define temp-table tt01
  field fld1 as char
  field fld2 as char
  field fld3 as char
  field fld4 as char
  index idx1 is primary unique fld1
  index idx2 fld1 fld2
  index idx3 fld2 fld3 fld4.

define temp-table tt02 like customer.
define temp-table tt03 like customer use-index Comments use-index CountryPost.
define temp-table tt04 like customer use-index comments
  index idx1 EmailAddress.
define temp-table tt05 like customer index idx1 emailaddress.

define temp-table tt06 like tt01.
define temp-table tt07 like tt01 use-index idx1 use-index idx2.
define temp-table tt08 like tt01 use-index idx1 index idx4 fld2.
define temp-table tt09 like tt01 index idx4 fld2.
