module top;

  initial
    do_stuff();

  function automatic void do_stuff();
    some_package::some_class from_some_package = new();
    from_some_package.do_some_thing();
  endfunction

endmodule
