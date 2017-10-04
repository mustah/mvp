import * as classNames from 'classnames';
import * as React from 'react';
import {Cell, Legend, Pie, PieChart} from 'recharts';
import {Column} from '../../../layouts/components/column/Column';
import {Row} from '../../../layouts/components/row/Row';
import {Normal} from '../texts/Texts';
import './DonutGraphWidget.scss';
import {DonutGraph, GraphRecord} from './models/DonutGraphModels';

interface PieChartProps {
  records: GraphRecord[];
}

const TwoSimplePieChart = (props: PieChartProps) => {
  const {records} = props;
  // TODO we need more colors, or possibly define the colors elsewhere (what if a customer wants to "brand" a chart? :P)
  const colors = ['#00B6F8', '#49C8F6', '#79D4F5'];

  const renderCell = (entry: GraphRecord, index: number) => (
    <Cell
      key={index}
      fill={colors[index % colors.length]}
      stroke={'transparent'}
    />);

  return (
    <PieChart width={180} height={140}>
      <Pie
        activeShape={null}
        activeIndex={[]}
        data={records}
        cx={90}
        cy={60}
        innerRadius={25}
        outerRadius={45}
      >
        {records.map(renderCell)}
      </Pie>
      <Legend/>
    </PieChart>
  );
};

export interface DonutGraphProps {
  donutGraph: DonutGraph;
}

export const DonutGraphWidget = (props: DonutGraphProps) => {
  const {donutGraph} = props;
  const {title, records} = donutGraph;

  return (
    <div className="Indicator-wrapper">
      <Column className={classNames('DonutGraph Indicator Column-center')}>
        <Row className={classNames('Row-center Indicator-name')}>
          <Normal>{title}</Normal>
        </Row>
        <Row className={classNames('Row-center')}>
          <TwoSimplePieChart records={records}/>
        </Row>
      </Column>
    </div>
  );
};
