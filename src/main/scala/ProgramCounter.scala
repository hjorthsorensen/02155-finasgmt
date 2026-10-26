import chisel3._

class ProgramCounter extends Module {
  val io = IO(new Bundle {
    val jump = Input(Bool())
    val target = Input(UInt(32.W))
    val pc = Output(UInt(32.W))
    val pcPlus4 = Output(UInt(32.W))
  })

  val pcReg = RegInit(0.U(32.W))
  val pcNext = pcReg + 4.U

  pcReg := Mux(io.jump, io.target, pcNext)

  io.pc := pcReg
  io.pcPlus4 := pcNext
}
