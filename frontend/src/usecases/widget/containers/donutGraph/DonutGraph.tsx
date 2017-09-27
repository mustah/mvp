import * as classNames from 'classnames';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {Cell, Legend, Pie, PieChart} from 'recharts';
import {Normal} from '../../../common/components/texts/Texts';
import {Column} from '../../../layouts/components/column/Column';
import {Row} from '../../../layouts/components/row/Row';
import {DonutGraphModel as DonutGraphModel, GraphRecord} from '../../models/DonutGraphModel';
import './DonutGraph.scss';

interface PieChartProps {
  records: GraphRecord[];
}

const TwoSimplePieChart = (props: PieChartProps) => {
  const {records} = props;
  // TODO we need more colors, or possibly define the colors elsewhere (what if a customer wants to "brand" a chart? :P)
  const colors = ['#00B6F8', '#49C8F6', '#79D4F5'];

  const makeCell = (entry, index) => (
    <Cell
      key={index}
      fill={colors[index % colors.length]}
      stroke={'transparent'}
    />
  );

  return (
    <PieChart width={180} height={140}>
      <Pie activeShape={null} activeIndex={[]} data={records} cx={90} cy={60} innerRadius={25} outerRadius={45}>
        {records.map(makeCell)}
      </Pie>
      <Legend/>
    </PieChart>
  );
};

export const DonutGraph = (props: DonutGraphModel) => {
  const {title, records, url} = props;
  return (
    <Link to={url} className="link">
      <Column className={classNames('DonutGraph Column-center')}>
        <Row className={classNames('Row-center DonutGraph-name')}>
          <Normal>{title}</Normal>
        </Row>
        <Row className={classNames('Row-center')}>
          <TwoSimplePieChart records={records}/>
        </Row>
      </Column>
    </Link>
  );
};
