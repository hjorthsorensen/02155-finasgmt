import chisel3._
import chisel3.util._

object Main extends App {
  emitVerilog(new Top("program/program.bin"), Array("--target-dir", "generated"))
  println("******************************************")
  println("Successfully generated Top.v in generated/")
  println("******************************************")
}

// MAKE SURE THE PROGRAM IN /program is called "program.bin" //
// Same for .res if you want it to compare //