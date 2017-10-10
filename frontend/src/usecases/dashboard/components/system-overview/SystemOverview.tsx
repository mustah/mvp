import * as React from 'react';
import {
  IndicatorWidgetProps,
  IndicatorWidgets,
} from '../../../common/components/indicators/components/IndicatorWidgets';
import {Title} from '../../../common/components/texts/Title';
import {Column} from '../../../common/components/layouts/column/Column';
import './SystemOverview.scss';

interface SystemOverviewProps extends IndicatorWidgetProps {
  title: string;
}

export const SystemOverview = (props: SystemOverviewProps) => {
  const {indicators, selectIndicatorWidget, selectedWidget, title} = props;
  return (
    <Column className="SystemOverview">
      <Title>{title}</Title>
      <IndicatorWidgets
        selectIndicatorWidget={selectIndicatorWidget}
        selectedWidget={selectedWidget}
        indicators={indicators}
      />
    </Column>
  );
};
