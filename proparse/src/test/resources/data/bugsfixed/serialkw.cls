class serialkw abstract serializable:
 define private serializable temp-table test no-undo
  field test as integer.
 define non-serializable property prop as char get.
end class.
