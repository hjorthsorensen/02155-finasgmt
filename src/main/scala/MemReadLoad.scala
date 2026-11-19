import chisel3._
import chisel3.util._
import java.nio.file.{Files, Paths}
import chisel3.util.experimental.loadMemoryFromFileInline

class MemReadLoad(programFile: String, memBytes: Int = 4096) extends Module {
  val io = IO(new Bundle {
    val loadReady = Input(UInt(2.W)) // 01 = load, 11 = loadSigned
    val saveReady = Input(Bool())
    val size      = Input(UInt(3.W))
    val address   = Input(UInt(32.W))
    val wordIn    = Input(UInt(32.W))
    val wordOut   = Output(UInt(32.W))
  })

  val memory = Mem(memBytes, UInt(8.W)) // standard is 4 bytes so all tests can pass

  // start data memory with the program file in it, so .data be loaded
  val hexFile = programFile + ".hex"
  var hexText = ""
  for (b <- Files.readAllBytes(Paths.get(programFile))) {
    hexText += "%02x".format(b & 0xff) + "\n" // one byte per line, 2 hex digits
  }
  Files.write(Paths.get(hexFile), hexText.getBytes)
  loadMemoryFromFileInline(memory, hexFile)
  io.wordOut := 1.U
  switch(io.size) {
    is(0.U)(io.wordOut := 0.U)
    is(3.U)(io.wordOut := 0.U)
    is(1.U) {
      when(io.loadReady === 1.U) {
        // load unsignedbyte
        io.wordOut := memory(io.address)
      }
      when(io.loadReady === 3.U) {
        // load signed byte
        io.wordOut := Cat(Fill(24, memory(io.address)(7)), memory(io.address))

      }
      when(io.saveReady === 1.U) {
        // save byte
        memory(io.address) := io.wordIn(7, 0)
      }
    }
    is(2.U) {
      when(io.loadReady === 1.U) {
        // load unsigned halfword
        io.wordOut := Cat(memory(io.address + 1.U), memory(io.address))
      }
      when(io.loadReady === 3.U) {
        // load signed halfWord
        io.wordOut := Cat(Fill(16, memory(io.address + 1.U)(7)), memory(io.address + 1.U), memory(io.address))
      }
      when(io.saveReady === 1.U) {
        // save halfword
        memory(io.address + 1.U) := io.wordIn(15, 8)
        memory(io.address)       := io.wordIn(7, 0)

      }
    }
    is(4.U) {
      when(io.loadReady === 1.U) {
        // load unsigned word
        io.wordOut :=
          Cat(memory(io.address + 3.U), memory(io.address + 2.U), memory(io.address + 1.U), memory(io.address))

      }
      when(io.loadReady === 3.U) {
        // load signed word
        io.wordOut :=
          Cat(memory(io.address + 3.U), memory(io.address + 2.U), memory(io.address + 1.U), memory(io.address))

      }
      when(io.saveReady === 1.U) {
        // save word
        memory(io.address + 3.U) := io.wordIn(31, 24)
        memory(io.address + 2.U) := io.wordIn(23, 16)
        memory(io.address + 1.U) := io.wordIn(15, 8)
        memory(io.address)       := io.wordIn(7, 0)

      }

    }
  }

}
