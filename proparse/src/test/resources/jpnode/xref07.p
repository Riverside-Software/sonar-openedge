// Alias known during compilation, XREF contains this alias name, but not referenced in Sonar config
// Scanner shouldn't crash
DEFINE BUFFER bWarehouse FOR unknownAlias.Warehouse. 
FOR EACH bWarehouse WHERE bWarehouse.Address2 = "" NO-LOCK:
  // Nothing
END. 

// Table known during compilation, but unknown to Sonar scanner. Make sure no crash happens
DEFINE BUFFER b2Cust FOR foo.xyz. 
FOR EACH b2Cust WHERE b2Cust.Address2 = "" NO-LOCK:
  // Hello
END.
