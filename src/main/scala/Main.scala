import chisel3._

class TestModul extends Module {
  val io = IO(new Bundle {
    val in  = Input(UInt(8.W))
    val out   = Output(UInt(8.W))
  })
  
  io.out := io.in + 1.U
}

object Main extends App {
  emitVerilog(new TestModul)
  println("******************************************")
  println("Successfully generated TestModul.v!")
  println("******************************************")
}
