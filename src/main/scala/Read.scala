import chisel3._

class Read extends Module {
  val io = IO(new Bundle {
    val pc_in = Input(UInt(32.W))
    val instruction_out = Output(UInt(32.W))
  })



}
