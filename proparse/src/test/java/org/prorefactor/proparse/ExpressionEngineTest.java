/********************************************************************************
 * Copyright (c) 2015-2023 Riverside Software
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
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

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
import org.prorefactor.core.nodetypes.SingleArgumentExpression;
import org.prorefactor.core.nodetypes.TwoArgumentsExpression;
import org.prorefactor.core.nodetypes.UserFunctionCallNode;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.symbols.Variable;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.PrimitiveDataType;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
import eu.rssw.pct.elements.fixed.PropertyElement;
import eu.rssw.pct.elements.fixed.TypeInfo;
import eu.rssw.pct.elements.fixed.VariableElement;

public class ExpressionEngineTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException, InvalidRCodeException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);

    // For testObjectAttribute02
    TypeInfo typeInfo01 = new TypeInfo("rssw.test.Class02", false, false, "Progress.Lang.Object", "");
    typeInfo01.addProperty(new PropertyElement("p1", false, DataType.CHARACTER));
    typeInfo01.addVariable(new VariableElement("v1", DataType.LONGCHAR));
    session.injectTypeInfo(typeInfo01);

    // For testObjectMethod03
    TypeInfo typeInfo02 = new TypeInfo("rssw.test.Class03", false, false, "Progress.Lang.Object", "");
    typeInfo02.addMethod(new MethodElement("m1", false, DataType.VOID));
    typeInfo02.addMethod(new MethodElement("m2", false, DataType.INT64));
    session.injectTypeInfo(typeInfo02);

    // For testStaticMethod
    TypeInfo typeInfo03 = new TypeInfo("rssw.test.Class04", false, false, "Progress.Lang.Object", "");
    typeInfo03.addMethod(new MethodElement("m1", true, DataType.CHARACTER));
    typeInfo03.addMethod(new MethodElement("m2", true, DataType.INTEGER));
    typeInfo03.addMethod(new MethodElement("m2", true, DataType.INT64, new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    session.injectTypeInfo(typeInfo03);
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
    ParseUnit unit = new ParseUnit(
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
    ParseUnit unit = new ParseUnit(

        "define temp-table tt1 field fld1 as int extent. define buffer b1 for tt1. buffer b1::fld1(1).", session);
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
  }

  @Test
  public void testMethod01() {
    ParseUnit unit = new ParseUnit("session:get-printers().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    assertEquals(nodes.get(0).getDataType(), DataType.CHARACTER);
  }

  @Test
  public void testMethod02() {
    ParseUnit unit = new ParseUnit("compiler:get-row().", session);
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
    ParseUnit unit = new ParseUnit("etime(true). recid(customer).", session);
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
    ParseUnit unit = new ParseUnit("function f1 returns char () forwards. message f1().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType(), DataType.CHARACTER);
  }

  @Test
  public void testNewObject01() {
    ParseUnit unit = new ParseUnit("def var xx as Progress.Lang.Object. message new Progress.Lang.Object().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp.getDataType().getClassName(), "Progress.Lang.Object");
  }

  @Test
  public void testNewObject02() {
    ParseUnit unit = new ParseUnit("def var xx as Progress.Lang.Object. message new Progress.Lang.Object():toString().",
        session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
  }

  @Test
  public void testNewObject03() {
    ParseUnit unit = new ParseUnit(
        "def var xx as Progress.Lang.Object. message new Progress.Lang.Object():GetClass():HasStatics().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);
  }

  @Test
  public void testIfExpr01() {
    ParseUnit unit = new ParseUnit("message (if true then 'abc' else 'def').", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
  }

  @Test
  public void testIfExpr02() {
    ParseUnit unit = new ParseUnit("message (if true then 123 else 456).", session);
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
    ParseUnit unit = new ParseUnit("def var xx as Progress.Lang.Object. message xx:Next-Sibling.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp.getDataType().getClassName(), "Progress.Lang.Object");
  }

  @Test
  public void testObjectAttribute02() {
    ParseUnit unit = new ParseUnit(
        "class rssw.test.Class02: define property p1 as char get. set. define variable v1 as longchar. method void m1(): this-object:p1. this-object:v1. super:Prev-Sibling. super:Next-Sibling. end method. end class.",
        session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 4);

    assertTrue(nodes.get(0) instanceof AttributeReferenceNode);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType(), DataType.CHARACTER);

    assertTrue(nodes.get(1) instanceof AttributeReferenceNode);
    IExpression exp2 = nodes.get(1);
    assertEquals(exp2.getDataType(), DataType.LONGCHAR);

    assertTrue(nodes.get(2) instanceof AttributeReferenceNode);
    IExpression exp3 = nodes.get(2);
    assertEquals(exp3.getDataType().getPrimitive(), PrimitiveDataType.CLASS);

    assertTrue(nodes.get(3) instanceof AttributeReferenceNode);
    IExpression exp4 = nodes.get(3);
    assertEquals(exp4.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
  }

  @Test
  public void testEnumValues01() {
    ParseUnit unit = new ParseUnit(
        "message Progress.Reflect.AccessMode:Public. message Progress.Reflect.AccessMode:Private.",
        session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof AttributeReferenceNode);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp.getDataType().getClassName(), "Progress.Reflect.AccessMode");

    assertTrue(nodes.get(1) instanceof AttributeReferenceNode);
    IExpression exp2 = nodes.get(1);
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp2.getDataType().getClassName(), "Progress.Reflect.AccessMode");
  }

  @Test
  public void testEnumValues02() {
    ParseUnit unit = new ParseUnit(
        "using Progress.Reflect.AccessMode. message AccessMode:Public. message AccessMode:Private.",
        session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof AttributeReferenceNode);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp.getDataType().getClassName(), "Progress.Reflect.AccessMode");

    assertTrue(nodes.get(1) instanceof AttributeReferenceNode);
    IExpression exp2 = nodes.get(1);
    assertEquals(exp2.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp2.getDataType().getClassName(), "Progress.Reflect.AccessMode");
  }

  @Test
  public void testStaticMethod01() {
    ParseUnit unit = new ParseUnit("message rssw.test.Class04:m1().", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);

    assertTrue(nodes.get(0) instanceof MethodCallNode);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
  }

  @Test
  public void testStaticMethod02() {
    ParseUnit unit = new ParseUnit("message rssw.test.Class04:m2(1).", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);

    assertTrue(nodes.get(0) instanceof MethodCallNode);
    IExpression exp = nodes.get(0);
    // Wrong answer. Current implementation doesn't take parameters into account.
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
  }

  @Test
  public void testObjectMethod() {
    ParseUnit unit = new ParseUnit(
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

  @Test
  public void testObjectMethod02() {
    ParseUnit unit = new ParseUnit("class rssw.pct: method void m1(): toString(). foobar(). end method. end class.",
        session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0) instanceof LocalMethodCallNode);
    LocalMethodCallNode exp = (LocalMethodCallNode) nodes.get(0);
    assertEquals(exp.getMethodName(), "toString");
    assertEquals(exp.getDataType(), DataType.CHARACTER);

    assertTrue(nodes.get(1) instanceof LocalMethodCallNode);
    LocalMethodCallNode exp2 = (LocalMethodCallNode) nodes.get(1);
    assertEquals(exp2.getMethodName(), "foobar");
    assertEquals(exp2.getDataType(), DataType.NOT_COMPUTED);
  }

  @Test
  public void testObjectMethod03() {
    ParseUnit unit = new ParseUnit(
        "class rssw.test.Class03: method void m1(): this-object:m2(). super:toString(). end method. method int64 m2(): return 0. end method. end class.",
        session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 3);

    assertTrue(nodes.get(0) instanceof MethodCallNode);
    MethodCallNode exp = (MethodCallNode) nodes.get(0);
    assertEquals(exp.getMethodName(), "m2");
    assertEquals(exp.getDataType(), DataType.INT64);

    assertTrue(nodes.get(1) instanceof MethodCallNode);
    MethodCallNode exp2 = (MethodCallNode) nodes.get(1);
    assertEquals(exp2.getMethodName(), "toString");
    assertEquals(exp2.getDataType(), DataType.CHARACTER);
  }

  @Test
  public void testFunctions() {
    ParseUnit unit = new ParseUnit(
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
    ParseUnit unit = new ParseUnit("define frame frm1. message frame frm1:box.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);
  }

  @Test
  public void testInlineVariable01() {
    ParseUnit unit = new ParseUnit("message 'xx' update lVar as logical.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);
    IExpression exp = nodes.get(1);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);
  }

  @Test
  public void testInlineVariable02() {
    ParseUnit unit = new ParseUnit("message 'xx' update lVar as integer.", session);
    unit.treeParser01();

    List<IExpression> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);
    IExpression exp = nodes.get(1);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
  }

  @Test
  public void testInlineVariable03() {
    ParseUnit unit = new ParseUnit("def var zz as decimal. message 'xx' update lVar like zz.", session);
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
    ParseUnit unit01 = new ParseUnit("procedure p1: message 'xx' update lVar as log. end.", session);
    unit01.treeParser01();
    ParseUnit unit02 = new ParseUnit("function f1 returns char(): message 'xx' update lVar as log. end.", session);
    unit02.treeParser01();
    ParseUnit unit03 = new ParseUnit("on choose of btn1 do: message 'xx' update lVar as log. end.", session);
    unit03.treeParser01();
    ParseUnit unit04 = new ParseUnit("class cls1: method public void m1(): message 'xx' update lVar as log. end. end.", session);
    unit04.treeParser01();
  }

  @Test
  public void testInlineVariable05() {
    // See previous unit test, this is still not good code...
    ParseUnit unit01 = new ParseUnit("message 'X' set x1 as logical.", session);
    unit01.treeParser01();
    Variable x1 = unit01.getRootScope().getVariable("x1");
    assertNotNull(x1);
    assertNotNull(x1.getDataType());
    assertEquals(x1.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);

    ParseUnit unit02 = new ParseUnit("message 'X' set x1 as logical. message 'X' set x1.", session);
    unit02.treeParser01();
    Variable x2 = unit02.getRootScope().getVariable("x1");
    assertNotNull(x2);
    assertNotNull(x2.getDataType());
    assertEquals(x2.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);
  }

  @Test
  public void testSingleArgGetExpression01() {
    ParseUnit unit01 = new ParseUnit("+ 45.", session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    IExpression innerExp = ((SingleArgumentExpression) exp).getExpression();
    assertNotEquals(exp, innerExp);
    assertEquals(innerExp.getDataType(), DataType.INTEGER);
  }

  @Test
  public void testSingleArgGetExpression02() {
    ParseUnit unit01 = new ParseUnit("- 45.", session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    IExpression innerExp = ((SingleArgumentExpression) exp).getExpression();
    assertNotEquals(exp, innerExp);
    assertEquals(innerExp.getDataType(), DataType.INTEGER);
  }

  @Test
  public void testSingleArgGetExpression03() {
    ParseUnit unit01 = new ParseUnit("( 45).", session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = nodes.get(0);
    IExpression innerExp = ((SingleArgumentExpression) exp).getExpression();
    assertNotEquals(exp, innerExp);
    assertEquals(innerExp.getDataType(), DataType.INTEGER);
  }

  @Test
  public void testTwoArgsGetExpression01() {
    ParseUnit unit01 = new ParseUnit("18.5 + 45.", session);
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
    ParseUnit unit01 = new ParseUnit(code, session);
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
    ParseUnit unit01 = new ParseUnit("x1[10] + x2[20].", session);
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
    ParseUnit unit01 = new ParseUnit("do x1[10] = 1 to 10: end.", session);
    unit01.treeParser01();

    List<IExpression> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 3);
    IExpression exp = nodes.get(0);

    assertTrue(exp instanceof ArrayReferenceNode);
    assertTrue(((ArrayReferenceNode) exp).getVariableExpression() instanceof FieldRefNode);
    assertTrue(((ArrayReferenceNode) exp).getOffsetExpression() instanceof ConstantNode);
  }

}
