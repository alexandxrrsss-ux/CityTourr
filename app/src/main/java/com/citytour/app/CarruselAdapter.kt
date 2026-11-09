package com.citytour.app

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

/** Datos de una diapositiva: imagen (res/drawable-nodpi) y su descripción de accesibilidad. */
data class Diapositiva(
    @DrawableRes val imagen: Int,
    @StringRes val descripcion: Int
)

/** Adaptador del carrusel: enlaza cada Diapositiva con un ImageView del ViewPager2. */
class CarruselAdapter(
    private val diapositivas: List<Diapositiva>
) : RecyclerView.Adapter<CarruselAdapter.DiapositivaViewHolder>() {

    class DiapositivaViewHolder(val vista: ImageView) : RecyclerView.ViewHolder(vista)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiapositivaViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diapositiva, parent, false) as ImageView
        return DiapositivaViewHolder(vista)
    }

    override fun onBindViewHolder(holder: DiapositivaViewHolder, position: Int) {
        val item = diapositivas[position]
        holder.vista.setImageResource(item.imagen)
        holder.vista.contentDescription = holder.vista.context.getString(item.descripcion)
    }

    override fun getItemCount(): Int = diapositivas.size
}
