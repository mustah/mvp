import * as React from 'react';
import {Maybe} from '../../helpers/Maybe';
import {Children} from '../../types/Types';
import {Indicator} from '../../usecases/report/reportModels';
import {Row} from '../layouts/row/Row';
import {IndicatorType, OnSelectIndicator} from './indicatorWidgetModels';
import {SelectableIndicatorWidget} from './SelectableIndicatorWidget';

export interface SelectedIndicatorWidgetProps {
  selectedIndicatorType: Maybe<IndicatorType>;
}

export interface IndicatorWidgetsDispatchProps {
  selectIndicatorWidget: OnSelectIndicator;
}

export interface IndicatorWidgetProps extends SelectedIndicatorWidgetProps, IndicatorWidgetsDispatchProps {
  indicators: Indicator[];
  children?: Children;
  className?: string;
}

export const SelectableIndicatorWidgets = (props: IndicatorWidgetProps) => {
  const {className, children, indicators, selectedIndicatorType, selectIndicatorWidget} = props;

  const renderIndicator = (indicator: Indicator, index: number) => {
    const isSelected = selectedIndicatorType.isDefined()
      ? selectedIndicatorType.get() === indicator.type
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
    <Row className={className}>
      {indicators.map(renderIndicator)}
      {children}
    </Row>);
};
