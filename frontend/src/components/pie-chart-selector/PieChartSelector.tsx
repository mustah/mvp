import * as React from 'react';
import {Cell, Legend, Pie, PieChart, Tooltip} from 'recharts';
import {FilterParam} from '../../state/search/selection/selectionModels';
import {ItemOrArray, uuid} from '../../types/Types';
import {Widget} from '../../usecases/dashboard/components/widgets/Widget';
import {ColumnCenter} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {splitDataIntoSlices} from './pieChartHelper';
import './PieChartSelector.scss';

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
  setSelection?: PieClick;
  colors: string[];
  heading: string;
  maxSlices: number;
}

interface LegendPayload extends PieSlice {
  fill: any;
  payload: PieSlice;
  stroke: any;
}

interface Legend {
  color: any;
  value: string;
  payload: LegendPayload;
  type: any;
}

interface RenderLegendProps {
  payload: Legend[];

  [key: string]: any;
}

interface PieSliceCallback {
  payload: PieSlice;

  [key: string]: any;
}

export const PieChartSelector = (props: PieChartSelectorProps) => {
  const {data, colors, heading, setSelection, maxSlices} = props;

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

  const margins = {top: 20, right: 0, bottom: 0, left: 0};

  const onPieClick = ({payload: {filterParam}}: PieSliceCallback) => setSelection && setSelection(filterParam);
  const onLegendClick = (filterParam: ItemOrArray<FilterParam>) => setSelection && setSelection(filterParam);

  const renderLegend = (props: RenderLegendProps) => {
    const {payload} = props;
    const render = ({color, payload: {value, name, filterParam}}: Legend, i) => {
      const onClick = () => onLegendClick(filterParam);
      return (
        <Row key={i} onClick={onClick}>
          <ColumnCenter>
            <div style={{height: 10, width: 10, marginRight: 5, backgroundColor: color}}/>
          </ColumnCenter>
          <div>{name} ({value})</div>
        </Row>);
    };
    return payload.map(render);
  };

  return (
    <Widget title={heading}>
      <PieChart width={240} height={300}>
        <Pie onClick={onPieClick} data={pieSlices} animationDuration={500} cy={110}>
          {pieSlices.map(renderCell)}
        </Pie>
        <Tooltip viewBox={{x: 1, y: 2, width: 200, height: 200}}/>
        <Legend
          margin={margins}
          align={'left'}
          content={renderLegend}
          layout={'vertical'}
        />
      </PieChart>
    </Widget>
  );
};
