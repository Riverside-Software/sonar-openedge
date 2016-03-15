/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.refactor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.core.nodetypes.ProparseDirectiveNode;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.Table;
import org.prorefactor.proparse.IntegerIndex;
import org.prorefactor.treeparser.FieldBuffer;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.SymbolI;
import org.prorefactor.treeparser.SymbolScopeRoot;
import org.prorefactor.treeparser.SymbolScopeSuper;
import org.prorefactor.treeparser.TableBuffer;
import org.prorefactor.treeparser.TreeParserException;
import org.prorefactor.treeparser01.ITreeParserAction;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/** The API for "Parse Unit Binary" files, which are a persistent store for syntax trees. */
public class PUB {

  /** This is like a "schema version" for .pub files. */
  public static final int LAYOUT_VERSION = 11;

  /**
   * loadTo(PUBFILE_TIMESTAMP) - just check if the binary exists and check that it is newer than the compile unit file.
   * Does not read anything from the binary.
   */
  public static final int PUBFILE_TIMESTAMP = 5;

  /** loadTo(FILES) - the index of include files referenced by this parse unit. */
  public static final int FILES = 10;

  /**
   * loadTo(HEADER) Gets all the segments necessary for checking if the binary is up to date or not. Also for classes,
   * gets class name and the name of the inherited class if any.
   */
  public static final int HEADER = 15;

  /** loadTo(SCHEMA) - the schema tables and fields referenced by this parse unit. */
  public static final int SCHEMA = 20;

  /**
   * loadTo(IMPORTS)
   * 
   * @see SymbolI#isImported()
   */
  public static final int IMPORTS = 30;

  /**
   * loadTo(EXPORTS)
   * 
   * @see SymbolI#isExported()
   */
  public static final int EXPORTS = 40;

  /** loadTo(AST) - just loads the node types - you almost certainly need STRINGS as well. */
  public static final int AST = 50;

  /** loadTo(STRINGS) - load the strings into the syntax tree. */
  public static final int STRINGS = 60;

  /** loadTo(END) - all binary file segments will be loaded. */
  public static final int END = 100;

  /** Scratch JPNode attributes for storing string index. */
  private static final int NODETEXT = 49001;
  /** Scratch JPNode attributes for storing string index. */
  private static final int NODECOMMENTS = 49002;

  private boolean checked = false;
  private boolean current = false;
  private boolean usingNonlocalInputBlob = false;
  private int nodeCount;
  private ArrayList<SymbolRef> exportList;
  private ArrayList<String> fileList;
  private ArrayList<SymbolRef> importList;
  private BiMap<Integer, String> stringTable;
  private File cuFile;
  private File pubFile;
  private DataInputStream inStream;
  private IntegerIndex<String> fileIndexes;
  private ProgramRootNode tree;
  private ParseUnit parseUnit;
  private String unitClassName;
  private RefactorSession prsession;

  private String superClassName;
  private String[] stringArray;
  private TreeMap<String, TableRef> tableMap;

  /**
   * Create a PUB for a compile unit fully qualified path/name. RefactorSession must be configured for the project
   * (schema loaded, project name set, etc) before working with PUB files.
   * 
   * @param fullPath The full path to the compile unit's source file.
   */
  public PUB(RefactorSession prsession, String fullPath) {
    this.prsession = prsession;
    cuFile = new File(fullPath);
    pubFile = new File(pubDirFileName(prsession, fullPath) + ".pub");
  }

  /** A record of symbol type and name, for import/export tables. */
  public class SymbolRef {
    /** The TokenType, ex: TokenTypes.VARIABLE */
    public int progressType;
    /**
     * For Primitive symbols (fields and variables) the data type, like CHARACTER or CLASS. Zero if this is not a
     * primitive.
     */
    public int dataType = 0;
    /** The symbol name (Symbol.fullName), with caseAsDefined. */
    public String symbolName;
    /** If is a CLASS object ref, then the class name, null otherwise. */
    public String classSymbolRefName = null;

    SymbolRef(int progressType, String symbolName) {
      this.progressType = progressType;
      this.symbolName = symbolName;
    }
  }

  private class TableRef {
    String name;
    TreeMap<String, String> fieldMap = new TreeMap<String, String>();

    TableRef(String name) {
      this.name = name;
    }
  }

  /**
   * It's possible, maybe even sensible, to reuse a PUB object. This method clears out old lists in preparation for
   * reloading or rebuilding.
   */
  private void _refresh() {
    exportList = new ArrayList<SymbolRef>();
    fileList = new ArrayList<String>();
    fileIndexes = new IntegerIndex<String>();
    importList = new ArrayList<SymbolRef>();
    tableMap = new TreeMap<String, TableRef>();
    stringTable = HashBiMap.create();
    /*
     * String index zero is not used. This allows us to use 0 from JPNode.attrGet() to indicate
     * "no string value present".
     */
    stringIndex("");
  }

