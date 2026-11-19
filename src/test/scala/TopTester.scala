import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.File
import java.nio.file.{Files, Paths}

class dataDump extends AnyFlatSpec with ChiselScalatestTester {
  "datadump" should "print all registers.... :)" in test(new Top("src/test/scala/test_add.bin")) { dut =>
    dut.clock.step(20) // we get to after ecall is done:)

    for (i <- 0 until 32) {
      println("data at " + i + " is...")
      println("as SInt: " + dut.io.dB_regs(i).peek().litValue.toInt)
      println("hex:     " + dut.io.dB_regs(i).peek().litValue.toString(16))
      println("binary:  " + dut.io.dB_regs(i).peekInt().toString(2).reverse.padTo(32, '0').reverse + " .")
    }
    println("done! :)")
  }
}

class SimpleTest extends AnyFlatSpec with ChiselScalatestTester {
  "DUT" should "pass" in test(new Read("src/test/scala/test_add.bin")) { dut =>
    dut.io.pc_in.poke(0.U)
    dut.clock.step(8)
    println("given test file is " + dut.memoryBytes.length + " bytes.")
    println("testing instructions now...")
    for (i <- 0 until dut.memoryBytes.length / 4) { // please dont make program with odd amount of bytes....
      dut.io.pc_in.poke(i * 4)
      println(
        " Result for instruction(" + i + ")(pc " + i * 4 + ") is:" + dut.io.instruction_out.peekInt().toString(2)
          .reverse.padTo(32, '0').reverse
      )
    }
  }
}
class TopTest extends AnyFlatSpec with ChiselScalatestTester {
  "TopTest" should "pass values around" in test(new Top("src/test/scala/test_add.bin")) { dut =>
    // tjek at PC fra top -> read -> instruction out -> decode -> execute -> top -> forfra

    println("Testing that pc can be set and changed, and that it changes instruction_out.")
    dut.io.pc_out.expect(0.U)
    println("Testing that first instruction got to Read.")
    dut.io.dB_instruction_out.expect("b00000111110100000000001010010011".U)
    dut.clock.step(1)
    dut.io.pc_out.expect(4.U)

    println("Testing that second instruction got to Read.")
    dut.io.dB_instruction_out.expect("b00000000010100000000001100110011".U)
    println("Testing that decode has correctly read the instruction.")
    println("Second instruction is: add t1, x0, t0.")
    dut.io.dB_opcode.expect("b110011".U)
    dut.io.dB_rs1.expect("b00000".U)
    dut.io.dB_rs2.expect("b00101".U)
    dut.io.dB_rd.expect("b00110".U)
    dut.io.dB_f3.expect("b000".U)
    dut.io.dB_f7.expect("b0000000".U)
    dut.io.dB_regs("b00101".U).expect(125.U)
    dut.clock.step(1)
    dut.io.dB_regs("b00110".U).expect(125.U)

    // now testing if final values are correct
    dut.clock.step(5)

    dut.io.dB_regs(5).expect(((BigInt(1) << 32) - 100).U) // t0 (111111..... - 100 = signed -100.)
    dut.io.dB_regs(6).expect(125.U)                       // t1
    dut.io.dB_regs(7).expect(160.U)                       // t2

    dut.io.dB_regs(28).expect(60.U) // t3

    dut.io.dB_regs(10).expect(10.U) // a0

  }
}

