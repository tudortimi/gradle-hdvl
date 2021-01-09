module some_class_unit_test;

  import svunit_pkg::svunit_testcase;
  `include "svunit_defines.svh"

  string name = "some_class_unit_test";
  svunit_testcase svunit_ut;


  import some_package::*;


  function void build();
    svunit_ut = new(name);
  endfunction

  task setup();
    svunit_ut.setup();
  endtask

  task teardown();
    svunit_ut.teardown();
  endtask


  `SVUNIT_TESTS_BEGIN

    `SVTEST(is_working__returns_1)
      some_class obj = new();
      `FAIL_UNLESS(obj.is_working())
    `SVTEST_END

  `SVUNIT_TESTS_END

endmodule
