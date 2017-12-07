import * as React from 'react';
import {Children} from '../../types/Types';
import {Indicator} from '../../usecases/report/models/reportModels';
import {Row} from '../layouts/row/Row';
import {IndicatorType} from './models/widgetModels';
import {SelectableIndicatorWidget} from './SelectableIndicatorWidget';

export interface SelectedIndicatorWidgetProps {
  selectedWidget?: IndicatorType | null;
}

export interface IndicatorWidgetsDispatchProps {
  selectIndicatorWidget: (type: IndicatorType) => void;
}

export interface IndicatorWidgetProps extends SelectedIndicatorWidgetProps, IndicatorWidgetsDispatchProps {
  indicators: Indicator[];
  children?: Children;
  className?: string;
}

export const SelectableIndicatorWidgets = (props: IndicatorWidgetProps) => {
  const {indicators, selectedWidget, selectIndicatorWidget} = props;

  const renderIndicator = (indicator: Indicator, index: number) => {
    const isSelected = selectedWidget !== null
      ? selectedWidget === indicator.type
      : indicator.type === IndicatorType.districtHeating;

    return (
      <SelectableIndicatorWidget
        key={indicator.type || index}
        indicator={indicator}
        isSelected={isSelected}
        select={selectIndicatorWidget}
      />
    );
  };

  return (
    <Row className={props.className}>
      {indicators.map(renderIndicator)}
      {props.children}
    </Row>);
};
