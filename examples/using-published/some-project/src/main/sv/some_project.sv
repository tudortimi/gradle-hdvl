module some_project;
    import some_published_dependency::*;

    initial
        do_stuff();

    function automatic void do_stuff();
        some_class o = new();
    endfunction
endmodule