class ExecuteTest extends AnyFlatSpec with ChiselScalatestTester {
  "ExecuteTest" should "pass" in {
    test(new Execute()) { dut =>
      // all control flags off to start
      dut.io.isLui.poke(false.B)
      dut.io.isAuipc.poke(false.B)
      dut.io.isBranch.poke(false.B)
      dut.io.isJal.poke(false.B)
      dut.io.isJalr.poke(false.B)

      println("Execute test R-type")
      dut.io.useImm.poke(false.B)
      dut.io.pc.poke(4.U)
      dut.io.rs1_val.poke("h_21".U)
      dut.io.rs2_val.poke("h_4300".U)
      dut.io.imm.poke(0.U)
      dut.io.funct3.poke("h_0".U)

      println("add")
      dut.io.aluOp.poke("h_0".U)
      dut.io.data.expect("h_4321".U)

      println("sub")
      dut.io.aluOp.poke("h_8".U)
      dut.io.data.expect("h_FFFFBD21".U)

      println("sll")
      dut.io.aluOp.poke("h_1".U)
      dut.io.data.expect("h_21".U) // shamt = 0x4300 & 31 = 0

      println("sll by rs2")
      dut.io.rs2_val.poke("h_24".U) // only low 5 bits count -> shamt 4
      dut.io.data.expect("h_210".U)
      dut.io.rs2_val.poke("h_4300".U)

      println("slt")
      dut.io.aluOp.poke("h_2".U)
      dut.io.data.expect(1.U)

      println("sltu")
      dut.io.aluOp.poke("h_3".U)
      dut.io.data.expect(1.U)

      println("sltu vs slt with negative")
      dut.io.rs1_val.poke("h_FFFFFFFF".U) // -1 signed, huge unsigned
      dut.io.rs2_val.poke(1.U)
      dut.io.data.expect(0.U) // sltu: 0xFFFFFFFF < 1 is false
      dut.io.aluOp.poke("h_2".U)
      dut.io.data.expect(1.U) // slt: -1 < 1 is true
      dut.io.rs1_val.poke("h_21".U)
      dut.io.rs2_val.poke("h_4300".U)

      println("xor")
      dut.io.aluOp.poke("h_4".U)
      dut.io.data.expect("h_4321".U)

      println("srl")
      dut.io.aluOp.poke("h_5".U)
      dut.io.data.expect("h_21".U)

      println("sra")
      dut.io.aluOp.poke("h_D".U)
      dut.io.data.expect("h_21".U)

      println("or")
      dut.io.aluOp.poke("h_6".U)
      dut.io.data.expect("h_4321".U)

      println("and")
      dut.io.aluOp.poke("h_7".U)
      dut.io.data.expect(0.U)

      println("unused aluOp")
      dut.io.aluOp.poke("h_9".U)
      dut.io.data.expect(0.U)
      dut.io.aluOp.poke("h_F".U)
      dut.io.data.expect(0.U)

      println("R-type complete, Testing I-type")
      dut.io.useImm.poke(true.B)
      dut.io.imm.poke(5.U)

      println("addi")
      dut.io.aluOp.poke("h_0".U)
      dut.io.data.expect("h_26".U)

      println("slli")
      dut.io.aluOp.poke("h_1".U)
      dut.io.data.expect("h_420".U)

      println("slti")
      dut.io.imm.poke("h_FFFFFFF0".U) // -16
      dut.io.aluOp.poke("h_2".U)
      dut.io.data.expect(0.U)

      println("sltiu")
      dut.io.imm.poke("h_100".U)
      dut.io.aluOp.poke("h_3".U)
      dut.io.data.expect(1.U)

      println("xori")
      dut.io.imm.poke(5.U)
      dut.io.aluOp.poke("h_4".U)
      dut.io.data.expect("h_24".U)

      println("srli")
      dut.io.aluOp.poke("h_5".U)
      dut.io.data.expect("h_1".U) // h_21 >> 5 = h_1

      println("srai")
      dut.io.aluOp.poke("h_D".U)
      dut.io.data.expect("h_1".U) // positive so same as srli

      println("srai negative")
      dut.io.rs1_val.poke("h_80000000".U)
      dut.io.data.expect("h_FC000000".U) // sign bit is copied in

      println("srli negative")
      dut.io.aluOp.poke("h_5".U)
      dut.io.data.expect("h_04000000".U) // zeros shifted in, not the sign bit
      dut.io.rs1_val.poke("h_21".U)

      println("ori")
      dut.io.aluOp.poke("h_6".U)
      dut.io.data.expect("h_25".U)

      println("andi")
      dut.io.aluOp.poke("h_7".U)
      dut.io.data.expect(1.U)

      println("I-type complete, testing lui") // Check the imm goes through
      dut.io.isLui.poke(true.B)
      dut.io.imm.poke("h_87654321".U)
      dut.io.data.expect("h_87654321".U)
      dut.io.isLui.poke(false.B)

      println("lui complete, testing auipc")
      dut.io.isAuipc.poke(true.B)
      dut.io.imm.poke(4.U)    // Not true valid imm value, but just checks the calculation is correct.
      dut.io.data.expect(8.U) // pc=4.U + imm=4.U = 8.U
      dut.io.isAuipc.poke(false.B)

      println("auipc complete, testing load/store address") // size/sign is Decode's job now
      dut.io.rs1_val.poke("h_1000".U)
      dut.io.imm.poke(4.U)
      dut.io.address.expect("h_1004".U)
      dut.io.imm.poke("h_FFFFFFFC".U) // -4
      dut.io.address.expect("h_FFC".U)

      println("Address complete, testing B-type")
      dut.io.isBranch.poke(true.B)
      dut.io.useImm.poke(false.B)
      dut.io.pc.poke("h_100".U)
      dut.io.imm.poke("h_20".U)
      dut.io.rs1_val.poke(5.U)
      dut.io.rs2_val.poke(5.U)

      println("beq")
      dut.io.funct3.poke("h_0".U)
      dut.io.pc_new.expect("h_120".U) // 5 == 5, taken

      println("bne")
      dut.io.funct3.poke("h_1".U)
      dut.io.pc_new.expect("h_104".U) // not taken

      dut.io.rs1_val.poke("h_FFFFFFFF".U) // -1 signed, huge unsigned
      dut.io.rs2_val.poke(1.U)

      println("blt")
      dut.io.funct3.poke("h_4".U)
      dut.io.pc_new.expect("h_120".U) // -1 < 1, taken

      println("bge")
      dut.io.funct3.poke("h_5".U)
      dut.io.pc_new.expect("h_104".U)

      println("bltu")
      dut.io.funct3.poke("h_6".U)
      dut.io.pc_new.expect("h_104".U) // 0xFFFFFFFF < 1 is false unsigned

      println("bgeu")
      dut.io.funct3.poke("h_7".U)
      dut.io.pc_new.expect("h_120".U)

      println("bgeu backwards")
      dut.io.imm.poke("h_FFFFFFE0".U) // -32, like a loop
      dut.io.pc_new.expect("h_E0".U)
      dut.io.imm.poke("h_20".U)
      dut.io.isBranch.poke(false.B)

      println("B-type complete, testing jal")
      dut.io.isJal.poke(true.B)
      dut.io.pc_new.expect("h_120".U) // pc + imm
      dut.io.data.expect("h_104".U)   // rd = pc + 4

      println("jal backwards")
      dut.io.imm.poke("h_FFFFFFE0".U) // -32
      dut.io.pc_new.expect("h_E0".U)
      dut.io.isJal.poke(false.B)

      println("jalr")
      dut.io.isJalr.poke(true.B)
      dut.io.useImm.poke(true.B)
      dut.io.rs1_val.poke("h_1001".U)
      dut.io.imm.poke(4.U)
      dut.io.pc_new.expect("h_1004".U) // (0x1001 + 4) & ~1
      dut.io.data.expect("h_104".U)

      println("jalr ignores useImm")
      dut.io.useImm.poke(false.B) // target is always rs1 + imm
      dut.io.pc_new.expect("h_1004".U)
      dut.io.address.expect("h_1005".U)

      println("Execute test complete")
    }
  }
}

