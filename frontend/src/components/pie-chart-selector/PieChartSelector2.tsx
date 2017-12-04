import * as React from 'react';
import {Cell, Legend, Pie, PieChart, Tooltip} from 'recharts';
import {uuid} from '../../types/Types';
import {Widget} from '../../usecases/dashboard/components/widgets/Widget';
import {pieData} from './pieChartHelper';
import './PieChartSelector.scss';

type FilterParam = uuid | boolean | Array<uuid | boolean>;

export interface Pie {
  name: string;
  value: number;
  filterParam: FilterParam;
}

export interface PieData2 {
  [key: string]: Pie;
}

export type PieClick = (id: uuid | boolean) => void;

interface PieChartSelector {
  data: PieData2;
  onClick?: PieClick;
  colors: string[];
  heading: string;
  maxLegends: number;
}

interface Legend {
  value: string | number;
  type: string;
  color: string;
  filterParam: FilterParam;
}

export const PieChartSelector = (props: PieChartSelector) => {
  const {data, colors, heading, onClick, maxLegends} = props;

  const fields: uuid[] = Object.keys(data);
  const pieSlices = pieData(fields, data, maxLegends);

  const renderCell = (entry: any, index: number) => (
    <Cell
      key={index}
      fill={colors[index % colors.length]}
      stroke={'transparent'}
    />);

  const onPieClick = ({payload: {filterParam}}) => {
    if (onClick) {
      if (Array.isArray(filterParam)) {
        filterParam.map((id: uuid) => onClick(id));
      } else {
        onClick(filterParam);
      }
    }
  };

  const onLegendClick = ({filterParam}: Legend) => {
    if (onClick) {
      if (Array.isArray(filterParam)) {
        filterParam.map((id: uuid) => onClick(id));
      } else {
        onClick(filterParam);
      }
    }
  };

  const legend = pieSlices.map(({name, value, filterParam}: Pie, index: number): Legend => ({
    value: `${name} (${value})`,
    type: 'square',
    color: colors[index % colors.length],
    filterParam,
  }));

  const margins = {top: 20, right: 0, bottom: 0, left: 0};

  return (
    <Widget title={heading}>
      <PieChart width={240} height={300}>
        <Pie onClick={onPieClick} data={pieSlices} activeIndex={[]} activeShape={null} animationDuration={500} cy={110}>
          {pieSlices.map(renderCell)}
        </Pie>
        <Tooltip viewBox={{x: 1, y: 2, width: 200, height: 200}}/>
        <Legend
          margin={margins}
          payload={legend}
          onClick={onLegendClick}
        />
      </PieChart>
    </Widget>
  );
};
