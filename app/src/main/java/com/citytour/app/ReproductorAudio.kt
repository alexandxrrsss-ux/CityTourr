package com.citytour.app

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.annotation.RawRes

/** Estados posibles del audio. Toda la lógica de los botones depende de este estado. */
enum class EstadoAudio { DETENIDO, CARGANDO, REPRODUCIENDO, PAUSADO }

/**
 * Administra un MediaPlayer a partir de un archivo de res/raw.
 * Notifica cada cambio de estado a la interfaz mediante [alCambiarEstado].
 */
class ReproductorAudio(
    private val context: Context,
    @RawRes private val recurso: Int,
    private val alCambiarEstado: (EstadoAudio) -> Unit,
    private val alFallar: () -> Unit
) {
    private var reproductor: MediaPlayer? = null
    private var volumen = 0.8f

    var estado: EstadoAudio = EstadoAudio.DETENIDO
        private set

    /** Inicia desde cero si estaba detenido, o reanuda si estaba en pausa. */
    fun reproducir() {
        when (estado) {
            EstadoAudio.DETENIDO -> iniciarNuevo()
            EstadoAudio.PAUSADO -> {
                reproductor?.start()
                cambiarEstado(EstadoAudio.REPRODUCIENDO)
            }
            else -> Unit // ya está cargando o reproduciendo: se ignora el toque
        }
    }

    /** Pausa conservando la posición actual para poder reanudar. */
    fun pausar() {
        if (estado == EstadoAudio.REPRODUCIENDO) {
            reproductor?.pause()
            cambiarEstado(EstadoAudio.PAUSADO)
        }
    }

    /** Detiene, libera el MediaPlayer y regresa al inicio del audio. */
    fun detener() {
        liberar()
        cambiarEstado(EstadoAudio.DETENIDO)
    }

    /** Ajusta el volumen del reproductor (0.0 a 1.0), incluso mientras suena. */
    fun cambiarVolumen(nivel: Float) {
        volumen = nivel.coerceIn(0f, 1f)
        reproductor?.setVolume(volumen, volumen)
    }

    /** Libera memoria nativa; se llama al cerrar la pantalla. */
    fun liberar() {
        reproductor?.release()
        reproductor = null
    }

    private fun iniciarNuevo() {
        cambiarEstado(EstadoAudio.CARGANDO)
        val descriptor = context.resources.openRawResourceFd(recurso)
        val mp = MediaPlayer()
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mp.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            mp.setVolume(volumen, volumen)
            // Preparación asíncrona: no bloquea el hilo principal ni la interfaz
            mp.setOnPreparedListener {
                it.start()
                cambiarEstado(EstadoAudio.REPRODUCIENDO)
            }
            mp.setOnCompletionListener { detener() }
            mp.setOnErrorListener { _, _, _ ->
                detener()
                alFallar()
                true
            }
            reproductor = mp
            mp.prepareAsync()
        } catch (e: Exception) {
            mp.release()
            reproductor = null
            cambiarEstado(EstadoAudio.DETENIDO)
            alFallar()
        } finally {
            descriptor.close()
        }
    }

    private fun cambiarEstado(nuevo: EstadoAudio) {
        estado = nuevo
        alCambiarEstado(nuevo)
    }
}
