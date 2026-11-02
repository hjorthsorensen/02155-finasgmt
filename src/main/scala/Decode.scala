import chisel3._

class Decode extends Module {
  val io = IO(new Bundle {
    // -- Inputs -- //
    val instruction = Input(UInt(32.W))

    // -- Outputs -- //
    val opcode = Output(UInt(7.W))

    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val rd = Output(UInt(5.W))

    val funct3 = Output(UInt(3.W))
    val funct7 = Output(UInt(7.W))
  })

}
