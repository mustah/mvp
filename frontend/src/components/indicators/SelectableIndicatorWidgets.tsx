import * as React from 'react';
import {Children} from '../../types/Types';
import {Indicator} from '../../usecases/report/reportModels';
import {Row} from '../layouts/row/Row';
import {Medium, OnSelectIndicator} from './indicatorWidgetModels';
import {SelectableIndicatorWidget} from './SelectableIndicatorWidget';

export interface SelectedIndicatorWidgetProps {
  selectedIndicatorTypes: Medium[];
}

export interface IndicatorWidgetsDispatchProps {
  onClick: OnSelectIndicator;
}

export interface IndicatorWidgetProps extends SelectedIndicatorWidgetProps, IndicatorWidgetsDispatchProps {
  indicators: Indicator[];
  children?: Children;
  className?: string;
}

export const SelectableIndicatorWidgets = (props: IndicatorWidgetProps) => {
  const {className, children, indicators, selectedIndicatorTypes, onClick} = props;

  const indicatorWidgets = indicators.map((indicator: Indicator) => (
    <SelectableIndicatorWidget
      key={indicator.type}
      indicator={indicator}
      isSelected={selectedIndicatorTypes.includes(indicator.type)}
      onClick={onClick}
    />
  ));

  return (
    <Row className={className}>
      {indicatorWidgets}
      {children}
    </Row>);
};
