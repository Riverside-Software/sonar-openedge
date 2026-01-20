/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.proparse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.nodetypes.ArrayReferenceNode;
import org.prorefactor.core.nodetypes.AttributeReferenceNode;
import org.prorefactor.core.nodetypes.BuiltinFunctionNode;
import org.prorefactor.core.nodetypes.ConstantNode;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.core.nodetypes.IExpression;
import org.prorefactor.core.nodetypes.LocalMethodCallNode;
import org.prorefactor.core.nodetypes.MethodCallNode;
import org.prorefactor.core.nodetypes.NamedMemberArrayNode;
import org.prorefactor.core.nodetypes.NamedMemberNode;
import org.prorefactor.core.nodetypes.NewTypeNode;
import org.prorefactor.core.nodetypes.SingleArgumentExpression;
import org.prorefactor.core.nodetypes.TwoArgumentsExpression;
import org.prorefactor.core.nodetypes.UserFunctionCallNode;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.symbols.Variable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.PrimitiveDataType;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
import eu.rssw.pct.elements.fixed.PropertyElement;
import eu.rssw.pct.elements.fixed.TypeInfo;
import eu.rssw.pct.elements.fixed.VariableElement;

public class ExpressionEngineTest extends AbstractProparseTest {
  private RefactorSession session;

