import * as React from 'react';
import {Cell, Legend, Pie, PieChart, Tooltip} from 'recharts';
import {uuid} from '../../types/Types';
import {Widget} from '../../usecases/dashboard/components/widgets/Widget';
import {splitDataIntoSlices} from './pieChartHelper';
import './PieChartSelector.scss';
import {FilterParam} from '../../state/search/selection/selectionModels';

export interface Pie {
  name: string;
  value: number;
  filterParam: FilterParam | FilterParam[];
}

export interface PieData2 {
  [key: string]: Pie;
}

export type PieClick = (id: uuid | boolean) => void;

export interface PieChartSelectorProps {
  data: PieData2;
  onClick?: PieClick;
  colors: string[];
  heading: string;
  maxSlices: number;
}

interface Legend {
  value: string | number;
  type: 'square';
  color: string;
  filterParam: FilterParam | FilterParam[];
}

export const PieChartSelector = (props: PieChartSelectorProps) => {
  const {data, colors, heading, onClick, maxSlices} = props;

  const segments: uuid[] = Object.keys(data);
  const pieSlices = splitDataIntoSlices(segments, data, maxSlices);

  const renderCell = (entry: any, index: number) => (
    <Cell
      key={index}
      fill={colors[index % colors.length]}
      stroke={'transparent'}
    />);

  // TODO: Should perhaps be included in the action to handle array arguments, so it also can be tested.
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
          align={'left'}
          layout={'vertical'}
        />
      </PieChart>
    </Widget>
  );
};
