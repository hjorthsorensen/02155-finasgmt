import chisel3._

class ProgramCounter extends Module {
  val io = IO(new Bundle {
    val jump = Input(Bool())
    val target = Input(UInt(32.W))
    val pc = Output(UInt(32.W))
  })

  val pcReg = RegInit(0.U(32.W))

  pcReg := Mux(io.jump, io.target, pcReg + 4.U)

  io.pc := pcReg

}
