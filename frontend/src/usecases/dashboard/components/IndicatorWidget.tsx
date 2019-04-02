import * as React from 'react';
import {Column, ColumnCenter} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Bold, Large, Normal, Xlarge} from '../../../components/texts/Texts';
import {thresholdClassName} from '../../../helpers/thresholds';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {ClassNamed, Clickable, WithChildren} from '../../../types/Types';
import './IndicatorWidget.scss';
import classNames = require('classnames');

export interface IndicatorWidgetProps extends ClassNamed, Clickable, WithChildren {
  value: number;
  title: string;
}

interface EmptyStateProps extends ClassNamed {
  title: string;
}

const NoExpectedMeasurementsWidget = ({className, title}: EmptyStateProps) => (
  <Column className={classNames('Indicator-wrapper NoExpectedMeasurementsWidget', className)}>
    <ColumnCenter className={classNames('Indicator', 'info')}>
      <Row className="Indicator-name Row-center">
        <Bold>{title}</Bold>
      </Row>
      <Row className="Row-center Row-bottom">
        {firstUpperTranslated('no measurements expected for the selected period')}
      </Row>
    </ColumnCenter>
  </Column>
);

export const IndicatorWidget = ({className, onClick, title, value}: IndicatorWidgetProps) => {
  if (isNaN(value)) {
    return <NoExpectedMeasurementsWidget title={title} className={className}/>;
  }
  const formattedValue = value.toFixed(value === 100.0 ? 0 : 1);
  return (
    <Column className={classNames('Indicator-wrapper clickable', className)} onClick={onClick}>
      <ColumnCenter className={classNames('Indicator', thresholdClassName(value))}>
        <Row className="Indicator-name Row-center">
          <Bold>{title}</Bold>
        </Row>
        <Row className="Row-center Row-bottom">
          <Xlarge className="Indicator-value">{formattedValue}</Xlarge>
          <Normal className="Indicator-unit">%</Normal>
        </Row>
      </ColumnCenter>
    </Column>
  );
};

export const NumMetersIndicatorWidget = ({className, onClick, title, value}: IndicatorWidgetProps) => {
  if (isNaN(value)) {
    return <NoExpectedMeasurementsWidget title={title} className={className}/>;
  }
  return (
    <Column className={classNames('Indicator-wrapper', className)} onClick={onClick}>
      <ColumnCenter className={classNames('Indicator', 'count')}>
        <Row className="Indicator-name Row-center">
          <Bold>{title}</Bold>
        </Row>
        <Row className="Row-center Row-bottom">
          <Xlarge className="Indicator-value">{value}</Xlarge>
          <Large style={{paddingBottom: 8}}>{translate('pcs')}</Large>
        </Row>
      </ColumnCenter>
    </Column>
  );
};
