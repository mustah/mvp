import 'PieChartSelector.scss';
import * as React from 'react';
import {Cell, Legend, Pie, PieChart, Tooltip} from 'recharts';
import {uuid} from '../../../../types/Types';
import {Column} from '../layouts/column/Column';

export interface PieData {
  name: string;
  value: number;
}

export type PieClick = (name: uuid) => void;

interface PieChartSelector {
  data: PieData[];
  onClick?: PieClick;
  colors: string[];
  heading: string;
}

/**
 * Known issue: if there is no data, the chart's legend will be shown, but there is no "empty" pie chart visualized.
 */
export const PieChartSelector = (props: PieChartSelector) => {
  const {data, colors, heading} = props;

  const renderCell = (entry: any, index: number) => (
    <Cell
      key={index}
      fill={colors[index % colors.length]}
      stroke={'transparent'}
    />);

  // TODO typing for handling rechart's onClick events is broken, see
  // https://github.com/DefinitelyTyped/DefinitelyTyped/issues/20722
  // Add this onClickProxy for the onClick property on <Pie>

  /*
  const onClickProxy = (data: any) => {
    if (onClick) {
      onClick(data.payload.name);
    }
  };
  */

  // the default legend only shows labels, I want to include the count as well
  const legend = data.map((dataTuple, index) => ({
    value: `${dataTuple.name} (${dataTuple.value})`,
    type: 'square',
    color: colors[index % colors.length],
    id: dataTuple.name,
  }));

  return (
    <Column className="PieContainer">
      <h3>{heading}</h3>
      <PieChart width={240} height={240}>
        <Pie data={data} activeIndex={[]} activeShape={null}>
          {data.map(renderCell)}
        </Pie>
        <Tooltip viewBox={{x: 1, y: 2, width: 200, height: 200}}/>
        <Legend
          payload={legend}
        />
      </PieChart>
    </Column>
  );
};
