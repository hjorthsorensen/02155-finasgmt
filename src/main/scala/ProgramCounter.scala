import chisel3._

class ProgramCounter extends Module {
  val io = IO(new Bundle {
    val nextPC = Input(UInt(32.W))
    val pc = Output(UInt(32.W))
  })

  val pcReg = RegInit(0.U(32.W))

  pcReg := io.nextPC
  io.pc := pcReg
}
