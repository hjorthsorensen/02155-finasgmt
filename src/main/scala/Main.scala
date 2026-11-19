import chisel3._
import chisel3.util._

object Main extends App {
  emitVerilog(new Basys3Top("program/program.bin"), Array("--target-dir", "generated"))
  println("******************************************")
  println("Successfully generated Basys3Top.v in generated/")
  println("******************************************")
}

// MAKE SURE THE PROGRAM IN /program is called "program.bin" //
// Same for .res if you want it to compare //