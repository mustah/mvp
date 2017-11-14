import * as React from 'react';
import {Row} from '../layouts/row/Row';
import {SelectableIndicatorWidget} from './SelectableIndicatorWidget';
import {IndicatorType} from './models/widgetModels';
import {Indicator} from '../../../report/models/reportModels';

export interface SelectedIndicatorWidgetProps {
  selectedWidget?: IndicatorType | null;
}

export interface IndicatorWidgetsDispatchProps {
  selectIndicatorWidget: (type: IndicatorType) => any;
}

export interface IndicatorWidgetProps extends SelectedIndicatorWidgetProps, IndicatorWidgetsDispatchProps {
  indicators: Indicator[];
  children?: React.ReactElement<any>;
  className?: string;
}

export const SelectableIndicatorWidgets = (props: IndicatorWidgetProps) => {
  const {indicators, selectedWidget, selectIndicatorWidget} = props;

  const renderIndicator = (indicator: Indicator, index: number) => (
    <SelectableIndicatorWidget
      key={indicator.type || index}
      indicator={indicator}
      isSelected={selectedWidget !== null ? selectedWidget === indicator.type : index === 0}
      select={selectIndicatorWidget}
    />);

  return (
    <Row className={props.className}>
      {indicators.map(renderIndicator)}
      {props.children}
    </Row>);
};
