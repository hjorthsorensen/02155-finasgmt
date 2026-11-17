import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RiscVSpec extends AnyFlatSpec with ChiselScalatestTester with Matchers {
  "RiscV" should "pass" in {
    test(new Top()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      

      for (i <- 0 until 4) {
        printReg()
        dut.clock.step(1)
      }
    }
  }
}
