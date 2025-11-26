package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import taller.ProyectoRiego._

@RunWith(classOf[JUnitRunner])
class PruebasIntegrante2 extends AnyFunSuite {

  // Test 1: Permutaciones para n=3 (debe ser 3! = 6)
  test("1. GenerarProgramacionesRiego debe generar 6 permutaciones únicas para n=3") {
    val finca3 = Vector.fill(3)((10, 2, 1))
    val resultado = generarProgramacionesRiego(finca3)

    assert(resultado.length == 6, "Tamaño debe ser 6")
    assert(resultado.distinct.length == 6, "Sin duplicados")
    assert(resultado.contains(Vector(0, 1, 2)), "Debe incluir base")
    assert(resultado.contains(Vector(2, 1, 0)), "Debe incluir inversa")
  }

  // Test 2: Permutaciones para n=4 (debe ser 4! = 24)
  test("2. GenerarProgramacionesRiego debe generar 24 permutaciones para n=4") {
    val finca4 = Vector.fill(4)((10, 2, 1))
    val resultado = generarProgramacionesRiego(finca4)

    assert(resultado.length == 24, "Tamaño debe ser 24")
  }

  // Test 3: Prioridad por supervivencia (Tablón 0 urgente)
  test("3. Optimización debe priorizar tablón con urgencia de supervivencia (n=2)") {
    // T0 urgente (ts=1), T1 relajado (ts=100)
    val fincaUrgente = Vector((1, 1, 4), (100, 1, 1))
    val distanciaNula = Vector(Vector(0, 0), Vector(0, 0))

    val (progOptima, _) = ProgramacionRiegoOptimo(fincaUrgente, distanciaNula)

    // Debe regar T0 primero
    assert(progOptima == Vector(0, 1), "Orden incorrecto, T0 debe ir primero")
  }

  // Test 4: Evitar rutas costosas (0 <-> 2 cuesta 1000)
  test("4. Optimización debe evitar caminos con costo de movilidad extremo (n=3)") {
    val fincaDistancia = Vector.fill(3)((100, 1, 1))

    // Distancias: 0-1 (10), 1-2 (10), 0-2 (1000)
    val distancias = Vector(
      Vector(0, 10, 1000),
      Vector(10, 0, 10),
      Vector(1000, 10, 0)
    )

    val (progOptima, _) = ProgramacionRiegoOptimo(fincaDistancia, distancias)

    // Verificar que no haya salto directo entre 0 y 2
    val saltoProhibido = progOptima.indexOf(0) - progOptima.indexOf(2)
    assert(Math.abs(saltoProhibido) != 1, "Evitar salto directo costoso 0-2")

    // Costo esperado: 0->1->2 (20) vs Directo (1000+)
    val costoMovilidadReal = costoMovilidad(fincaDistancia, progOptima, distancias)
    assert(costoMovilidadReal == 20, s"Costo movilidad $costoMovilidadReal incorrecto, esperado 20")
  }

  // Test 5: Consistencia (Costo reportado == Riego + Movilidad)
  test("5. El costo devuelto debe ser consistente con la suma de riego y movilidad") {
    val finca = Vector((10, 2, 4), (5, 2, 2), (8, 2, 3))
    val dist = Vector(
      Vector(0, 3, 5),
      Vector(3, 0, 2),
      Vector(5, 2, 0)
    )

    val (progOptima, costoReportado) = ProgramacionRiegoOptimo(finca, dist)

    // Recálculo manual
    val costoRiego = costoRiegoFinca(finca, progOptima)
    val costoMov = costoMovilidad(finca, progOptima, dist)

    assert(costoReportado == (costoRiego + costoMov), "Suma de costos inconsistente")
  }
}