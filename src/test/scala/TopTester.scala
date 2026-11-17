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

