import * as classNames from 'classnames';
import * as React from 'react';
import {Normal} from '../../../common/components/texts/Texts';
import {Column} from '../../../layouts/components/column/Column';
import {Row} from '../../../layouts/components/row/Row';
import './DonutGraph.scss';
import {DonutGraphModel} from 'usecases/widget/models/WidgetModels';
import {Cell, Legend, Pie, PieChart} from 'recharts';
import {Link} from 'react-router-dom';

export interface RechartsDonutChartProps {
  records: object[];
}

const TwoSimplePieChart = (props: RechartsDonutChartProps) => {
  const {records} = props;

  // TODO we need more colors, or possibly define the colors elsewhere (what if a customer wants to "brand" a chart? :P)
  const colors = ['#00B6F8', '#49C8F6', '#79D4F5'];

  return (
    <PieChart width={180} height={140}>
      <Pie activeShape={null} activeIndex={[]} data={records} cx={90} cy={60} innerRadius={25} outerRadius={45}>
        {records.map((entry, index) => {
          return (<Cell
            key={index}
            fill={colors[index % colors.length]}
            stroke={"transparent"}
          />);
        })}
      </Pie>
      <Legend/>
    </PieChart>
  );
};

export const DonutGraph = (props: DonutGraphModel) => {
  const {title, records, href} = props;
  return (
    <Link to={href} className="link">
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
