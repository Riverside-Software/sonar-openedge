package org.prorefactor.proparse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.AttributeReferenceNode;
import org.prorefactor.core.nodetypes.BuiltinFunctionNode;
import org.prorefactor.core.nodetypes.IExpression;
import org.prorefactor.core.nodetypes.LocalMethodCallNode;
import org.prorefactor.core.nodetypes.MethodCallNode;
import org.prorefactor.core.nodetypes.NamedMemberArrayNode;
import org.prorefactor.core.nodetypes.NamedMemberNode;
import org.prorefactor.core.nodetypes.UserFunctionCallNode;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.PrimitiveDataType;

public class ExpressionEngineTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException, InvalidRCodeException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testUnaryExpression() {
    testSimpleExpression("+ 45", DataType.INTEGER);
    testSimpleExpression("- 45", DataType.INTEGER);
    testSimpleExpression("- -45.112", DataType.DECIMAL);
    testSimpleExpression("def var xx as log. not xx.", DataType.LOGICAL);
    testSimpleExpression("def var xx as log. not (not xx).", DataType.LOGICAL);
  }

  @Test
  public void testNamedMember01() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("define temp-table tt1 field fld1 as int. define buffer b1 for tt1. buffer b1::fld1.".getBytes()), session);
    unit.treeParser01();
    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    assertTrue(nodes.get(0).isExpression());
    NamedMemberNode exp = (NamedMemberNode) nodes.get(0);
    assertEquals(exp.getNamedMember(), "fld1");
    assertEquals(exp.getDataType(), DataType.NOT_COMPUTED);
  }

  @Test
  public void testNamedMemberArray01() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("define temp-table tt1 field fld1 as int extent. define buffer b1 for tt1. buffer b1::fld1(1).".getBytes()), session);
    unit.treeParser01();
    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    assertTrue(nodes.get(0).isExpression());
    NamedMemberArrayNode exp = (NamedMemberArrayNode) nodes.get(0);
    assertEquals(exp.getNamedMember(), "fld1");
    assertEquals(exp.getDataType(), DataType.NOT_COMPUTED);
  }

  @Test
  public void testTwoArguments() {
    testSimpleExpression("1 + 1", DataType.INTEGER);
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
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("session:get-printers().".getBytes()), session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType(), DataType.CHARACTER);
  }

  @Test
  public void testMethod02() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("compiler:get-row().".getBytes()), session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType(), DataType.INTEGER);
  }

  @Test
  public void testBuiltinFunctions() {
    testSimpleExpression("def var hnd as handle. valid-handle(hnd).", DataType.LOGICAL);
    testSimpleExpression("def var xxx as Progress.Lang.Object. get-class(xxx).", new DataType("Progress.Lang.Class"));
    testSimpleExpression("def var xx as Progress.Lang.Object. message cast(new Progress.Lang.Object(), Progress.Lang.Class).",
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
  }

  @Test
  public void testSideEffect() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("etime(true). recid(customer).".getBytes()), session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);
    IExpression exp1 = (IExpression) nodes.get(0);
    assertTrue(exp1 instanceof BuiltinFunctionNode);
    assertTrue(((BuiltinFunctionNode) exp1).hasSideEffect());
    IExpression exp2 = (IExpression) nodes.get(1);
    assertTrue(exp2 instanceof BuiltinFunctionNode);
    assertFalse(((BuiltinFunctionNode) exp2).hasSideEffect());
  }

  @Test
  public void testFunction01() {
    ParseUnit unit = new ParseUnit(
        new ByteArrayInputStream("function f1 returns char () forwards. message f1().".getBytes()), session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType(), DataType.CHARACTER);
  }

  @Test
  public void testNewObject01() {
    ParseUnit unit = new ParseUnit(
        new ByteArrayInputStream("def var xx as Progress.Lang.Object. message new Progress.Lang.Object().".getBytes()),
        session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp.getDataType().getClassName(), "Progress.Lang.Object");
  }

  @Test
  public void testNewObject02() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream(
        "def var xx as Progress.Lang.Object. message new Progress.Lang.Object():toString().".getBytes()), session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
  }

  @Test
  public void testNewObject03() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream(
        "def var xx as Progress.Lang.Object. message new Progress.Lang.Object():GetClass():HasStatics().".getBytes()),
        session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);
  }

  @Test
  public void testIfExpr01() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("message (if true then 'abc' else 'def').".getBytes()),
        session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
  }

  @Test
  public void testIfExpr02() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("message (if true then 123 else 456).".getBytes()),
        session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = (IExpression) nodes.get(0);
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
    testSimpleExpression("message active-form:nextform", new DataType("Progress.Windows.IForm"));
    testSimpleExpression("message active-form:prowinHandle", DataType.HANDLE);
    testSimpleExpression("message active-form:unknownAttribute", DataType.NOT_COMPUTED);
    testSimpleExpression("message session:xml-data-type", DataType.CHARACTER);
    testSimpleExpression("message session:file-mod-date", DataType.DATE);
    testSimpleExpression("message session:seal-timestamp", DataType.DATETIME_TZ);
    testSimpleExpression("message session:x-document", DataType.HANDLE);
    testSimpleExpression("message session:year-offset", DataType.INTEGER);
    testSimpleExpression("message session:form-long-input", DataType.RAW);
    testSimpleExpression("message session:word-wrap", DataType.LOGICAL);
    testSimpleExpression("message session:after-rowid", DataType.ROWID);
  }

  @Test
  public void testObjectAttribute01() {
    ParseUnit unit = new ParseUnit(
        new ByteArrayInputStream("def var xx as Progress.Lang.Object. message xx:Next-Sibling.".getBytes()), session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(exp.getDataType().getClassName(), "Progress.Lang.Object");
  }

  @Test(enabled = false) // TODO Implementation missing for now
  public void testObjectAttribute02() {
    ParseUnit unit = new ParseUnit(
        new ByteArrayInputStream("class rssw.pct: define property p1 as char get. set. define variable v1 as longchar. method void m1(): this-object:p1. this-object:v1. end method. end class.".getBytes()), session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0).isExpression());
    assertTrue(nodes.get(0) instanceof AttributeReferenceNode);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType(), DataType.CHARACTER);

    assertTrue(nodes.get(1).isExpression());
    assertTrue(nodes.get(1) instanceof AttributeReferenceNode);
    IExpression exp2 = (IExpression) nodes.get(1);
    assertEquals(exp2.getDataType(), DataType.LONGCHAR);
  }

  @Test
  public void testObjectMethod() {
    ParseUnit unit = new ParseUnit(
        new ByteArrayInputStream("def var xx as Progress.Lang.Object. message xx:toString(). message xx:UnknownMethod().".getBytes()), session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0).isExpression());
    assertTrue(nodes.get(0) instanceof MethodCallNode);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType(), DataType.CHARACTER);

    assertTrue(nodes.get(1).isExpression());
    assertTrue(nodes.get(1) instanceof MethodCallNode);
    IExpression exp2 = (IExpression) nodes.get(1);
    assertEquals(exp2.getDataType(), DataType.NOT_COMPUTED);
  }

  @Test
  public void testObjectMethod02() {
    ParseUnit unit = new ParseUnit(
        new ByteArrayInputStream("class rssw.pct: method void m1(): toString(). foobar(). end method. end class.".getBytes()), session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0).isExpression());
    assertTrue(nodes.get(0) instanceof LocalMethodCallNode);
    LocalMethodCallNode exp = (LocalMethodCallNode) nodes.get(0);
    assertEquals(exp.getMethodName(), "toString");
    assertEquals(exp.getDataType(), DataType.CHARACTER);

    assertTrue(nodes.get(1).isExpression());
    assertTrue(nodes.get(1) instanceof LocalMethodCallNode);
    LocalMethodCallNode exp2 = (LocalMethodCallNode) nodes.get(1);
    assertEquals(exp2.getMethodName(), "foobar");
    assertEquals(exp2.getDataType(), DataType.NOT_COMPUTED);
  }

  @Test
  public void testFunctions() {
    ParseUnit unit = new ParseUnit(
        new ByteArrayInputStream("function f1 returns char(): end function. function f2 returns int64(): end function. f1(). f2().".getBytes()), session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 2);

    assertTrue(nodes.get(0).isExpression());
    assertTrue(nodes.get(0) instanceof UserFunctionCallNode);
    UserFunctionCallNode exp = (UserFunctionCallNode) nodes.get(0);
    assertEquals(exp.getFunctionName(), "f1");
    assertEquals(exp.getDataType(), DataType.CHARACTER);

    assertTrue(nodes.get(1).isExpression());
    assertTrue(nodes.get(1) instanceof UserFunctionCallNode);
    UserFunctionCallNode exp2 = (UserFunctionCallNode) nodes.get(1);
    assertEquals(exp2.getFunctionName(), "f2");
    assertEquals(exp2.getDataType(), DataType.INT64);
  }

  @Test
  public void testHandleAttribute() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("define frame frm1. message frame frm1:box.".getBytes()),
        session);
    unit.treeParser01();

    List<JPNode> nodes = unit.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), PrimitiveDataType.LOGICAL);
  }

  private void testSimpleExpression(String code, DataType expected) {
    ParseUnit unit01 = new ParseUnit(new ByteArrayInputStream(code.getBytes()), session);
    unit01.treeParser01();

    List<JPNode> nodes = unit01.getTopNode().queryExpressions();
    assertEquals(nodes.size(), 1);
    assertTrue(nodes.get(0).isExpression());
    IExpression exp = (IExpression) nodes.get(0);
    assertEquals(exp.getDataType().getPrimitive(), expected.getPrimitive());
    if (expected.getPrimitive() == PrimitiveDataType.CLASS)
      assertEquals(exp.getDataType().getClassName(), expected.getClassName());
  }

}
