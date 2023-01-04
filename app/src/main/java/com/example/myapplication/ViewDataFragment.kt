package com.example.myapplication

import CloudClient
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.androidplot.xy.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class ViewDataFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_data, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val times = context?.getSharedPreferences("times", Context.MODE_PRIVATE)
        var startHour : Int = times?.getInt("start hour", -1) ?: -1
        var startMinute: Int = times?.getInt("start minute", -1) ?: -1
        var endHour: Int = times?.getInt("end hour", -1) ?: -1
        var endMinute :Int = times?.getInt("end minute", -1) ?: -1
        var domBounds = getDomBounds(startHour, startMinute, endHour, endMinute)

        super.onViewCreated(view, savedInstanceState)
        var sleepDataX = mutableListOf<Double>()
        var sleepData = mutableListOf<Double>()
        val dataSeries: XYSeries = SimpleXYSeries(sleepDataX.toList(), sleepData.toList(), "Sleep Data" )
        val dataFormat = FastLineAndPointRenderer.Formatter(Color.BLUE, Color.BLACK, null, null)

        val graph = view.findViewById<XYPlot>(R.id.graph)
        val refresh = view.findViewById<Button>(R.id.refresh)
        graph.addSeries(dataSeries, dataFormat)

        graph.setDomainBoundaries(domBounds.first, domBounds.second, BoundaryMode.FIXED)
        graph.setRangeBoundaries(0, 100, BoundaryMode.FIXED)

        refresh.setOnClickListener{
            graph.clear()
            val updatedData = updateGraph()
            for ( (time,amplitude) in updatedData) {
                sleepDataX.add(time)
                sleepData.add(amplitude)
            }
            val updatedDataSeries: XYSeries = SimpleXYSeries(sleepDataX.toList(), sleepData.toList(), "Sleep Data" )
            graph.addSeries(updatedDataSeries, dataFormat)
            startHour = times?.getInt("start hour", -1) ?: -1
            startMinute = times?.getInt("start minute", -1) ?: -1
            endHour = times?.getInt("end hour", -1) ?: -1
            endMinute = times?.getInt("end minute", -1) ?: -1
            domBounds = getDomBounds(startHour, startMinute, endHour, endMinute)
            graph.setDomainBoundaries(domBounds.first, domBounds.second, BoundaryMode.FIXED)
            graph.redraw()

            Toast.makeText(requireActivity().application, "Updated Data!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getDomBounds(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Pair<Double, Double>
    {
        var domLow = startHour +startMinute/60.0
        var domHigh = endHour + endMinute/60.0
        if(startHour == -1 ||startMinute == -1 || endHour == -1 || endMinute == -1){
            domHigh = 24.0
            domLow = 0.0
        }
        return Pair(domLow, domHigh)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateGraph(): Array<Pair<Double, Double>> {
        val cloudClient = CloudClient("/path/key.pem", "longAPIToken")
        val date = ZonedDateTime.now()
        val dateString = date.format(DateTimeFormatter.ISO_DATE)
        println(dateString)

        val cloudData = cloudClient.querySoundData(dateString)
        val (timestamp, amplitude) = cloudData.first()
        val datetime = ZonedDateTime.parse(timestamp)
        val time: Double = datetime.hour + datetime.minute/60.0
        println(datetime.hour)
        println(datetime.minute)
        println(time)
        return arrayOf(Pair(time,amplitude))
    }

}




