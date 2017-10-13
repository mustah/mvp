import * as classNames from 'classnames';
import * as React from 'react';
import {Row} from '../layouts/row/Row';
import {IndicatorWidget} from './IndicatorWidget';
import {Indicator, IndicatorType} from './models/IndicatorModels';

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

export const IndicatorWidgets = (props: IndicatorWidgetProps) => {
  const {indicators, selectedWidget, selectIndicatorWidget} = props;

  const renderWidget = (indicator: Indicator, index: number) => (
    <IndicatorWidget
      key={indicator.type || index}
      indicator={indicator}
      isSelected={selectedWidget !== null ? selectedWidget === indicator.type : index === 0}
      select={selectIndicatorWidget}
    />);

  return (
    <Row className={classNames('Indicators', props.className)}>
      {indicators.map(renderWidget)}
      {props.children}
    </Row>);
};
