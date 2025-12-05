package taller

import scala.util.Random
import scala.collection.parallel.CollectionConverters._

object ProyectoRiego {

  // ---------- Tipos básicos del problema ----------

  // Un tablon es (tiempoSupervivencia, tiempoRiego, prioridad)
  type Tablon = (Int, Int, Int)

  // Una finca es un vector de tablones
  type Finca = Vector[Tablon]

  // Matriz de distancias entre tablones
  type Distancia = Vector[Vector[Int]]

  // Programación de riego: en la posición j está el índice del tablón regado en el turno j
  type ProgRiego = Vector[Int]

  // Tiempo de inicio de riego: t(i) = instante en que empieza el riego del tablón i
  type TiempoInicioRiego = Vector[Int]

  // ---------- 1. Generación de entradas aleatorias ----------

  private val random = new Random()

  /** Crea una finca de long tablones con valores aleatorios.
   * ts en [1, long*2], tr en [1, long], prioridad en [1, 4].
   */
  def fincaAlAzar(long: Int): Finca = {
    require(long > 0, "La longitud de la finca debe ser positiva")
    Vector.fill(long)(
      (
        random.nextInt(long * 2) + 1, // ts
        random.nextInt(long) + 1,     // tr
        random.nextInt(4) + 1         // prioridad
      )
    )
  }

  /** Crea una matriz de distancias simétrica para una finca de long tablones.
   * Distancias en [1, long*3], diagonal en 0.
   */
  def distanciaAlAzar(long: Int): Distancia = {
    require(long > 0, "La longitud de la finca debe ser positiva")
    val v = Vector.fill(long, long)(random.nextInt(long * 3) + 1)
    Vector.tabulate(long, long) { (i, j) =>
      if (i < j) v(i)(j)
      else if (i == j) 0
      else v(j)(i)
    }
  }

  // ---------- 2. Exploración de datos de la finca ----------

  // tsup: tiempo de supervivencia del tablón i
  def tsup(f: Finca, i: Int): Int =
    f(i)._1

  // treg: tiempo de riego del tablón i
  def treg(f: Finca, i: Int): Int =
    f(i)._2

  // prio: prioridad del tablón i
  def prio(f: Finca, i: Int): Int =
    f(i)._3

  // ---------- 3. Cálculo del tiempo de inicio de riego ----------

  /** tIR(f, pi) devuelve un vector t tal que t(i) es el instante
   * en que inicia el riego del tablón i, siguiendo la programación pi.
   *
   * Representación:
   *   - pi(j) = índice del tablón que se riega en el turno j (0 = primer turno).
   */
  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    val n = f.length
    require(pi.length == n, "La programación debe tener un turno por tablón")

    // Recorrido recursivo sobre los turnos 0..n-1, acumulando el tiempo "actual"
    def loop(turno: Int, tiempoActual: Int, t: TiempoInicioRiego): TiempoInicioRiego =
      if (turno >= n) t
      else {
        val tablon = pi(turno)
        val tActualizado = t.updated(tablon, tiempoActual)
        val duracionRiego = treg(f, tablon)
        loop(turno + 1, tiempoActual + duracionRiego, tActualizado)
      }

