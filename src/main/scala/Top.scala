import chisel3._
import chisel3.util._

class Top(fileProgram:String) extends Module {
  val io = IO(new Bundle {
    // -- Inputs -- //

    // -- Outputs -- //
    val regDeb = Output(Vec(4, UInt(32.W))) // debug output for the tester
    val done   = Output(Bool())
  })

// -- Program Counter -- //
  val pcModule = Module(new ProgramCounter)
  val pc       = pcModule.io.pc
  // ellers compiler lortet ikke

// -- Decode -- //
  val decodeModule = Module(new Decode)
  // needs to be instruction but this compiles for now

// -- Read -- //
val readModule = Module(new Read(fileProgram))








  // -- CONNECTIONS -- //

  //Pcmodule -------------------
  pcModule.io.nextPC := DontCare
  //Decode ---------------------
  decodeModule.io.instruction := readModule.io.instruction_out
  //Read -----------------------
    readModule.io.pc_in := pcModule.io.pc




  



  //from schoeberl with love

    // TODO: there should be an elegant way to express this
  // We initialize the register file to 0 for a nicer display
  // In a real processor this is usually not done
  val regDebug = RegInit(VecInit(Seq.fill(4)(0.U(32.W))))

  // done should be set when the program ends, and the tester shall stop
  io.done := true.B

  // Make the register file visible to the tester
  io.regDeb := regDebug
}
