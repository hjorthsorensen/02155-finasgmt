# 02155 Computer Architecture and Engineering – Final Assignment

A single-cycle RISC-V (RV32I) processor written in Chisel. It runs in simulation and on a Basys 3 FPGA.
The program stops at ecall with a7 = 10, or when the PC goes past the end of the program.

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
| Basys3Top | `src/main/scala/Basys3Top.scala` | Mikkel |
| UART | `src/main/scala/Uart.scala` | Martin Schoeberl, changed to 1 stop bit |

## Requirements

- Java 17+ and sbt
- For the FPGA: Vivado 2025.2 or newer (the project file is from 2025.2), a Basys 3 board and Python 3 with pyserial (sudo apt install python3-serial on Ubuntu)
  - If you use a .venv, install it there instead: pip install pyserial

## Run all tests

    sbt test

Runs the unit tests and every course program in `tests/task1` to `tests/task4`, and compares the registers with the .res files.

## Run your own program

Put your program in `program/` as `program.bin`. If you also put the expected result there as `program.res`, it is checked too.

    sbt "testOnly Program"

It prints x0-x31 and writes `program/program.dump` (same format as .res).

## Run on the Basys 3

1. Put your program in `program/program.bin` (and `program.res`) like above.
2. sbt run generates `generated/Basys3Top.v`.
3. Open `fpga/vivado/vivadoproject/vivadoproject.xpr`. Vivado doesn't always see that the .v file changed, so reset the synthesis run first.
4. Generate bitstream (it synthesizes for you) and program the board.
5. Run the reader with the board's serial port, then press btnC:

       python3 fpga/read_regs.py /dev/ttyUSB1

   The board shows up as two ports (usually `/dev/ttyUSB0` and `/dev/ttyUSB1`), and the UART is normally the second one.
   If you get "permission denied", add yourself to the dialout group (sudo usermod -aG dialout $USER, then log out and in).
   On Windows use the COM port from Device Manager instead, e.g. COM4.

   It prints the registers and says MATCHES .res or DIFFERENT .res.

LD0 turns on when the program is done, and btnC is reset.

### Limits on the board

- Data memory on the board is only 64 bytes, so programs that use the stack (loop, recursive, t12, t13)
  or a .data section (string, t14) don't run on the board yet. They all pass in simulation.
- The CPU runs at 12.5 MHz (100 MHz / 8).

## Tests

All tests are in `src/test/scala/TopTester.scala`. Run one with sbt "testOnly <Name>".

| Test | What it checks | Author |
|---|---|---|
| CourseTests | Runs all 29 course programs in `tests/` and compares with the .res files | Mikkel |
| Program | Runs the .bin in `program/`, prints the registers, writes .dump, compares with .res | Mikkel |
| DecodeTest | Decode: one instruction of each type | Mikkel |
| ExecuteTest | Execute: every ALU operation, branches, jumps and edge cases | Christian |
| MemTest | MemReadLoad: byte and halfword stores, word load | Noah |
| TopTest | Top: PC, instruction fetch, decode fields and final registers for `test_add.bin` | Noah |
| SimpleTest | Read: prints every instruction in `test_add.bin` | Noah |
| dataDump | Top: prints all registers after `test_add.bin` | Noah |
