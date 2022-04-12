var Progress.Collections.List<Progress.Lang.Object> thelist.
var Progress.Collections.IIterator<Progress.Lang.Object> iterator.
var Progress.Lang.Object president1.
var Progress.Lang.Object president2.

// Create the list
thelist = new Progress.Collections.List<Progress.Lang.Object>().

// Add 3 elements to the list
thelist:Add(new Progress.Lang.Object("George")).
thelist:Add(new Progress.Lang.Object ("John")).
thelist:Add(new Progress.Lang.Object("Thomas")).

// Retrieve the first element from the list
president1 = thelist:Get(1).
message president1:ToString().

// Replace the first element in the list with a fully named president.
thelist:Set(1, new Progress.Lang.Object("George Washington")).
president2 = thelist:Get(1).
message president2:ToString().

// Remove the second president from the list
thelist:RemoveAt(2).

// Print out the number of presidents in the list
message thelist:Count.

// Iterate over the entries in the list
iterator = thelist:GetIterator().
repeat while iterator:MoveNext():
  message iterator:Current:ToString().
end.
