import chisel3._
import chisel3.util._
//see flow chart here: https://app.diagrams.net/#Lflow%20dia%2009-11-26.drawio#%7B%22pageId%22%3A%22jD8GbT7tNcJAKHjxoHZN%22%7D
class Top(fileProgram: String) extends Module {
  val io = IO(new Bundle {
    // -- Inputs -- //

    // -- Outputs -- //
    val done   = Output(Bool())
    val pc_out = Output(UInt(32.W))

    // -- Debug ports -- //
    val dB_instruction_out = Output(UInt(32.W))
    val dB_opcode          = Output(UInt(7.W))
    val dB_rs1             = Output(UInt(5.W))
    val dB_rs2             = Output(UInt(5.W))
    val dB_rd              = Output(UInt(5.W))
    val dB_f3              = Output(UInt(3.W))
    val dB_f7              = Output(UInt(7.W))
    val dB_regs            = Output(Vec(32, UInt(32.W)))

  })

//connections has to be after we create modules; otherwise decode module cant see readModule for example.
  // -- INITIALIZATION OF MODULES -- //

// -- Decode -- //
  val decodeModule = Module(new Decode)

// -- Read -- //
  val readModule = Module(new Read(fileProgram))

// -- Execute -- //
  val executeModule = Module(new Execute())

// -- Memory -- //
  val memoryModule = Module(new MemReadLoad(fileProgram))

  // -- Regs and Wires in top -- //
//not modules, but has to be created:)//
  val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val pc   = RegInit(0.U(32.W))

  // -- CONNECTIONS -- //

// Pcmodule -------------------
// vi skal også tjekke om programmet færdig står der, så det nemt nok med en linje her
  io.done := false.B
val programEnd = pc >= readModule.memoryBytes.length.U // PC moved past the last byte of the program file
when((decodeModule.io.isEcall && regs(17) === 10.U) || programEnd)(io.done := true.B).otherwise(pc := executeModule.io.pc_new)
  io.pc_out := pc

// Read -----------------------
  readModule.io.pc_in := io.pc_out

// Decode ---------------------
  decodeModule.io.instruction := readModule.io.instruction_out

// Execute --------------------
  executeModule.io.funct3  := decodeModule.io.funct3
  executeModule.io.imm     := decodeModule.io.imm
  executeModule.io.rs1_val := regs(decodeModule.io.rs1)
  executeModule.io.rs2_val := regs(decodeModule.io.rs2)
  executeModule.io.pc      := io.pc_out

  executeModule.io.aluOp    := decodeModule.io.aluOp
  executeModule.io.useImm   := decodeModule.io.aluSrcB
  executeModule.io.isLui    := decodeModule.io.lui
  executeModule.io.isAuipc  := decodeModule.io.auipc
  executeModule.io.isBranch := decodeModule.io.branch
  executeModule.io.isJal    := decodeModule.io.jal
  executeModule.io.isJalr   := decodeModule.io.jalr

// MemReadLoad ------------
  memoryModule.io.loadReady := decodeModule.io.memRead
  memoryModule.io.saveReady := decodeModule.io.memWrite
  memoryModule.io.size      := decodeModule.io.lsSize
  memoryModule.io.address   := executeModule.io.address
  memoryModule.io.wordIn    := regs(decodeModule.io.rs2)

// Register update:
  // hvis vi ikke har !io.done så kan programmet bare spytte lort ud hvis den kører videre, så kan det errors
  when(decodeModule.io.regWrite && decodeModule.io.rd =/= 0.U && !io.done)(regs(decodeModule.io.rd) := Mux(decodeModule.io.memToReg === 1.U, memoryModule.io.wordOut, executeModule.io.data))

// Debug --------------------
  // readmodule
  io.dB_instruction_out := readModule.io.instruction_out

  // decodemodule
  io.dB_f3     := decodeModule.io.funct3
  io.dB_f7     := decodeModule.io.funct7
  io.dB_opcode := decodeModule.io.opcode
  io.dB_rd     := decodeModule.io.rd
  io.dB_rs1    := decodeModule.io.rs1
  io.dB_rs2    := decodeModule.io.rs2

  // regs
  io.dB_regs := regs

}
