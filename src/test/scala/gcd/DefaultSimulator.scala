package gcd

import chisel3._
import chisel3.simulator.{PeekPokeAPI, SingleBackendSimulator}
import svsim.{CommonCompilationSettings, verilator}

object DefaultSimulator extends PeekPokeAPI {

  def simulate[T <: RawModule](
                                module: => T,
                                buildDir: String,
                                enableWaves: Boolean = false
                              )(body: (T) => Unit): Unit = {
    makeSimulator(buildDir, enableWaves)
      .simulate(module) { module =>
        module.controller.setTraceEnabled(enableWaves)
        body(module.wrapped)
      }
      .result
  }

  private class DefaultSimulator(
                                  val workspacePath: String,
                                  enableWaves: Boolean = false
                                ) extends SingleBackendSimulator[verilator.Backend] {
    val backend = verilator.Backend.initializeFromProcessEnvironment()
    val tag = "default"
    val commonCompilationSettings = CommonCompilationSettings()
    val backendSpecificCompilationSettings = {
      val settings = verilator.Backend.CompilationSettings()
      if (enableWaves) {
        settings.copy(
          traceStyle = Some(verilator.Backend.CompilationSettings.TraceStyle.Vcd(traceUnderscore = false))
        )
      } else {
        settings
      }
    }
  }

  private def makeSimulator(
                             buildDir: String,
                             enableWaves: Boolean
                           ): DefaultSimulator = {
    new DefaultSimulator(buildDir, enableWaves)
  }

}