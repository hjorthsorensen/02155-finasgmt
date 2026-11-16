import chisel3._
import chisel3.util._
import java.nio.file.{Files, Paths}

class MemReadLoad() extends Module {
  val io = IO(new Bundle {
    val loadReady = Input(UInt(2.W)) // 01 = load, 11 = loadSigned
    val saveReady = Input(Bool())
    val size      = Input(UInt(3.W))
    val address   = Input(UInt(32.W))
    val wordIn    = Input(UInt(32.W))
    val wordOut   = Output(UInt(32.W))
  })

  val memory = VecInit(Seq.fill(1024)(0.U(8.W))) // 1 kb of dynamic ram:)

  switch(io.loadReady) { // load unsigned
    is(1.U) {
      switch(io.size) {
        is(1.U) {
          io.wordOut := memory(io.address)
          // load byte
        }
        is(2.U) {
          io.wordOut := Cat(memory(io.address+1.U)+memory(io.address))
          // load hW
        }
        is(4.U) {
          io.wordOut := Cat(memory(io.address+3.U)+memory(io.address+2.U)+memory(io.address+1.U)+memory(io.address))

          // load w
        }
      }
    }
    is(2.U) { // load signed
      switch(io.size) {
        is(1.U) {

          // load byte
        }
        is(2.U) {

          // load hW
        }
        is(4.U) {

          // load w
        }
      }

    }
  }
  switch(io.size) {
    is(1.U) {

      // load byte
    }
    is(2.U) {

      // load hW
    }
    is(4.U) {

      // load w
    }
  }
}
