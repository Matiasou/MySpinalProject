package TipToe_writer

import spinal.core._ 
import spinal.lib._ 
import spinal.lib.bus.amba4.axilite._
import spinal.lib.fsm._

case class WriterComponent(
// in here go the parameters for the component
val STORAGE_SIZE: Int = 1024 * 1024, // 1MB
val LOG_BASE_ADDR: BigInt = 0x80000000, 
val ID_WIDTH: Int = 16,
val ADDR_WIDTH: Int = 32,
val DATA_WIDTH: Int = 128,
val FULL_PAYLOAD_WIDTH: Int = 0, // ???
val QUEUE_SIZE: Int = 8 // Default value
) extends Component{
    val STORAGE_DATA_WIDTH = 256 
    val ENTRY_PER_PAGE = STORAGE_DATA_WIDTH / FULL_PAYLOAD_WIDTH

    val io = new Bundle {
        val write = slave Stream(Bits(FULL_PAYLOAD_WIDTH bits)) 
        // AW channel signals
        val axi_m2_awvilid = out Bool()
        val axi_m2_awready = in Bool()
        val axi_m2_awaddr = out Bits(ADDR_WIDTH bits) //  output wire[ADDR_WIDTH-1:0] axi_m2_awaddr 
        val xi_m2_awburst = out Bits(2 bits) 
        val axi_m2_awcache = out Bits(4 bits)
        val axi_m2_awlen = out Bits(8 bits) 
        val axi_m2_awlock = out Bits(2 bits)
        val axi_m2_awprot = out Bits(3 bits)
        val axi_m2_awqos = out Bits(4 bits)
        val axi_m2_awregion = out Bits(4 bits)
        val axi_m2_awsize = out Bits(3 bits) 
        val axi_m2_awid = out Bits(ID_WIDTH bits) 
    }
    val queue = io.write.queue(size = QUEUE_SIZE)


    val fsm = new StateMachine {
        val idle = new State with EntryPoint
        val write = new State
        io.axi_m2_awvilid := False
        
        idle
            .whenIsActive(
                // only when queue is not empty meaning we need to write 
                when(queue.valid) {
                    io.axi_m2_awvilid := True
                    when(io.axi_m2_awready && io.axi_m2_awvilid) {
                        goto(write)
                    }
                }
            )


        write 
            . whenIsActive( 
                // aw handshake then increment idx 
                when(!(io.axi_m2_awvilid && !io.axi_m2_awready))
                idx 
            )
    }


}


object WriterComponentVivado extends App {
    Config.spinal.generateVerilog(WriterComponent())
}