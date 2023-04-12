FOR EACH customer, EACH order OF customer:
    UPDATE customer.balance
    EDITING:
       DISPLAY " Editing customer: ". 
       IF LASTKEY = KEYCODE("RETURN") THEN
           MESSAGE "Pressed Return".
    END.
    MESSAGE "X1".
END.
