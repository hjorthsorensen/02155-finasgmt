import chisel3._

class Decode extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))

    val opcode = Output(UInt(7.W))

    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val rd = Output(UInt(5.W))

    val funct3 = Output(UInt(3.W))
    val funct7 = Output(UInt(7.W))
    val imm = Output(UInt(32.W)) // Vi kan vælge om IMM generation også er her eller er et modul for sig selv(Måske klogest siden det kan blive ret stort )
  })



}
