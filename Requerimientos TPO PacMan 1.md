1. **Movimiento:** PacMan se mueve en las 4 direcciones según cómo lo indique el jugador por medio del teclado.
2. **Paredes:** PacMan sólo puede girar al estar alineado con una celda del laberinto, no puede atravesar paredes.
3. **Puntaje:** El laberinto tiene puntos en las celdas transitables (10 de puntaje por punto comido), y cuatro super puntos (50 de puntaje por super punto) ubicados en las esquinas. PacMan come un punto o super punto al pararse en la celda en que el punto se ubica.
4. **Enemigos:** Cuatro fantasmas persiguen a PacMan moviéndose de manera autónoma por las celdas del laberinto. En cada cruce eligen el camino que más los acerca a PacMan.
5. **Super punto:** Al comer un super punto, los fantasmas que no estén volviendo a su posición de origen pasan a estar asustados. El estado ASUSTADO hace que, durante un tiempo determinado, los fantasmas se muevan aleatoriamente por las celdas y puedan ser comidos por PacMan.
6. **Consumo de fantasmas:** Comer un fantasma asustado suma 200 puntos al jugador. El fantasma pasa al estado REGRESANDO tras ser comido, y vuelve a su posición de origen. Al llegar, pasa al estado PERSIGUIENDO y vuelve a perseguir a PacMan.
7. **Muerte PacMan:** Si PacMan toca a un fantasma que no está asustado, entonces PacMan pierde una vida. Tanto PacMan como los fantasmas, vuelven a sus posiciones iniciales.
8. **Vidas:** Al iniciar la partida, el jugador tiene 3 vidas. Cuando pierde las 3 vidas, la partida finaliza en derrota.
9. **Victoria:** Si el jugador come todos los puntos y super puntos del laberinto, la partida finaliza en victoria.
10. **Control del juego:** El jugador puede pausar y reanudar la partida en cualquier momento, y reiniciar una nueva partida al finalizar la anterior.
