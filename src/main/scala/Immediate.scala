import chisel3._
import chisel3.util._


class Immediate extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))

    val imm = Output(UInt(32.W))
  })

}
