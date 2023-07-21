import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import ImmType._

class BasicImmGenTest extends AnyFlatSpec with ChiselScalatestTester {
    val m = 32 // Number of registers
    val xlen = 32 // Width of register in bits
    it should "(I-Type) build UInt of value 0 from 12-bit 0 value" in {
        test(new ImmGenSimple(xlen)) { r => 
            r.io.sel.poke(IImm)
            r.io.inst.poke(0 << 20)
            r.io.imm.expect(0)
        }
    }
    it should "(I-Type) build UInt from 12-bit a positive value" in {
        test(new ImmGenSimple(xlen)) { r =>
            r.io.sel.poke(IImm)
            r.io.inst.poke(12 << 20)
            r.io.imm.expect(12)
        }
    }
    it should "(I-Type) build UInt from the biggest positive value" in {
        test(new ImmGenSimple(xlen)) { r =>
            r.io.sel.poke(IImm)
            r.io.inst.poke(0x7ff << 20)
            r.io.imm.expect(0x7ff)
        }
    }
    it should "(I-Type) build UInt from a negative 12-bit value" in {
        test(new ImmGenSimple(xlen)) { r => 
            r.io.sel.poke(IImm)

            // value -2048
            r.io.inst.poke("h8000_0000".U)
            r.io.imm.expect("hffff_f800".U)
        }
    }
    it should "(I-Type) built UInt of value -1/-2 from 12-bit -1/-2 value" in {
        test(new ImmGenSimple(xlen)) { r =>
            r.io.sel.poke(IImm)
            // value -1
            r.io.inst.poke("hfff0_0000".U)
            r.io.imm.expect("hffff_ffff".U)

            // value -2
            r.io.inst.poke("hffe0_0000".U)
            r.io.imm.expect("hffff_fffe".U)
        }
    }
    it should "(S-type) build a positive UInt value from 12 bits" in {
        test(new ImmGenSimple(xlen)) { r =>
            r.io.sel.poke(SImm)

            // value 1
            r.io.inst.poke(1 << 7)
            r.io.imm.expect(1)
        }
    }
    it should "(S-type) sign-extend a 12-bit negative value into a UInt" in {
        test(new ImmGenSimple(xlen)) { r =>
            r.io.sel.poke(SImm)

            // value -1
            r.io.inst.poke("hfe00_0f80".U)
            r.io.imm.expect("hffff_ffff".U)
        }
    }
    it should "{S-type) create 0 from 0" in {
        test(new ImmGenSimple(xlen)) { r =>
            r.io.sel.poke(SImm)

            // value 0
            r.io.inst.poke(0.U)
            r.io.imm.expect(0.U)
        }
    }
}
