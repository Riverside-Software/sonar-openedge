class test23: 
  define private variable xxx as System.Windows.Forms.SaveFileDialog no-undo.

  method public void foo():
    define variable oDialogResult as DialogResult no-undo.
    wait-for this-object:xxx:ShowDialog () set oDialogResult .
  end method.

end class.
