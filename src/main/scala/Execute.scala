import chisel3._
import chisel3.util._

class Execute extends Module {
  val io = IO(new Bundle {

    val funct3 = Input(UInt(3.W))
    val imm = Input(UInt(32.W))
    val rs1_val = Input(UInt(32.W))
    val rs2_val = Input(UInt(32.W))
    val pc = Input(UInt(32.W))

    //New inputs
    val aluOp = Input(UInt(4.W))
    val useImm = Input(Bool())
    val isLui = Input(Bool())
    val isAuipc = Input(Bool())
    val isBranch = Input(Bool())
    val isJal = Input(Bool())
    val isJalr = Input(Bool())

    //OUTPUTS. Vel basically kun den nye PC eller opdaterede værdier i en af de 32 Registre.
    val pc_new = Output(UInt(32.W))
    val data = Output(UInt(32.W)) //

    val address = Output(UInt(32.W)) //Adress for load/stores.

  })

  //R and I-type
  val a = io.rs1_val
  val b = Mux(io.useImm, io.imm, io.rs2_val)
  val shamt = b(4,0)

  val aluOut = WireDefault(0.U(32.W))
  switch(io.aluOp){
    is("h_0".U) {aluOut := a + b}
    is("h_8".U) {aluOut := a - b} // 8=sub
    is("h_1".U) {aluOut := a << shamt}
    is("h_2".U) {aluOut := (a.asSInt < b.asSInt).asUInt}
    is("h_3".U) {aluOut := (a < b).asUInt}
    is("h_4".U) {aluOut := a ^ b}
    is("h_5".U) {aluOut := a >> shamt}
    is("h_D".U) {aluOut := (a.asSInt >> shamt).asUInt}
    is("h_6".U) {aluOut := a | b}
    is("h_7".U) {aluOut := a & b}
  }

  //Branch
  val eq = a === io.rs2_val
  val lt = a.asSInt < io.rs2_val.asSInt
  val ltu = a < io.rs2_val

  val btaken = MuxLookup(io.funct3, false.B)(Seq(
    0.U -> eq, 1.U -> !eq, //beq + bne
    4.U -> lt, 5.U -> !lt, //blt + bge
    6.U -> ltu, 7.U -> !ltu // bltu + bgeu
  ))

  val pc4 = io.pc + 4.U
  val branchpc = io.pc + io.imm
  val jalrPc = (a + io.imm) & "h_FFFFFFFE".U

  //Default assignments (MuxCase makes it so the first argument is the default case.)
  io.pc_new := MuxCase(pc4, Seq(
    (io.isBranch && btaken) -> branchpc,
    io.isJal -> branchpc,
    io.isJalr -> jalrPc
  ))

  io.data := MuxCase(aluOut, Seq( // By default assumes R and I type, If isJal, isLui, isAuipc then another data is supplied to io.data
    (io.isJal || io.isJalr) -> pc4,
    io.isLui -> io.imm,
    io.isAuipc -> (io.pc + io.imm)
  ))

  io.address := a + io.imm
  

}
