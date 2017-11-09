import 'PieChartSelector.scss';
import * as React from 'react';
import {Cell, Legend, Pie, PieChart, Tooltip} from 'recharts';
import {uuid} from '../../../../types/Types';
import {Column} from '../layouts/column/Column';

interface PieData {
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

  return (
    <Column className="PieContainer">
      <h3>{heading}</h3>
      <PieChart width={240} height={240}>
        <Pie data={data} activeIndex={[]} activeShape={null}>
          {data.map(renderCell)}
        </Pie>
        <Tooltip viewBox={{x: 1, y: 2, width: 200, height: 200}}/>
        <Legend/>
      </PieChart>
    </Column>
  );
};
