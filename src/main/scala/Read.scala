import chisel3._
import chisel3.util._
import java.nio.file.{Files, Paths}

class Read(programFile: String) extends Module {
  val io = IO(new Bundle {
    val pc_in = Input(UInt(32.W))
    val instruction_out = Output(UInt(32.W))
  })

// Read should import all instructions in memory register,
// and send the corresponding instruction to instruction_out.


// pc_in -> index in memory -> instruction -> out
  //val memory  = Files.readAllBytes(Paths.get(programFile))
  val memoryVec = VecInit(Files.readAllBytes(Paths.get(programFile)).map(_.U(32.W)))

  io.instruction_out := memoryVec(io.pc_in)

}
