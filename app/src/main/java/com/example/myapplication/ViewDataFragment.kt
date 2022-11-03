package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.androidplot.xy.*


class ViewDataFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sleepDataX = arrayOf<Number>(0,1,1.5,3,7,8,9,10,12,14,17,20,23)
        val sleepData = arrayOf<Number>(2,35,67,84,3,2,9,0,123,124,1,56,9)
        val dataSeries: XYSeries = SimpleXYSeries(sleepDataX.asList(), sleepData.asList(), "Sleep Data" )
        val dataFormat = FastLineAndPointRenderer.Formatter(Color.BLUE, Color.BLACK, null, null)


        val graph = view.findViewById<XYPlot>(R.id.graph)
        val refresh = view.findViewById<Button>(R.id.refresh)
        graph.addSeries(dataSeries, dataFormat)
        graph.setRangeLowerBoundary(0, BoundaryMode.FIXED)
        graph.setDomainBoundaries(0, 23, BoundaryMode.FIXED)

        refresh.setOnClickListener{
            Toast.makeText(requireActivity().application, "Updating Data!", Toast.LENGTH_SHORT).show()
        }

    }

}


