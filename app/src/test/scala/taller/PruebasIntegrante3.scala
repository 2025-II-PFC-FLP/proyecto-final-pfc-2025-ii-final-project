package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PruebasIntegrante3 extends AnyFunSuite {

  import ProyectoRiego._

  // Finca y distancias de ejemplo, reutilizables
  val fincaEj: Finca = Vector(
    (10, 2, 1),
    (8,  3, 2),
    (12, 1, 3)
  )

  val distEj: Distancia = Vector(
    Vector(0,  5, 10),
    Vector(5,  0,  3),
    Vector(10, 3,  0)
  )

  val piEj: ProgRiego = Vector(0, 1, 2)

  // 1. costoRiegoFincaPar == costoRiegoFinca para finca pequeña
  test("1. costoRiegoFincaPar coincide con la versión secuencial en fincaEj") {
    val sec = costoRiegoFinca(fincaEj, piEj)
    val par = costoRiegoFincaPar(fincaEj, piEj)
    assert(sec == par)
  }

  // 2. costoMovilidadPar == costoMovilidad para distEj
  test("2. costoMovilidadPar coincide con la versión secuencial en distEj") {
    val sec = costoMovilidad(fincaEj, piEj, distEj)
    val par = costoMovilidadPar(fincaEj, piEj, distEj)
    assert(sec == par)
  }

  // 3. Igualdad para varias programaciones en la misma finca
  test("3. costoRiegoFincaPar es igual al secuencial para varias permutaciones") {
    val perms = Vector(
      Vector(0, 1, 2),
      Vector(0, 2, 1),
      Vector(1, 0, 2),
      Vector(1, 2, 0),
      Vector(2, 0, 1),
      Vector(2, 1, 0)
    )

    perms.foreach { pi =>
      val sec = costoRiegoFinca(fincaEj, pi)
      val par = costoRiegoFincaPar(fincaEj, pi)
      assert(sec == par, s"Falla en permutación $pi")
    }
  }

  // 4. Igualdad para movilidad en varias permutaciones
  test("4. costoMovilidadPar es igual al secuencial para varias permutaciones") {
    val perms = Vector(
      Vector(0, 1, 2),
      Vector(0, 2, 1),
      Vector(1, 0, 2),
      Vector(2, 1, 0)
    )

    perms.foreach { pi =>
      val sec = costoMovilidad(fincaEj, pi, distEj)
      val par = costoMovilidadPar(fincaEj, pi, distEj)
      assert(sec == par, s"Falla en permutación $pi")
    }
  }

  // 5. Prueba con finca y distancias generadas al azar (propiedad de igualdad)
  test("5. Versiones paralelas y secuenciales coinciden en instancias aleatorias") {
    val n = 8
    val fincaRand = fincaAlAzar(n)
    val distRand  = distanciaAlAzar(n)
    val piRand: ProgRiego = Vector.range(0, n) // orden natural

    val crSec = costoRiegoFinca(fincaRand, piRand)
    val crPar = costoRiegoFincaPar(fincaRand, piRand)

    val cmSec = costoMovilidad(fincaRand, piRand, distRand)
    val cmPar = costoMovilidadPar(fincaRand, piRand, distRand)

    assert(crSec == crPar, "costoRiegoFinca vs costoRiegoFincaPar no coinciden")
    assert(cmSec == cmPar, "costoMovilidad vs costoMovilidadPar no coinciden")
  }

}