  /**
   * Update the PUB. You would normally call load() first, to check whether a fresh build is really necessary. Once a
   * build() has been done, then all of the values for the PUB are available - it is not necessary for you to call
   * load() or loadTo(). This takes care of creating the ParseUnit (if not already set) and running treeParser01.
   * 
   * @throws RefactorException
   */
  public ParseUnit build() throws IOException, RefactorException {
    if (parseUnit == null) {
      parseUnit = new ParseUnit(cuFile, prsession);
      parseUnit.setPUB(this);
    }
    pubFile.delete();
    parseUnit.treeParser01(); // This calls build(TP01Support)
    return parseUnit;
  }

  /** This is called by TreeParser01, and should not be called directly. */
  public void build(ITreeParserAction support) throws IOException, RefactorException, TreeParserException {
    ParseUnit pu = support.getParseUnit();
    // treeParser01 needs to have been run already.
    assert pu.getRootScope() != null;
    tree = pu.getTopNode();
    _refresh();
    pubFile.getParentFile().mkdirs();
    OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(pubFile));
    DataOutputStream out = new DataOutputStream(fileOut);
    writeVersion(out);
    writeFileIndex(out);
    writeHeader(out, pu.getRootScope());
    List<Symbol> rootSymbols = pu.getRootScope().getAllSymbols();
    writeSchemaSegment(out, rootSymbols);
    writeImportSegment(out, rootSymbols);
    writeExportSegment(out, rootSymbols);
    writeTree(out, tree);
    writeStrings(out);
    out.close();
  }

  /**
   * Copies the lower case names of all schema tables into your collection. The names are of the format
   * "database.table". You might use a sorted set or a hash set, depending on what you need it for. To get the
   * mixed-case names, use the "org.prorefactor.core.schema" package to look up the table objects.
   * 
   * @param c
   */
  @SuppressWarnings("unchecked")
  public void copySchemaTableLowercaseNamesInto(Collection<String> c) {
    c.addAll(tableMap.keySet());
  }

  /**
   * Copies the lower case names of all schema fields for one table into your collection. The names are of the format
   * "field" - i.e. no db or table name prefix. You might use a sorted set or a hash set, depending on what you need it
   * for. To get the mixed-case names, use the "org.prorefactor.core.schema" package to look up the field objects.
   * 
   * @param fromTableName Your table name. Case insenstitive. Must be of the format "database.table".
   */
  @SuppressWarnings("unchecked")
  public void copySchemaFieldLowercaseNamesInto(Collection c, String fromTableName) {
    TableRef tableRef = tableMap.get(fromTableName.toLowerCase());
    if (tableRef == null)
      return;
    for (String fieldName : tableRef.fieldMap.keySet()) {
      c.add(fieldName);
    }
  }

  public String getClassName() {
    return unitClassName;
  }

  /**
   * Get the array of exported symbols, in no particular order. Currently just for DEF NEW [GLOBAL] SHARED symbols.
   */
  public SymbolRef[] getExportTable() {
    SymbolRef[] ret = new SymbolRef[exportList.size()];
    exportList.toArray(ret);
    return ret;
  }

  /**
   * Get the array of imported symbols, in no particular order. Currently just for DEF SHARED symbols.
   */
  public SymbolRef[] getImportTable() {
    SymbolRef[] ret = new SymbolRef[importList.size()];
    importList.toArray(ret);
    return ret;
  }

  private DataInputStream getDataInputStream() {
    try {
      InputStream fileIn = new BufferedInputStream(new FileInputStream(pubFile));
      DataInputStream inStream = new DataInputStream(fileIn);
      return inStream;
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  public ParseUnit getParseUnit() {
    return parseUnit;
  }

  public String getSuperClassName() {
    return superClassName;
  }

  /**
   * Get the time stamp (File.lastModified()) of the PUB file. Returns zero if the PUB file does not exist.
   */
  public long getTimestamp() {
    if (pubFile == null)
      return 0;
    return pubFile.lastModified();
  }

  /** Return the JPNode syntax tree that was loaded with load() */
  public ProgramRootNode getTree() {
    return tree;
  }

  /**
   * Has the PUB been checked to see if it's current? i.e. Has it been loaded at least to HEADER?
   */
  public boolean isChecked() {
    return checked;
  }

  /**
   * Is the PUB current? You should have used isChecked() or loaded the PUB to HEADER before checking isCurrent().
   * 
   * @see PUB#isChecked()
   */
  public boolean isCurrent() {
    return current;
  }

  /** Same as loadTo(PUB.END) */
  public boolean load() {
    return loadTo(END);
  }

  /**
   * Load the PUB file to the end of the specified segment. For example, if you only need to read as far as the
   * "imports" segment, then use loadTo(PUB.IMPORTS).
   * 
   * @return false if the file is out of date and you need to call build() instead.
   */
  public boolean loadTo(int lastSegmentToLoad) {
    _refresh();
    if (lastSegmentToLoad >= HEADER)
      checked = true;
    if (!usingNonlocalInputBlob) {
      if (!pubFile.exists())
        return false;
      if (cuFile.lastModified() > pubFile.lastModified())
        return false;
      if (lastSegmentToLoad == PUBFILE_TIMESTAMP)
        return true;
      inStream = getDataInputStream();
      if (inStream == null)
        return false;
    }
    try {
      if (!readVersion(inStream))
        return false;
      readFileIndex(inStream);
      if ((!usingNonlocalInputBlob) && (!testTimeStamps()))
        return false;
      current = true;
      if (lastSegmentToLoad == PUB.FILES)
        return true;
      readHeader(inStream);
      if (lastSegmentToLoad == PUB.HEADER)
        return true;
      readSchema(inStream);
      if (lastSegmentToLoad == PUB.SCHEMA)
        return true;
      readImportSegment(inStream);
      if (lastSegmentToLoad == PUB.IMPORTS)
        return true;
      readExportSegment(inStream);
      if (lastSegmentToLoad == PUB.EXPORTS)
        return true;
      nodeCount = -1;
      tree = (ProgramRootNode) readTree(inStream);
      if (lastSegmentToLoad == PUB.AST)
        return true;
      readStrings(inStream);
      setStrings(tree);
    } catch (IOException e1) {
      return false;
    } finally {
      try {
        inStream.close();
      } catch (IOException e) {
      }
    }
    return true;
  }

  /**
   * Returns the path to the .pub file but without the .pub extension. Useful for adding additional files like listing
   * files into the pubs directory tree. The project for the RefactorSession must already be assigned.
   */
  public static String pubDirFileName(RefactorSession prsession, String fullPath) {
    // For "C:" in the path, replace the ':' with '_'.
    String path2;
    if (fullPath.charAt(1) == ':') {
      path2 = fullPath.substring(0, 1) + "_" + fullPath.substring(2);
    } else {
      path2 = fullPath;
    }
    return "pubs/" + path2;
  }

  private void readExportSegment(DataInputStream in) throws IOException {
    for (;;) {
      SymbolRef symbolRef = readSymbol(in);
      if (symbolRef == null)
        break;
      exportList.add(symbolRef);
    }
  }

  private void readFileIndex(DataInputStream in) throws IOException {
    int index;
    String filename;
    for (;;) {
      index = in.readInt();
      filename = in.readUTF();
      if (index == -1)
        break;
      fileList.add(filename);
      fileIndexes.add(filename);
    }
  }

  private void readHeader(DataInputStream in) throws IOException {
    unitClassName = in.readUTF();
    if (unitClassName.length() == 0)
      unitClassName = null;
    superClassName = in.readUTF();
    if (superClassName.length() == 0)
      superClassName = null;
  }

  private void readImportSegment(DataInputStream in) throws IOException {
    for (;;) {
      SymbolRef symbolRef = readSymbol(in);
      if (symbolRef == null)
        break;
      importList.add(symbolRef);
    }
  }

  private void readSchema(DataInputStream in) throws IOException {
    for (;;) {
      String tableName = in.readUTF();
      if (tableName.length() == 0)
        break;
      TableRef tableRef = new TableRef(tableName);
      tableMap.put(tableName.toLowerCase(), tableRef);
      for (;;) {
        String fieldName = in.readUTF();
        if (fieldName.length() == 0)
          break;
        tableRef.fieldMap.put(fieldName.toLowerCase(), fieldName);
      }
    }
  }

  private void readStrings(DataInputStream in) throws IOException {
    int size = in.readInt();
    stringArray = new String[size];
    for (int i = 0; i < size; i++) {
      stringArray[i] = in.readUTF();
    }
  }

  private SymbolRef readSymbol(DataInputStream in) throws IOException {
    SymbolRef symbolRef = new SymbolRef(in.readInt(), in.readUTF());
    if (symbolRef.progressType == -1)
      return null;
    symbolRef.dataType = in.readInt();
    if (symbolRef.dataType == NodeTypes.CLASS)
      symbolRef.classSymbolRefName = in.readUTF();
    return symbolRef;
  }

  private JPNode readTree(DataInputStream in) throws IOException {
    int nodeClass = in.readInt();
    if (nodeClass == -1)
      return null;
    JPNode node = org.prorefactor.proparse.NodeFactory.createByIndex(nodeClass);
    node.setFilenameList(fileIndexes);
    node.setNodeNum(++nodeCount);
    node.setType(in.readInt());
    node.setFileIndex(in.readShort());
    node.setLine(in.readInt());
    node.setColumn(in.readShort());
    node.setSourceNum(in.readInt());
    int key;
    int value;
    for (key = in.readInt(), value = in.readInt(); key != -1; key = in.readInt(), value = in.readInt()) {
      node.attrSet(key, value);
    }
    node.setFirstChild(readTree(in));
    node.setParentInChildren();
    node.setNextSiblingWithLinks(readTree(in));
    return node;
  }

  /** Read the version, return false if the PUB file is out of date, true otherwise. */
  private boolean readVersion(DataInputStream in) throws IOException {
    if (in.readInt() != LAYOUT_VERSION)
      return false;
    return true;
  }

  /** Use a binary input stream rather than a local './prorefactor/projects/...' .pub file. */
  public void setDataInputStream(DataInputStream inStream) {
    this.inStream = inStream;
    usingNonlocalInputBlob = true;
  }

  public void setParseUnit(ParseUnit pu) {
    this.parseUnit = pu;
    if (pu.getPUB() != this)
      pu.setPUB(this);
  }

  private void setStrings(JPNode node) {
    if (node == null)
      return;
    int index;
    if ((index = node.attrGet(NODETEXT)) > 0)
      node.setText(stringArray[index]);
    if ((index = node.attrGet(NODECOMMENTS)) > 0)
      node.setComments(stringArray[index]);
    if ((index = node.attrGet(IConstants.PROPARSEDIRECTIVE)) > 0)
      ((ProparseDirectiveNode) node).setDirectiveText(stringArray[index]);
    if ((index = node.attrGet(IConstants.QUALIFIED_CLASS_INT)) > 0)
      node.attrSetS(IConstants.QUALIFIED_CLASS_STRING, stringArray[index]);
    setStrings(node.firstChild());
    setStrings(node.nextSibling());
  }

  private int stringIndex(String s) {
    Integer index = stringTable.inverse().get(s);
    if (index == null) {
      index = new Integer(stringTable.size()); // index is 0 if this is the first entry...
      stringTable.put(index, s);
    }
    return index.intValue();
  }

  private boolean testTimeStamps() {
    long pubTime = pubFile.lastModified();
    for (String filename : fileList) {
      if (filename == null || filename.length() == 0)
        continue;
      File file = prsession.findFile2(filename);
      if (file == null)
        return false;
      if (file.lastModified() > pubTime)
        return false;
    }
    return true;
  }

  private void writeExportSegment(DataOutputStream out, List rootSymbols) throws IOException {
    for (Iterator it = rootSymbols.iterator(); it.hasNext();) {
      Symbol symbol = (Symbol) it.next();
      if (symbol.isExported())
        writeSymbol(out, symbol);
    }
    out.writeInt(-1);
    out.writeUTF("");
  }

  private void writeFileIndex(DataOutputStream out) throws IOException {
    String[] files = tree.getFilenames();
    for (int i = 0; i < files.length; i++) {
      out.writeInt(i);
      out.writeUTF(files[i]);
    }
    out.writeInt(-1);
    out.writeUTF("");
  }

  private void writeHeader(DataOutputStream out, SymbolScopeRoot unitScope) throws IOException {
    String s = unitScope.getClassName();
    if (s != null)
      out.writeUTF(s);
    else
      out.writeUTF("");
    SymbolScopeSuper superScope = (SymbolScopeSuper) unitScope.getParentScope();
    if (superScope != null)
      out.writeUTF(superScope.getClassName());
    else
      out.writeUTF("");
  }

  private void writeImportSegment(DataOutputStream out, List rootSymbols) throws IOException {
    for (Iterator it = rootSymbols.iterator(); it.hasNext();) {
      Symbol symbol = (Symbol) it.next();
      if (symbol.isImported())
        writeSymbol(out, symbol);
    }
    out.writeInt(-1);
    out.writeUTF("");
  }

  private void writeSymbol(DataOutputStream out, Symbol symbol) throws IOException {
    out.writeInt(symbol.getProgressType());
    out.writeUTF(symbol.fullName()); // We write caseAsDefined
    if (symbol instanceof Primative) {
      Primative primative = (Primative) symbol;
      int dataType = primative.getDataType().getTokenType();
      out.writeInt(dataType);
      if (dataType == NodeTypes.CLASS)
        out.writeUTF(primative.getClassName());
    } else
      out.writeInt(0);
  }

  private void writeTree(DataOutputStream out, JPNode node) throws IOException {
    int nodeType = node.getType();

    out.writeInt(node.getSubtypeIndex());
    out.writeInt(nodeType);
    out.writeShort(node.getFileIndex());
    out.writeInt(node.getLine());
    out.writeShort(node.getColumn());
    out.writeInt(node.getSourceNum());

    if (!NodeTypes.hasDefaultText(nodeType)) {
      out.writeInt(NODETEXT);
      out.writeInt(stringIndex(node.getText()));
    }
    String comments = node.getComments();
    if (comments != null) {
      out.writeInt(NODECOMMENTS);
      out.writeInt(stringIndex(comments));
    }
    if (node.attrGet(IConstants.STATEHEAD) == IConstants.TRUE) {
      out.writeInt(IConstants.STATEHEAD);
      out.writeInt(IConstants.TRUE);
      out.writeInt(IConstants.STATE2);
      out.writeInt(node.getState2());
    }
    int attrVal;
    if ((attrVal = node.attrGet(IConstants.STORETYPE)) > 0) {
      out.writeInt(IConstants.STORETYPE);
      out.writeInt(attrVal);
    }
    if (node instanceof ProparseDirectiveNode) {
      out.writeInt(IConstants.PROPARSEDIRECTIVE);
      out.writeInt(stringIndex(((ProparseDirectiveNode) node).getDirectiveText()));
    }
    if ((attrVal = node.attrGet(IConstants.OPERATOR)) > 0) {
      out.writeInt(IConstants.OPERATOR);
      out.writeInt(attrVal);
    }
    if ((attrVal = node.attrGet(IConstants.INLINE_VAR_DEF)) > 0) {
      out.writeInt(IConstants.INLINE_VAR_DEF);
      out.writeInt(attrVal);
    }
    if (nodeType == NodeTypes.TYPE_NAME) {
      out.writeInt(IConstants.QUALIFIED_CLASS_INT);
      out.writeInt(stringIndex(node.attrGetS(IConstants.QUALIFIED_CLASS_STRING)));
    }
    out.writeInt(-1);
    out.writeInt(-1); // Terminate the attribute key/value pairs.
    JPNode next;
    if ((next = node.firstChild()) != null)
      writeTree(out, next);
    else
      out.writeInt(-1);
    if ((next = node.nextSibling()) != null)
      writeTree(out, next);
    else
      out.writeInt(-1);
  }

  private void writeSchemaSegment(DataOutputStream out, List rootSymbols) throws IOException {
    for (Iterator it = rootSymbols.iterator(); it.hasNext();) {
      Object obj = it.next();
      if (obj instanceof TableBuffer) {
        Table table = ((TableBuffer) obj).getTable();
        if (table.getStoretype() != IConstants.ST_DBTABLE)
          continue;
        writeSchema_addTable(table);
        continue;
      }
      if (obj instanceof FieldBuffer) {
        Field field = ((FieldBuffer) obj).getField();
        Table table = field.getTable();
        if (table.getStoretype() != IConstants.ST_DBTABLE)
          continue;
        TableRef tableRef = writeSchema_addTable(table);
        tableRef.fieldMap.put(field.getName().toLowerCase(), field.getName());
      }
    }
    for (TableRef tableRef : tableMap.values()) {
      out.writeUTF(tableRef.name);
      for (String fieldName : tableRef.fieldMap.values()) {
        out.writeUTF(fieldName);
      }
      out.writeUTF(""); // terminate the list of fields in the table
    }
    out.writeUTF(""); // terminate the schema segment
  }

  private TableRef writeSchema_addTable(Table table) {
    String name = table.getDatabase().getName() + "." + table.getName();
    String lowerName = name.toLowerCase();
    TableRef tableRef = tableMap.get(lowerName);
    if (tableRef != null)
      return tableRef;
    tableRef = new TableRef(name);
    tableMap.put(lowerName, tableRef);
    return tableRef;
  }

  private void writeStrings(DataOutputStream out) throws IOException {
    int size = stringTable.size();
    out.writeInt(size);
    for (int i = 0; i < size; i++) {
      out.writeUTF((String) stringTable.get(new Integer(i)));
    }
  }

  private void writeVersion(DataOutputStream out) throws IOException {
    out.writeInt(LAYOUT_VERSION);
  }

}
