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


