set_property IOSTANDARD LVCMOS33 [get_ports *]


## Clock signal
set_property PACKAGE_PIN W5 [get_ports clock]
create_clock -add -name sys_clk_pin -period 10.00 -waveform {0 5} [get_ports clock]

## CPU clock = 100 MHz / 8 = 12.5 MHz, made by clockDiv(2) in Basys3Top
create_generated_clock -name cpu_clk -source [get_ports clock] -divide_by 8 [get_pins {clockDiv_reg[2]/Q}]

## Switches
#set_property PACKAGE_PIN V17 [get_ports {io_sw[0]}]
#set_property PACKAGE_PIN V16 [get_ports {io_sw[1]}]
#set_property PACKAGE_PIN W16 [get_ports {io_sw[2]}]
#set_property PACKAGE_PIN W17 [get_ports {io_sw[3]}]
#set_property PACKAGE_PIN W15 [get_ports {io_sw[4]}]
#set_property PACKAGE_PIN V15 [get_ports {io_sw[5]}]
#set_property PACKAGE_PIN W14 [get_ports {io_sw[6]}]
#set_property PACKAGE_PIN W13 [get_ports {io_sw[7]}]
#set_property PACKAGE_PIN V2  [get_ports {io_sw[8]}]
#set_property PACKAGE_PIN T3  [get_ports {io_sw[9]}]
#set_property PACKAGE_PIN T2  [get_ports {io_sw[10]}]
#set_property PACKAGE_PIN R3  [get_ports {io_sw[11]}]
#set_property PACKAGE_PIN W2  [get_ports {io_sw[12]}]
#set_property PACKAGE_PIN U1  [get_ports {io_sw[13]}]
#set_property PACKAGE_PIN T1  [get_ports {io_sw[14]}]
#set_property PACKAGE_PIN R2  [get_ports {io_sw[15]}]

## LEDs
# LED 0 = program done
set_property PACKAGE_PIN U16 [get_ports io_led]
#set_property PACKAGE_PIN E19 [get_ports {led[1]}]
#set_property PACKAGE_PIN U19 [get_ports {led[2]}]
#set_property PACKAGE_PIN V19 [get_ports {led[3]}]
#set_property PACKAGE_PIN W18 [get_ports {led[4]}]
#set_property PACKAGE_PIN U15 [get_ports {led[5]}]
#set_property PACKAGE_PIN U14 [get_ports {led[6]}]
#set_property PACKAGE_PIN V14 [get_ports {led[7]}]
#set_property PACKAGE_PIN V13 [get_ports {led[8]}]
#set_property PACKAGE_PIN V3  [get_ports {led[9]}]
#set_property PACKAGE_PIN W3  [get_ports {led[10]}]
#set_property PACKAGE_PIN U3  [get_ports {led[11]}]
#set_property PACKAGE_PIN P3  [get_ports {led[12]}]
#set_property PACKAGE_PIN N3  [get_ports {led[13]}]
#set_property PACKAGE_PIN P1  [get_ports {led[14]}]
#set_property PACKAGE_PIN L1  [get_ports {led[15]}]

##Buttons
# btnC as reset
set_property PACKAGE_PIN U18 [get_ports reset]
#set_property PACKAGE_PIN T18 [get_ports btnU]
#set_property PACKAGE_PIN W19 [get_ports btnL]
#set_property PACKAGE_PIN T17 [get_ports btnR]
#set_property PACKAGE_PIN U17 [get_ports btnD]


## Pmod in lower row
# BTN0
#set_property PACKAGE_PIN A15 [get_ports {io_btn[0]}]
# BTN1
#set_property PACKAGE_PIN A17 [get_ports {io_btn[1]}]
# BTN2
#set_property PACKAGE_PIN C15 [get_ports {io_btn[2]}]
# BTN3 -  can be used as manual clock
#set_property PACKAGE_PIN C16 [get_ports {io_btn[3]}]

##7 segment display
#set_property PACKAGE_PIN W7 [get_ports {io_seg[0]}]
#set_property PACKAGE_PIN W6 [get_ports {io_seg[1]}]
#set_property PACKAGE_PIN U8 [get_ports {io_seg[2]}]
#set_property PACKAGE_PIN V8 [get_ports {io_seg[3]}]
#set_property PACKAGE_PIN U5 [get_ports {io_seg[4]}]
#set_property PACKAGE_PIN V5 [get_ports {io_seg[5]}]
#set_property PACKAGE_PIN U7 [get_ports {io_seg[6]}]
#set_property PACKAGE_PIN V7 [get_ports io_dp]
#set_property PACKAGE_PIN U2 [get_ports {io_an[0]}]
#set_property PACKAGE_PIN U4 [get_ports {io_an[1]}]
#set_property PACKAGE_PIN V4 [get_ports {io_an[2]}]
#set_property PACKAGE_PIN W4 [get_ports {io_an[3]}]

## UART
# TX = FPGA -> PC, RX = PC -> FPGA
set_property PACKAGE_PIN A18 [get_ports {io_txd}]
#set_property PACKAGE_PIN B18 [get_ports {io_rxd}]

## Board configuration
set_property CFGBVS VCCO [current_design]
set_property CONFIG_VOLTAGE 3.3 [current_design]
