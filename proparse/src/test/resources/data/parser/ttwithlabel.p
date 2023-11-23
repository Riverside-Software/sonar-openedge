define temp-table tt1 label "lbl1"
  field id as int
  index ix is unique id.
define temp-table tt2 like tt1 label 'xyz'.
