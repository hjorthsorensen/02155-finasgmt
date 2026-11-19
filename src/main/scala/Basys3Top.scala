import chisel3._
import chisel3.util._

// Basys 3 board: runs Top on a slower clock, LED 0 turns on when the program is done,
// then sends x0-x31 over UART (4 bytes each, little-endian, same as .res)
class Basys3Top(programFile: String, clockHz: Int = 100000000, baud: Int = 115200) extends Module {
  val io = IO(new Bundle {
    val led = Output(Bool())
    val txd = Output(UInt(1.W))
  })

  // 100 MHz / 8 = 12.5 MHz for the CPU. Reg without init so btnC doesn't stop the clock
  val clockDiv = Reg(UInt(3.W))
  clockDiv := clockDiv + 1.U

  val cpu = withClock(clockDiv(2).asClock)(Module(new Top(programFile, memBytes = 64)))

  io.led := cpu.io.done

  // -- UART -- //
  val doneOld = RegNext(cpu.io.done)
  val start   = cpu.io.done && !doneOld // done just went high

  // byteCount = which of the 128 bytes we send next, 128 = done sending
  val byteCount = RegInit(128.U(8.W))
  when(start)(byteCount := 0.U)

  val regNum  = byteCount / 4.U
  val byteNum = byteCount % 4.U
  val reg  = cpu.io.dB_regs(regNum)
  val byte = (reg >> (byteNum * 8.U))(7, 0) // byte 0 = lowest 8 bits

  val tx = Module(new Tx(clockHz, baud))
  tx.io.channel.bits  := byte
  tx.io.channel.valid := byteCount < 128.U
  when(tx.io.channel.valid && tx.io.channel.ready)(byteCount := byteCount + 1.U)
  io.txd := tx.io.txd
}
