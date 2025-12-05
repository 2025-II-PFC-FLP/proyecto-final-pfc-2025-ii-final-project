package taller

import org.scalameter._
import ProyectoRiego._

object BenchmarksOptimo extends Bench.LocalTime {

  // Tamaños manejables por factorial (8, 9 y 10)
  val tamanosFinca: Gen[Int] = Gen.range("n")(8, 10, 1)

  // Generación de datos usando for (como exige el taller)
  val datos: Gen[(Finca, Distancia)] =
    for (n <- tamanosFinca) yield {
      val f = fincaAlAzar(n)
      val d = distanciaAlAzar(n)
      (f, d)
    }

  performance of "ProgramacionRiegoOptimo (Óptimo global secuencial)" in {
    measure method "secuencial" in {
      using(datos) in { case (f, d) =>
        ProgramacionRiegoOptimo(f, d)
      }
    }
  }

  performance of "ProgramacionRiegoOptimoPar (Óptimo global paralelo)" in {
    measure method "paralelo" in {
      using(datos) in { case (f, d) =>
        ProgramacionRiegoOptimoPar(f, d)
      }
    }
  }
}
