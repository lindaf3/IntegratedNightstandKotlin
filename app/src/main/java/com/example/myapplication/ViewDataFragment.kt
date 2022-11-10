package com.example.myapplication

import CloudClient
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
        setFragmentResultListener("requestKey") { _, bundle ->
            val startHour = bundle.getInt("start hour")
            val endHour = bundle.getInt("end hour")
            val startMinute = bundle.getInt("start minute")
            val endMinute = bundle.getInt("end minute")
            val domLow = startHour +startMinute/60.0
            val domHigh = endHour + endMinute/60.0

            super.onViewCreated(view, savedInstanceState)
            var sleepDataX = mutableListOf<Double>()
            var sleepData = mutableListOf<Double>()
            val dataSeries: XYSeries = SimpleXYSeries(sleepDataX.toList(), sleepData.toList(), "Sleep Data" )
            val dataFormat = FastLineAndPointRenderer.Formatter(Color.BLUE, Color.BLACK, null, null)

            val graph = view.findViewById<XYPlot>(R.id.graph)
            val refresh = view.findViewById<Button>(R.id.refresh)
            graph.addSeries(dataSeries, dataFormat)

            graph.setDomainBoundaries(domLow, domHigh, BoundaryMode.FIXED)
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
                graph.redraw()

                Toast.makeText(requireActivity().application, "Updated Data!", Toast.LENGTH_SHORT).show()
            }

        }


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




