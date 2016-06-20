/**
* Name: Series Examples
* Author: Philippe Caillou
* Description: A demonstration of charts composed of series
* Tags: gui, chart
*/
model series


global
{
}

experiment "Different series" type: gui
{
	float minimum_cycle_duration <- 0.2;
	output
	{
		display "data_cumulative_serie_spline_chart" type: java2D
		{
			chart "Nice cumulative series chart" type: series background: # darkblue color: # lightgreen axes: # lightgreen title_font: 'Serif' title_font_size: 32.0 title_font_style:
			'italic' tick_font: 'Monospaced' tick_font_size: 14 tick_font_style: 'bold' label_font: 'Serif' label_font_size: 18 label_font_style: 'plain' legend_font: 'SanSerif'
			legend_font_size: 18 legend_font_style: 'bold' x_range: 50 x_tick_unit: 5 x_serie_labels: ("T+" + cycle) x_label: 'Nice Xlabel' y_label: 'Nice Ylabel'
			{
				data "Spline" value: cos(100 * cycle) * cycle * cycle color: # orange marker_shape: marker_empty style: spline;
				data "Step" value: cycle * cycle style: step color: # lightgrey;
				data "Classic" value: [cycle + 1, cycle] marker_shape: marker_circle color: # yellow;
			}

		}

		display "style_cumulative_style_chart" type: java2D
		{
			chart "Style Cumulative chart" type: series
			{
				data "Spline" value: cos(100 * cycle) color: # orange style: spline;
				data "area" value: cos(100 * cycle) * 0.3 color: # red style: "area";
				data "dot" value: cos(100 * cycle + 60) color: # green style: dot;
			}

		}

		display "datalist_xy_chart" type: java2D
		{
			chart "datalist_xy_cumulative_chart" type: xy
			{
				datalist legend: ["A", "B", "C"] value:
				[[cycle * cos(cycle * 100), cycle * sin(cycle * 100), 2], [cycle / 2 * sin(cycle * 100), cycle * 2 * cos(cycle * 100), 1], [cycle + 2, cycle - 2, cos(cycle * 100)]]
				x_err_values: [3, 2, 10] y_err_values: [3, cos(cycle * 100), 2 * sin(cycle * 100)] marker_shape: marker_circle // same for all
				color: [# green, # blue, # red];
			}

		}

		display "datalist_xy_line_chart" type: java2D
		{
			chart "datalist_xy_cumulative_chart" type: xy
			{
				datalist legend: ["A", "B"] value: [[cycle * cos(cycle * 100), cycle * sin(cycle * 100), 2], [cycle / 2 * sin(cycle * 100), cycle * 2 * cos(cycle * 100), 1]] marker_shape:
				marker_circle // same for all
				color: [# green, # blue] style: line;
			}

		}

		display "datalist_xy_non_cumulative_chart" type: java2D
		{
			chart "datalist_xy_non_cumulative_chart" type: xy
			{
				datalist legend: ["A", "B", "C"] value: [[10, 10], [12, 10], [20 + cycle, 10]] accumulate_values: false x_err_values: [3, 1, 2] y_err_values:
				[[9, 20], [5, 11], [8, 10 + cycle / 2]] // different low/high values for yerr
				marker_size: [1, cycle, 2] // size keyword instead of size in values
				marker_shape: marker_circle // same for all
				color: [# green, # blue, # red];
			}

		}

		display "data_cumulative_serie_chart" type: java2D
		{
			chart "data_cumulative_serie_chart" type: series x_serie_labels: (cycle * cycle)
			{
				data "A" value: [1, 2];
				data "ABC" value: [cycle, cycle] marker_shape: marker_circle x_err_values: 2 * cos(cycle * 100) y_err_values: 2 * sin(cycle * 100) color: # black;
				data "BCD" value: [cycle / 2 + cos(cycle * 100), 1] style: spline;
				data "BCC" value: [2, cycle];
			}

		}

		display "my_data_cumulative_xy" type: java2D
		{
			chart "my_data_cumulative_xy" type: xy
			{
				data "123" value: [1 + cycle, 2, 3] marker_shape: marker_down_triangle;
				data "ABC" value: [cycle + 1, cycle * 2, cos(cycle)] marker_shape: marker_circle fill: false line_visible: false color: # black x_err_values: ln(cycle) y_err_values:
				cos(cycle * 100) * 3;
			}

		}

	}

}