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

//memory loads entire file passed to program at generation; memoryBytes loads
//into Bytes, and instruction out is then passed onwards as those four concatenated,
//little endian direction.
  val memory  = Files.readAllBytes(Paths.get(programFile))
  val memoryBytes = VecInit(memory.map(b => (b & 0xFF).U(8.W)).toSeq)


  io.instruction_out := Cat(memoryBytes(io.pc_in+3.U),
  memoryBytes(io.pc_in+2.U),
  memoryBytes(io.pc_in+1.U),
  memoryBytes(io.pc_in))

}
