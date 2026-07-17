using Progress.Json.ObjectModel.JsonArray.
define new global shared variable gs01 as int.
define new global shared variable gs02 as int.
define new shared variable s01 as int.
define new shared variable s02 as int.
define shared variable s03 as int.

define variable x1 as Progress.Lang.Object.
define variable x2 as JsonArray.
define variable x3 as rssw.VerySimpleObjectChild.

x1 = new Progress.Lang.Object().
x1:Next-Sibling.
x1:ToString().
x1:GetClass().

x2:Length.
x2:Add(10).
x2:IsNull(0).

x3:myArray.
