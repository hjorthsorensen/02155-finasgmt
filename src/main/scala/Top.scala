import chisel3._
import chisel3.util._
import java.nio.file.{Files, Paths}
//see flow chart here: https://app.diagrams.net/#Lflow%20dia%2009-11-26.drawio#%7B%22pageId%22%3A%22jD8GbT7tNcJAKHjxoHZN%22%7D
class Top(fileProgram: String) extends Module {
  val io = IO(new Bundle {
    // -- Inputs -- //

    // -- Outputs -- //
    val regDeb = Output(Vec(4, UInt(32.W))) // debug output for the tester
    val done   = Output(Bool())
    val pc_in = Input(UInt(32.W))
    val pc_out = Output(UInt(32.W))


    // -- Debug ports -- //
    val dB_instruction_out = Output(UInt(32.W))
    val dB_opcode = Output(UInt(7.W))
    val dB_rs1 = Output(UInt(5.W))
    val dB_rs2 = Output(UInt(5.W))
    val dB_rd = Output(UInt(5.W))
    val dB_f3 = Output(UInt(3.W))
    val dB_f7 = Output(UInt(7.W))
    
    
  })


// -- Decode -- //
  val decodeModule = Module(new Decode)
  // needs to be instruction but this compiles for now

// -- Read -- //
  val readModule = Module(new Read(fileProgram))

// -- Immediate -- //
  val immediateModule = Module(new Immediate)

//connections has to be after we create modules; otherwise decode module cant see readModule for example.

// -- CONNECTIONS -- //

// Pcmodule -------------------
  io.pc_in := DontCare
  io.pc_out := io.pc_in

// Decode ---------------------
  decodeModule.io.instruction := readModule.io.instruction_out

// Read -----------------------
  readModule.io.pc_in := io.pc_out


// Immediate ------------------
immediateModule.io.instruction := readModule.io.instruction_out


// Debug --------------------
io.dB_instruction_out := readModule.io.instruction_out
io.dB_f3 := decodeModule.io.funct3
io.dB_f7 := decodeModule.io.funct7
io.dB_opcode :=decodeModule.io.opcode
io.dB_rd := decodeModule.io.rd
io.dB_rs1 := decodeModule.io.rs1
io.dB_rs2  := decodeModule.io.rs2
  // from schoeberl with love

  // TODO: there should be an elegant way to express this
  // We initialize the register file to 0 for a nicer display
  // In a real processor this is usually not done
  val regDebug = RegInit(VecInit(Seq.fill(4)(0.U(32.W))))

  // done should be set when the program ends, and the tester shall stop
  io.done := true.B

  // Make the register file visible to the tester
  io.regDeb := regDebug
}
