# 02155 Computer Architecture and Engineering – Final Assignment

A single-cycle RISC-V (RV32I) processor written in Chisel. It runs in simulation and on a Basys 3 FPGA,
and prints/sends the 32 registers when the program ends (`ecall` with a7 = 10, or PC past the end).

**Group members:**

| Name | GitHub |
|---|---|
| Christian Emil Mohr Bentzen | [@Pikachu287](https://github.com/Pikachu287) |
| Noah Dahl Christensen | [@Noahdc9](https://github.com/Noahdc9) |
| Mikkel Emil Hjorth Sørensen | [@hjorthsorensen](https://github.com/hjorthsorensen) |

**Who made what:**

| Module | File | Author |
|---|---|---|
| Top | `src/main/scala/Top.scala` | Christian, Noah, Mikkel |
| Read | `src/main/scala/Read.scala` | Noah |
| Decode | `src/main/scala/Decode.scala` | Mikkel |
| Execute | `src/main/scala/Execute.scala` | Christian |
| MemReadLoad | `src/main/scala/MemReadLoad.scala` | Noah |
| Basys3Top | `src/main/scala/Basys3Top.scala` | Christian, Noah, Mikkel |
| UART | `src/main/scala/Uart.scala` | Martin Schoeberl (*Digital Design with Chisel*), 1 stop bit change by us |

## Requirements

- Java 11+ and [sbt](https://www.scala-sbt.org/)
- For the FPGA: Vivado 2025.2 or newer, a Basys 3 board, Python 3 with `pip install pyserial`

## 1. Run all tests

    sbt test

Runs every course program in `tests/task1`–`task4` and compares the registers with the `.res` files.

## 2. Run your own program (simulation)

1. Put your program in `program/` and name it **`program.bin`**
   (optional: the expected result as **`program.res`**, then it is checked too).
2. Run:

       sbt "testOnly Program"

It prints x0–x31 and writes the binary dump `program/program.dump` (same format as `.res`).

## 3. Run on the Basys 3 FPGA

1. Put your program in `program/program.bin` (+ `program.res`) as above.
2. Generate the Verilog:

       sbt run

3. Open `fpga/vivado/vivadoproject/vivadoproject.xpr` in Vivado.
4. **Flow Navigator → right-click Run Synthesis → Reset Synthesis Run**
   (Vivado does not notice that `generated/Basys3Top.v` changed, so always do this).
5. **Generate Bitstream → Open Hardware Manager → Program Device → Program**.
6. Find the board's COM port (Device Manager → Ports → "USB Serial Port (COMx)") and run:

       python fpga/read_regs.py COM4

7. **Press btnC** (middle button). The board resets, runs the program, and sends the registers.
   The script prints them and says `MATCHES .res` or `DIFFERENT from .res`.

On the board: **LD0** (above switch SW0) lights up when the program is done. **btnC** = reset.

### FPGA limits

- Data memory on the board is only 64 bytes (it is built from flip-flops), so programs that use a stack
  (`loop`, `recursive`, t12, t13) or a `.data` section (`string`, t14) do not run on the board yet.
  They all pass in simulation.
- The CPU runs at 12.5 MHz (100 MHz / 8).

## Tests

All tests are in `src/test/scala/TopTester.scala`. `sbt test` runs them all; `sbt "testOnly <Name>"` runs one.

| Test | What it checks | Author |
|---|---|---|
| `CourseTests` | Runs all 29 course programs in `tests/` and compares the registers with the `.res` files | Mikkel |
| `Program` | Runs `program/program.bin`, prints the registers, writes `program.dump`, compares with `program.res` | Mikkel |
| `DecodeTest` | Decode on its own: one instruction of each type (R, I, load, store, branch, jal, lui, ecall) | Mikkel |
| `ExecuteTest` | Execute on its own: every ALU operation, branches, jumps, edge cases | Christian |
| `MemTest` | MemReadLoad on its own: byte/halfword stores, word load | Noah |
| `TopTest` | Top: PC, instruction fetch, decode fields and final register values for `test_add.bin` | Noah |
| `SimpleTest` | Read: prints every instruction of `test_add.bin` (debug, always passes) | Noah |
| `dataDump` | Top: prints all registers after `test_add.bin` (debug, always passes) | Noah |

## Project structure

| Path | What |
|---|---|
| `src/main/scala/Top.scala` | The CPU: connects Read, Decode, Execute, MemReadLoad and the registers |
| `src/main/scala/Read.scala` | Instruction memory (the program is built in from the `.bin`) |
| `src/main/scala/Decode.scala` | Splits the instruction and sets the control signals |
| `src/main/scala/Execute.scala` | ALU, branches, jumps |
| `src/main/scala/MemReadLoad.scala` | Data memory (lb/lh/lw/lbu/lhu/sb/sh/sw) |
| `src/main/scala/Basys3Top.scala` | FPGA wrapper: slow clock, done LED, UART register dump |
| `src/main/scala/Uart.scala` | UART transmitter/receiver |
| `src/main/scala/Main.scala` | `sbt run`: generates `generated/Basys3Top.v` |
| `src/test/scala/TopTester.scala` | All tests (see above) |
| `tests/` | Course test programs (`.bin` + expected `.res`) |
| `program/` | Put the program you want to run here as `program.bin` |
| `fpga/` | `Basys3.xdc` (pins), `read_regs.py` (UART reader), Vivado project |
