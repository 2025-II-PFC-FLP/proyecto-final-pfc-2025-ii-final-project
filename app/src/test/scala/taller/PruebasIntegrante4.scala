package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import ProyectoRiego._

@RunWith(classOf[JUnitRunner])
class PruebasIntegrante4 extends AnyFunSuite {

  // Test 1: Coincidencia de costos entre versión secuencial y paralela
  test("1. ProgramacionRiegoOptimoPar debe coincidir en costo con la secuencial para n=4") {
    val f = fincaAlAzar(4)
    val d = distanciaAlAzar(4)

    val (piSeq, cSeq) = ProgramacionRiegoOptimo(f, d)
    val (piPar, cPar) = ProgramacionRiegoOptimoPar(f, d)

    assert(cSeq == cPar, s"Costos distintos: seq=$cSeq par=$cPar")
  }

  // Test 2: Determinismo en finca pequeña fija
  test("2. ProgramacionRiegoOptimoPar coincide para fincaEj pequeña") {
    val f = Vector((10, 2, 1), (8, 3, 2), (12, 1, 3))
    val d = Vector(
      Vector(0, 5, 10),
      Vector(5, 0, 3),
      Vector(10, 3, 0)
    )

    val (_, cSeq) = ProgramacionRiegoOptimo(f, d)
    val (_, cPar) = ProgramacionRiegoOptimoPar(f, d)

    assert(cSeq == cPar)
  }

  // Test 3: Verificar que la versión paralela evalúe correctamente varias fincas
  test("3. Coincidencia en varias instancias aleatorias") {
    val tamaños = List(4, 5, 6)
    tamaños.foreach { n =>
      val f = fincaAlAzar(n)
      val d = distanciaAlAzar(n)

      val (_, cSeq) = ProgramacionRiegoOptimo(f, d)
      val (_, cPar) = ProgramacionRiegoOptimoPar(f, d)

      assert(cSeq == cPar, s"Falla para tamaño $n")
    }
  }

  // Test 4: Generación paralela de permutaciones produce mismo conteo
  test("4. generarProgramacionesRiegoPar genera mismo número que la secuencial") {
    val f = fincaAlAzar(5)
    val seq = generarProgramacionesRiego(f)
    val par = generarProgramacionesRiegoPar(f)

    assert(seq.length == par.length)
    assert(seq.toSet == par.toSet)
  }

  // Test 5: Optimización paralela evita rutas muy costosas igual que la secuencial
  test("5. Ambas versiones evitan rutas costosas en movilidad") {
    val f = Vector.fill(3)((100, 1, 1))

    val d = Vector(
      Vector(0,   10, 1000),
      Vector(10,   0,   10),
      Vector(1000, 10,   0)
    )

    val (_, cSeq) = ProgramacionRiegoOptimo(f, d)
    val (_, cPar) = ProgramacionRiegoOptimoPar(f, d)

    assert(cSeq == cPar)
  }
}
