import * as React from 'react';
import {Cell, Legend, Pie, PieChart, Tooltip} from 'recharts';
import {ItemOrArray, uuid} from '../../types/Types';
import {Widget} from '../../usecases/dashboard/components/widgets/Widget';
import {splitDataIntoSlices} from './pieChartHelper';
import './PieChartSelector.scss';
import {FilterParam} from '../../state/search/selection/selectionModels';

export interface PieSlice {
  name: string;
  value: number;
  filterParam: ItemOrArray<FilterParam>;
}

export interface PieData {
  [key: string]: PieSlice;
}

export type PieClick = (id: ItemOrArray<FilterParam>) => void;

export interface PieChartSelectorProps {
  data: PieData;
  onClick?: PieClick;
  colors: string[];
  heading: string;
  maxSlices: number;
}

interface Legend {
  value: string | number;
  type: 'square';
  color: string;
  filterParam: ItemOrArray<FilterParam>;
}

interface PieSliceCallback {
  payload: PieSlice;

  [key: string]: any;
}

export const PieChartSelector = (props: PieChartSelectorProps) => {
  const {data, colors, heading, onClick, maxSlices} = props;

  const segments: uuid[] = Object.keys(data);
  const pieSlices: PieSlice[] = splitDataIntoSlices(segments, data, maxSlices);

  // TODO typing for handling rechart's onClick events is broken, see
  // https://github.com/DefinitelyTyped/DefinitelyTyped/issues/20722
  const renderCell = (entry: any, index: number) => (
    <Cell
      key={index}
      fill={colors[index % colors.length]}
      stroke={'transparent'}
    />);

  const legend = pieSlices.map(({name, value, filterParam}: PieSlice, index: number): Legend => ({
    value: `${name} (${value})`,
    type: 'square',
    color: colors[index % colors.length],
    filterParam,
  }));
  const margins = {top: 20, right: 0, bottom: 0, left: 0};

  const onPieClick = ({payload: {filterParam}}: PieSliceCallback) => onClick && onClick(filterParam);
  const onLegendClick = ({filterParam}: Legend) => onClick && onClick(filterParam);

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
