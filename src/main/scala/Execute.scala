import chisel3._

class Execute extends Module {
  val io = IO(new Bundle {
    //Inputs så den kender instruktionen
    val opcode = Input(UInt(7.W))
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val rd = Input(UInt(5.W))
    val funct3 = Input(UInt(3.W))
    val funct7 = Input(UInt(7.W))
    val imm = Input(UInt(32.W))

    //OUTPUTS. Vel basically kun den nye PC eller opdaterede værdier i en af de 32 Registre.
    val pc_new = Output(UInt(32.W))
    val data = Output(UInt(32.W)) //Bare kaldt data for nu, Vi kan lige ændre senere
  })
    // VI kan vælge om vi har lyst til at videre inddele så at vi har en seperat ALU, BRANCH og eller MEMORY, eller vi bare har 1 fil med execute.


}
