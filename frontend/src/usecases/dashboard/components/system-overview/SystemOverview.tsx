import * as React from 'react';
import {DonutGraphWidget} from '../../../common/components/indicators/DonutGraphWidget';
import {IndicatorWidgetProps, IndicatorWidgets} from '../../../common/components/indicators/IndicatorWidgets';
import {DonutGraph} from '../../../common/components/indicators/models/DonutGraphModels';
import {MainTitle} from '../../../common/components/texts/MainTitle';
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
      <MainTitle title={title}/>
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
