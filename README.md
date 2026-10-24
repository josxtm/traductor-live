# Traductor LIVE v0.1.0

Aplicación Android para traducción de conversaciones presenciales al castellano.

## Funciones
- Pulsación del botón rojo para escuchar, nueva pulsación para detener.
- Texto original y traducción por fragmentos.
- Histórico local con fecha y hora (SQLite); no se guarda audio.
- Reconocimiento de voz de Android con idioma de entrada configurable; identificación del texto y traducción con Google ML Kit.
- El micrófono se detiene cuando la aplicación abandona primer plano.

## APK
Abrir **Actions → Build Android APK** y descargar el artefacto ZIP de la última ejecución correcta. Extraer la APK e instalar en el teléfono.

## Limitaciones
El reconocimiento depende del servicio de voz de Android y puede necesitar Internet. Esta versión requiere elegir el idioma de reconocimiento; la identificación automática se aplica al texto capturado. No está verificada aún en Nothing Phone 2a.

## Privacidad
El audio no se almacena. El proveedor de reconocimiento de voz puede procesar el audio fuera del dispositivo. Informar a los participantes antes de usar.
