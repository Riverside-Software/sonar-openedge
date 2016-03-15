
using newsyntax.102b.*.
using Progress.Lang.*.

class newsyntax.102b.ExtentTest:

    def var QueryHandle as handle no-undo.

    constructor public ExtentTest():
    end constructor.

    method public logical AddArray(objectArray as Object extent):
        define variable iLoop as integer no-undo.
        define variable iMax as integer no-undo.
        iMax = extent(objectArray).
        do iLoop = 1 to iMax:
            this-object:Add(objectArray[iLoop]).
        end.
        return true.
    end method.
    
    method void add(obj as Object):
    end method.

    method public character extent GetCurrentRowKey():
        return GetCurrentRowKey(QueryHandle).
    end method.
    
    method protected character extent GetCurrentRowKey(phQuery as handle):
        def var ret as char extent 2 no-undo.
        return ret.
    end method.


    method protected IEmpty GetCache(poService as class Class):
        define variable oCache as IEmpty no-undo.
        oCache = dynamic-new(poService:TypeName) (
                    this-object:Get(Class:GetClass('OpenEdge.Core.InjectABL.Lifecycle.IPipeline')),
                    Kernel:Settings:CachePruningInterval,
                    Kernel:Settings:CachePruningUnit ).
        return oCache.
    end method.
    
    
    method char Get(obj as Object):
        return "".
    end method.


end class.

