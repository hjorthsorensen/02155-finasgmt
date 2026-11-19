import sys
import serial

port = sys.argv[1]
res  = sys.argv[2] if len(sys.argv) > 2 else "program/program.res"

with serial.Serial(port, 115200, timeout=10) as s:
    print(f"Press btnC on the board")
    data = s.read(128)

if len(data) != 128:
    sys.exit(f"Got {len(data)} of 128 bytes - ERROR?")

for i in range(32):
    v = int.from_bytes(data[i * 4:i * 4 + 4], "little")
    print(f"x{i:<2} = 0x{v:08x} ({v})")

print("MATCHES .res" if data == open(res, "rb").read() else "DIFFERENT .res")
