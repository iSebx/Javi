# Javi

Una mascota virtual para Android: Javi camina sobre la pantalla, por encima de las apps.

- Tocarlo: ronronea.
- Arrastrarlo: lo llevás a otro lado.
- Desde la app: darle de comer, mimarlo y jugar.
- De noche duerme más.

Javi nunca se enferma ni se pone triste: si no lo atendés, duerme tranquilo y espera.

## Instalar

Cada cambio en `main` compila el APK con GitHub Actions y lo publica en
[Releases](../../releases/latest) como `Javi.apk`. Bajalo desde el celular e instalalo
(hay que permitir "instalar apps desconocidas" la primera vez).

## Código

- `Javi.kt`: comportamiento y poses (Kotlin puro, sin Android).
- `JaviPainter.kt`: lo dibuja en un Canvas.
- `PetService.kt`: servicio en primer plano con la ventana overlay.
- `MainActivity.kt`: la casita, con vista previa y cuidados.
