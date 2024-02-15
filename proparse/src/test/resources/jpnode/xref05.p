for each Customer where Customer.Country eq ""
                    and Customer.PostalCode eq ""
                    and Customer.Comments contains "Foo"
                    and Customer.Name eq "" 
                  by Customer.Name by Customer.Country by Customer.PostalCode:
  // TODO
end.