  @BeforeMethod
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());

    // Inject content of catalog.json
    try (Reader reader = new FileReader("src/test/resources/catalog.json")) {
      session.injectClassesFromCatalog(reader);
    }
  }

  @Test
  public void testUnaryExpression() {
    testSimpleExpression("+ 45.", DataType.INTEGER);
    testSimpleExpression("- 45.", DataType.INTEGER);
    testSimpleExpression("- -45.112.", DataType.DECIMAL);
    testSimpleExpression("def var xx as log. not xx.", DataType.LOGICAL);
    testSimpleExpression("def var xx as log. not (not xx).", DataType.LOGICAL);
  }

  @Test
  public void testNamedMember01() {
    ParseUnit unit = getParseUnit(
        "define temp-table tt1 field fld1 as int. define buffer b1 for tt1. buffer b1::fld1.", session);
    unit.treeParser01();
    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    NamedMemberNode exp = (NamedMemberNode) nodes.get(0);
    assertEquals(exp.getNamedMember(), "fld1");
    assertEquals(exp.getDataType(), DataType.NOT_COMPUTED);
  }

  @Test
  public void testNamedMemberArray01() {
    ParseUnit unit = getParseUnit(
        "define temp-table tt1 field fld1 as int extent. temp-table tt1::fld1(1).", session);
    unit.treeParser01();
    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    NamedMemberArrayNode exp = (NamedMemberArrayNode) nodes.get(0);
    assertEquals(exp.getNamedMember(), "fld1");
    assertEquals(exp.getDataType(), DataType.NOT_COMPUTED);
  }

  @Test
  public void testTwoArguments() {
    testSimpleExpression("1 + 1.", DataType.INTEGER);
    testSimpleExpression("def var xx as date. xx + 3.", DataType.DATE);
    testSimpleExpression("def var xx as date. 3 + xx.", DataType.DATE);
    testSimpleExpression("def var xx as int. xx + 3.", DataType.INTEGER);
    testSimpleExpression("def var xx as int. 3 + xx.", DataType.INTEGER);
    testSimpleExpression("def var xx as int. 4 * xx.", DataType.INTEGER);
    testSimpleExpression("5 ge 4.", DataType.LOGICAL);
    testSimpleExpression("'xxx' contains 'x'.", DataType.LOGICAL);
    testSimpleExpression("3 / 4.", DataType.INTEGER);
    testSimpleExpression("def var xx as int64. xx / 4.", DataType.INT64);
  }

  @Test
  public void testMethod01() {
    ParseUnit unit = getParseUnit("session:get-printers().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    assertEquals(nodes.get(0).getDataType(), DataType.CHARACTER);
  }

  @Test
  public void testMethod02() {
    ParseUnit unit = getParseUnit("compiler:get-row().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    assertEquals(nodes.get(0).getDataType(), DataType.INTEGER);
  }

  @Test
  public void testBuiltinFunctions() {
    testSimpleExpression("def var hnd as handle. valid-handle(hnd).", DataType.LOGICAL);
    testSimpleExpression("def var xxx as Progress.Lang.Object. get-class(xxx).", new DataType("Progress.Lang.Class"));
    testSimpleExpression(
        "def var xx as Progress.Lang.Object. message cast(new Progress.Lang.Object(), Progress.Lang.Class).",
        new DataType("Progress.Lang.Class"));
    testSimpleExpression("def var xx as datetime. message add-interval(xx, 1, 'weeks').", DataType.DATETIME);
    testSimpleExpression("def var xx as datetime-tz. message add-interval(xx, 1, 'weeks').", DataType.DATETIME_TZ);
    testSimpleExpression("message minimum(1, 2, 3, 4).", DataType.INTEGER);
    testSimpleExpression("message maximum(1, 2.4, 3, 4).", DataType.DECIMAL);
    testSimpleExpression("message box(1).", new DataType("System.Object"));
    testSimpleExpression("message iso-date('xxx').", DataType.DATE);
    testSimpleExpression("define frame frm1. message frame-col(frm1).", DataType.DECIMAL);
    testSimpleExpression("define var xx as date. year(xx).", DataType.INTEGER);
    testSimpleExpression("int64('123').", DataType.INT64);
    testSimpleExpression("lc('TEST').", DataType.CHARACTER);
    testSimpleExpression("locked(customer).", DataType.LOGICAL);
    testSimpleExpression("base64-encode('test').", DataType.LONGCHAR);
    testSimpleExpression("base64-decode(xyz).", DataType.MEMPTR);
    testSimpleExpression("md5-digest('test').", DataType.RAW);
    testSimpleExpression("decimal('12.34').", DataType.DECIMAL);
    testSimpleExpression("date(1,1,1980).", DataType.DATE);
    testSimpleExpression("datetime(1,1,1980).", DataType.DATETIME);
    testSimpleExpression("datetime-tz(1,1,1980).", DataType.DATETIME_TZ);
    testSimpleExpression("handle('123').", DataType.HANDLE);
    testSimpleExpression("load-picture('img.jpg').", DataType.COMPONENT_HANDLE);
    testSimpleExpression("recid(customer).", DataType.RECID);
    testSimpleExpression("rowid(customer).", DataType.ROWID);
    testSimpleExpression("dynamic-function('funcName').", DataType.RUNTYPE);
    testSimpleExpression("now.", DataType.DATETIME_TZ);
    testSimpleExpression("NUM-DBS.", DataType.INTEGER);
    testSimpleExpression("num-aliases.", DataType.INTEGER);
    testSimpleExpression("opsys.", DataType.CHARACTER);
    testSimpleExpression("progress.", DataType.CHARACTER);
    testSimpleExpression("RETURN-VALUE.", DataType.CHARACTER);
    testSimpleExpression("transaction.", DataType.LOGICAL);
  }

  @Test
  public void testSideEffect() {
    ParseUnit unit = getParseUnit("etime(true). recid(customer).", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);
    IExpression exp1 = nodes.get(0);
    assertTrue(exp1 instanceof BuiltinFunctionNode);
    assertTrue(((BuiltinFunctionNode) exp1).hasSideEffect());
    IExpression exp2 = nodes.get(1);
    assertTrue(exp2 instanceof BuiltinFunctionNode);
    assertFalse(((BuiltinFunctionNode) exp2).hasSideEffect());
  }

  @Test
  public void testFunction01() {
    ParseUnit unit = getParseUnit("function f1 returns char () forwards. message f1().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType(), DataType.CHARACTER);
  }

  @Test
  public void testNewObject01() {
    ParseUnit unit = getParseUnit("def var xx as Progress.Lang.Object. message new Progress.Lang.Object().", session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    var exp = nodes.get(0);
    assertTrue (exp instanceof NewTypeNode);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp.getDataType().getClassName(), "Progress.Lang.Object");
    var methd = ((NewTypeNode) exp).getMethod().getO2();
    assertNotNull(methd) ;
    assertEquals(methd.getReturnType().getPrimitive(), PrimitiveDataType.VOID);
  }

  @Test
  public void testNewObject02() {
    ParseUnit unit = getParseUnit("def var xx as Progress.Lang.Object. message new Progress.Lang.Object():toString().",
        session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
  }

  @Test
  public void testNewObject03() {
    ParseUnit unit = getParseUnit(
        "def var xx as Progress.Lang.Object. message new Progress.Lang.Object():GetClass():HasStatics().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);
  }

  @Test
  public void testNewObject04() {
    var unit = getParseUnit("def var xx as Progress.IO.FileInputStream. message new Progress.IO.FileInputStream('filename.txt').", session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    var exp = nodes.get(0);
    assertTrue (exp instanceof NewTypeNode);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp.getDataType().getClassName(), "Progress.IO.FileInputStream");
    var methd = ((NewTypeNode) exp).getMethod().getO2();
    assertNotNull(methd) ;
    assertEquals(methd.getReturnType().getPrimitive(), PrimitiveDataType.VOID);
  }

  @Test
  public void testNewObject05() {
    var code = """
        using Progress.IO.FileInputStream.
        def var xx as Progress.IO.FileInputStream.
        message new FileInputStream('filename.txt').
        """;
    var unit = getParseUnit(code, session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    var exp = nodes.get(0);
    assertTrue (exp instanceof NewTypeNode);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp.getDataType().getClassName(), "Progress.IO.FileInputStream");
    var methd = ((NewTypeNode) exp).getMethod().getO2();
    assertNotNull(methd) ;
    assertEquals(methd.getReturnType().getPrimitive(), PrimitiveDataType.VOID);
  }

  @Test
  public void testIfExpr01() {
    ParseUnit unit = getParseUnit("message (if true then 'abc' else 'def').", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
  }

  @Test
  public void testIfExpr02() {
    ParseUnit unit = getParseUnit("message (if true then 123 else 456).", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
  }

  @Test
  public void testSysHandles() {
    testSimpleExpression("message rcode-info.", DataType.HANDLE);
    // Methods
    testSimpleExpression("message audit-control:log-audit-event().", DataType.CHARACTER);
    testSimpleExpression("message audit-control:clear-appl-context().", DataType.LOGICAL);
    testSimpleExpression("message audit-control:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message audit-policy:encrypt-audit-mac-key().", DataType.CHARACTER);
    testSimpleExpression("message audit-policy:refresh-audit-policy().", DataType.LOGICAL);
    testSimpleExpression("message audit-policy:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message color-table:get-green-value().", DataType.INTEGER);
    testSimpleExpression("message color-table:set-green-value().", DataType.LOGICAL);
    testSimpleExpression("message color-table:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message compiler:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message compiler:get-file-name().", DataType.CHARACTER);
    testSimpleExpression("message compiler:get-number().", DataType.INTEGER);
    testSimpleExpression("message compiler:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message debugger:cancel-break().", DataType.LOGICAL);
    testSimpleExpression("message debugger:display-message().", DataType.INTEGER);
    testSimpleExpression("message debugger:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message error-status:get-message().", DataType.CHARACTER);
    testSimpleExpression("message error-status:get-number().", DataType.INTEGER);
    testSimpleExpression("message error-status:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message font-table:GET-TEXT-WIDTH-CHARS().", DataType.DECIMAL);
    testSimpleExpression("message font-table:GET-TEXT-WIDTH-PIXELS().", DataType.INTEGER);
    testSimpleExpression("message font-table:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message log-manager:write-message().", DataType.LOGICAL);
    testSimpleExpression("message log-manager:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message this-procedure:add-super-procedure().", DataType.LOGICAL);
    testSimpleExpression("message this-procedure:get-signature().", DataType.CHARACTER);
    testSimpleExpression("message this-procedure:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message profiler:user-data().", DataType.LOGICAL);
    testSimpleExpression("message profiler:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message rcode-info:InvalidFunction().", DataType.UNKNOWN);
    testSimpleExpression("message security-policy:get-client().", DataType.HANDLE);
    testSimpleExpression("message security-policy:load-domains().", DataType.LOGICAL);
    testSimpleExpression("message security-policy:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message session:get-printers().", DataType.CHARACTER);
    testSimpleExpression("message session:export().", DataType.LOGICAL);
    testSimpleExpression("message session:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message web-context:get-binary-data().", DataType.MEMPTR);
    testSimpleExpression("message web-context:get-cgi-list().", DataType.CHARACTER);
    testSimpleExpression("message web-context:get-cgi-long-value().", DataType.LONGCHAR);
    testSimpleExpression("message web-context:InvalidFunction().", DataType.NOT_COMPUTED);
    testSimpleExpression("message current-window:end-file-drop().", DataType.LOGICAL);
    testSimpleExpression("message current-window:get-dropped-file().", DataType.CHARACTER);
    testSimpleExpression("message current-window:get-selected-widget().", DataType.HANDLE);
    testSimpleExpression("message current-window:InvalidFunction().", DataType.NOT_COMPUTED);
    // Attributes
    testSimpleExpression("message active-form:nextform.", new DataType("Progress.Windows.IForm"));
    testSimpleExpression("message active-form:prowinHandle.", DataType.HANDLE);
    testSimpleExpression("message active-form:unknownAttribute.", DataType.NOT_COMPUTED);
    testSimpleExpression("message session:xml-data-type.", DataType.CHARACTER);
    testSimpleExpression("message session:file-mod-date.", DataType.DATE);
    testSimpleExpression("message session:seal-timestamp.", DataType.DATETIME_TZ);
    testSimpleExpression("message session:x-document.", DataType.HANDLE);
    testSimpleExpression("message session:year-offset.", DataType.INTEGER);
    testSimpleExpression("message session:form-long-input.", DataType.RAW);
    testSimpleExpression("message session:word-wrap.", DataType.LOGICAL);
    testSimpleExpression("message session:after-rowid.", DataType.ROWID);
  }

  @Test
  public void testObjectAttribute01() {
    var unit = getParseUnit("def var xx as Progress.Lang.Object. message xx:Next-Sibling.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp.getDataType().getClassName(), "Progress.Lang.Object");
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeObjectAttribute02() {
    TypeInfo typeInfo = new TypeInfo("rssw.test.Class02", false, false, "Progress.Lang.Object", "");
    typeInfo.addProperty(new PropertyElement("p1", false, DataType.CHARACTER));
    typeInfo.addVariable(new VariableElement("v1", DataType.LONGCHAR));
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void testObjectAttribute02() {
    var code = """
        class rssw.test.Class02:
          define property p1 as char get. set.
          define variable v1 as longchar.
          method void m1():
            this-object:p1.
            this-object:v1.
            this-object:Prev-Sibling.
            super:Next-Sibling.
            this-object:v2.
          end method.
        end class.
        """;
    var unit = getParseUnit(code, session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 5);

    assertTrue(nodes.get(0) instanceof AttributeReferenceNode);
    var exp1 = (AttributeReferenceNode) nodes.get(0);
    assertEquals(exp1.getDataType(), DataType.CHARACTER);
    assertTrue(exp1.isProperty());
    assertFalse(exp1.isVariable());
    assertEquals(exp1.getTypeInfo().getTypeName(), "rssw.test.Class02");
    assertNotNull(exp1.getPropertyElement());
    assertNull(exp1.getVariableElement());

    assertTrue(nodes.get(1) instanceof AttributeReferenceNode);
    var exp2 = (AttributeReferenceNode) nodes.get(1);
    assertEquals(exp2.getDataType(), DataType.LONGCHAR);
    assertFalse(exp2.isProperty());
    assertTrue(exp2.isVariable());
    assertEquals(exp2.getTypeInfo().getTypeName(), "rssw.test.Class02");
    assertNull(exp2.getPropertyElement());
    assertNotNull(exp2.getVariableElement());

    assertTrue(nodes.get(2) instanceof AttributeReferenceNode);
    var exp3 = (AttributeReferenceNode) nodes.get(2);
    assertEquals(exp3.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp3.getTypeInfo().getTypeName(), "Progress.Lang.Object");

    assertTrue(nodes.get(3) instanceof AttributeReferenceNode);
    var exp4 = (AttributeReferenceNode) nodes.get(3);
    assertEquals(exp4.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp4.getTypeInfo().getTypeName(), "Progress.Lang.Object");

    assertTrue(nodes.get(4) instanceof AttributeReferenceNode);
    var exp5 = (AttributeReferenceNode) nodes.get(4);
    assertEquals(exp5.getDataType(), DataType.NOT_COMPUTED);
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeObjectAttribute03() {
    var typeInfo = new TypeInfo("rssw.test.IFace01", true, false, "Progress.Lang.Object", "");
    typeInfo.addProperty(new PropertyElement("p1", false, DataType.INTEGER));
    session.injectTypeInfo(typeInfo);

    // When an interface inherits another one, the TypeInfo object still inherits from P.L.O. Inherited interface
    // are stored in the list of interfaces
    var typeInfo2 = new TypeInfo("rssw.test.IFace02", true, false, "Progress.Lang.Object", "", "rssw.test.IFace01");
    typeInfo2.addProperty(new PropertyElement("p2", false, DataType.CHARACTER));
    session.injectTypeInfo(typeInfo2);
  }

  @Test
  public void testObjectAttribute03() {
    var code = """
        define variable x1 as rssw.test.IFace02.
        message x1:p1.
        message x1:p2.
        """;
    var unit = getParseUnit(code, session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof AttributeReferenceNode);
    var exp1 = (AttributeReferenceNode) nodes.get(0);
    assertEquals(exp1.getDataType(), DataType.INTEGER);
    assertTrue(exp1.isProperty());
    assertFalse(exp1.isVariable());
    assertEquals(exp1.getTypeInfo().getTypeName(), "rssw.test.IFace01");

    assertTrue(nodes.get(1) instanceof AttributeReferenceNode);
    var exp2 = (AttributeReferenceNode) nodes.get(1);
    assertEquals(exp2.getDataType(), DataType.CHARACTER);
    assertTrue(exp2.isProperty());
    assertFalse(exp2.isVariable());
    assertEquals(exp2.getTypeInfo().getTypeName(), "rssw.test.IFace02");
  }

  @Test
  public void testEnumValues01() {
    var code = """
        message Progress.Reflect.AccessMode:Public.
        message Progress.Reflect.AccessMode:Private.
        """;
    var unit = getParseUnit(code, session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof AttributeReferenceNode);
    var exp1 = (AttributeReferenceNode) nodes.get(0);
    assertEquals(exp1.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp1.getDataType().getClassName(), "Progress.Reflect.AccessMode");
    assertEquals(exp1.getTypeInfo().getTypeName(), "Progress.Reflect.AccessMode");
    assertTrue(exp1.isProperty());
    assertFalse(exp1.isVariable());

    assertTrue(nodes.get(1) instanceof AttributeReferenceNode);
    var exp2 = (AttributeReferenceNode) nodes.get(1);
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp2.getDataType().getClassName(), "Progress.Reflect.AccessMode");
    assertEquals(exp2.getTypeInfo().getTypeName(), "Progress.Reflect.AccessMode");
    assertTrue(exp2.isProperty());
    assertFalse(exp2.isVariable());
  }

  @Test
  public void testEnumValues02() {
    var code = """
        using Progress.Reflect.AccessMode.
        message AccessMode:Public.
        message AccessMode:Private.
        """;
    var unit = getParseUnit(code, session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof AttributeReferenceNode);
    var exp1 = (AttributeReferenceNode) nodes.get(0);
    assertEquals(exp1.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp1.getDataType().getClassName(), "Progress.Reflect.AccessMode");
    assertEquals(exp1.getTypeInfo().getTypeName(), "Progress.Reflect.AccessMode");
    assertTrue(exp1.isProperty());
    assertFalse(exp1.isVariable());

    assertTrue(nodes.get(1) instanceof AttributeReferenceNode);
    var exp2 = (AttributeReferenceNode) nodes.get(1);
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp2.getDataType().getClassName(), "Progress.Reflect.AccessMode");
    assertEquals(exp2.getTypeInfo().getTypeName(), "Progress.Reflect.AccessMode");
    assertTrue(exp2.isProperty());
    assertFalse(exp2.isVariable());
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeStaticMethod() {
    TypeInfo typeInfo = new TypeInfo("rssw.test.Class04", false, false, "Progress.Lang.Object", "");
    typeInfo.addMethod(new MethodElement("m1", true, DataType.CHARACTER));
    typeInfo.addMethod(new MethodElement("m2", true, DataType.INTEGER));
    typeInfo.addMethod(new MethodElement("m2", true, DataType.INT64, //
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void testStaticMethod01() {
    String sourceCode = "message rssw.test.Class04:m1(). "
        + "message rssw.test.Class04:m2(). "
        + "message rssw.test.Class04:m2(123).";
    ParseUnit unit = getParseUnit(sourceCode, session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 3);

    IExpression exp1 = nodes.get(0);
    assertTrue(exp1 instanceof MethodCallNode);
    assertEquals(exp1.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);

    IExpression exp2 = nodes.get(1);
    assertTrue(exp2 instanceof MethodCallNode);
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);

    IExpression exp3 = nodes.get(2);
    assertTrue(exp3 instanceof MethodCallNode);
    assertEquals(exp3.getDataType().getPrimitive(), PrimitiveDataType.INT64);
  }

  @Test
  public void testStaticMethod02() {
    ParseUnit unit = getParseUnit("message rssw.test.Class04:m2(1).", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);

    assertTrue(nodes.get(0) instanceof MethodCallNode);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.INT64);
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeStaticProperty() {
    var typeInfo = new TypeInfo("rssw.test.Class08", false, false, "Progress.Lang.Object", "");
    typeInfo.addProperty(new PropertyElement("prop01", true, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("prop02", true, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("prop03", true, DataType.DECIMAL));
    session.injectTypeInfo(typeInfo);

    var typeInfo2 = new TypeInfo("rssw.test.Class08Child", false, false, "rssw.test.Class08", "");
    typeInfo2.addProperty(new PropertyElement("prop04", true, DataType.CHARACTER));
    typeInfo2.addProperty(new PropertyElement("prop05", true, DataType.INTEGER));
    typeInfo2.addProperty(new PropertyElement("prop06", true, DataType.DECIMAL));
    session.injectTypeInfo(typeInfo2);
  }

  @Test
  public void testStaticProperty01() {
    var sourceCode = """
        message rssw.test.Class08:prop01. 
        message rssw.test.Class08Child:prop02. 
        message rssw.test.Class08:prop03.
        message rssw.test.Class08Child:prop04.
        """;
    var unit = getParseUnit(sourceCode, session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 4);

    assertTrue(nodes.get(0) instanceof AttributeReferenceNode);
    var exp1 = (AttributeReferenceNode) nodes.get(0);
    assertEquals(exp1.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
    assertEquals(exp1.getTypeInfo().getTypeName(), "rssw.test.Class08");
    assertEquals(exp1.getNumberOfChildren(), 3);
    assertEquals(exp1.getDirectChildren().get(2).getNodeType(), ABLNodeType.ID);

    assertTrue(nodes.get(1) instanceof AttributeReferenceNode);
    var exp2 = (AttributeReferenceNode) nodes.get(1);
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
    assertEquals(exp2.getTypeInfo().getTypeName(), "rssw.test.Class08");

    assertTrue(nodes.get(2) instanceof AttributeReferenceNode);
    var exp3 = (AttributeReferenceNode) nodes.get(2);
    assertEquals(exp3.getDataType().getPrimitive(), PrimitiveDataType.DECIMAL);
    assertEquals(exp3.getTypeInfo().getTypeName(), "rssw.test.Class08");

    assertTrue(nodes.get(3) instanceof AttributeReferenceNode);
    var exp4 = (AttributeReferenceNode) nodes.get(3);
    assertEquals(exp4.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
    assertEquals(exp4.getTypeInfo().getTypeName(), "rssw.test.Class08Child");
  }

  @Test
  public void testStaticProperty02() {
    var sourceCode = """
        using rssw.test.Class08.
        message Class08:prop01. 
        message rssw.test.Class08Child:prop02. 
        message rssw.test.Class08:prop03.
        message rssw.test.Class08Child:prop04.
        """;
    var unit = getParseUnit(sourceCode, session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 4);

    assertTrue(nodes.get(0) instanceof AttributeReferenceNode);
    var exp1 = (AttributeReferenceNode) nodes.get(0);
    assertEquals(exp1.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
    assertEquals(exp1.getTypeInfo().getTypeName(), "rssw.test.Class08");
    assertEquals(exp1.getNumberOfChildren(), 3);
    assertEquals(exp1.getDirectChildren().get(2).getNodeType(), ABLNodeType.ID);
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeNonStaticProperty() {
    var typeInfo = new TypeInfo("rssw.test.Class09", false, false, "Progress.Lang.Object", "");
    typeInfo.addProperty(new PropertyElement("instance", true, new DataType("rssw.test.Class09")));
    typeInfo.addMethod(new MethodElement("methd01", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("prop01", false, DataType.CHARACTER));
    typeInfo.addProperty(new PropertyElement("prop02", false, DataType.INTEGER));
    typeInfo.addProperty(new PropertyElement("prop03", true, DataType.DECIMAL));
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void testNonStaticProperty01() {
    var sourceCode = """
        message rssw.test.Class09:instance:methd01().
        """;
    var unit = getParseUnit(sourceCode, session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);

    assertTrue(nodes.get(0) instanceof MethodCallNode);
    var exp1 = (MethodCallNode) nodes.get(0);
    assertEquals(exp1.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
    assertEquals(exp1.getTypeInfo().getTypeName(), "rssw.test.Class09");
    assertEquals(exp1.getNumberOfChildren(), 4);
    assertEquals(exp1.getDirectChildren().get(0).getNodeType(), ABLNodeType.ATTRIBUTE_REF);
    assertEquals(exp1.getDirectChildren().get(2).getNodeType(), ABLNodeType.ID);
    assertEquals(exp1.getDirectChildren().get(3).getNodeType(), ABLNodeType.METHOD_PARAM_LIST);

    var exp2 = (AttributeReferenceNode) exp1.getDirectChildren().get(0);
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp2.getTypeInfo().getTypeName(), "rssw.test.Class09");
    assertEquals(exp2.getNumberOfChildren(), 3);

    assertEquals(exp2.getDirectChildren().get(0).getNodeType(), ABLNodeType.FIELD_REF);
    assertEquals(exp2.getDirectChildren().get(2).getNodeType(), ABLNodeType.ID);
  }

  @Test
  public void testObjectMethod() {
    ParseUnit unit = getParseUnit(
        "def var xx as Progress.Lang.Object. message xx:toString(). message xx:UnknownMethod().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof MethodCallNode);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType(), DataType.CHARACTER);

    assertTrue(nodes.get(1) instanceof MethodCallNode);
    IExpression exp2 = nodes.get(1);
    assertEquals(exp2.getDataType(), DataType.NOT_COMPUTED);
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeObjectMethod02() {
    TypeInfo typeInfo = new TypeInfo("rssw.pct.Class02", false, false, "Progress.Lang.Object", "");
    typeInfo.addMethod(new MethodElement("m1", false, DataType.VOID));
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void testObjectMethod02() {
    ParseUnit unit = getParseUnit(
        "class rssw.pct.Class02: method void m1(): toString(). foobar(). end method. end class.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof LocalMethodCallNode);
    LocalMethodCallNode exp = (LocalMethodCallNode) nodes.get(0);
    assertNotNull(exp.getTypeInfo());
    assertNotNull(exp.getMethodElement());
    assertEquals(exp.getTypeInfo().getTypeName(), "Progress.Lang.Object");
    assertEquals(exp.getMethodName(), "toString");
    assertEquals(exp.getDataType(), DataType.CHARACTER);

    assertTrue(nodes.get(1) instanceof LocalMethodCallNode);
    LocalMethodCallNode exp2 = (LocalMethodCallNode) nodes.get(1);
    assertNull(exp2.getMethodElement());
    assertNull(exp2.getTypeInfo());
    assertEquals(exp2.getMethodName(), "foobar");
    assertEquals(exp2.getDataType(), DataType.NOT_COMPUTED);
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeObjectMethod03() {
    TypeInfo typeInfo = new TypeInfo("rssw.test.Class03", false, false, "Progress.Lang.Object", "");
    typeInfo.addMethod(new MethodElement("m1", false, DataType.VOID));
    typeInfo.addMethod(new MethodElement("m2", false, DataType.INT64));
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void testObjectMethod03() {
    ParseUnit unit = getParseUnit(
        "class rssw.test.Class03: method void m1(): this-object:m2(). super:toString(). super:unknown(). end method. method int64 m2(): return 0. end method. end class.",
        session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 4);

    assertTrue(nodes.get(0) instanceof MethodCallNode);
    MethodCallNode exp = (MethodCallNode) nodes.get(0);
    assertNotNull(exp.getMethodElement());
    assertEquals(exp.getTypeInfo().getTypeName(), "rssw.test.Class03");
    assertEquals(exp.getMethodName(), "m2");
    assertEquals(exp.getDataType(), DataType.INT64);

    assertTrue(nodes.get(1) instanceof MethodCallNode);
    MethodCallNode exp2 = (MethodCallNode) nodes.get(1);
    assertEquals(exp2.getTypeInfo().getTypeName(), "Progress.Lang.Object");
    assertNotNull(exp2.getMethodElement());
    assertEquals(exp2.getMethodName(), "toString");
    assertEquals(exp2.getDataType(), DataType.CHARACTER);

    assertTrue(nodes.get(2) instanceof MethodCallNode);
    MethodCallNode exp3 = (MethodCallNode) nodes.get(2);
    assertNull(exp3.getMethodElement());
    assertNull(exp3.getTypeInfo());
    assertEquals(exp3.getMethodName(), "unknown");
    assertEquals(exp3.getDataType(), DataType.NOT_COMPUTED);
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeObjectMethod04() {
    TypeInfo typeInfo = new TypeInfo("rssw.test.Class06", false, false, "Progress.Lang.Object", "");
    typeInfo.addMethod(new MethodElement("m1", false, DataType.VOID));
    typeInfo.addMethod(new MethodElement("m2", false, DataType.HANDLE));
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void testObjectMethod04() {
    ParseUnit unit = getParseUnit("class rssw.test.Class06: " //
        + "method void m1():" //
        + " m2():add-super-procedure(xx). " //
        + "end method. " //
        + "method handle m2():" //
        + " return session. " //
        + "end method. " //
        + "end class.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof MethodCallNode);
    MethodCallNode exp = (MethodCallNode) nodes.get(0);
    assertEquals(exp.getMethodName(), "add-super-procedure");
    assertEquals(exp.getDataType(), DataType.LOGICAL);
  }

  @Test
  public void testObjectMethod05() {
    ParseUnit unit = getParseUnit("class rssw.test.Class07: " //
        + "constructor Class07():" //
        + " this-object(1). " //
        + "end constructor. " //
        + "constructor Class07(xx as int):" //
        + " super(1). " //
        + "end constructor. " //
        + "end class.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof MethodCallNode);
    MethodCallNode exp = (MethodCallNode) nodes.get(0);
    assertNull(exp.getMethodElement()); // Currently not available
    assertEquals(exp.getDataType().getClassName(), "rssw.test.Class07");

    assertTrue(nodes.get(1) instanceof MethodCallNode);
    MethodCallNode exp2 = (MethodCallNode) nodes.get(1);
    assertNull(exp2.getMethodElement()); // Currently not available
    assertEquals(exp2.getDataType().getClassName(), "rssw.test.Class07");
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeObjectMethod06() {
    var typeInfo = new TypeInfo("rssw.test.Class10", false, false, "Progress.Lang.Object", "");
    typeInfo.addMethod(new MethodElement("method01", false, DataType.VOID, //
        new Parameter(1, "prm1", 0, ParameterMode.INPUT_OUTPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("method01", false, DataType.VOID, //
        new Parameter(1, "prm1", 0, ParameterMode.OUTPUT, DataType.CHARACTER)));
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void testObjectMethod06() {
    var unit = getParseUnit("""
        var character xx.
        var rssw.test.Class10 obj.
        obj:method01(input xx).
        obj:method01(input-output xx).
        obj:method01(output xx).
        obj:method01(buffer bTable).
        """, session);
    unit.treeParser01();

    var nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 4);

    assertTrue(nodes.get(0) instanceof MethodCallNode);
    var exp = (MethodCallNode) nodes.get(0);
    assertNotNull(exp.getMethodElement());
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.VOID);

    assertTrue(nodes.get(1) instanceof MethodCallNode);
    var exp2 = (MethodCallNode) nodes.get(1);
    assertNotNull(exp2.getMethodElement());
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.VOID);

    assertTrue(nodes.get(2) instanceof MethodCallNode);
    var exp3 = (MethodCallNode) nodes.get(2);
    assertNotNull(exp3.getMethodElement());
    assertEquals(exp3.getDataType().getPrimitive(), PrimitiveDataType.VOID);

    assertTrue(nodes.get(3) instanceof MethodCallNode);
    var exp4 = (MethodCallNode) nodes.get(3);
    assertNull(exp4.getMethodElement());
  }

  @Test
  public void testFunctions() {
    ParseUnit unit = getParseUnit(
        "function f1 returns char(): end function. function f2 returns int64(): end function. f1(). f2().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof UserFunctionCallNode);
    UserFunctionCallNode exp = (UserFunctionCallNode) nodes.get(0);
    assertEquals(exp.getFunctionName(), "f1");
    assertEquals(exp.getDataType(), DataType.CHARACTER);

    assertTrue(nodes.get(1) instanceof UserFunctionCallNode);
    UserFunctionCallNode exp2 = (UserFunctionCallNode) nodes.get(1);
    assertEquals(exp2.getFunctionName(), "f2");
    assertEquals(exp2.getDataType(), DataType.INT64);
  }

  @Test
  public void testHandleAttribute() {
    ParseUnit unit = getParseUnit("define frame frm1. message frame frm1:box.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);
  }

  @Test
  public void testInlineVariable01() {
    ParseUnit unit = getParseUnit("message 'xx' update lVar as logical.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);
    IExpression exp = nodes.get(1);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);
  }

  @Test
  public void testInlineVariable02() {
    ParseUnit unit = getParseUnit("message 'xx' update lVar as integer.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);
    IExpression exp = nodes.get(1);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
  }

  @Test
  public void testInlineVariable03() {
    ParseUnit unit = getParseUnit("def var zz as decimal. message 'xx' update lVar like zz.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 3);
    IExpression exp = nodes.get(1);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.DECIMAL);
  }

  @Test
  public void testInlineVariable04() {
    // Not a good test case, this has to be removed. The way inline variables are handled has to be rewritten,
    // as it is the only reason why we have to maintain the ParseSupport.currentScope object from JPNodeVisitor
    // This test case just ensures that the scope is set correctly, as a bug was detected late in the dev cycle
    // that broke the parser with NPE in some inline variables cases
    ParseUnit unit01 = getParseUnit("procedure p1: message 'xx' update lVar as log. end.", session);
    unit01.treeParser01();
    ParseUnit unit02 = getParseUnit("function f1 returns char(): message 'xx' update lVar as log. end.", session);
    unit02.treeParser01();
    ParseUnit unit03 = getParseUnit("on choose of btn1 do: message 'xx' update lVar as log. end.", session);
    unit03.treeParser01();
    ParseUnit unit04 = getParseUnit("class cls1: method public void m1(): message 'xx' update lVar as log. end. end.", session);
    unit04.treeParser01();
  }

  @Test
  public void testInlineVariable05() {
    // See previous unit test, this is still not good code...
    ParseUnit unit01 = getParseUnit("message 'X' set x1 as logical.", session);
    unit01.treeParser01();
    Variable x1 = unit01.getRootScope().getVariable("x1");
    assertNotNull(x1);
    assertNotNull(x1.getDataType());
    assertEquals(x1.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);

    ParseUnit unit02 = getParseUnit("message 'X' set x1 as logical. message 'X' set x1.", session);
    unit02.treeParser01();
    Variable x2 = unit02.getRootScope().getVariable("x1");
    assertNotNull(x2);
    assertNotNull(x2.getDataType());
    assertEquals(x2.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);
  }

  public void testSingleArgGetExpression(String expr) {
    ParseUnit unit01 = getParseUnit(expr, session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    IExpression innerExp = ((SingleArgumentExpression) exp).getExpression();
    assertNotEquals(exp, innerExp);
    assertEquals(innerExp.getDataType(), DataType.INTEGER);
  }

  @Test
  public void testSingleArgGetExpression01() {
    testSingleArgGetExpression("+ 45.");
  }

  @Test
  public void testSingleArgGetExpression02() {
    testSingleArgGetExpression("- 45.");
  }

  @Test
  public void testSingleArgGetExpression03() {
    testSingleArgGetExpression("( 45).");
  }

  @Test
  public void testTwoArgsGetExpression01() {
    ParseUnit unit01 = getParseUnit("18.5 + 45.", session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    IExpression leftExp = ((TwoArgumentsExpression) exp).getLeftExpression();
    IExpression rightExp = ((TwoArgumentsExpression) exp).getRightExpression();
    assertNotEquals(exp, leftExp);
    assertNotEquals(exp, rightExp);
    assertNotEquals(leftExp, rightExp);
    assertEquals(leftExp.getDataType(), DataType.DECIMAL);
    assertEquals(rightExp.getDataType(), DataType.INTEGER);
  }

  private void testSimpleExpression(String code, DataType expected) {
    ParseUnit unit01 = getParseUnit(code, session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), expected.getPrimitive());
    if (expected.getPrimitive() == PrimitiveDataType.CLASS)
      assertEquals(exp.getDataType().getClassName(), expected.getClassName());
  }

  @Test
  public void testArrayGetExpressions01() {
    ParseUnit unit01 = getParseUnit("x1[10] + x2[20].", session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    IExpression leftExp = ((TwoArgumentsExpression) exp).getLeftExpression();
    IExpression rightExp = ((TwoArgumentsExpression) exp).getRightExpression();

    assertTrue(leftExp instanceof ArrayReferenceNode);
    assertTrue(rightExp instanceof ArrayReferenceNode);
    assertTrue(((ArrayReferenceNode) leftExp).getVariableExpression() instanceof FieldRefNode);
    assertTrue(((ArrayReferenceNode) rightExp).getVariableExpression() instanceof FieldRefNode);
    assertTrue(((ArrayReferenceNode) leftExp).getOffsetExpression() instanceof ConstantNode);
    assertTrue(((ArrayReferenceNode) rightExp).getOffsetExpression() instanceof ConstantNode);
  }

  @Test
  public void testArrayGetExpressions02() {
    ParseUnit unit01 = getParseUnit("do x1[10] = 1 to 10: end.", session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 3);
    IExpression exp = nodes.get(0);

    assertTrue(exp instanceof ArrayReferenceNode);
    assertTrue(((ArrayReferenceNode) exp).getVariableExpression() instanceof FieldRefNode);
    assertTrue(((ArrayReferenceNode) exp).getOffsetExpression() instanceof ConstantNode);
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeOverloadedMethodCall() {
    TypeInfo typeInfo = new TypeInfo("rssw.test.Class05", false, false, "Progress.Lang.Object", "");
    typeInfo.addMethod(new MethodElement("over01", true, DataType.CHARACTER));
    typeInfo.addMethod(new MethodElement("over01", true, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    typeInfo.addMethod(new MethodElement("over01", true, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("over01", true, DataType.INT64, //
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER), //
        new Parameter(2, "prm2", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void testOverloadedMethodCall01() {
    String sourceCode = "def var var1 as rssw.test.Class05. "
        + "var1:over01(). "
        + "var1:over01(123). "
        + "var1:over01('123'). "
        + "var1:over01('123', '456').";
    ParseUnit unit01 = getParseUnit(sourceCode, session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 4);

    IExpression exp1 = nodes.get(0);
    assertTrue(exp1 instanceof MethodCallNode);
    assertEquals(exp1.getDataType(), DataType.CHARACTER);

    IExpression exp2 = nodes.get(1);
    assertTrue(exp2 instanceof MethodCallNode);
    assertEquals(exp2.getDataType(), DataType.VOID);

    IExpression exp3 = nodes.get(2);
    assertTrue(exp3 instanceof MethodCallNode);
    assertEquals(exp3.getDataType(), DataType.INTEGER);

    IExpression exp4 = nodes.get(3);
    assertTrue(exp4 instanceof MethodCallNode);
    assertEquals(exp4.getDataType(), DataType.INT64);
  }

  @Test
  public void testOverloadedMethodCall02() {
    TypeInfo testClass = new TypeInfo("rssw.MyTestClass", false, false, "rssw.test.Class05", "");
    testClass.addMethod(new MethodElement("test01", false, DataType.VOID));
    session.injectTypeInfo(testClass);

    String sourceCode = "class rssw.MyTestClass inherits rssw.test.Class05: "
        + "method public test01(): "
        + "over01(123). "
        + "this-object:over01('123'). "
        + "super:over01('123', '456'). "
        + "end method. "
        + "end class.";
    ParseUnit unit01 = getParseUnit(sourceCode, session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 3);

    IExpression exp1 = nodes.get(0);
    assertTrue(exp1 instanceof LocalMethodCallNode);
    assertEquals(exp1.getDataType(), DataType.VOID);

    IExpression exp2 = nodes.get(1);
    assertTrue(exp2 instanceof MethodCallNode);
    assertEquals(exp2.getDataType(), DataType.INTEGER);

    IExpression exp3 = nodes.get(2);
    assertTrue(exp3 instanceof MethodCallNode);
    assertEquals(exp3.getDataType(), DataType.INT64);
  }

  @Test
  public void testSuperConstructor() {
    TypeInfo testClass = new TypeInfo("rssw.MyTestClass", false, false, "rssw.test.Class05", "");
    session.injectTypeInfo(testClass);

    String sourceCode = "class rssw.MyTestClass inherits rssw.test.Class05: "
        + "constructor public MyTestClass(): "
        + "  super(). "
        + "end constructor. "
        + "constructor public MyTestClass(xyz as int): "
        + "  this-object(). "
        + "end constructor. "
        + "end class.";
    ParseUnit unit01 = getParseUnit(sourceCode, session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    IExpression exp1 = nodes.get(0);
    assertTrue(exp1 instanceof MethodCallNode);
    assertEquals(exp1.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp1.getDataType().getClassName(), "rssw.MyTestClass"); // Suspicious
    IExpression exp2 = nodes.get(1);
    assertTrue(exp2 instanceof MethodCallNode);
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp2.getDataType().getClassName(), "rssw.MyTestClass");
  }

  @Test
  public void testEvent01() {
    String sourceCode = "class rssw.MyTestClass: "
        + "define public event myEvent01 signature void(). "
        + "constructor MyTestClass(): "
        + "  myEvent01:publish(). "
        + "  myEvent01:subscribe(m1). "
        + "end constructor. "
        + "method public void m1(): end method. "
        + "end class.";
    ParseUnit unit01 = getParseUnit(sourceCode, session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    IExpression exp1 = nodes.get(0);
    assertTrue(exp1 instanceof MethodCallNode);
    assertEquals(exp1.getDataType().getPrimitive(), PrimitiveDataType.VOID);
    IExpression exp2 = nodes.get(1);
    assertTrue(exp2 instanceof MethodCallNode);
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.VOID);
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeCatalog01() {
    TypeInfo typeInfo = new TypeInfo("rssw.MyTestClassCatalog", false, false, "System.Windows.Forms.Control", "");
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void testCatalog01() {
    String sourceCode = "class rssw.MyTestClassCatalog inherits System.Windows.Forms.Control: "
        + "constructor MyTestClassCatalog(): "
        + "  message Size. "
        + "  message Size:Width. "
        + "end constructor. "
        + "end class.";
    ParseUnit unit01 = getParseUnit(sourceCode, session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);
    IExpression exp1 = nodes.get(0);
    assertEquals(exp1.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp1.getDataType().getClassName(), "System.Drawing.Size");
    IExpression exp2 = nodes.get(1);
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
  }

  @BeforeMethod(dependsOnMethods = "setUp")
  public void beforeSignature01() {
    TypeInfo typeInfo = new TypeInfo("rssw.test.FooClass01", false, false, "Progress.Lang.Object", "");
    typeInfo.addMethod(new MethodElement("Foo", false, DataType.CHARACTER, //
        new Parameter(1, "prm1", 0, ParameterMode.INPUT_OUTPUT, DataType.CHARACTER)));
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void testSignature01() {
    String sourceCode = "class rssw.test.FooClass01:"
        + "  constructor FooClass01( ):"
        + "    this-object:Foo('')."
        + "  end constructor."
        + "  method public character Foo(input-output pcTest as character):"
        + "  end method. "
        + "end class.";
    ParseUnit unit01 = getParseUnit(sourceCode, session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp1 = nodes.get(0);
    assertEquals(exp1.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
  }
  
  @Test
  public void testGenerics01() {
    testSimpleExpression(
        "def var xx as Progress.Collections.IMap<Employee,Manager>. message new Progress.Collections.IMap<Employee,Manager>().",
        new DataType("Progress.Collections.IMap"));
    testSimpleExpression(
        "def var xx as class Progress.Collections.IMap<Employee,Manager>. message new Progress.Collections.IMap<Employee,Manager>().",
        new DataType("Progress.Collections.IMap"));
    testSimpleExpression(
        "def var xx as Progress.Collections.IMap<OpenEdge.Core.String,OpenEdge.Core.String>. message new Progress.Collections.IMap<OpenEdge.Core.String,OpenEdge.Core.String>().",
        new DataType("Progress.Collections.IMap"));
    testSimpleExpression(
        "def var xx as class Progress.Collections.IMap<OpenEdge.Core.String,OpenEdge.Core.String>. message new Progress.Collections.IMap<OpenEdge.Core.String,OpenEdge.Core.String>().",
        new DataType("Progress.Collections.IMap"));
    testSimpleExpression(
        "def var xx as Progress.Collections.IMap<Progress.Collections.ListIterator<OpenEdge.Core.String>,OpenEdge.Core.String>. message new Progress.Collections.IMap<Progress.Collections.ListIterator<OpenEdge.Core.String>,OpenEdge.Core.String>().",
        new DataType("Progress.Collections.IMap"));

  } 


}
