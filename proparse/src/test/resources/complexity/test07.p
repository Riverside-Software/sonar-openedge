def var x1 as Progress.Lang.Object.

if true then // +1
  x1 = new Object().
else if true then // +1
  x1 = new Object().
else // +1
  x1 = new Object().

if true then // +1
  x1 = dynamic-new ('Progress.Lang.Object') ().
else if true then // +1
  x1 = dynamic-new Progress.Lang.Object ().
else // +1
  x1 = dynamic-new ('Progress.Lang.Object') ().
