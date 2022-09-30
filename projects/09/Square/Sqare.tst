// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/08/FunctionCalls/FibonacciElement/FibonacciElementVME.tst

load,  // Load all the VM files from the current directory
output-file Square.out,
output-list RAM[0]%D1.6.1 ;

set sp 261,

repeat {
  vmstep;
}

output;
