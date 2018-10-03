import * as React from 'react';
import {Cell, Legend, LegendPayload, LegendProps, Pie, PieChart, Tooltip} from 'recharts';
import {ItemOrArray, uuid} from '../../types/Types';
import {WidgetWithTitle} from '../../usecases/dashboard/components/widgets/Widget';
import {ColumnCenter, ColumnContent} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {FirstUpper} from '../texts/Texts';
import {splitDataIntoSlices} from './pieChartHelper';
import './PieChartSelector.scss';

export interface PieSlice {
  name: string;
  value: number;
  filterParam: ItemOrArray<uuid>;
}

export interface PieData {
  [key: string]: PieSlice;
}

export type PieClick = (id: ItemOrArray<uuid>) => void;

export interface PieChartSelectorProps {
  data: PieData;
  setSelection?: PieClick;
  colors: string[];
  heading: string;
  maxSlices: number;
}

interface LegendNestedData extends PieSlice {
  payload: PieSlice;

  [key: string]: any;
}

interface LegendData extends LegendPayload {
  color: any;
  payload: LegendNestedData;

  [key: string]: any;
}

interface RenderLegendProps extends LegendProps {
  payload: LegendData[];

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

  const margins = {top: 20, right: 0, bottom: 0, left: 0};
  const viewBoxStyle = {x: 1, y: 2, width: 200, height: 200};

  const onPieClick = ({payload: {filterParam}}: PieSliceCallback) => setSelection && setSelection(
    filterParam);
  const onLegendClick = (filterParam: ItemOrArray<uuid>) => setSelection && setSelection(
    filterParam);

  const renderCell = (entry: any, index: number) => (
    <Cell
      key={index}
      fill={colors[index % colors.length]}
      stroke={'transparent'}
      cursor="pointer"
    />);
  const renderLegends = (props: RenderLegendProps) => {
    const {payload} = props;
    const render = ({color, payload: {value, name, filterParam}}: LegendData, i) => {
      const onClick = () => onLegendClick(filterParam);
      const legendStyle = {height: 10, width: 10, marginRight: 5, backgroundColor: color};
      return (
        <Row key={i} onClick={onClick} className="clickable">
          <ColumnCenter>
            <div style={legendStyle}/>
          </ColumnCenter>
          <FirstUpper>{name} ({value})</FirstUpper>
        </Row>);
    };
    return payload.map(render);
  };

  return (
    <WidgetWithTitle title={heading}>
      <ColumnContent>
        <PieChart width={240} height={300}>
          <Pie onClick={onPieClick} data={pieSlices} animationDuration={500} cy={110} dataKey="PieChart">
            {pieSlices.map(renderCell)}
          </Pie>
          <Tooltip viewBox={viewBoxStyle}/>
          <Legend
            margin={margins}
            align={'left'}
            content={renderLegends}
            layout={'vertical'}
          />
        </PieChart>
      </ColumnContent>
    </WidgetWithTitle>
  );
};
