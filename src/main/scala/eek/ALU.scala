package eek

import chisel3._
import chisel3.stage._
import chisel3.util._
import chisel3.experimental._

object Operation {
    val ADD    = 0.U(4.W)
    val SUB    = 1.U(4.W)
    val SLL    = 2.U(4.W)
    val SLT    = 3.U(4.W)
    val SLTU   = 4.U(4.W)
    val XOR    = 5.U(4.W)
    val SRL    = 6.U(4.W)
    val SRA    = 7.U(4.W)
    val OR     = 8.U(4.W)
    val AND    = 9.U(4.W)
}

import Operation._

class AluIO(xlen: Int) extends Bundle {
    val oper = Input(UInt(xlen.W))
    val rs1Data = Input(UInt(xlen.W))
    val rs2Data = Input(UInt(xlen.W))
    val rdData  = Output(UInt(xlen.W))
}

trait AluGen extends Module {
    def xlen: Int
    val io: AluIO
}

class AluSimple(val xlen: Int) extends AluGen {
    val io = IO(new AluIO(xlen))

    val add: UInt => UInt = a => io.rs1Data + a
    val sll: UInt => UInt = a => {
        // Make sure we use as little bits as needed. This is necessary
        // as {dshl} widens the target variable by as many bits as needed
        // to perform the left shift. - FIRRTL Spec., Version 0.2.0, 7.13
        val shft = Wire(UInt(xlen.W))
        shft := io.rs1Data << a(4, 0)
        shft
    }
    val slt: UInt => UInt = a => Mux(io.rs1Data.asSInt < a.asSInt, 1.U, 0.U)
    val sltu: UInt => UInt = a => Mux(io.rs1Data < a, 1.U, 0.U)
    val xor: UInt => UInt = a => io.rs1Data ^ a
    val srl: UInt => UInt = a => io.rs1Data >> a(4, 0)
    val sra: UInt => UInt = a => (io.rs1Data.asSInt >> a(4, 0)).asUInt
    val or: UInt => UInt = a => io.rs1Data | a
    val and: UInt => UInt = a => io.rs1Data & a

    io.rdData := MuxLookup(
        io.oper,
        io.rs1Data,
        Seq(
            ADD -> add(io.rs2Data),
            SUB -> add(-io.rs2Data),
            SLL -> sll(io.rs2Data),
            SLT -> slt(io.rs2Data),
            SLTU -> sltu(io.rs2Data),
            XOR -> xor(io.rs2Data),
            SRL -> srl(io.rs2Data),
            SRA -> sra(io.rs2Data),
            OR -> or(io.rs2Data),
            AND -> and(io.rs2Data),
        )
    )
}

object AluDriver extends App {
    val xlen = 32
    (new ChiselStage).emitVerilog(new AluSimple(xlen), args)
}
