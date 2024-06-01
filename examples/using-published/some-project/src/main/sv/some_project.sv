module some_project;
    import some_published_dependency::*;
    `include "some_published_dependency_macros.svh"

    initial
        do_stuff();

    function automatic void do_stuff();
        some_class o = new();
        `some_published_dependency_macro
        some_dpi_func();
    endfunction
endmodule
