DEFINE VARIABLE xx AS OpenEdge.Core.System.ErrorSeverityEnum.
DEFINE VARIABLE zz AS OpenEdge.Core.System.ErrorSeverityEnum.

xx = OpenEdge.Core.System.ErrorSeverityEnum:Critical.
MESSAGE xx XOR OpenEdge.Core.System.ErrorSeverityEnum:Critical XOR OpenEdge.Core.System.ErrorSeverityEnum:MESSAGE.
zz = xx XOR OpenEdge.Core.System.ErrorSeverityEnum:Critical XOR OpenEdge.Core.System.ErrorSeverityEnum:MESSAGE.
ASSIGN zz = xx XOR OpenEdge.Core.System.ErrorSeverityEnum:Critical XOR OpenEdge.Core.System.ErrorSeverityEnum:MESSAGE.
