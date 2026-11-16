import chisel3._
import chisel3.util._

class Immediate extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W)) // from decode through top

    val imm = Output(UInt(32.W))
  })

  val opcode = io.instruction(6, 0) // Opcode decides the instruction type
  io.imm := 0.U // default value

  switch(opcode) {
    is("b0010011".U) { // I-type (addi, slti, sltiu, xori, ori, andi, slli, srli, srai)
      io.imm := io.instruction(31, 20).asSInt.pad(32).asUInt
    }
    is("b0000011".U) { // Loads (lb, lh, lw, lbu, lhu) - I-type
      io.imm := io.instruction(31, 20).asSInt.pad(32).asUInt
    }
    is("b0100011".U) { // Stores (sb, sh, sw - S-type)
      io.imm := Cat(io.instruction(31, 25), io.instruction(11, 7)).asSInt.pad(32).asUInt
    }
    is("b0110111".U) { // LUI (0 + imm) - U-type
      io.imm := Cat(io.instruction(31, 12), 0.U(12.W))
    }
    is("b0010111".U) { // AUIPC (pc + imm) - U-type
      io.imm := Cat(io.instruction(31, 12), 0.U(12.W))
    }
    is("b1100011".U) { // Branches (beq, bne, blt, bge, bltu, bgeu) - B-type
      io.imm := Cat(io.instruction(31), io.instruction(7), io.instruction(30, 25), io.instruction(11, 8), 0.U(1.W))
        .asSInt.pad(32).asUInt
    }
    is("b1101111".U) { // JAL - J-type
      io.imm := Cat(io.instruction(31), io.instruction(19, 12), io.instruction(20), io.instruction(30, 21), 0.U(1.W))
        .asSInt.pad(32).asUInt
    }
    is("b1100111".U) { // JALR - I-type
      io.imm := io.instruction(31, 20).asSInt.pad(32).asUInt
    }
  }

}
