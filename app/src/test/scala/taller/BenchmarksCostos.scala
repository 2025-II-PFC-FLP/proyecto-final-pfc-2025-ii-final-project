package taller

import org.scalameter.api._
import ProyectoRiego._

object BenchmarksCostos extends Bench.LocalTime {

  // Tamaños de finca a probar
  val tamanosFinca: Gen[Int] = Gen.range("n")(10, 50, 10)

  // Generador de datos (finca, distancias, programación)
  val datos: Gen[(Finca, Distancia, ProgRiego)] =
    for (n <- tamanosFinca) yield {
      val f  = fincaAlAzar(n)
      val d  = distanciaAlAzar(n)
      val pi = Vector.range(0, n)
      (f, d, pi)
    }

  performance of "costoRiegoFinca" in {

    measure method "secuencial" in {
      using(datos) in { case (f, _, pi) =>
        costoRiegoFinca(f, pi)
      }
    }

    measure method "paralelo" in {
      using(datos) in { case (f, _, pi) =>
        costoRiegoFincaPar(f, pi)
      }
    }
  }

  performance of "costoMovilidad" in {

    measure method "secuencial" in {
      using(datos) in { case (f, d, pi) =>
        costoMovilidad(f, pi, d)
      }
    }

    measure method "paralelo" in {
      using(datos) in { case (f, d, pi) =>
        costoMovilidadPar(f, pi, d)
      }
    }
  }
}
