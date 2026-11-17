import chisel3._
import chisel3.util._

class Execute extends Module {
  val io = IO(new Bundle {
    //Inputs så den kender instruktionen
    val opcode = Input(UInt(7.W))
    // val rs1 = Input(UInt(5.W))
    // val rs2 = Input(UInt(5.W))
    // val rd = Input(UInt(5.W))
    // val rs1 = Input(UInt(5.W))
    // val rs2 = Input(UInt(5.W))
    // val rd = Input(UInt(5.W))
    val funct3 = Input(UInt(3.W))
    val funct7 = Input(UInt(7.W))
    val imm = Input(UInt(32.W))
    val rs1_val = Input(UInt(32.W))
    val rs2_val = Input(UInt(32.W))
    val pc = Input(UInt(32.W))

    //OUTPUTS. Vel basically kun den nye PC eller opdaterede værdier i en af de 32 Registre.
    val pc_new = Output(UInt(32.W))
    val data = Output(UInt(32.W)) //Bare kaldt data for nu, Vi kan lige ændre senere
    val writeEnable = Output(Bool())

    val adress = Output(UInt(32.W))
    val loadReady = Output(UInt(2.W))
    val saveReady = Output(Bool())
    val LS_size = Output(UInt(3.W))

  })
    // VI kan vælge om vi har lyst til at videre inddele så at vi har en seperat ALU, BRANCH og eller MEMORY, eller vi bare har 1 fil med execute.

  // R-Type
  val add = Wire(UInt(32.W))
  add := io.rs1_val + io.rs2_val

  val sub = Wire(UInt(32.W))
  sub := io.rs1_val - io.rs2_val
  
  val sll = Wire(UInt(32.W))
  sll := io.rs1_val << io.rs2_val(4, 0)
  
  val slt = Wire(UInt(32.W))
  when(io.rs1_val.asSInt < io.rs2_val.asSInt){
    slt := 1.U
  }.otherwise{
    slt := 0.U
  }
  
  val sltu = Wire(UInt(32.W))
  when(io.rs1_val < io.rs2_val){
    sltu := 1.U
  }.otherwise{
    sltu := 0.U
  }
  
  val xor = Wire(UInt(32.W))
  xor := io.rs1_val ^ io.rs2_val
  
  val sra = Wire(UInt(32.W))
  sra := (io.rs1_val.asSInt >> io.rs2_val(4, 0)).asUInt
  
  val srl = Wire(UInt(32.W))
  srl := io.rs1_val >> io.rs2_val(4, 0)
  
  val or = Wire(UInt(32.W))
  or := io.rs1_val | io.rs2_val
  
  val and = Wire(UInt(32.W))
  and := io.rs1_val & io.rs2_val

  // I-Type
  val shamt = Wire(UInt(5.W))
  shamt := io.imm(4, 0)

  val addi = Wire(UInt(32.W))
  addi := io.rs1_val + io.imm

  val slli = Wire(UInt(32.W))
  slli := io.rs1_val << shamt
  
  val slti = Wire(UInt(32.W))
  when(io.rs1_val.asSInt < io.imm.asSInt){
    slti := 1.U
  }.otherwise{
    slti := 0.U
  }
  
  val sltiu = Wire(UInt(32.W))
  when(io.rs1_val < io.imm){
    sltiu := 1.U
  }.otherwise{
    sltiu := 0.U
  }
  
  val xori = Wire(UInt(32.W))
  xori := io.rs1_val ^ io.imm
  
  val srli = Wire(UInt(32.W))
  srli := io.rs1_val >> shamt
  
  val srai = Wire(UInt(32.W))
  srai := (io.rs1_val.asSInt >> shamt).asUInt
  
  val ori = Wire(UInt(32.W))
  ori := io.rs1_val | io.imm
  
  val andi = Wire(UInt(32.W))
  andi := io.rs1_val & io.imm

  //ECALL/EBREAK

  //Loads
  val adress = (io.rs1_val + io.imm)
  // val lb = Wire(UInt(32.W))
  // val lh = Wire(UInt(32.W))
  // val lw = Wire(UInt(32.W))
  // val lbu = Wire(UInt(32.W))
  // val lhu = Wire(UInt(32.W))

  //JALR
  val jalr = Wire(UInt(32.W))
  jalr := io.pc + 4.U//To save
  val newPC_jalr = (io.rs1_val + io.imm) & "h_FFFFFFFE".U
  
  //JAL
  val jal = Wire(UInt(32.W))
  jal := io.pc + 4.U //to be saved
  val newPC_jal = io.pc + io.imm


  //Stores
  // val sb = Wire(UInt(32.W))
  // val sh = Wire(UInt(32.W))
  // val sw = Wire(UInt(32.W))

  //B-Type
  val beq = Wire(UInt(32.W))
  when(io.rs1_val === io.rs2_val){
    beq := io.pc + io.imm
  }.otherwise{
    beq := io.pc + 4.U
  }
  
  val bne = Wire(UInt(32.W))
  when(io.rs1_val =/= io.rs2_val){
    bne := io.pc + io.imm
  }.otherwise{
    bne := io.pc + 4.U
  }
  
  val blt = Wire(UInt(32.W))
  when(io.rs1_val.asSInt < io.rs2_val.asSInt){
    blt := io.pc + io.imm
  }.otherwise{
    blt := io.pc + 4.U
  }
  
