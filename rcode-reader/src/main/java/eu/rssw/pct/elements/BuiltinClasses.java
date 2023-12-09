/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package eu.rssw.pct.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Generated;

import eu.rssw.pct.elements.fixed.EnumGetValueMethodElement;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
import eu.rssw.pct.elements.fixed.PropertyElement;
import eu.rssw.pct.elements.fixed.TypeInfo;

@Generated(value = "genBuiltinClasses.p")
public class BuiltinClasses {
  private static final Collection<ITypeInfo> BUILTIN_CLASSES = new ArrayList<>();
  private static final Set<String> BUILTIN_CLASSES_NAMES = new HashSet<>();

  public static final ITypeInfo PROGRESS_LANG_OBJECT;
  public static final ITypeInfo PROGRESS_LANG_ENUM;
  public static final String PLO_CLASSNAME = "Progress.Lang.Object";
  public static final String PLE_CLASSNAME = "Progress.Lang.Enum";

  private BuiltinClasses() {
    // No constructor
  }

  public static boolean isBuiltinClass(String name) {
    return BUILTIN_CLASSES_NAMES.contains(name);
  }

  public static Collection<ITypeInfo> getBuiltinClasses() {
    return Collections.unmodifiableCollection(BUILTIN_CLASSES);
  }

  static {
    TypeInfo typeInfo = new TypeInfo(PLO_CLASSNAME, false, false, null, "");
    typeInfo.addMethod(new MethodElement("GetClass", false, new DataType("Progress.Lang.Class")));
    typeInfo.addMethod(new MethodElement("ToString", false, DataType.CHARACTER));
    typeInfo.addMethod(new MethodElement("Equals", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME))));
    typeInfo.addMethod(new MethodElement("Clone", false, new DataType(PLO_CLASSNAME)));
    typeInfo.addProperty(new PropertyElement("Next-Sibling", false, new DataType(PLO_CLASSNAME)));
    typeInfo.addProperty(new PropertyElement("Prev-Sibling", false, new DataType(PLO_CLASSNAME)));
    PROGRESS_LANG_OBJECT = typeInfo;
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo(PLE_CLASSNAME, false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("GetValue", false, DataType.INT64));
    typeInfo.addMethod(new MethodElement("CompareTo", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLE_CLASSNAME))));
    PROGRESS_LANG_ENUM = typeInfo;
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.ApplicationServer.AdapterTypes", false, false, PLE_CLASSNAME, "");
    typeInfo.addMethod(new EnumGetValueMethodElement(typeInfo));
    typeInfo.addProperty(new PropertyElement("Unexpected", true, new DataType("Progress.ApplicationServer.AdapterTypes")));
    typeInfo.addProperty(new PropertyElement("APSV", true, new DataType("Progress.ApplicationServer.AdapterTypes")));
    typeInfo.addProperty(new PropertyElement("SOAP", true, new DataType("Progress.ApplicationServer.AdapterTypes")));
    typeInfo.addProperty(new PropertyElement("REST", true, new DataType("Progress.ApplicationServer.AdapterTypes")));
    typeInfo.addProperty(new PropertyElement("WEB", true, new DataType("Progress.ApplicationServer.AdapterTypes")));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.ApplicationServer.AgentInfo", false, false, PLO_CLASSNAME, "");
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.ApplicationServer.AgentManager", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("CancelRequest", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("TerminateSession", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("TerminateFreeSessions", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("StopAgent", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("InitiateDebugger", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("DynamicPropertiesUpdate", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("TerminateFreeSession", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("DebugTest", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(4, "prm4", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(5, "prm5", 0, ParameterMode.INPUT_OUTPUT, DataType.INTEGER),
        new Parameter(6, "prm6", 0, ParameterMode.INPUT_OUTPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("TerminateSession", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("TrackABLObjects", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("GetABLObjectsReport", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT_OUTPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("GetABLObjectsReport", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT_OUTPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("TrackingABLObjects", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("TrackMemoryUse", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("GetMemoryUseReport", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT_OUTPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("GetMemoryUseReport", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT_OUTPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("TrackingMemoryUse", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("StopAgent", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("PushProfilerData", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("GetProfilerSettings", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT_OUTPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("flushDeferredLog", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("resetDeferredLog", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("ResetDynamicABLSessionLimit", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT_OUTPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("SetDynamicABLSessionLimit", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT_OUTPUT, DataType.LONGCHAR)));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.BPMError", false, false, "Progress.Lang.SysError", "", "Progress.Lang.Error");
    typeInfo.addProperty(new PropertyElement("NumMessages", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Severity", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.DataSlot", false, false, PLO_CLASSNAME, "");
    typeInfo.addProperty(new PropertyElement("Name", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("BPMDataTypeName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Choices", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("DataTypeName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("ReadOnly", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("Value", false, DataType.RUNTYPE));
    typeInfo.addProperty(new PropertyElement("WriteOnly", false, DataType.LOGICAL));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.DataSlotTemplate", false, false, PLO_CLASSNAME, "");
    typeInfo.addProperty(new PropertyElement("Name", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("BPMDataTypeName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Choices", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("DataTypeName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("ProcessTemplateName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("ReadOnly", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("Value", false, DataType.RUNTYPE));
    typeInfo.addProperty(new PropertyElement("WriteOnly", false, DataType.LOGICAL));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Filter.ITaskFilter", true, false, null, "");
    typeInfo.addMethod(new MethodElement("Is", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Task"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Filter.TaskActivityFilter", false, false, PLO_CLASSNAME, "",
        "Progress.BPM.Filter.ITaskFilter");
    typeInfo.addMethod(new MethodElement("Is", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Task"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Filter.TaskCreatorFilter", false, false, PLO_CLASSNAME, "",
        "Progress.BPM.Filter.ITaskFilter");
    typeInfo.addMethod(new MethodElement("Is", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Task"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Filter.TaskDueDateFilter", false, false, PLO_CLASSNAME, "",
        "Progress.BPM.Filter.ITaskFilter");
    typeInfo.addMethod(new MethodElement("Is", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Task"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Filter.TaskNameFilter", false, false, PLO_CLASSNAME, "",
        "Progress.BPM.Filter.ITaskFilter");
    typeInfo.addMethod(new MethodElement("Is", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Task"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Filter.TaskPerformerFilter", false, false, PLO_CLASSNAME, "",
        "Progress.BPM.Filter.ITaskFilter");
    typeInfo.addMethod(new MethodElement("Is", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Task"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Filter.TaskPriorityFilter", false, false, PLO_CLASSNAME, "",
        "Progress.BPM.Filter.ITaskFilter");
    typeInfo.addMethod(new MethodElement("Is", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Task"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Filter.TaskProcessFilter", false, false, PLO_CLASSNAME, "",
        "Progress.BPM.Filter.ITaskFilter");
    typeInfo.addMethod(new MethodElement("Is", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Task"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Filter.TaskProcessTemplateFilter", false, false, PLO_CLASSNAME, "",
        "Progress.BPM.Filter.ITaskFilter");
    typeInfo.addMethod(new MethodElement("Is", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Task"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Filter.TaskTimeStartedFilter", false, false, PLO_CLASSNAME, "",
        "Progress.BPM.Filter.ITaskFilter");
    typeInfo.addMethod(new MethodElement("Is", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Task"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Process", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("GetDataSlots", false, new DataType("Progress.BPM.DataSlot")));
    typeInfo.addMethod(new MethodElement("UpdateDataSlots", false, DataType.LOGICAL,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, new DataType("Progress.BPM.DataSlot"))));
    typeInfo.addMethod(new MethodElement("ActivateWorkstep", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("ActivateWorkstep", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, new DataType("Progress.BPM.DataSlot")),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("CompleteWorkstep", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("CompleteWorkstep", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, new DataType("Progress.BPM.DataSlot")),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Remove", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("Name", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Id", false, DataType.INT64));
    typeInfo.addProperty(new PropertyElement("Priority", false, DataType.CHARACTER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.Task", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("Complete", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("Assign", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Reassign", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetDataSlots", false, new DataType("Progress.BPM.DataSlot")));
    typeInfo.addMethod(new MethodElement("MakeAvailable", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("GetAvailablePresentationTypes", false, DataType.CHARACTER));
    typeInfo.addMethod(new MethodElement("GetPresentationUI", false, DataType.CHARACTER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addProperty(new PropertyElement("Name", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("DueDate", false, DataType.DATETIME));
    typeInfo.addProperty(new PropertyElement("Performer", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Creator", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Priority", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Status", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("TimeStarted", false, DataType.DATETIME));
    typeInfo.addProperty(new PropertyElement("ActivityName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("ProcessName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("ProcessTemplateName", false, DataType.CHARACTER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.BPM.UserSession", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("Connect", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Disconnect", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("StartProcess", false, new DataType("Progress.BPM.Process"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetTask", false, new DataType("Progress.BPM.Task"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetProcessTemplateNames", false, DataType.CHARACTER));
    typeInfo.addMethod(new MethodElement("GetAssignedTasks", false, new DataType("Progress.BPM.Task")));
    typeInfo.addMethod(new MethodElement("GetAvailableTasks", false, new DataType("Progress.BPM.Task")));
    typeInfo.addMethod(new MethodElement("GetDataSlotTemplates", false, new DataType("Progress.BPM.DataSlotTemplate"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("StartProcess", false, new DataType("Progress.BPM.Process"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, new DataType("Progress.BPM.DataSlotTemplate"))));
    typeInfo.addMethod(new MethodElement("StartProcess", false, new DataType("Progress.BPM.Process"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, new DataType("Progress.BPM.DataSlotTemplate")),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(4, "prm4", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Connect", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetAssignedTasks", false, new DataType("Progress.BPM.Task"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Filter.ITaskFilter"))));
    typeInfo.addMethod(new MethodElement("GetAvailableTasks", false, new DataType("Progress.BPM.Task"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.BPM.Filter.ITaskFilter"))));
    typeInfo.addMethod(new MethodElement("GetAssignedTasks", false, new DataType("Progress.BPM.Task"),
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, new DataType("Progress.BPM.Filter.ITaskFilter")),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetAvailableTasks", false, new DataType("Progress.BPM.Task"),
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, new DataType("Progress.BPM.Filter.ITaskFilter")),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetProcess", false, new DataType("Progress.BPM.Process"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetProcess", false, new DataType("Progress.BPM.Process"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("StartProcess", false, new DataType("Progress.BPM.Process"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, new DataType("Progress.BPM.DataSlotTemplate")),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(4, "prm4", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(5, "prm5", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetProcess", false, new DataType("Progress.BPM.Process"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetProcesses", false, new DataType("Progress.BPM.Process")));
    typeInfo.addMethod(new MethodElement("Connect", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("GetClientPrincipal", false, DataType.HANDLE));
    typeInfo.addProperty(new PropertyElement("SessionID", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Connected", false, DataType.LOGICAL));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.IO.BinarySerializer", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("Serialize", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.IO.OutputStream"))));
    typeInfo.addMethod(new MethodElement("Deserialize", false, new DataType(PLO_CLASSNAME),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.IO.InputStream"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.IO.FileInputStream", false, false, "Progress.IO.InputStream", "");
    typeInfo.addProperty(new PropertyElement("FileName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Closed", false, DataType.LOGICAL));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.IO.FileOutputStream", false, false, "Progress.IO.OutputStream", "");
    typeInfo.addProperty(new PropertyElement("FileName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Append", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("Closed", false, DataType.LOGICAL));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.IO.InputStream", false, true, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("Read", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.MEMPTR),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INT64),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Read", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Read", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT_OUTPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Read", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT_OUTPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("SkipBytes", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Read", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Close", false, DataType.VOID));
    typeInfo.addProperty(new PropertyElement("Closed", false, DataType.LOGICAL));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.IO.JsonSerializer", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("Serialize", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.IO.OutputStream"))));
    typeInfo.addMethod(new MethodElement("Deserialize", false, new DataType(PLO_CLASSNAME),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.IO.InputStream"))));
    typeInfo.addProperty(new PropertyElement("Formatted", false, DataType.LOGICAL));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.IO.OutputStream", false, true, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("Write", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.MEMPTR),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INT64),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Close", false, DataType.VOID));
    typeInfo.addMethod(new MethodElement("Flush", false, DataType.VOID));
    typeInfo.addProperty(new PropertyElement("Closed", false, DataType.LOGICAL));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Json.JsonError", false, false, "Progress.Lang.SysError", "",
        "Progress.Lang.Error");
    typeInfo.addProperty(new PropertyElement("NumMessages", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Severity", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Json.JsonParser", false, true, PLO_CLASSNAME, "");
    typeInfo.addProperty(new PropertyElement("IgnoreComments", false, DataType.LOGICAL));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Json.JsonParserError", false, false, "Progress.Json.JsonError", "",
        "Progress.Lang.Error");
    typeInfo.addProperty(new PropertyElement("Offset", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("NumMessages", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Severity", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Json.ObjectModel.JsonArray", false, false,
        "Progress.Json.ObjectModel.JsonConstruct", "");
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.COMPONENT_HANDLE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.COMPONENT_HANDLE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.DATE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.DATE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.DATETIME)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.DATETIME)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.DATETIME_TZ)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.DATETIME_TZ)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.DECIMAL)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.DECIMAL)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonArray"))));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonArray"))));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonObject"))));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonObject"))));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.RAW)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.RAW)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.RECID)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.RECID)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.ROWID)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.ROWID)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.COMPONENT_HANDLE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.COMPONENT_HANDLE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.DATE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATETIME)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.DATETIME)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATETIME_TZ)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.DATETIME_TZ)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DECIMAL)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.DECIMAL)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonArray"))));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonArray"))));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonObject"))));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonObject"))));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RAW)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.RAW)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RECID)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.RECID)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.ROWID)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.ROWID)));
    typeInfo.addMethod(new MethodElement("AddNull", false, DataType.INTEGER));
    typeInfo.addMethod(new MethodElement("AddNull", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("AddNull", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("AddNumber", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("AddNumber", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("AddNumber", false, DataType.INTEGER,
        new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("AddNumber", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", -1, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Clone", false, new DataType(PLO_CLASSNAME)));
    typeInfo.addMethod(new MethodElement("GetType", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetJsonText", false, DataType.LONGCHAR));
    typeInfo.addMethod(new MethodElement("GetJsonText", false, DataType.LONGCHAR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetJsonText", false, DataType.LONGCHAR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetCharacter", false, DataType.CHARACTER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetCharacter", false, DataType.CHARACTER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetCOMHandle", false, DataType.COMPONENT_HANDLE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetCOMHandle", false, DataType.COMPONENT_HANDLE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetDate", false, DataType.DATE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetDate", false, DataType.DATE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetDatetime", false, DataType.DATETIME,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetDatetime", false, DataType.DATETIME,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetDatetimeTZ", false, DataType.DATETIME_TZ,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetDatetimeTZ", false, DataType.DATETIME_TZ,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetDecimal", false, DataType.DECIMAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetDecimal", false, DataType.DECIMAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetHandle", false, DataType.HANDLE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetHandle", false, DataType.HANDLE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetInt64", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetInt64", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetInteger", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetInteger", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetJsonArray", false, new DataType("Progress.Json.ObjectModel.JsonArray"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetJsonArray", false, new DataType("Progress.Json.ObjectModel.JsonArray"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetJsonObject", false, new DataType("Progress.Json.ObjectModel.JsonObject"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetJsonObject", false, new DataType("Progress.Json.ObjectModel.JsonObject"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetLogical", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetLogical", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetLongchar", false, DataType.LONGCHAR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetLongchar", false, DataType.LONGCHAR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetLongchar", false, DataType.LONGCHAR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetLongchar", false, DataType.LONGCHAR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetMemptr", false, DataType.MEMPTR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetMemptr", false, DataType.MEMPTR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetRaw", false, DataType.RAW,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetRaw", false, DataType.RAW,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetRecid", false, DataType.RECID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetRecid", false, DataType.RECID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetRowid", false, DataType.ROWID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetRowid", false, DataType.ROWID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("IsNull", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Read", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Read", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Remove", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Remove", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.COMPONENT_HANDLE)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATE)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATETIME)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATETIME_TZ)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DECIMAL)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonArray"))));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonObject"))));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RAW)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RECID)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.ROWID)));
    typeInfo.addMethod(new MethodElement("SetNull", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("SetNumber", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addProperty(new PropertyElement("Length", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Json.ObjectModel.JsonConstruct", false, true, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.OUTPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.OUTPUT, DataType.LONGCHAR),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.OUTPUT, DataType.LONGCHAR),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.MEMPTR),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.MEMPTR),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT_OUTPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Write", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT_OUTPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("WriteFile", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("WriteFile", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("WriteFile", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("WriteStream", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("WriteStream", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("WriteStream", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Json.ObjectModel.JsonDataType", false, false, PLO_CLASSNAME, "");
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Json.ObjectModel.JsonObject", false, false,
        "Progress.Json.ObjectModel.JsonConstruct", "");
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.COMPONENT_HANDLE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATETIME)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATETIME_TZ)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DECIMAL)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonArray"))));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonObject"))));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RAW)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RECID)));
    typeInfo.addMethod(new MethodElement("Add", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.ROWID)));
    typeInfo.addMethod(new MethodElement("AddNull", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("AddNumber", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetJsonText", false, DataType.LONGCHAR));
    typeInfo.addMethod(new MethodElement("GetJsonText", false, DataType.LONGCHAR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Clone", false, new DataType(PLO_CLASSNAME)));
    typeInfo.addMethod(new MethodElement("GetNames", false, DataType.CHARACTER));
    typeInfo.addMethod(new MethodElement("GetType", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetCharacter", false, DataType.CHARACTER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetCOMHandle", false, DataType.COMPONENT_HANDLE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetDate", false, DataType.DATE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetDatetime", false, DataType.DATETIME,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetDatetimeTZ", false, DataType.DATETIME_TZ,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetDecimal", false, DataType.DECIMAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetHandle", false, DataType.HANDLE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetInt64", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetInteger", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetJsonArray", false, new DataType("Progress.Json.ObjectModel.JsonArray"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetJsonObject", false, new DataType("Progress.Json.ObjectModel.JsonObject"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetLogical", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetLongchar", false, DataType.LONGCHAR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetLongchar", false, DataType.LONGCHAR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetMemptr", false, DataType.MEMPTR,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetRaw", false, DataType.RAW,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetRecid", false, DataType.RECID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetRowid", false, DataType.ROWID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Has", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("IsNull", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Remove", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Read", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Read", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Read", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.COMPONENT_HANDLE)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATE)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATETIME)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DATETIME_TZ)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.DECIMAL)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonArray"))));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonObject"))));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LOGICAL)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RAW)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RECID)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.ROWID)));
    typeInfo.addMethod(new MethodElement("SetNull", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("SetNumber", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Json.ObjectModel.ObjectModelParser", false, false, "Progress.Json.JsonParser",
        "");
    typeInfo.addMethod(new MethodElement("Parse", false, new DataType("Progress.Json.ObjectModel.JsonConstruct"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addMethod(new MethodElement("Parse", false, new DataType("Progress.Json.ObjectModel.JsonConstruct"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.LONGCHAR)));
    typeInfo.addMethod(new MethodElement("Parse", false, new DataType("Progress.Json.ObjectModel.JsonConstruct"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.MEMPTR)));
    typeInfo.addMethod(new MethodElement("Parse", false, new DataType("Progress.Json.ObjectModel.JsonConstruct"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("ParseFile", false, new DataType("Progress.Json.ObjectModel.JsonConstruct"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addProperty(new PropertyElement("IgnoreComments", false, DataType.LOGICAL));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.AppError", false, false, "Progress.Lang.ProError", "",
        "Progress.Lang.Error");
    typeInfo.addMethod(new MethodElement("AddMessage", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("RemoveMessage", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addProperty(new PropertyElement("ReturnValue", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("NumMessages", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Severity", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.Class", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("IsInterface", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("IsFinal", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("New", false, new DataType(PLO_CLASSNAME),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addMethod(new MethodElement("New", false, new DataType(PLO_CLASSNAME)));
    typeInfo.addMethod(new MethodElement("Invoke", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addMethod(new MethodElement("Invoke", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Invoke", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Invoke", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addMethod(new MethodElement("GetPropertyValue", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetPropertyValue", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetPropertyValue", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addMethod(new MethodElement("GetPropertyValue", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("SetPropertyValue", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addMethod(new MethodElement("SetPropertyValue", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addMethod(new MethodElement("SetPropertyValue", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.RUNTYPE),
        new Parameter(4, "prm4", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addMethod(new MethodElement("SetPropertyValue", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addMethod(new MethodElement("GetMethod", false, new DataType("Progress.Reflect.Method"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addMethod(new MethodElement("GetMethod", false, new DataType("Progress.Reflect.Method"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags")),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addMethod(new MethodElement("GetMethods", false, new DataType("Progress.Reflect.Method")));
    typeInfo.addMethod(new MethodElement("GetMethods", false, new DataType("Progress.Reflect.Method"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    typeInfo.addMethod(new MethodElement("GetConstructor", false, new DataType("Progress.Reflect.Constructor"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addMethod(new MethodElement("GetConstructor", false, new DataType("Progress.Reflect.Constructor"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags")),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addMethod(new MethodElement("GetConstructors", false, new DataType("Progress.Reflect.Constructor")));
    typeInfo.addMethod(new MethodElement("GetConstructors", false, new DataType("Progress.Reflect.Constructor"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    typeInfo.addMethod(new MethodElement("IsA", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("IsA", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME))));
    typeInfo.addMethod(new MethodElement("HasStatics", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("IsAbstract", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("HasWidgetPool", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("IsSerializable", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("GetInterfaces", false, new DataType("Progress.Lang.Class")));
    typeInfo.addMethod(new MethodElement("IsGeneric", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("IsIndexed", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("IsEnum", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("IsFlagsEnum", false, DataType.LOGICAL));
    typeInfo.addMethod(new MethodElement("GetEnumName", false, DataType.CHARACTER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INT64)));
    typeInfo.addMethod(new MethodElement("GetEnumValue", false, DataType.INT64,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetEnumNames", false, DataType.CHARACTER));
    typeInfo.addMethod(new MethodElement("GetEnumValues", false, DataType.CHARACTER));
    typeInfo.addMethod(new MethodElement("GetProperties", false, new DataType("Progress.Reflect.Property")));
    typeInfo.addMethod(new MethodElement("GetProperties", false, new DataType("Progress.Reflect.Property"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    typeInfo.addMethod(new MethodElement("GetVariables", false, new DataType("Progress.Reflect.Variable")));
    typeInfo.addMethod(new MethodElement("GetVariables", false, new DataType("Progress.Reflect.Variable"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    typeInfo.addMethod(new MethodElement("GetEvents", false, new DataType("Progress.Reflect.Event")));
    typeInfo.addMethod(new MethodElement("GetEvents", false, new DataType("Progress.Reflect.Event"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    typeInfo.addMethod(new MethodElement("GetProperty", false, new DataType("Progress.Reflect.Property"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetProperty", false, new DataType("Progress.Reflect.Property"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    typeInfo.addMethod(new MethodElement("GetVariable", false, new DataType("Progress.Reflect.Variable"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetVariable", false, new DataType("Progress.Reflect.Variable"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    typeInfo.addMethod(new MethodElement("GetEvent", false, new DataType("Progress.Reflect.Event"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetEvent", false, new DataType("Progress.Reflect.Event"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    typeInfo.addProperty(new PropertyElement("TypeName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Package", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("SuperClass", false, new DataType("Progress.Lang.Class")));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.Error", true, false, null, "");
    typeInfo.addMethod(new MethodElement("GetMessage", false, DataType.CHARACTER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetMessageNum", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addProperty(new PropertyElement("NumMessages", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Severity", false, DataType.INTEGER));

    BUILTIN_CLASSES.add(typeInfo);
    typeInfo = new TypeInfo("Progress.Lang.FlagsEnum", false, false, PLE_CLASSNAME, "");
    typeInfo.addMethod(new EnumGetValueMethodElement(typeInfo));
    typeInfo.addMethod(new MethodElement("IsFlagSet", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Lang.FlagsEnum"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.LockConflict", false, false, "Progress.Lang.Stop", "");
    typeInfo.addProperty(new PropertyElement("TableName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("User", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Device", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.OERequestInfo", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("GetClientPrincipal", false, DataType.HANDLE));
    typeInfo.addMethod(new MethodElement("SetClientPrincipal", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE)));
    typeInfo.addProperty(new PropertyElement("ClientContextId", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("RequestId", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("VersionInfo", false, new DataType("Progress.Lang.OEVersionInfo")));
    typeInfo.addProperty(new PropertyElement("ProcedureName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("AgentId", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("SessionId", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("ThreadId", false, DataType.INTEGER));
    typeInfo.addProperty(
        new PropertyElement("AdapterType", false, new DataType("Progress.ApplicationServer.AdapterTypes")));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.OEVersionInfo", false, false, PLO_CLASSNAME, "");
    typeInfo.addProperty(new PropertyElement("OEMajorVersion", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("OEMinorVersion", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("OEMaintVersion", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("OEClientType", false, DataType.CHARACTER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.ParameterList", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("SetParameter", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(4, "prm4", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addMethod(new MethodElement("SetParameter", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addMethod(new MethodElement("Clear", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("NumParameters", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("SignatureList", false, DataType.CHARACTER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.ProError", false, false, PLO_CLASSNAME, "", "Progress.Lang.Error");
    typeInfo.addMethod(new MethodElement("GetMessage", false, DataType.CHARACTER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetMessageNum", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addProperty(new PropertyElement("NumMessages", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Severity", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.SoapFaultError", false, false, "Progress.Lang.SysError", "",
        "Progress.Lang.Error");
    typeInfo.addProperty(new PropertyElement("SoapFault", false, DataType.HANDLE));
    typeInfo.addProperty(new PropertyElement("NumMessages", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Severity", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.Stop", false, false, PLO_CLASSNAME, "");
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.StopAfter", false, false, "Progress.Lang.Stop", "");
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.StopError", false, false, "Progress.Lang.SysError", "",
        "Progress.Lang.Error");
    typeInfo.addProperty(new PropertyElement("NumMessages", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Severity", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.SysError", false, false, "Progress.Lang.ProError", "",
        "Progress.Lang.Error");
    typeInfo.addProperty(new PropertyElement("NumMessages", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Severity", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Lang.UserInterrupt", false, false, "Progress.Lang.Stop", "");
    typeInfo.addProperty(new PropertyElement("CallStack", false, DataType.CHARACTER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Reflect.AccessMode", false, false, PLE_CLASSNAME, "");
    typeInfo.addMethod(new EnumGetValueMethodElement(typeInfo));
    typeInfo.addProperty(new PropertyElement("Public", true, new DataType("Progress.Reflect.AccessMode")));
    typeInfo.addProperty(new PropertyElement("Protected", true, new DataType("Progress.Reflect.AccessMode")));
    typeInfo.addProperty(new PropertyElement("Private", true, new DataType("Progress.Reflect.AccessMode")));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Reflect.Constructor", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("GetParameters", false, new DataType("Progress.Reflect.Parameter")));
    typeInfo.addMethod(new MethodElement("Invoke", false, new DataType(PLO_CLASSNAME),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addProperty(new PropertyElement("Name", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("NumParameters", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("AccessMode", false, new DataType("Progress.Reflect.AccessMode")));
    typeInfo.addProperty(new PropertyElement("OriginatingClass", false, new DataType("Progress.Lang.Class")));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Reflect.DataType", false, false, PLE_CLASSNAME, "");
    typeInfo.addMethod(new EnumGetValueMethodElement(typeInfo));
    typeInfo.addProperty(new PropertyElement("AnyType", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Buffer", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Byte", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Character", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("COMHandle", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("DataSet", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("DataSetHandle", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Date", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("DateTime", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("DateTimeTZ", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Decimal", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Double", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Float", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Handle", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Int64", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Integer", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Logical", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Longchar", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Memptr", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Object", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Raw", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Recid", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Rowid", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Short", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("SingleCharacter", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Table", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("TableHandle", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("UnsignedByte", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("UnsignedInt", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("InsignedInt64", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("UnsignedShort", true, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("Void", true, new DataType("Progress.Reflect.DataType")));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Reflect.Event", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("GetHandlerParameters", false, new DataType("Progress.Reflect.Parameter")));
    typeInfo.addMethod(new MethodElement("Subscribe", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Subscribe", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Subscribe", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.HANDLE),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Subscribe", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Subscribe", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Subscribe", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("Publish", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addMethod(new MethodElement("Publish", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addProperty(new PropertyElement("Name", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("DeclaringClass", false, new DataType("Progress.Lang.Class")));
    typeInfo.addProperty(new PropertyElement("OriginatingClass", false, new DataType("Progress.Lang.Class")));
    typeInfo.addProperty(new PropertyElement("IsStatic", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("IsAbstract", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("IsOverride", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("AccessMode", false, new DataType("Progress.Reflect.AccessMode")));
    typeInfo.addProperty(new PropertyElement("NumHandlerParameters", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Reflect.Flags", false, false, "Progress.Lang.FlagsEnum", "");
    typeInfo.addProperty(new PropertyElement("Public", true, new DataType("Progress.Reflect.Flags")));
    typeInfo.addProperty(new PropertyElement("Protected", true, new DataType("Progress.Reflect.Flags")));
    typeInfo.addProperty(new PropertyElement("Private", true, new DataType("Progress.Reflect.Flags")));
    typeInfo.addProperty(new PropertyElement("Static", true, new DataType("Progress.Reflect.Flags")));
    typeInfo.addProperty(new PropertyElement("Instance", true, new DataType("Progress.Reflect.Flags")));
    typeInfo.addProperty(new PropertyElement("DeclaredOnly", true, new DataType("Progress.Reflect.Flags")));
    typeInfo.addMethod(new MethodElement("SetFlag", false, new DataType("Progress.Reflect.Flags"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    typeInfo.addMethod(new MethodElement("UnsetFlag", false, new DataType("Progress.Reflect.Flags"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    typeInfo.addMethod(new MethodElement("ToggleFlag", false, new DataType("Progress.Reflect.Flags"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.Flags"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Reflect.Method", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("GetParameters", false, new DataType("Progress.Reflect.Parameter")));
    typeInfo.addMethod(new MethodElement("Invoke", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addMethod(new MethodElement("Invoke", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Lang.ParameterList"))));
    typeInfo.addProperty(new PropertyElement("Name", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("NumParameters", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("ReturnType", false, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("ReturnTypeName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("ReturnExtent", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("AccessMode", false, new DataType("Progress.Reflect.AccessMode")));
    typeInfo.addProperty(new PropertyElement("IsAbstract", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("IsStatic", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("IsOverride", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("IsFinal", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("DeclaringClass", false, new DataType("Progress.Lang.Class")));
    typeInfo.addProperty(new PropertyElement("OriginatingClass", false, new DataType("Progress.Lang.Class")));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Reflect.Parameter", false, false, PLO_CLASSNAME, "");
    typeInfo.addProperty(new PropertyElement("Name", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Mode", false, new DataType("Progress.Reflect.ParameterMode")));
    typeInfo.addProperty(new PropertyElement("DataType", false, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("DataTypeName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Position", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("Extent", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Reflect.ParameterMode", false, false, "Progress.Lang.FlagsEnum", "");
    typeInfo.addMethod(new MethodElement("SetFlag", false, new DataType("Progress.Reflect.ParameterMode"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.ParameterMode"))));
    typeInfo.addMethod(new MethodElement("UnsetFlag", false, new DataType("Progress.Reflect.ParameterMode"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.ParameterMode"))));
    typeInfo.addMethod(new MethodElement("ToggleFlag", false, new DataType("Progress.Reflect.ParameterMode"),
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType("Progress.Reflect.ParameterMode"))));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Reflect.Property", false, false, "Progress.Reflect.Variable", "");
    typeInfo.addProperty(new PropertyElement("GetterAccessMode", false, new DataType("Progress.Reflect.AccessMode")));
    typeInfo.addProperty(new PropertyElement("SetterAccessMode", false, new DataType("Progress.Reflect.AccessMode")));
    typeInfo.addProperty(new PropertyElement("IsAbstract", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("IsOverride", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("IsIndexed", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("CanRead", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("CanWrite", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("Name", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("AccessMode", false, new DataType("Progress.Reflect.AccessMode")));
    typeInfo.addProperty(new PropertyElement("DataType", false, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("DataTypeName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Extent", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("IsStatic", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("DeclaringClass", false, new DataType("Progress.Lang.Class")));
    typeInfo.addProperty(new PropertyElement("OriginatingClass", false, new DataType("Progress.Lang.Class")));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Reflect.Variable", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("Get", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME))));
    typeInfo.addMethod(new MethodElement("Get", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Get", false, DataType.RUNTYPE));
    typeInfo.addMethod(new MethodElement("Get", false, DataType.RUNTYPE,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME)),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addMethod(new MethodElement("Set", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.RUNTYPE)));
    typeInfo.addProperty(new PropertyElement("Name", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("AccessMode", false, new DataType("Progress.Reflect.AccessMode")));
    typeInfo.addProperty(new PropertyElement("DataType", false, new DataType("Progress.Reflect.DataType")));
    typeInfo.addProperty(new PropertyElement("DataTypeName", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("Extent", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("IsStatic", false, DataType.LOGICAL));
    typeInfo.addProperty(new PropertyElement("DeclaringClass", false, new DataType("Progress.Lang.Class")));
    typeInfo.addProperty(new PropertyElement("OriginatingClass", false, new DataType("Progress.Lang.Class")));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Security.PAMStatus", false, false, PLO_CLASSNAME, "");
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Security.Realm.IHybridRealm", true, false, null, "");
    typeInfo.addMethod(new MethodElement("GetAttribute", false, DataType.CHARACTER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("GetAttributeNames", false, DataType.CHARACTER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("GetUserNames", false, DataType.CHARACTER));
    typeInfo.addMethod(new MethodElement("GetUserNamesByQuery", false, DataType.CHARACTER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("RemoveAttribute", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("SetAttribute", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("ValidatePassword", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("ValidatePassword", false, DataType.LOGICAL,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(4, "prm4", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("ValidateUser", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Web.AbstractWebRouter", false, true, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("HandleRequest", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Web.InternalWebHandler", false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("HandleRequest", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER),
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.MEMPTR),
        new Parameter(3, "prm3", 0, ParameterMode.INPUT, DataType.MEMPTR)));
    BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo("Progress.Web.IWebHandler", true, false, null, "");
    typeInfo.addMethod(new MethodElement("HandleRequest", false, DataType.INTEGER));
    BUILTIN_CLASSES.add(typeInfo);

    BUILTIN_CLASSES.stream().map(it -> it.getTypeName()).forEach(it -> BUILTIN_CLASSES_NAMES.add(it));
  }
}
