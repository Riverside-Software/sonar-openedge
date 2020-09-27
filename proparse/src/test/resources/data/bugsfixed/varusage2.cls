class bugsfixed.varusage2:

 define public property x1 as logical no-undo
        get.
        set(iReadOnly as logical):

            if iReadOnly
            then do:
                this-object:x2 = this-object:x3.
                this-object:x3 = no.
            end.
            else do:
                if this-object:x2 ne ?
                then this-object:x3 = this-object:x2.
            end.
            this-object:x1 = iReadOnly.

        end set.

    define private property x2 as logical no-undo initial ?
        get.
        set.

    define private property x3 as logical no-undo
        get.
        set.

end.
