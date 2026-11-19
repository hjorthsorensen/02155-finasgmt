import chisel3._
import chisel3.util._

class Decode extends Module {
  val io = IO(new Bundle {
    // -- Inputs -- //
    val instruction = Input(UInt(32.W))

    // -- Instruction Fields -- //
    val opcode = Output(UInt(7.W)) // instruction type
    val rs1    = Output(UInt(5.W)) // source register 1
    val rs2    = Output(UInt(5.W)) // source register 2
    val rd     = Output(UInt(5.W)) // destination register
    val funct3 = Output(UInt(3.W)) 
    val funct7 = Output(UInt(7.W)) 
    val imm    = Output(UInt(32.W))

    // -- Decode Controls -- //
    val regWrite = Output(Bool())    // Write to register file or not
    val aluSrcB  = Output(Bool())    // 0 = rs2, 1 = immediate
    val aluOp    = Output(UInt(4.W)) // funct7(5), funct3: 0=add 8=sub 1=sll 2=slt 3=sltu 4=xor 5=srl 13=sra 6=or 7=and
    val isEcall  = Output(Bool())    // Is the instruction an ecall
    val memRead  = Output(UInt(2.W)) // Read from data memory (loads) 2Wide to do Unsigned loads.
    val memWrite = Output(Bool())    // Write to data memory (stores)
    val memToReg = Output(UInt(2.W)) // Value written to rd: 0 = Execute result (default), 1 = memory
    val branch   = Output(Bool())    // Conditional branch (beq, bne, blt, bge, bltu, bgeu)
    val jal     = Output(Bool())    // jal
    val jalr     = Output(Bool())    // jalr
    val lui = Output(Bool())
    val auipc = Output(Bool())
    val lsSize = Output(UInt(3.W)) // What size to load/save in memory
  })

  // -- Decode the instruction -- //
  val instruction = io.instruction
  io.opcode := instruction(6, 0)   // Opcode decides the instruction type
  io.rd     := instruction(11, 7)  // Destination register
  io.funct3 := instruction(14, 12) // Function 3 field (used for ALU operations, family of instructions)
  io.rs1    := instruction(19, 15) // Source register 1
  io.rs2    := instruction(24, 20) // Source register 2
  io.funct7 := instruction(31, 25) // Function 7 field (used for ALU operations, specific instruction )

  // -- Default values -- //
  io.regWrite := false.B
  io.aluSrcB  := false.B
  io.aluOp    := 0.U // add instruction per default
  io.isEcall  := false.B
  io.memRead  := "b00".U
  io.memWrite := false.B
  io.memToReg := 0.U
  io.branch   := false.B
  io.jal    := false.B
  io.jalr     := false.B
  io.imm      := 0.U // default value
  io.lui := false.B
  io.auipc := false.B
  io.lsSize := 0.U

  // -- Switch statement to decode instruction based on opcode -- //
  switch(io.opcode) {
    is("b0110011".U) { // R-type (add, sub, sll, srl, sra, slt, sltu, and, or, xor)
      io.regWrite := true.B
      io.aluSrcB  := false.B // need rs2
      io.aluOp    := Cat(io.funct7(5), io.funct3)
    }
    is("b0010011".U) { // I-type (addi, slti, sltiu, xori, ori, andi, slli, srli, srai)
      io.regWrite := true.B
      io.aluSrcB  := true.B // need immediate
      val alt = io.funct3 === "b101".U && io.funct7(5)
      // only srai uses funct7(5), otherwise its part of imm
      io.aluOp    := Cat(alt, io.funct3)
      io.imm      := io.instruction(31, 20).asSInt.pad(32).asUInt
    }
    is("b0110111".U) { // LUI (0 + imm)
      io.regWrite := true.B
      io.imm      := Cat(io.instruction(31, 12), 0.U(12.W))
      io.lui := true.B
    }
    is("b0010111".U) { // auipc (pc + imm)
      io.regWrite := true.B
      io.imm      := Cat(io.instruction(31, 12), 0.U(12.W))
      io.auipc := true.B
    }
    is("b1110011".U) { // ecall
      io.isEcall := true.B
    }
    is("b0000011".U) { // Loads (lb, lh, lw, lbu, lhu)
      io.regWrite := true.B
      io.aluSrcB  := true.B // immediate
      io.aluOp    := 0.U    // add -> address
      io.memRead := MuxLookup(io.funct3,0.U)(Seq(
        0.U -> "b11".U, 
        1.U -> "b11".U,
        2.U -> "b11".U,
        4.U -> "b01".U,
        5.U -> "b01".U
      ))
      io.memToReg := 1.U    // write memory data to rd
      io.imm      := io.instruction(31, 20).asSInt.pad(32).asUInt
      io.lsSize := MuxLookup(io.funct3, 0.U)(Seq(
        0.U -> 1.U, 
        1.U -> 2.U,
        2.U -> 4.U,
        4.U -> 1.U,
        5.U -> 2.U
      ))
    }
    is("b0100011".U) { // Stores (sb, sh, sw)
      io.aluSrcB  := true.B // immediate
      io.aluOp    := 0.U    // add -> address
      io.memWrite := true.B
      io.imm := Cat(io.instruction(31, 25), io.instruction(11, 7)).asSInt.pad(32).asUInt
      io.lsSize := MuxLookup(io.funct3, 0.U)(Seq(
        0.U -> 1.U, 
        1.U -> 2.U,
        2.U -> 4.U
      ))
    }
    is("b1100011".U) { // Branches (beq, bne, blt, bge, bltu, bgeu)
      io.aluSrcB := false.B // rs2
      io.branch  := true.B
      io.imm := Cat(io.instruction(31), io.instruction(7), io.instruction(30, 25), io.instruction(11, 8), 0.U(1.W))
        .asSInt.pad(32).asUInt
    }
    is("b1101111".U) { // JAL
      io.regWrite := true.B
      io.jal     := true.B
      io.imm := Cat(io.instruction(31), io.instruction(19, 12), io.instruction(20), io.instruction(30, 21), 0.U(1.W))
        .asSInt.pad(32).asUInt

    }
    is("b1100111".U) { // JALR
      io.regWrite := true.B
      io.aluSrcB  := true.B // immediate
      io.aluOp    := 0.U    // add -> jump target
      io.jalr     := true.B
      io.imm      := io.instruction(31, 20).asSInt.pad(32).asUInt
    }
  }
}