    loop(0, 0, Vector.fill(n)(0))
  }

  // ---------- 4. Cálculo de costos (versión secuencial) ----------

  /** Costo de riego de un tablón i de la finca f bajo la programación pi.
   *
   * Si ts_i - tr_i >= t_i:
   *     CR[i] = ts_i - (t_i + tr_i)
   * En otro caso:
   *     CR[i] = p_i * ((t_i + tr_i) - ts_i)
   */
  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    val (ts, tr, p) = f(i)
    val tiempos = tIR(f, pi)
    val ti = tiempos(i)
    val limite = ts - tr

    if (limite >= ti) {
      // Llega a tiempo o antes: holgura positiva
      ts - (ti + tr)
    } else {
      // Se pasa del tiempo de supervivencia: penaliza por prioridad
      p * ((ti + tr) - ts)
    }
  }

  /** Costo total de riego de la finca f con programación pi. (secuencial) */
  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
    val n = f.length
    (0 until n).foldLeft(0) { (acum, i) =>
      acum + costoRiegoTablon(i, f, pi)
    }
  }

  /** Costo de movilidad del sistema de riego según pi y la matriz de distancias d. (secuencial)
   *
   * CM = Σ_{j=0}^{n-2} d[pi(j), pi(j+1)]
   */
  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    require(d.length == f.length && d.forall(_.length == f.length),
      "La matriz de distancias debe ser del mismo tamaño que la finca")

    if (pi.length <= 1) 0
    else {
      (0 until (pi.length - 1)).foldLeft(0) { (acum, j) =>
        val from = pi(j)
        val to   = pi(j + 1)
        acum + d(from)(to)
      }
    }
  }


  /** Costo total de riego de la finca f con programación pi, usando colecciones paralelas. */
  def costoRiegoFincaPar(f: Finca, pi: ProgRiego): Int = {
    val n = f.length
    // Paralelizamos la suma de costos por tablón
    (0 until n).par.map(i => costoRiegoTablon(i, f, pi)).sum
  }

  /** Costo de movilidad en versión paralela. */
  def costoMovilidadPar(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    require(d.length == f.length && d.forall(_.length == f.length),
      "La matriz de distancias debe ser del mismo tamaño que la finca")

    if (pi.length <= 1) 0
    else {
      (0 until (pi.length - 1)).par.map { j =>
        val from = pi(j)
        val to   = pi(j + 1)
        d(from)(to)
      }.sum
    }
  }

  /**
   * Genera todas las posibles permutaciones de riego para una finca f.
   */
  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
    val n = f.length
    val indices = Vector.range(0, n)

    // Función auxiliar
    def permutar(lista: Vector[Int]): Vector[Vector[Int]] = {
      if (lista.isEmpty) Vector(Vector())
      else {
        // Para cada elemento, lo fijamos y permutamos el resto
        lista.flatMap { elem =>
          permutar(lista.filter(_ != elem)).map(p => elem +: p)
        }
      }
    }
    permutar(indices)
  }

  /**
   * Devuelve la programación que minimiza (Costo Riego + Costo Movilidad).
   */
  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
    // obtenemos todas las programaciones posibles
    val programaciones = generarProgramacionesRiego(f)

    def calcularCostoTotal(pi: ProgRiego): Int = {
      val cRiego = costoRiegoFinca(f, pi)
      val cMovilidad = costoMovilidad(f, pi, d)
      cRiego + cMovilidad
    }

    // buscamos el mínimo para comparar los costos.
    val mejorProgramacion = programaciones.minBy(pi => calcularCostoTotal(pi))
    val mejorCosto = calcularCostoTotal(mejorProgramacion)

    (mejorProgramacion, mejorCosto)
  }

  /** Genera todas las posibles programaciones de riego usando paralelismo */
  def generarProgramacionesRiegoPar(f: Finca): Vector[ProgRiego] = {
    val n = f.length
    val indices = Vector.range(0, n)

    // Función recursiva para generar permutaciones (igual a la secuencial)
    def permutar(xs: Vector[Int]): Vector[Vector[Int]] =
      if (xs.isEmpty) Vector(Vector())
      else
        xs.flatMap { elem =>
          val resto = xs.filter(_ != elem)
          permutar(resto).map(p => elem +: p)
        }

    // Paralelizamos la capa exterior
    indices.par.flatMap { elem =>
      val resto = indices.filter(_ != elem)
      permutar(resto).map(p => elem +: p)
    }.toVector
  }

  /** Programación de riego óptima paralela */
  def ProgramacionRiegoOptimoPar(f: Finca, d: Distancia): (ProgRiego, Int) = {
    // Generar permutaciones ya paralelizado
    val programaciones = generarProgramacionesRiegoPar(f)

    // Costo total usando funciones paralelas
    def costoTotal(pi: ProgRiego): Int = {
      val cr = costoRiegoFincaPar(f, pi)
      val cm = costoMovilidadPar(f, pi, d)
      cr + cm
    }

    // Buscar mínimo en paralelo
    val mejor = programaciones.par.minBy(pi => costoTotal(pi))
    val mejorCosto = costoTotal(mejor)

    (mejor, mejorCosto)
  }
}


