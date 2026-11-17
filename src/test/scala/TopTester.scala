import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimpleTest extends AnyFlatSpec with
ChiselScalatestTester {
"DUT" should "pass" in {
test(new Read("src/test/scala/test_add.bin") ) { dut =>
dut.io.pc_in.poke(0.U)
dut.clock.step(8)
println("given test file is " + dut.memoryBytes.length + " bytes.")
  println("testing instructions now...")
for (i <- 0 until dut.memoryBytes.length/4 ){ //please dont make program with odd amount of bytes....
  dut.io.pc_in.poke(i*4)
  println (" Result for instruction("+i+")(pc "+i*4+") is:" + dut.io.instruction_out.peekInt().toString(2).reverse.padTo(32,'0').reverse)
}
}
}
}
class ExecuteTest extends AnyFlatSpec with ChiselScalatestTester {
  "ExecuteTest" should "pass" in {
    test(new Execute()) { dut =>
      println("Execute test R-type")
      dut.io.opcode.poke("b0110011".U)
      dut.io.pc.poke(4.U)
      dut.io.rs1_val.poke("h_21".U)
      dut.io.rs2_val.poke("h_4300".U)
      dut.io.imm.poke(0.U)
      dut.io.funct3.poke("h_0".U)

      println("add")
      dut.io.funct7.poke("h_0".U)
      dut.io.data.expect("h_4321".U)
      dut.io.writeEnable.expect(true.B)

      println("sub")
      dut.io.funct7.poke("h_20".U)
      dut.io.data.expect("h_FFFFBD21".U)

      println("sll")
      dut.io.funct3.poke("h_1".U)
      dut.io.data.expect("h_21".U)

      println("slt")
      dut.io.funct3.poke("h_2".U)
      dut.io.data.expect(1.U)

      println("sltu")
      dut.io.funct3.poke("h_3".U)
      dut.io.data.expect(1.U)

      println("xor")
      dut.io.funct3.poke("h_4".U)
      dut.io.data.expect("h_4321".U)

      println("srl")
      dut.io.funct3.poke("h_5".U)
      dut.io.funct7.poke("h_0".U)
      dut.io.data.expect("h_21".U)

      println("sra")
      dut.io.funct7.poke("h_20".U)
      dut.io.data.expect("h_21".U)

      println("or")
      dut.io.funct3.poke("h_6".U)
      dut.io.data.expect("h_4321".U)

      println("and")
      dut.io.funct3.poke("h_7".U)
      dut.io.data.expect(0.U)
      
      println("R-type complete, Testing I-type")
      dut.io.opcode.poke("b0010011".U)
      dut.io.imm.poke(5.U)
      
      println("addi")
      dut.io.funct3.poke("h_0".U)
      dut.io.data.expect("h_26".U)
      dut.io.writeEnable.expect(true.B)

      println("slli")
      dut.io.funct3.poke("h_1".U)
      dut.io.data.expect("h_420".U)

      println("slti")
      dut.io.imm.poke("h_FFFFFFF0".U) //-16
      dut.io.funct3.poke("h_2".U)
      dut.io.data.expect(0.U)

      println("sltui")
      dut.io.imm.poke("h_100".U)
      dut.io.funct3.poke("h_3".U)
      dut.io.data.expect(1.U)

      println("xori")
      dut.io.imm.poke(5.U)
      dut.io.funct3.poke("h_4".U)
      dut.io.data.expect("h_24".U)

      println("srli")
      dut.io.funct3.poke("h_5".U)
      dut.io.funct7.poke("h_0".U)
      dut.io.data.expect("h_1".U) // h_21 >> 5 = h_1

      println("srai")
      dut.io.funct7.poke("h_20".U)
      dut.io.data.expect("h_1".U) //positive so same as srli

      println("ori")
      dut.io.funct3.poke("h_6".U)
      dut.io.data.expect("h_25".U)

      println("andi")
      dut.io.funct3.poke("h_7".U)
      dut.io.data.expect(1.U)

      println("I-type complete, testing lui") //Check the imm goes through
      dut.io.opcode.poke("b0110111".U)
      dut.io.imm.poke("h_87654321".U)
      dut.io.data.expect("h_87654321".U)
      dut.io.writeEnable.expect(true.B)

      println("lui complete, testing auipc")
      dut.io.opcode.poke("b0010111".U)
      dut.io.imm.poke(4.U) //Not true valid imm value, but just checks the calculation is correct.
      dut.io.data.expect(8.U) // pc=4.u + imm=4.U. data=4.U + 4.U = 8.U
      dut.io.writeEnable.expect(true.B)

      println("auipc complete, testing load output is correct")
      dut.io.opcode.poke("b0000011".U)
      println("lb")
      dut.io.funct3.poke("h_0".U)
      dut.io.rs1_val.poke("h_1000".U)
      dut.io.imm.poke(4.U)
      dut.io.adress.expect("h_1002".U)
      dut.io.LS_size.expect(1.U)
      dut.io.loadReady.expect("b11".U)

      println("lh")
      dut.io.funct3.poke("h_1".U)
      dut.io.LS_size.expect(2.U)
      dut.io.loadReady.expect("b11".U)

      println("lw")
      dut.io.funct3.poke("h_2".U)
      dut.io.LS_size.expect(4.U)
      dut.io.loadReady.expect("b11".U)

      println("lbu")
      dut.io.funct3.poke("h_4".U)
      dut.io.LS_size.expect(1.U)
      dut.io.loadReady.expect("b10".U)
      
      println("lhu")
      dut.io.funct3.poke("h_5".U)
      dut.io.LS_size.expect(2.U)
      dut.io.loadReady.expect("b10".U)

      println("Load complete, testing stores")
      dut.io.opcode.poke("b0100011".U)
      println("sb")
      dut.io.funct3.poke("h_0".U)
      dut.io.saveReady.expect(true.B)
      dut.io.LS_size.expect(1.U)

      println("sh")
      dut.io.funct3.poke("h_1".U)
      dut.io.saveReady.expect(true.B)
      dut.io.LS_size.expect(2.U)

      println("sw")
      dut.io.funct3.poke("h_2".U)
      dut.io.saveReady.expect(true.B)
      dut.io.LS_size.expect(4.U)

      println("Save complete, testing B-type")
      dut.io.opcode.poke("b1100011".U)

      println("beq")
      dut.io.funct3.poke("h_0".U)

      println("bne")
      dut.io.funct3.poke("h_1".U)

      println("blt")
      dut.io.funct3.poke("h_4".U)

      println("bge")
      dut.io.funct3.poke("h_5".U)

      println("bltu")
      dut.io.funct3.poke("h_6".U)

      println("bgeu")
      dut.io.funct3.poke("h_7".U)




}
}
}



class MemTest extends AnyFlatSpec with ChiselScalatestTester{
  "memTest" should "pass" in {test(new MemReadLoad() ) { dut =>
println("unsigned test")
dut.io.address.poke(1.U)
dut.io.wordIn.poke(44.U)
dut.io.size.poke(1.U)
dut.io.saveReady.poke(1.U)
println("Saving byte 00101100 address=>"+1+"...")
dut.clock.step(2)
dut.io.saveReady.poke(0.U)
dut.clock.step(2)
dut.io.address.poke(2.U)
dut.io.wordIn.poke(300.U)
dut.io.size.poke(2.U)
dut.io.saveReady.poke(1.U)
println("Saving halfword 0000000100101100 at address 2 and 3.")

println("expecting word 00000000 00101100 00000001 00101100 from load...")
dut.clock.step(2)
dut.io.saveReady.poke(0.U)
dut.clock.step(2)
dut.io.address.poke(0.U)

dut.io.size.poke(4.U)
dut.io.loadReady.poke(1.U)
dut.clock.step(1)
dut.io.wordOut.expect("b00000001_00101100_00101100_00000000".U)  // 0x012C2C00

}
}
}

