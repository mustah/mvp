import * as React from 'react';
import {RowCenter} from '../../../../components/layouts/row/Row';
import {Medium, OnSelectIndicator} from '../../../../components/indicators/indicatorWidgetModels';
import {ClickableReportIndicatorProps, ReportIndicatorProps, ReportIndicatorWidget} from './ReportIndicatorWidget';

export interface SelectedIndicatorWidgetProps {
  selectedIndicatorTypes: Medium[];
}

export interface IndicatorWidgetsDispatchProps {
  onClick: OnSelectIndicator;
}

export interface IndicatorWidgetProps extends SelectedIndicatorWidgetProps, IndicatorWidgetsDispatchProps {
  indicators: ReportIndicatorProps[];
  enabledIndicatorTypes: Set<Medium>;
}

export const ReportIndicatorWidgets = (props: IndicatorWidgetProps) => {
  const {indicators, selectedIndicatorTypes, enabledIndicatorTypes, onClick} = props;

  const indicatorWidgets = indicators.map((props: ClickableReportIndicatorProps) => (
    <ReportIndicatorWidget
      {...props}
      key={props.type}
      isSelected={selectedIndicatorTypes.includes(props.type)}
      enabled={enabledIndicatorTypes.has(props.type)}
      onClick={onClick}
    />
  ));

  return (
    <RowCenter>
      {indicatorWidgets}
    </RowCenter>
  );
};
