import chisel3._
import chisel3.util._

// https://www.engineering.upenn.edu/~cis2400/26fa/references/riscv_encoding.pdf

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

    // -- Decode Controls -- //
    val regWrite = Output(Bool())    // Write to register file or not
    val aluSrcA  = Output(UInt(2.W)) // 0 = rs1, 1 = PC, 2 = zero
    val aluSrcB  = Output(Bool())    // 0 = rs2, 1 = immediate
    val aluOp    = Output(UInt(4.W)) // funct7(5), funct3: 0=add 8=sub 1=sll 2=slt 3=sltu 4=xor 5=srl 13=sra 6=or 7=and
    val isEcall  = Output(Bool())    // Is the instruction an ecall
    val memRead  = Output(Bool())    // Read from data memory (loads)
    val memWrite = Output(Bool())    // Write to data memory (stores)
    val memToReg = Output(UInt(2.W)) // Value written to rd: 0 = ALU result, 1 = memory, 2 = PC+4
    val branch   = Output(Bool())    // Conditional branch (beq, bne, blt, bge, bltu, bgeu)
    val jump     = Output(Bool())    // jal
    val jalr     = Output(Bool())    // jalr
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
  io.aluSrcA  := 0.U
  io.aluSrcB  := false.B
  io.aluOp    := 0.U // add instruction per default
  io.isEcall  := false.B
  io.memRead  := false.B
  io.memWrite := false.B
  io.memToReg := 0.U
  io.branch   := false.B
  io.jump     := false.B
  io.jalr     := false.B

  // -- Switch statement to decode instruction based on opcode -- //
  switch(io.opcode) {
    is("b0110011".U) { // R-type (add, sub, sll, srl, sra, slt, sltu, and, or, xor)
      io.regWrite := true.B
      io.aluSrcA  := 0.U     // need rs1
      io.aluSrcB  := false.B // need rs2
      io.aluOp    := io.funct7(5) ## io.funct3
      io.memToReg := 0.U     // write ALU result to rd
    }
    is("b0010011".U) { // I-type (addi, slti, sltiu, xori, ori, andi, slli, srli, srai)
      io.regWrite := true.B
      io.aluSrcA  := 0.U    // need rs1
      io.aluSrcB  := true.B // need immediate
      val alt = io.funct3 === "b101".U && io.funct7(5)
      // funct7(5) selects srai over srli. for all other I-type operations, it is an immediate bit, so alt = 0
      io.aluOp    := alt ## io.funct3
      io.memToReg := 0.U // write ALU result to rd
    }
    is("b0110111".U) { // LUI (0 + imm)
      io.regWrite := true.B
      io.aluSrcA  := 2.U    // zero
      io.aluSrcB  := true.B // need immediate
      io.aluOp    := 0.U    // add
      io.memToReg := 0.U    // write ALU result to rd
    }
    is("b0010111".U) { // auipc (pc + imm)
      io.regWrite := true.B
      io.aluSrcA  := 1.U    // pc
      io.aluSrcB  := true.B // need immediate
      io.aluOp    := 0.U    // add
      io.memToReg := 0.U    // write ALU result to rd
    }
    is("b1110011".U) { // ecall
      io.isEcall := true.B
    }
    is("b0000011".U) { // Loads (lb, lh, lw, lbu, lhu): rd = mem[rs1 + imm]
      io.regWrite := true.B
      io.aluSrcA  := 0.U    // rs1
      io.aluSrcB  := true.B // immediate
      io.aluOp    := 0.U    // add -> address
      io.memRead  := true.B
      io.memToReg := 1.U    // write memory data to rd
    }
    is("b0100011".U) { // Stores (sb, sh, sw): mem[rs1 + imm] = rs2
      io.aluSrcA  := 0.U    // rs1
      io.aluSrcB  := true.B // immediate
      io.aluOp    := 0.U    // add -> address
      io.memWrite := true.B
      // regWrite stays false: stores don't write a register
    }
    is("b1100011".U) { // Branches (beq, bne, blt, bge, bltu, bgeu)
      io.aluSrcA := 0.U     // rs1
      io.aluSrcB := false.B // rs2
      io.branch  := true.B
      // compare (selected by funct3) is done in Execute; target = PC + imm
    }
    is("b1101111".U) { // JAL: rd = PC + 4, PC = PC + imm
      io.regWrite := true.B
      io.memToReg := 2.U // write PC + 4 to rd
      io.jump     := true.B
    }
    is("b1100111".U) { // JALR: rd = PC + 4, PC = (rs1 + imm) & ~1
      io.regWrite := true.B
      io.aluSrcA  := 0.U    // rs1
      io.aluSrcB  := true.B // immediate
      io.aluOp    := 0.U    // add -> jump target
      io.memToReg := 2.U    // write PC + 4 to rd
      io.jalr     := true.B
    }
  }
}
