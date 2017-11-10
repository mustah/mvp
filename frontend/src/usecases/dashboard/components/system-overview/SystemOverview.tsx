import * as React from 'react';
import {IndicatorWidgetProps, IndicatorWidgets} from '../../../common/components/indicators/IndicatorWidgets';
import {Column} from '../../../common/components/layouts/column/Column';
import './SystemOverview.scss';

export const SystemOverview = (props: IndicatorWidgetProps) => {
  const {indicators, selectIndicatorWidget, selectedWidget, showSelected} = props;
  return (
    <Column className="SystemOverview">
      <IndicatorWidgets
        selectIndicatorWidget={selectIndicatorWidget}
        selectedWidget={selectedWidget}
        indicators={indicators}
        showSelected={showSelected}
      />
    </Column>
  );
};
