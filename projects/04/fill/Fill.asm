// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

// screen 256 rows x 512(32*16) cols

(KBD_PROB)
	@color
  M=0

  @KBD
	D=M
  @COLOR_SCREEN
	D;JEQ 
	
  @color
  M=-1
	

(COLOR_SCREEN)
  @i
  M=0

  @SCREEN
  D=A
  @addr
  M=D

(SCREEN_LOOP)
  @i
  D=M
  @8192
  D=D-A
  @KBD_PROB
  D;JEQ

  @color
  D=M
  @addr
  A=M
  M=D // RAM[addr]=color

  @addr
  M=M+1;
  @i
  M=M+1; //i++

  @SCREEN_LOOP
  0;JMP  // Goto LOOP






