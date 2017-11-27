import 'PieChartSelector.scss';
import * as React from 'react';
import {Cell, Legend, Pie, PieChart, Tooltip} from 'recharts';
import {translate} from '../../services/translationService';
import {uuid} from '../../types/Types';
import {Widget} from '../../usecases/dashboard/components/widgets/Widget';

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
  const {data, colors, heading, onClick} = props;

  const showIfGreaterPartThan = 0.006;

  // lump together all values under a certain percentage, as "other"
  // clicking on "other", is.. undefined behavior, for now
  const total: number = data.reduce((sum, piece) => sum + piece.value, 0);

  const localizedOther = translate('other');

  const filteredData: PieData[] = data
    .reduce((filtered: PieData[], current: PieData) => {
      if ((current.value / total) >= showIfGreaterPartThan) {
        filtered.push(current);
      } else {
        const indexOfOther = filtered.findIndex((current: PieData) => current.name === localizedOther);
        if (indexOfOther !== -1) {
          filtered[indexOfOther].value += current.value;
        } else {
          filtered.push({name: localizedOther, value: current.value});
        }
      }
      return filtered;
    }, []);

  const renderCell = (entry: any, index: number) => (
    <Cell
      key={index}
      fill={colors[index % colors.length]}
      stroke={'transparent'}
    />);

  // TODO typing for handling rechart's onClick events is broken, see
  // https://github.com/DefinitelyTyped/DefinitelyTyped/issues/20722
  const onPieClick = (data: any) => onClick && onClick(data.payload.name);

  // the default legend only shows labels, I want to include the count as well
  const legend = filteredData.map((dataTuple, index) => ({
    value: `${dataTuple.name} (${dataTuple.value})`,
    type: 'square',
    color: colors[index % colors.length],
    id: dataTuple.name,
  }));

  const margins = {top: 20, right: 0, bottom: 0, left: 0};

  return (
    <Widget title={heading}>
      <PieChart width={240} height={300}>
        <Pie onClick={onPieClick} data={data} activeIndex={[]} activeShape={null} animationDuration={500} cy={110}>
          {filteredData.map(renderCell)}
        </Pie>
        <Tooltip viewBox={{x: 1, y: 2, width: 200, height: 200}}/>
        <Legend
          margin={margins}
          payload={legend}
        />
      </PieChart>
    </Widget>
  );
};
