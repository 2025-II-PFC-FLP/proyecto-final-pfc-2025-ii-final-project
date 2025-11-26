# Proyecto Final – Programación Funcional y Concurrente
## Informe 1
### Fundamentos, tIR y Cálculo de Costos Secuenciales

*Integrante:* Daniela Orrego Alfonso
*Código:* 2510208
*Correo:* daniela.orrego@correounivalle.edu.co

---

# 1. RESPONSABILIDADES 

- Generación de datos base (fincaAlAzar, distanciaAlAzar)
- Exploración de la finca (tsup, treg, prio)
- Cálculo del tiempo de inicio de riego (tIR)
- Cálculo de los costos secuenciales:
    - costoRiegoTablon
    - costoRiegoFinca
    - costoMovilidad
- Creación de pruebas unitarias
- Documentación y soporte teórico

---

# 2. FUNCIONES IMPLEMENTADAS

## 2.1 Tipos base

scala
type Tablon = (Int, Int, Int)
type Finca = Vector[Tablon]
type Distancia = Vector[Vector[Int]]
type ProgRiego = Vector[Int]
type TiempoInicioRiego = Vector[Int]


---

## 2.2 Generación aleatoria de datos

### fincaAlAzar(long: Int): Finca

Genera una finca con long tablones donde:

- ts: tiempo de supervivencia ∈ [1, 2*long]
- tr: tiempo de riego ∈ [1, long]
- p: prioridad ∈ [1, 4]

### distanciaAlAzar(long: Int): Distancia

Genera una matriz:

- Cuadrada long × long
- Simétrica
- Diagonal = 0

---

## 2.3 Exploración de la finca

scala
def tsup(f,i) = f(i)._1
def treg(f,i) = f(i)._2
def prio(f,i) = f(i)._3


---

# 3. TIEMPO DE INICIO DE RIEGO — tIR

La programación pi(j) indica qué tablón se riega en el turno j.

### Funcionamiento general

tIR recorre turno por turno acumulando los tiempos de riego previos.

---

## 3.1 Proceso en Mermaid

mermaid
graph TD
A[Inicio tIR] --> B[loop(0,0,t)]
B --> C[Actualizar t(pi(0))]
C --> D[turno 1]
D --> E[turno 2]
E --> F[turno n]
F --> G[Devolver vector t]


---

## 3.2 Argumento de corrección (LaTeX)

El tiempo de inicio del tablón \( i \) es:

\[
t_i = \sum_{j < k(i)} tr_{\pi(j)}
\]

La recursión garantiza:

- Cada turno suma correctamente el tiempo acumulado.
- Se actualiza exactamente el tablón correspondiente.
- No se usa mutabilidad.
- Se cumple la definición matemática del problema.

---

# 4. COSTOS

## 4.1 costoRiegoTablon

Casos:

\[
\text{Si } ts_i - tr_i \ge t_i:
\quad CR[i] = ts_i - (t_i + tr_i)
\]

\[
\text{De lo contrario: } CR[i] = p_i \cdot ((t_i + tr_i) - ts_i)
\]

---

## 4.2 costoRiegoFinca

\[
CR_{total} = \sum_i CR[i]
\]

Implementado con foldLeft.

---

## 4.3 costoMovilidad

\[
CM = \sum_{j=0}^{n-2} d[\pi(j), \pi(j+1)]
\]

Usa foldLeft y no emplea mutabilidad.

---

# 5. PRUEBAS UNITARIAS

Suite: ProyectoRiegoSpec.scala

Pruebas realizadas:

- Generación correcta de finca y matriz de distancias
- Cálculo exacto de tIR con diferentes programaciones
- Validación de costoRiegoTablon
- Validación de costoRiegoFinca
- Validación de costoMovilidad

Todas las pruebas pasaron.

