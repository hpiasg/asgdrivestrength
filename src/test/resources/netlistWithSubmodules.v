module aSubmodule (in, out);
  input in;
  output out;
  
  DUMMYGATE I1 (in, out);
endmodule

module aModule (in, out);
  input in;
  output out;
  
  wire aWire;
  
  aSubmodule I1 (.in(in), .out(aWire));
  aSubmodule I2 (aWire, out);
endmodule

