import * as React from 'react';
import {thresholdClassName} from '../../helpers/thresholds';
import {firstUpperTranslated} from '../../services/translationService';
import {ClassNamed, WithChildren} from '../../types/Types';
import {Column, ColumnCenter} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Bold, Normal, Xlarge} from '../texts/Texts';
import './IndicatorWidget.scss';
import {WidgetModel} from './indicatorWidgetModels';
import classNames = require('classnames');

export interface IndicatorWidgetProps extends ClassNamed, WithChildren {
  widget: WidgetModel;
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

export const IndicatorWidget = ({className, title, widget: {collectionPercentage}}: IndicatorWidgetProps) => {
  if (isNaN(collectionPercentage)) {
    return <NoExpectedMeasurementsWidget title={title} className={className}/>;
  }
  const value = collectionPercentage.toFixed(collectionPercentage === 100.0 ? 0 : 1);
  const statusCss = thresholdClassName(collectionPercentage);

  return (
    <Column className={classNames('Indicator-wrapper', className)}>
      <ColumnCenter className={classNames('Indicator', statusCss)}>
        <Row className="Indicator-name Row-center">
          <Bold>{title}</Bold>
        </Row>
        <Row className="Row-center Row-bottom">
          <Xlarge className="Indicator-value">{value}</Xlarge>
          <Normal className="Indicator-unit">%</Normal>
        </Row>
      </ColumnCenter>
    </Column>
  );
};
