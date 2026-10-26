import chisel3._

object Main extends App {
  emitVerilog(new Top(), Array("--target-dir", "generated"))
  println("******************************************")
  println("Successfully generated Top.v in generated/")
  println("******************************************")
}