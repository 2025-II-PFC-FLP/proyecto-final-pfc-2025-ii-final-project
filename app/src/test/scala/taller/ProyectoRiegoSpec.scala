package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProyectoRiegoSpec extends AnyFunSuite {

  import ProyectoRiego._

  // Finca pequeña fija para pruebas deterministas
  // (ts, tr, p)
  val fincaEj: Finca = Vector(
    (10, 2, 1),  // tablón 0
    (8,  3, 2),  // tablón 1
    (12, 1, 3)   // tablón 2
  )

  // Matriz de distancias pequeña
  val distEj: Distancia = Vector(
    Vector(0,  5, 10),
    Vector(5,  0,  3),
    Vector(10, 3,  0)
  )

  // Programación de ejemplo: primero tablon 1, luego 0, luego 2
  val progEj: ProgRiego = Vector(1, 0, 2)

  // ---------------------------
  // Pruebas para fincaAlAzar
  // ---------------------------

  test("fincaAlAzar genera la cantidad correcta de tablones") {
    val f = fincaAlAzar(5)
    assert(f.length == 5)
  }

  test("fincaAlAzar genera valores positivos en los campos") {
    val f = fincaAlAzar(4)
    assert(f.forall { case (ts, tr, p) => ts > 0 && tr > 0 && p > 0 })
  }

  // -------------------------------
  // Pruebas para distanciaAlAzar
  // -------------------------------

  test("distanciaAlAzar genera matriz cuadrada con diagonal en cero") {
    val d = distanciaAlAzar(4)
    assert(d.length == 4)
    assert(d.forall(_.length == 4))
    assert((0 until 4).forall(i => d(i)(i) == 0))
  }

  test("distanciaAlAzar es simétrica") {
    val d = distanciaAlAzar(5)
    assert((0 until 5).forall(i =>
      (0 until 5).forall(j => d(i)(j) == d(j)(i))
    ))
  }

  // ---------------------------
  // Pruebas para tIR
  // ---------------------------

  test("tIR con programación en orden 0,1,2") {
    val pi = Vector(0, 1, 2)
    val t = tIR(fincaEj, pi)
    // t(0) empieza en 0
    // t(1) empieza cuando termina el 0: 0 + tr0 = 2
    // t(2) empieza cuando termina el 1: 2 + tr1 = 5
    assert(t === Vector(0, 2, 5))
  }

  test("tIR con programación 1,0,2") {
    val t = tIR(fincaEj, progEj)
    // Turno 0: tablón 1, empieza en 0, dura 3
    // Turno 1: tablón 0, empieza en 3, dura 2
    // Turno 2: tablón 2, empieza en 5
    assert(t === Vector(3, 0, 5))
  }

  // ---------------------------------
  // Pruebas para costoRiegoTablon
  // ---------------------------------

  test("costoRiegoTablon caso en que llega a tiempo (holgura positiva)") {
    val pi = Vector(0, 1, 2)
    val c0 = costoRiegoTablon(0, fincaEj, pi)
    // ts0 = 10, tr0 = 2, t0 = 0
    // límite = 10 - 2 = 8 >= 0 → costo = 10 - (0+2) = 8
    assert(c0 == 8)
  }

  test("costoRiegoTablon suma correcta en caso de atraso o llegada ajustada") {
    val pi = Vector(0, 2, 1) // 1 se riega de último
    val c1 = costoRiegoTablon(1, fincaEj, pi)
    // ts1 = 8, tr1 = 3, p1 = 2
    // t1 = 3, límite = 5 >= 3 → costo = 8 - (3+3) = 2
    assert(c1 == 2)
  }

  // -----------------------------
  // Pruebas para costoRiegoFinca
  // -----------------------------

  test("costoRiegoFinca suma los costos de todos los tablones") {
    val pi = Vector(0, 1, 2)
    val c0 = costoRiegoTablon(0, fincaEj, pi)
    val c1 = costoRiegoTablon(1, fincaEj, pi)
    val c2 = costoRiegoTablon(2, fincaEj, pi)
    val total = costoRiegoFinca(fincaEj, pi)
    assert(total == c0 + c1 + c2)
  }

  // -----------------------------
  // Pruebas para costoMovilidad
  // -----------------------------

  test("costoMovilidad con programación 0,1,2") {
    val pi = Vector(0, 1, 2)
    val cm = costoMovilidad(fincaEj, pi, distEj)
    // Distancias:
    // 0 -> 1 = 5
    // 1 -> 2 = 3
    assert(cm == 8)
  }

  test("costoMovilidad con un solo tablón es cero") {
    val finca1: Finca = Vector((5, 1, 1))
    val dist1: Distancia = Vector(Vector(0))
    val pi = Vector(0)
    val cm = costoMovilidad(finca1, pi, dist1)
    assert(cm == 0)
  }

}