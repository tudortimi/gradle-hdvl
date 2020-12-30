`ifndef SOME_PACKAGE_WITH_EXPORTED_HEADERS_MACROS
`define SOME_PACKAGE_WITH_EXPORTED_HEADERS_MACROS

`define some_package_with_exported_headers_macro \
  begin \
    $display("Hello from some_package_with_exported_headers"); \
  end

`endif
