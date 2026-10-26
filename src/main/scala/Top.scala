import chisel3._

class Top extends Module {
  val io = IO(new Bundle {
    // -- Inputs -- //

    // -- Outputs -- //
    val regDeb = Output(Vec(4, UInt(32.W))) // debug output for the tester
    val done = Output(Bool())
  })

// -- Program Counter -- //
  val pcModule = Module(new ProgramCounter)
  val pc = pcModule.io.pc
  // ellers compiler lortet ikke
  pcModule.io.nextPC := pc + 4.U

  // TODO: there should be an elegant way to express this
  // We initialize the register file to 0 for a nicer display
  // In a real processor this is usually not done
  val reg = RegInit(VecInit(Seq.fill(4)(0.U(32.W))))

  // done should be set when the program ends, and the tester shall stop
  io.done := true.B

  // Make the register file visible to the tester
  io.regDeb := reg
}
