import * as React from 'react';
import {DonutGraphWidget} from '../../../common/components/indicators/DonutGraphWidget';
import {IndicatorWidgetProps, IndicatorWidgets} from '../../../common/components/indicators/IndicatorWidgets';
import {DonutGraph} from '../../../common/components/indicators/models/DonutGraphModels';
import {Title} from '../../../common/components/texts/Title';
import {Column} from '../../../layouts/components/column/Column';
import './SystemOverview.scss';

interface SystemOverviewProps extends IndicatorWidgetProps {
  title: string;
  donutGraphs: DonutGraph[];
}

export const SystemOverview = (props: SystemOverviewProps) => {
  const {indicators, donutGraphs, selectIndicatorWidget, selectedWidget, title} = props;
  return (
    <Column className="SystemOverview">
      <Title>{title}</Title>
      <IndicatorWidgets
        selectIndicatorWidget={selectIndicatorWidget}
        selectedWidget={selectedWidget}
        indicators={indicators}
      >
        <DonutGraphWidget donutGraph={donutGraphs[0]}/>
      </IndicatorWidgets>
    </Column>
  );
};