class MemTest extends AnyFlatSpec with ChiselScalatestTester {
  val program   = "tests/task1/addpos.bin"
  val firstByte = Files.readAllBytes(Paths.get(program))(0) & 0xff // memory starts preloaded with the program

  "memTest" should "pass" in test(new MemReadLoad(program)) { dut =>
    println("unsigned test")
    dut.io.address.poke(1.U)
    dut.io.wordIn.poke(44.U)
    dut.io.size.poke(1.U)
    dut.io.saveReady.poke(1.U)
    println("Saving byte 00101100 address=>" + 1 + "...")
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
    // byte 0 was never written, so it still holds the program's first byte
    dut.io.wordOut.expect(((BigInt("012C2C", 16) << 8) | firstByte).U) // 0x012C2C?? (?? = program byte 0)

  }
}

class CourseTests extends AnyFlatSpec with ChiselScalatestTester {
  val programs = Seq("task1", "task2", "task3", "task4").flatMap { task =>
    new File(s"tests/$task").listFiles.filter(_.getName.endsWith(".bin"))
      .map(f => s"tests/$task/${f.getName.stripSuffix(".bin")}").sorted
  }

  for (name <- programs) "Top" should s"run $name" in test(new Top(s"$name.bin")) { dut =>
    dut.clock.setTimeout(0) // programs can run longer than chiseltest's default 1000 cycles
    var cycles = 0
    while (!dut.io.done.peek().litToBoolean && cycles < 10000) {
      dut.clock.step()
      cycles += 1
    }
    assert(dut.io.done.peek().litToBoolean, s"$name never finished (hit the cycle limit)")
    println(s"$name finished after $cycles cycles")

    val dump = (0 until 32).flatMap { i =>
      val v = dut.io.dB_regs(i).peekInt()
      (0 until 4).map(b => ((v >> (8 * b)) & 0xff).toByte)
    }.toArray
    Files.write(Paths.get(s"$name.dump"), dump)

    // .res = 32 registers, 4 bytes each, little-endian
    val res = Files.readAllBytes(Paths.get(s"$name.res"))
    for (i <- 0 until 32) {
      val expected = (0 until 4).map(b => BigInt(res(i * 4 + b) & 0xff) << (8 * b)).sum
      dut.io.dB_regs(i).expect(expected.U(32.W), s"$name: x$i wrong")
    }
  }
}


// Runs every .bin TA puts in program/: prints registers, writes .dump, .res
class Program extends AnyFlatSpec with ChiselScalatestTester {
  val programs = Option(new File("program").listFiles).getOrElse(Array.empty[File])
    .filter(_.getName.endsWith(".bin")).map(_.getPath.stripSuffix(".bin")).sorted

  for (name <- programs) {
    "Top" should s"run $name" in test(new Top(s"$name.bin")) { dut =>
      dut.clock.setTimeout(0)
      var cycles = 0
      while (!dut.io.done.peek().litToBoolean && cycles < 100000) {
        dut.clock.step()
        cycles += 1
      }
      assert(dut.io.done.peek().litToBoolean, s"$name never finished (hit the cycle limit)")
      println(s"$name finished after $cycles cycles")

      // print registers and write binary dump 
      val regs = (0 until 32).map(i => dut.io.dB_regs(i).peekInt())
      regs.zipWithIndex.foreach { case (v, i) => println(f"x$i%-2d = 0x$v%08x ($v)") }
      val dump = regs.flatMap(v => (0 until 4).map(b => ((v >> (8 * b)) & 0xff).toByte)).toArray
      Files.write(Paths.get(s"$name.dump"), dump)

      // compare with expected results
      val res = Paths.get(s"$name.res")
      if (Files.exists(res)) assert(java.util.Arrays.equals(dump, Files.readAllBytes(res)), s"$name: registers differ from .res")
    }
  }
}