  val bge = Wire(UInt(32.W))
  when(io.rs1_val.asSInt >= io.rs2_val.asSInt){
    bge := io.pc + io.imm
  }.otherwise{
    bge := io.pc + 4.U
  }
  
  val bltu = Wire(UInt(32.W))
  when(io.rs1_val < io.rs2_val){
    bltu := io.pc + io.imm
  }.otherwise{
    bltu := io.pc + 4.U
  }
  
  val bgeu = Wire(UInt(32.W))
  when(io.rs1_val >= io.rs2_val){
    bgeu := io.pc + io.imm
  }.otherwise{
    bgeu := io.pc + 4.U
  }

    //LUI - U-Type
  val lui = Wire(UInt(32.W))
  lui := io.imm

  // auipc - U-Type
  val auipc = Wire(UInt(32.W))
  auipc := io.pc + io.imm

  //Default output assignments
  io.pc_new := io.pc + 4.U
  io.data := 0.U
  io.writeEnable := false.B
  //IO for Save/Load
  io.adress := adress
  io.loadReady := 0.U
  io.saveReady := false.B
  io.LS_size := 0.U


    switch (io.opcode){
    is("b0110011".U) { //R-type (add, sub, sll, srl, sra, slt, sltu, and, or, xor)
      io.writeEnable := true.B//Will always write when R-type is given, may cause a wrong funct3/funct7 to write 0.U to a register
      switch(io.funct3){
        is("h_0".U){
          switch(io.funct7){
            is("h_0".U){//add
              io.data := add
            }
            is("h_20".U){//sub
              io.data := sub
            }
          }
        }
        is("h_1".U){//sll
          io.data := sll
        }
        is("h_2".U){//slt
          io.data := slt
        }
        is("h_3".U){//sltu
          io.data := sltu
        }
        is("h_4".U){//xor
          io.data := xor
        }
        is("h_5".U){//
          switch(io.funct7){
            is("h_0".U){//srl
              io.data := srl
            }
            is("h_20".U){//sra
              io.data := sra
            }
          }
        }
        is("h_6".U){//or
          io.data := or
        }
        is("h_7".U){//and
          io.data := and
        }
      }
    }
    is("b0010011".U) { // I-type (addi, slti, sltiu, xori, ori, andi, slli, srli, srai)
      io.writeEnable := true.B//Will always write when I-type is given, may cause a wrong funct3/funct7 to write 0.U to a register
      switch(io.funct3){
        is("h_0".U){//addi
          io.data := addi
        }
        is("h_1".U){//slli
          io.data := slli
        }
        is("h_2".U){//slti
          io.data := slti
        }
        is("h_3".U){//sltiu
          io.data := sltiu
        }
        is("h_4".U){//xori
          io.data := xori
        }
        is("h_5".U){//
          switch(io.funct7){
            is("h_0".U){//srli
              io.data := srli
            }
            is("h_20".U){//srai
              io.data := srai
            }
          }
        }
        is("h_6".U){//ori
          io.data := ori
        }
        is("h_7".U){//andi
          io.data := andi
        }
      }

    }
    is("b0110111".U) { // LUI
      io.writeEnable := true.B//Will always write when Lui is given
      io.data := lui
    }
    is("b0010111".U) { // auipc 
      io.writeEnable := true.B//Will always write when auipc is given
      io.data := auipc
    }
    is("b1110011".U) { // ecall - Empty for now, need to execute syscall
    }
    is("b0000011".U) { // Loads (lb, lh, lw, lbu, lhu)
      
      switch(io.funct3){
        is("h_0".U){//lb
          io.loadReady := "b11".U
          io.LS_size := 1.U
        }
        is("h_1".U){//lh
          io.loadReady := "b11".U
          io.LS_size := 2.U
          
        }
        is("h_2".U){//lw
          io.loadReady := "b11".U
          io.LS_size := 4.U
        }
        is("h_4".U){//lbu
          io.loadReady := "b01".U
          io.LS_size := 1.U
        }
        is("h_5".U){//lhu
          io.loadReady := "b01".U
          io.LS_size := 2.U
        }
      }
    }
    is("b0100011".U) { // Stores (sb, sh, sw)
      switch(io.funct3){
        is("h_0".U){//sb
          io.LS_size := 1.U
          io.saveReady := true.B
        }
        is("h_1".U){//sh
          io.LS_size := 2.U
          io.saveReady := true.B
        }
        is("h_2".U){//sw
          io.LS_size := 4.U
          io.saveReady := true.B
        }
      }
    }
    is("b1100011".U) { // Branches (beq, bne, blt, bge, bltu, bgeu)
      switch(io.funct3){
        is("h_0".U){//beq
          io.pc_new := beq

        }
        is("h_1".U){//bne
          io.pc_new := bne
        }
        is("h_4".U){//blt
          io.pc_new := blt
        }
        is("h_5".U){//bge
          io.pc_new := bge
        }
        is("h_6".U){//bltu
          io.pc_new := bltu
        }
        is("h_7".U){//bgeu 
          io.pc_new := bgeu
        }
      }
    }
    is("b1101111".U) { // JAL
      io.data := jal
      io.pc_new := newPC_jal

    }
    is("b1100111".U) { // JALR
      io.data := jalr
      io.pc_new := newPC_jalr
    }

  }
}
