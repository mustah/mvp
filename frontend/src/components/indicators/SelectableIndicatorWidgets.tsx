import * as React from 'react';
import {Children} from '../../types/Types';
import {Indicator} from '../../usecases/report/reportModels';
import {Row} from '../layouts/row/Row';
import {IndicatorType, OnSelectIndicator} from './indicatorWidgetModels';
import {SelectableIndicatorWidget} from './SelectableIndicatorWidget';

export interface SelectedIndicatorWidgetProps {
  selectedIndicatorType: IndicatorType;
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
  const {className, children, indicators, selectedIndicatorType, onClick} = props;

  const indicatorWidgets = indicators.map((indicator: Indicator) => (
    <SelectableIndicatorWidget
      key={indicator.type}
      indicator={indicator}
      isSelected={indicator.type === selectedIndicatorType}
      onClick={onClick}
    />
  ));

  return (
    <Row className={className}>
      {indicatorWidgets}
      {children}
    </Row>);
};
