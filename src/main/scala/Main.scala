import chisel3._
import chisel3.util._

object Main extends App {
  emitVerilog(new Top(), Array("--target-dir", "generated"))
  println("******************************************")
  println("Successfully generated Top.v in generated/")
  println("******************************************")
}