module top;

  initial
    do_stuff();

  function automatic void do_stuff();
    some_package::some_class from_some_package = new();
    some_other_package::some_class from_some_other_package = new();
    from_some_package.do_some_thing();
    from_some_other_package.do_some_other_thing();
  endfunction

endmodule
