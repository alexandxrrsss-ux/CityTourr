package com.citytour.app

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

/** Pantalla única de CityTour: carrusel de imágenes + controles del audio histórico. */
class MainActivity : AppCompatActivity() {

    private lateinit var carrusel: ViewPager2
    private lateinit var contador: TextView
    private lateinit var textoEstado: TextView
    private lateinit var btnReproducir: Button
    private lateinit var btnPausa: Button
    private lateinit var btnDetener: Button
    private lateinit var reproductor: ReproductorAudio

    // Imágenes ubicadas en res/drawable-nodpi (formato WebP)
    private val diapositivas = listOf(
        Diapositiva(R.drawable.monumento_1, R.string.desc_imagen_1),
        Diapositiva(R.drawable.monumento_2, R.string.desc_imagen_2),
        Diapositiva(R.drawable.monumento_3, R.string.desc_imagen_3),
        Diapositiva(R.drawable.monumento_4, R.string.desc_imagen_4)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        carrusel = findViewById(R.id.carrusel)
        contador = findViewById(R.id.contadorImagen)
        textoEstado = findViewById(R.id.estadoAudio)
        btnReproducir = findViewById(R.id.btnReproducir)
        btnPausa = findViewById(R.id.btnPausa)
        btnDetener = findViewById(R.id.btnDetener)

        configurarCarrusel()
        configurarAudio()
    }

    private fun configurarCarrusel() {
        carrusel.adapter = CarruselAdapter(diapositivas)
        actualizarContador(0)
        // Actualiza el contador cuando el usuario desliza el dedo
        carrusel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = actualizarContador(position)
        })
        findViewById<Button>(R.id.btnAnterior).setOnClickListener {
            if (carrusel.currentItem > 0) carrusel.currentItem -= 1
        }
        findViewById<Button>(R.id.btnSiguiente).setOnClickListener {
            if (carrusel.currentItem < diapositivas.size - 1) carrusel.currentItem += 1
        }
    }

    private fun configurarAudio() {
        reproductor = ReproductorAudio(
            context = this,
            recurso = R.raw.audio_historia,
            alCambiarEstado = { actualizarControles(it) },
            alFallar = { Toast.makeText(this, R.string.error_audio, Toast.LENGTH_SHORT).show() }
        )
        btnReproducir.setOnClickListener { reproductor.reproducir() }
        btnPausa.setOnClickListener { reproductor.pausar() }
        btnDetener.setOnClickListener { reproductor.detener() }

        findViewById<SeekBar>(R.id.barraVolumen).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar, progreso: Int, delUsuario: Boolean) {
                    reproductor.cambiarVolumen(progreso / 100f)
                }
                override fun onStartTrackingTouch(sb: SeekBar) = Unit
                override fun onStopTrackingTouch(sb: SeekBar) = Unit
            }
        )
        actualizarControles(EstadoAudio.DETENIDO)
    }

    /** Habilita solo los botones que tienen sentido en cada estado del audio. */
    private fun actualizarControles(estado: EstadoAudio) {
        btnReproducir.isEnabled = estado == EstadoAudio.DETENIDO || estado == EstadoAudio.PAUSADO
        btnReproducir.setText(
            if (estado == EstadoAudio.PAUSADO) R.string.btn_reanudar else R.string.btn_reproducir
        )
        btnPausa.isEnabled = estado == EstadoAudio.REPRODUCIENDO
        btnDetener.isEnabled = estado != EstadoAudio.DETENIDO
        textoEstado.setText(
            when (estado) {
                EstadoAudio.DETENIDO -> R.string.estado_detenido
                EstadoAudio.CARGANDO -> R.string.estado_cargando
                EstadoAudio.REPRODUCIENDO -> R.string.estado_reproduciendo
                EstadoAudio.PAUSADO -> R.string.estado_pausado
            }
        )
    }

    private fun actualizarContador(posicion: Int) {
        contador.text = "${posicion + 1} / ${diapositivas.size}"
    }

    // Ciclo de vida: el audio se pausa al salir de la app y se libera al cerrar
    override fun onStop() {
        super.onStop()
        reproductor.pausar()
    }

    override fun onDestroy() {
        super.onDestroy()
        reproductor.liberar()
    }
}
