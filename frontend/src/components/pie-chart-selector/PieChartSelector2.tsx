import * as React from 'react';
import {Cell, Legend, Pie, PieChart, Tooltip} from 'recharts';
import {translate} from '../../services/translationService';
import {uuid} from '../../types/Types';
import {Widget} from '../../usecases/dashboard/components/widgets/Widget';
import './PieChartSelector.scss';

export interface Pie {
  name: string;
  value: number;
  filterParam?: uuid | boolean;
  other?: Pie[];
}

export interface PieData2 {
  [key: string]: Pie;
}

export type PieClick = (id: uuid) => void;

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
  id: uuid;
}

const bundleSmallestToOther = (data: Pie[], maxLegends: number): Pie[] => {

  const sortFunction = ({value: value1}: Pie, {value: value2}: Pie) =>
    (value1 < value2 ? 1 : value1 > value2 ? -1 : 0);

  const sortedBySize = data.sort(sortFunction);
  const largestFields = sortedBySize.slice(0, maxLegends - 1);

  const other = sortedBySize.slice(maxLegends - 1).reduce((prev: Pie, curr: Pie) => {
    return {...prev, value: prev.value + curr.value, other: [...prev.other, {...curr}]};
  }, {name: translate('other'), value: 0, other: []});

  return [...largestFields, ...other];
};

const pieData = (fields: uuid[], data: PieData2, maxLegends: number): Pie[] => {

  const pieSlices = fields.map((field) => (data[field]));
  if (fields.length >= maxLegends) {
    return bundleSmallestToOther(pieSlices, maxLegends);
  } else {
    return pieSlices;
  }
};

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

  const onPieClick = (data) => {
    if (onClick) {
      onClick(data.payload.filterParam);
    }
  };

  const onLegendClick = (data: Legend) => {
    if (onClick) {
      onClick(data.id);
    }
  };

  const legend = pieSlices.map((dataTuple: Pie, index: number): Legend => ({
    value: `${dataTuple.name} (${dataTuple.value})`,
    type: 'square',
    color: colors[index % colors.length],
    id: dataTuple.filterParam,
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
