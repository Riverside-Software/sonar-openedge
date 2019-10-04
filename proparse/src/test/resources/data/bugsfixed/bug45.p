define variable xxx as Progres.Lang.Object.
dynamic-property(xxx, "myProp") = "".
assign dynamic-property(xxx, "myProp") = "".
dynamic-property(xxx, "myProp") = dynamic-property(xxx, "myProp").
assign dynamic-property(xxx, "myProp") = dynamic-property(xxx, "myProp").
