package eek

import chisel3._
import chisel3.testers._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import ImmType._

class ImmGenTester(immgen: => ImmGen, count: => Int, sel: => UInt) extends BasicTester {
    val dut = Module(immgen)
    val xlen = dut.xlen

    val rnd = new scala.util.Random

    assert(count > 0, "Count must be at least 1.")

    val (cntr, done) = Counter(true.B, count)

    // Create a bunch of random values starting with 0
    val randomInstructions = Seq(0) ++ Seq.fill(count - 1)(rnd.nextInt())

    val i = VecInit(randomInstructions.map(x => (x.asSInt).asUInt))

    def imm_i(x: UInt) = { Cat(Fill(21, x(31)), x(30, 20)) }
    def imm_u(x: UInt) = { Cat(x(31, 12), Fill(12, 0.B)) }
    def imm_s(x: UInt) = { Cat(Fill(21, x(31)), x(30, 25), x(11, 7)) }
    def imm_b(x: UInt) = { Cat(Fill(20, x(31)), x(7), x(30, 25), x(11, 8), 0.B) }
    def imm_j(x: UInt) = { Cat(Fill(12, x(31)), x(19, 12), x(20), x(30, 21), 0.B) }

    val inst = i(cntr)

    val out = MuxLookup(
        sel,
        imm_i(i(cntr)),
        Seq(
            IImm -> imm_i(inst),
            UImm -> imm_u(inst),
            SImm -> imm_s(inst),
            BImm -> imm_b(inst),
            JImm -> imm_j(inst),
        )
    )

    dut.io.sel := sel
    dut.io.inst := inst

    when(done) { stop() }
    assert(dut.io.imm === out)
    printf("Counter: %d, i: 0x%x, sel: %x, Out: %x ?= %x\n", cntr, inst, sel, dut.io.imm, out)
}

class ImmGenTests extends AnyFlatSpec with ChiselScalatestTester {
    val xlen = 32
    val count = 50
    "I-type immediates" should "pass" in {
        test(new ImmGenTester(new ImmGenSimple(xlen), count, IImm)).runUntilStop()
    }
    "U-type immediates" should "pass" in {
        test(new ImmGenTester(new ImmGenSimple(xlen), count, UImm)).runUntilStop()
    }
    "S-type immediates" should "pass" in {
        test(new ImmGenTester(new ImmGenSimple(xlen), count, SImm)).runUntilStop()
    }
    "B-type immediates" should "pass" in {
        test(new ImmGenTester(new ImmGenSimple(xlen), count, BImm)).runUntilStop()
    }
    "J-type immediates" should "pass" in {
        test(new ImmGenTester(new ImmGenSimple(xlen), count, JImm)).runUntilStop()
    }
}
