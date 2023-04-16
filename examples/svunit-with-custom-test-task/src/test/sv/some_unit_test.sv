module some_unit_test;

  import svunit_pkg::svunit_testcase;
  `include "svunit_defines.svh"

  string name = "some_class_unit_test";
  svunit_testcase svunit_ut;


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

    `SVTEST(plusarg_set)
      if ($test$plusargs("WITH_GUI"))
        $display("Started with GUI");
    `SVTEST_END

  `SVUNIT_TESTS_END

endmodule
