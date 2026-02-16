DEFINE BUFFER bWarehouse FOR Warehouse. 

FOR EACH bWarehouse WHERE bWarehouse.Address2 = "" NO-LOCK:
    // Hello  
END. 

DEFINE BUFFER b2Warehouse FOR foo.Warehouse. 

FOR EACH b2Warehouse WHERE b2Warehouse.Address2 = "" NO-LOCK:
    // Hello  
END.

DEFINE BUFFER b3Warehouse FOR sports2000.Warehouse.
FOR EACH b3Warehouse WHERE b3Warehouse.Address2 = "" NO-LOCK:
  // Nothing
END.
